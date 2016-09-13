package iw.ie.reporting.services.multithreading;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.Callable;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;

public class DocumentCallable<T> implements Callable<File> {

	String startMonthDetail;

	String nextMonthDetail;

	String rootFolderId;

	Session cmisSession;

	String type;

	@Override
	public File call() throws Exception {
		OperationContext context = cmisSession.getDefaultContext();
		context.setMaxItemsPerPage(1000);
		ItemIterable<CmisObject> results = cmisSession.queryObjects("cmis:document",
				"cmis:creationDate  > TIMESTAMP '" + startMonthDetail
						+ "-01T00:00:00.000-00:00' and cmis:creationDate < TIMESTAMP '" + nextMonthDetail
						+ "-01T00:00:00.000-00:00'",
				false, context);
		System.out.println("cmis:creationDate  > TIMESTAMP '" + startMonthDetail
				+ "-01T00:00:00.000-00:00' and cmis:creationDate < TIMESTAMP '" + nextMonthDetail
				+ "-01T00:00:00.000-00:00'");
		File result = new File(type+ startMonthDetail + ".txt");
		FileWriter writer = null;
		
		try {
			writer = new FileWriter(result);
			writer.write("Bill Ids");
			writer.write("\n");
			writer.flush();
			Iterator<CmisObject> iter2 = results.iterator();
			while (iter2.hasNext()) {
				Document doc = (Document) iter2.next();
				writer.write(doc.getName().split("\\s")[0]);
				writer.write("\n");
				writer.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
			}
		}

		return result;
	}

	public DocumentCallable(String startMonthDetail, String nextMonthDetail, String rootFolderId, Session cmisSession,
			String type) {
		super();
		this.startMonthDetail = startMonthDetail;
		this.nextMonthDetail = nextMonthDetail;
		this.rootFolderId = rootFolderId;
		this.cmisSession = cmisSession;
		this.type = type;
	}

}
