/*package iw.ie.reporting.services.archived;

import java.util.Iterator;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import iw.ie.reporting.dao.CloudContentRepo;
import iw.ie.reporting.pojos.CloudContent;
import iw.ie.reporting.setup.CloudUtility;
import iw.ie.reporting.setup.Command;

@Component
public class CloudSyncPopulate implements Command {

	@Autowired
	CloudUtility cmisUtil;
	
	@Autowired
	private CloudContentRepo repo;


	private Session cmisSession;


	@Override
	public void execute() throws Exception {
			if (cmisSession == null) {
				synchronized (this) {
					if (cmisSession == null) {
						cmisSession = cmisUtil.getCmisSession();
					}
				}
			}
				OperationContext context = cmisSession.getDefaultContext();
		Iterable<CloudContent> contents = repo.findAll();
		Iterator<CloudContent> iter = contents.iterator();
		while(iter.hasNext()){
			CloudContent content = iter.next();
			ItemIterable<CmisObject> results = cmisSession.queryObjects("cmis:document", "cmis:objectId in ('" + content.getNodeRef().split(";")[0] + "')", false,context);
			Iterator<CmisObject> iter2 = results.iterator();
			while(iter2.hasNext()){
				Document document= (Document) iter2.next();
				content.setCreatedBy(document.getCreatedBy());
				content.setModifiedBy(document.getLastModifiedBy());
				content.setContentSize(document.getContentStreamLength());
				repo.save(content);
			}
		}
	}
}
*/