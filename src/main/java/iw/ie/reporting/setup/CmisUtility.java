package iw.ie.reporting.setup;

import org.apache.chemistry.opencmis.client.api.Session;

public interface CmisUtility {
	public Session getCmisSession() throws Exception;
}
