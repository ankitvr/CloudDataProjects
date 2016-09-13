package iw.ie.reporting.services;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import iw.ie.reporting.setup.Command;
import iw.ie.reporting.setup.LocalCmisUtility;

@Component
public class GetAllCloudUsers  implements Command  {
	
	@Autowired
	LocalCmisUtility cmisUtil;

	private Session cmisSession;
	
	@Override
	public List<File>  execute() throws Exception {
		if (cmisSession == null) {
			synchronized (this) {
				if (cmisSession == null) {
					cmisSession = cmisUtil.getCmisSession();
				}
			}
		}
		ItemIterable<QueryResult> users = cmisSession.query("Select cm:userName,cm:homeFolder from cm:person", false);
		Iterator<QueryResult> iter = users.iterator();
		while(iter.hasNext()){
			QueryResult person = iter.next();
			System.out.println((String)person.getPropertyValueById("cm:userName"));
			System.out.println((String)person.getPropertyValueById("cm:homeFolder"));
		}
		return null;
	}

}
