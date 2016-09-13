package iw.ie.reporting.reports;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import iw.ie.reporting.reports.utils.Utils;
import iw.ie.reporting.setup.LocalCmisUtility;

@Component
public class ReportingServiceImpl implements ReportingService {

	@Autowired
	LocalCmisUtility cmisUtil;

	private Session cmisSession;


	@Override
	public void uploadReport(List<File> reportFiles, String folderName) {
		if (cmisSession == null) {
			synchronized (this) {
				if (cmisSession == null) {
					cmisSession = cmisUtil.getCmisSession();
				}
			}
		}
		
		String folderPath = REPORTING_SITE+folderName; 
				
		Folder reportingRootFolder = (Folder) cmisSession.getObjectByPath(folderPath);

		Folder folder = getTargetFolder(reportingRootFolder);
		
		File targetZipFile = new File(folderName+"_report.zip");
		
		try {
			Utils.zipFile(reportFiles, targetZipFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		uploadReportOnTarget(targetZipFile,folder);

	}

	private void uploadReportOnTarget(File zippedReport, Folder folder) {
			String fileName = zippedReport.getName();
			Map<String, Object> props = new HashMap<>();
			if (props.get("cmis:objectTypeId") == null) {
				props.put("cmis:objectTypeId", "cmis:document");
			}
			if (props.get("cmis:name") == null) {
				props.put("cmis:name", fileName);
			}
			Document document = null;
			try {
				ContentStream contentStream = cmisSession.getObjectFactory().createContentStream(fileName,
						zippedReport.length(), "application/zip", new FileInputStream(zippedReport));
				document = folder.createDocument(props, contentStream, null);
				System.out.println("Created new document: " + document.getId());
			} catch (CmisContentAlreadyExistsException ccaee) {
				document = (Document) cmisSession.getObjectByPath(folder.getPath() + "/" + fileName);
				System.out.println("Document already exists: " + fileName);
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	private Folder getTargetFolder(Folder reportingRootFolder) {
		Calendar currentTime = Calendar.getInstance();
		Stack<String> reportingFolderNames = new Stack<>();
		reportingFolderNames.push("" + currentTime.get(Calendar.YEAR));
		reportingFolderNames.push("" + (currentTime.get(Calendar.MONTH)+1));
		reportingFolderNames.push("" + currentTime.get(Calendar.DAY_OF_MONTH));
		reportingFolderNames.push("" + currentTime.get(Calendar.HOUR_OF_DAY));
		reportingFolderNames.push("" + currentTime.get(Calendar.MINUTE));
		Folder innerMostFolder = createListOfFolders(reportingRootFolder, reportingFolderNames);
		return innerMostFolder;
	}

	private Folder createListOfFolders(Folder parentFolder, Stack<String> reportingFolderNames) {
		String folderName = reportingFolderNames.peek();
		if (reportingFolderNames.size() > 1) {
			reportingFolderNames.pop();
			parentFolder = createListOfFolders(parentFolder, reportingFolderNames);
		}
		return createFolderIfNotExists(parentFolder, folderName);
	}

	private Folder createFolderIfNotExists(Folder outerFolder, String folderName) {
		Folder subFolder = null;
		try {
			subFolder = (Folder) cmisSession.getObjectByPath(outerFolder.getPath() + "/" + folderName);
			System.out.println("Folder already existed!");
		} catch (CmisObjectNotFoundException onfe) {
			Map<String, Object> props = new HashMap<>();
			props.put("cmis:objectTypeId", "cmis:folder");
			props.put("cmis:name", folderName);
			subFolder = outerFolder.createFolder(props);
			String subFolderId = subFolder.getId();
			System.out.println("Created new folder: " + subFolderId);
		}
		return subFolder;
	}

}
