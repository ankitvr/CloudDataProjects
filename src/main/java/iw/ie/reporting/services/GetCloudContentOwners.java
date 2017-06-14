package iw.ie.reporting.services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import iw.ie.reporting.setup.CloudUtility;
import iw.ie.reporting.setup.Command;

@Component
public class GetCloudContentOwners implements Command {

	@Autowired
	CloudUtility cmisUtil;

	private static final int SKIPCOUNTER = 1000;
	private Session cmisSession;

	@Override
	public List<File> execute() throws Exception {

		if (cmisSession == null) {
			synchronized (this) {
				if (cmisSession == null) {
					cmisSession = cmisUtil.getCmisSession();
				}
			}
		}

		List<File> reports = new ArrayList<>();

		OperationContext context = cmisSession.getDefaultContext();
		context.setMaxItemsPerPage(SKIPCOUNTER);
		context.setOrderBy("cmis:creationDate");
		ItemIterable<CmisObject> results = cmisSession.queryObjects("cmis:document", "IN_TREE('38a67ec9-c5a5-4ed9-b7a1-8d83f47bda53') OR IN_TREE('39d28501-6416-4cb4-9fa1-8cee89a1c479')" +
                "OR IN_TREE('659d6f49-0163-4342-8068-602472539c62')" +
                "OR IN_TREE('f4af754d-1b58-441e-87ce-6020a3fab72a')" +
                "OR IN_TREE('891c090c-158a-4ee0-8ab6-1c9a5110bf4b')" +
                "OR IN_TREE('683c8b1a-23e9-4537-945f-a5361ca8ce7d')" +
                "OR IN_TREE('2924c403-486d-4945-b1d2-ccaf932a0d5f')", false, context);
		long totalContents = results.getTotalNumItems();
		System.out.println("Total Number of contents are " + totalContents);
		int totalIterations = (int) (totalContents / SKIPCOUNTER);
		System.out.println("Total Number of iterations are " + totalIterations);
		ExecutorService executorService = Executors.newFixedThreadPool(40);
		List<Callable<List<CloudData>>> tasks = new ArrayList<>();

		for (int i = 0; i <= totalIterations; i++) {
			Callable<List<CloudData>> task = new CloudDocumentCallable<>(i * SKIPCOUNTER, results);
			tasks.add(task);
			System.out.println("Total Number of tasks are " + tasks.size());
		}

		List<Future<List<CloudData>>> futures = executorService.invokeAll(tasks);
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-YYYY");
		File reportFile = new File("cloudContentStats_" + dateFormat.format(new Date()) + ".txt");
		try (FileWriter writer = new FileWriter(reportFile)) {
			for (Future<List<CloudData>> future : futures) {
				List<CloudData> data = future.get();
				System.out.println("Writing " + data.size() + " data lines in file");
				for (CloudData cloudData : data) {
					writer.write(cloudData.toString() + "\n");
					writer.flush();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		executorService.shutdown();
		reports.add(reportFile);
		return reports;
	}
}

class CloudDocumentCallable<T> implements Callable<List<CloudData>> {

	int skipCount;

	private ItemIterable<CmisObject> results;

	@Override
	public List<CloudData> call() throws Exception {
		Iterator<CmisObject> iterator = results.skipTo(skipCount).iterator();
		List<CloudData> list = new ArrayList<>();
		while (iterator.hasNext()) {
			CloudData data = convertCmisToFolderData(iterator.next());
			if (data != null) {
				System.out.println("Added " + data.path);
				list.add(data);
			}
		}
		return list;
	}

	private CloudData convertCmisToFolderData(CmisObject result) {
		CloudData data = null;
		if (result instanceof Document) {
			Document doc = ((Document) result);
			long size = 0;
			for (Document version : doc.getAllVersions()) {
				size += Long.parseLong(version.getProperty("cmis:contentStreamLength").getValueAsString());
			}

			try {
				data = new CloudData(doc.getType().getDisplayName(), doc.getName(),
						doc.getPaths().get(0) + "/" + doc.getName(), doc.getCreatedBy(),
						doc.getCreationDate().getTime(), doc.getLastModificationDate().getTime(),
						doc.getLastModifiedBy(), size);
			} catch (Exception e) {
			}
		} else {
			System.out.println("Result Not a document " + result);
		}
		return data;
	}

	public CloudDocumentCallable(int skipCount, ItemIterable<CmisObject> results) {
		super();
		this.skipCount = skipCount;
		this.results = results;
	}

}

class CloudData {
	String baseType;
	String name;
	String path;
	String createdBy;
	Date createdDate;
	Date modifiedDate;
	String modifiedBy;
	long size;

	public CloudData(String baseType, String name, String path, String createdBy, Date createdDate, Date modifiedDate,
			String modifiedBy, long size) {
		super();
		this.baseType = baseType;
		this.name = name;
		this.path = path;
		this.createdBy = createdBy;
		this.createdDate = createdDate;
		this.modifiedDate = modifiedDate;
		this.modifiedBy = modifiedBy;
		this.size = size;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + (int) (size ^ (size >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CloudData other = (CloudData) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (size != other.size)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return String.format(
				"\"%s\"" + ",\"%s\"" + ",\"%s\"" + ",\"%s\"" + ",\"%s\"" + ",\"%s\"" + ",\"%s\"" + ",\"%s\"", path,
				name, baseType, createdBy, formatDate(createdDate), modifiedBy, formatDate(createdDate),
				Long.toString(size));
	}

	private String formatDate(Date date) {
		SimpleDateFormat format = new SimpleDateFormat("dd-MM-YYYY");
		return format.format(date);
	}
}