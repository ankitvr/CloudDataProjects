package iw.ie.reporting.services;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import iw.ie.reporting.setup.CloudUtility;
import iw.ie.reporting.setup.Command;

@Component
public class DeleteCloudContent implements Command {

	@Autowired
	CloudUtility cmisUtil;

	private Session cmisSession;

	@Value(value = "${deletion.lastModifidied.days}")
	private int daysAgo;

	private static final Logger log = LoggerFactory.getLogger(DeleteCloudContent.class);

	@Override
	public List<File> execute() throws Exception {

		if (cmisSession == null) {
			synchronized (this) {
				if (cmisSession == null) {
					cmisSession = cmisUtil.getCmisSession();
				}
			}
		}

		String filterDateString = buildFilterDateString();

		// Cleaning documents
		ItemIterable<CmisObject> result = cmisSession.queryObjects("cmis:document",
				"IN_TREE('569f8be6-30bc-4dd7-a1b4-e223cd962ca1') AND cmis:lastModificationDate < TIMESTAMP '"
						+ filterDateString + "'",
				false, cmisSession.getDefaultContext());
		Iterator<CmisObject> iter = result.iterator();
		if (log.isDebugEnabled()) {
			log.debug("Deleting " + result.getTotalNumItems() + "Contents");
		}

		while (iter.hasNext()) {
			Document document = (Document) iter.next();
			log.debug(document.getProperties().toString());
		}
		// document.delete(); }

		// Cleaning Folders
		result = cmisSession.queryObjects("cmis:folder",
				"IN_TREE('569f8be6-30bc-4dd7-a1b4-e223cd962ca1') AND cmis:lastModificationDate < TIMESTAMP '"
						+ filterDateString + "'",
				false, cmisSession.getDefaultContext());
		iter = result.iterator();
		if (log.isDebugEnabled()) {
			log.debug("Found " + result.getTotalNumItems() + " Folders");
		}
		while (iter.hasNext()) {
			Folder folder = (Folder) iter.next();
			if (folder != null) {
				String id = folder.getId();
				ItemIterable<CmisObject> innerDocuments = cmisSession.queryObjects("cmis:document",
						"IN_TREE('" + id + "') AND cmis:lastModificationDate  > TIMESTAMP '" + filterDateString + "'",
						false, cmisSession.getDefaultContext());
				ItemIterable<CmisObject> innerFolders = cmisSession.queryObjects("cmis:folder",
						"IN_TREE('" + id + "') AND cmis:lastModificationDate  > TIMESTAMP '" + filterDateString + "'",
						false, cmisSession.getDefaultContext());
				if (innerDocuments.getTotalNumItems() == 0 && innerFolders.getTotalNumItems() == 0) {
					log.debug("Deleting Folder " + id);
					// folder.deleteTree(true, null, true);
				}
			}
		}
		List<File> reports = null;
		return reports;
	}

	private String buildFilterDateString() {
		Date referenceDate = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
		Calendar c = Calendar.getInstance();
		c.setTime(referenceDate);
		c.add(Calendar.DAY_OF_YEAR, daysAgo);
		String dateStr = dateFormat.format((c.getTime())).concat("T00:00:00.000-00:00");
		return dateStr;
	}
}
