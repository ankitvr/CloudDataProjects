package iw.ie.reporting.services;

import iw.ie.reporting.setup.CloudUtility;
import iw.ie.reporting.setup.Command;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class GetAllCloudUsers  implements Command  {

	@Autowired
	CloudUtility cmisUtil;

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
		OperationContext context = new OperationContextImpl();
		context.setCacheEnabled(true);
		int pageSize = 100;
		int count = 0;
		context.setMaxItemsPerPage(pageSize);
		ItemIterable<QueryResult> allUsers = cmisSession.query("SELECT * FROM cm:person WHERE cm:email LIKE '%@water.ie'", false,context);
		Set<String> userIds = new HashSet<>();
		long total = allUsers.getTotalNumItems();
		System.out.println(allUsers.getTotalNumItems());
		List<File> reports = new ArrayList<>();
		while(count*pageSize < total) {
			ItemIterable<QueryResult> tmpPage = allUsers.skipTo(count * pageSize).getPage();

			for (QueryResult person : tmpPage) {
				try{
					userIds.add(JSONObject.escape(person.getPropertyValueById("cm:userName")+"\n"));
				}catch (Exception e){e.printStackTrace();}

			}
			System.out.println("All users "+userIds.size());
			count++;
			System.out.println(count);
		}

		JSONObject jsonObject = new JSONObject();
		System.out.println(userIds);
		jsonObject.put("users",123);

		File report = new File("resultsNetwork.txt");
		Writer writer = new FileWriter(report);
		jsonObject.writeJSONString(writer);
		writer.flush();
		writer.close();

		reports.add(report);

		return reports;
	}

}
