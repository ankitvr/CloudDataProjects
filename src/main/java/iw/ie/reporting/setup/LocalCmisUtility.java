package iw.ie.reporting.setup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.springframework.stereotype.Component;

/**
 * Knows how to provide the values specific to Alfresco on-premise, versions
 * 4.2c and earlier. Extend this class to load files into Alfresco running on
 * your own server.
 * 
 * @author jpotts
 */
@Component
public class LocalCmisUtility implements CmisUtility {

	public static final String ALFRESCO_API_URL = "http://iwdc1preecma01/";

	public static final String USER_NAME = "admin";

	public static final String PASSWORD = "Admin123";

	public static final String ATOMPUB_URL = ALFRESCO_API_URL + "alfresco/api/-default-/public/cmis/versions/1.1/atom";

	private Repository reporsitory;

	
	@Override
	public Session getCmisSession() {
		if (reporsitory == null) {
			SessionFactory factory = SessionFactoryImpl.newInstance();
			Map<String, String> parameter = new HashMap<String, String>();

			parameter.put(SessionParameter.ATOMPUB_URL, ATOMPUB_URL);
			parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
			parameter.put(SessionParameter.AUTH_HTTP_BASIC, "true");
			parameter.put(SessionParameter.USER, USER_NAME);
			parameter.put(SessionParameter.PASSWORD, PASSWORD);
			parameter.put(SessionParameter.OBJECT_FACTORY_CLASS,
					"org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");

			List<Repository> repositories = factory.getRepositories(parameter);
			reporsitory = repositories.get(0);
		}
		return reporsitory.createSession();
	}

}
