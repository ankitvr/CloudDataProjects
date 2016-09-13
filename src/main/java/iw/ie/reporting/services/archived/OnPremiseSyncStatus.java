/*package iw.ie.reporting.services.archived;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import iw.ie.reporting.dao.OnPremiseContentRepo;
import iw.ie.reporting.pojos.OnPremiseContent;
import iw.ie.reporting.setup.Command;
import iw.ie.reporting.setup.LocalCmisUtility;

@Component

public class OnPremiseSyncStatus extends LocalCmisUtility implements Command {
	@Autowired
	private OnPremiseContentRepo repo;

	@Value("${datafile.onpremise}")
	private String dataFilePath;

	private Session cmisSession;

	public void provideCloudStats(String objectId) throws IOException {

		try {
			if (cmisSession == null) {
				synchronized (this) {
					if (cmisSession == null) {
						cmisSession = getCmisSession();
					}
				}
			}
			try {
				OperationContext context = cmisSession.getDefaultContext();
				ItemIterable<CmisObject> allFolders = null;
				synchronized (this) {
					allFolders = cmisSession.queryObjects("cmis:folder", "cmis:objectId in ('" + objectId + "')", false,
							context);
				}
				Iterator<CmisObject> iter = allFolders.getPage().iterator();
				while (iter.hasNext()) {
					displayEntireTree(iter.next());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cmisSession.clear();
		}

	}

	private void displayEntireTree(CmisObject cmisObject) {
		if (cmisObject instanceof Folder) {
			ItemIterable<CmisObject> children = ((Folder) cmisObject).getChildren();
			for (CmisObject child : children) {
				displayEntireTree(child);
			}
		} else if (cmisObject instanceof Document) {
			Document document = (Document) cmisObject;
			String nodeRef = document.getId();
			OnPremiseContent content = null;
				String checkSum = downloadAndGetCheckSum(document);
				List<String> paths = document.getPaths();
				String path = "";
				if (!(paths == null || paths.isEmpty())) {
					path = paths.get(0);
				}
				content = new OnPremiseContent(nodeRef, path, document.getName(), checkSum,document.getCreatedBy(),document.getLastModifiedBy(),document.getContentStreamLength(),(String)document.getPropertyValue("sync:otherNodeRefString"));
				System.out.println(content);
				repo.save(content);
		}
	}

	private String downloadAndGetCheckSum(Document document) {
		String checkSum = "";
		if (document.getAllowableActions().getAllowableActions().contains(Action.CAN_GET_CONTENT_STREAM) == false) {
		}
		File file = null;
		InputStream input = null;
		OutputStream output = null;

		try {
			// Create the file on the local drive without any content
			file = new File(document.getName());
			if (!file.exists()) {
				file.createNewFile();
			}
			// Get the object content stream and write to
			// the new local file
			input = document.getContentStream().getStream();
			output = new FileOutputStream(file);
			IOUtils.copy(input, output);
			// Close streams and handle exceptions
			input.close();
			output.close();
			try {
				checkSum = getCheckSum(file);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				file.delete();
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return checkSum;
	}

	private String getCheckSum(File dataFile) throws IOException, NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA1");
		FileInputStream fis = new FileInputStream(dataFile);
		byte[] dataBytes = new byte[1024];
		int nread = 0;
		while ((nread = fis.read(dataBytes)) != -1) {
			md.update(dataBytes, 0, nread);
		}
		;
		byte[] mdbytes = md.digest();
		// convert the byte to hex format
		StringBuffer sb = new StringBuffer("");
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		fis.close();
		return sb.toString();
	}

	@Override
	public void execute() throws Exception {
		List<String> allNodeRefs = new ArrayList<>();
		FileReader fileReader = new FileReader(dataFilePath);
		try (BufferedReader br = new BufferedReader(fileReader)) {
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				if (null != line && line.startsWith("workspace")) {
					allNodeRefs.add(line.split("workspace://SpacesStore/")[1]);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (String l : allNodeRefs) {
			try {
				provideCloudStats(l);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
*/