/*package iw.ie.reporting.services.archived;

import java.util.Iterator;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import iw.ie.reporting.dao.OnPremiseContentRepo;
import iw.ie.reporting.pojos.OnPremiseContent;
import iw.ie.reporting.setup.Command;
import iw.ie.reporting.setup.LocalCmisUtility;

@Component
public class OnPremiseSyncPopulate extends LocalCmisUtility implements Command {

	@Autowired
	private OnPremiseContentRepo repo;


	private Session cmisSession;


	@Override
	public void execute() throws Exception {
			if (cmisSession == null) {
				synchronized (this) {
					if (cmisSession == null) {
						cmisSession = getCmisSession();
					}
				}
			}
				OperationContext context = cmisSession.getDefaultContext();
		Iterable<OnPremiseContent> contents = repo.findAll();
		Iterator<OnPremiseContent> iter = contents.iterator();
		while(iter.hasNext()){
			OnPremiseContent content = iter.next();
			ItemIterable<CmisObject> results = cmisSession.queryObjects("cmis:document", "cmis:objectId in ('" + content.getNodeRef().split(";")[0] + "')", false,context);
			Iterator<CmisObject> iter2 = results.iterator();
			while(iter2.hasNext()){
				Document document= (Document) iter2.next();
				content.setCreatedBy(document.getCreatedBy());
				content.setModifiedBy(document.getLastModifiedBy());
				content.setContentSize(document.getContentStreamLength());
				content.setOtherNodeRef((String)document.getPropertyValue("sync:otherNodeRefString"));
				System.out.println(content);
				repo.save(content);
			}
		}
	}
}
*/