package iw.ie.reporting.setup;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson.JacksonFactory;

import iw.ie.reporting.cmis.model.ContainerEntry;
import iw.ie.reporting.cmis.model.ContainerList;

/**
 * Knows how to provide the values specific to Alfresco in the cloud. Extend
 * this class to load files into an existing site you've created in the cloud.
 */
@Component
public class CloudUtility implements CmisUtility {

	// Change these to match your network, site, and folder in Alfresco in the
	// Cloud
	/**
	 * Specify the cloud user's home network. In real life you'd probably make
	 * an API call to determine this.
	 */
	public static final String HOME_NETWORK = "alfresco.com";

	@Autowired
	LoginCloud loginCloud;

	/**
	 * Specify the short name of the Alfresco cloud site where the files should
	 * be uploaded.
	 */

	// Probably do not need to change any constants below this
	public static final String ALFRESCO_API_URL = "https://api.alfresco.com/";
	public static final String ATOMPUB_URL = ALFRESCO_API_URL + "cmis/versions/1.1/atom";
	public static final String SCOPE = "public_api";

	public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	public static final JsonFactory JSON_FACTORY = new JacksonFactory();

	public static final String TOKEN_SERVER_URL = ALFRESCO_API_URL + "auth/oauth/versions/2/token";
	public static final String AUTHORIZATION_SERVER_URL = ALFRESCO_API_URL + "auth/oauth/versions/2/authorize";
	public static final String SITES_URL = "/public/alfresco/versions/1/sites";

	public HttpRequestFactory requestFactory;

	private Session cmisSession;
	
	@Autowired
	private LocalServerReceiver receiver; 

	@Override
	public Session getCmisSession() throws Exception {
		if (cmisSession == null) {
			// default factory implementation
			SessionFactory factory = SessionFactoryImpl.newInstance();
			Map<String, String> parameter = new HashMap<String, String>();

			// connection settings
			parameter.put(SessionParameter.ATOMPUB_URL, ATOMPUB_URL);
			parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
			parameter.put(SessionParameter.AUTH_HTTP_BASIC, "false");
			parameter.put(SessionParameter.HEADER + ".0", "Authorization: Bearer " + getAccessToken());
			parameter.put(SessionParameter.OBJECT_FACTORY_CLASS, "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");

			List<Repository> repositories = factory.getRepositories(parameter);

			this.cmisSession = repositories.get(0).createSession();
		}
		
		return this.cmisSession;
	}



	/**
	 * Get the OAuth2 access token.
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getAccessToken() throws Exception {
		String accessToken = "";
		try {
			final String redirectUri = receiver.getRedirectUri();
			Runnable authorize = new Runnable() {

				@Override
				public void run() {
					try {
						getOauthAutomatically(redirectUri, OAuth2ClientCredentials.CLIENT_ID, SCOPE);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			new Thread(authorize).start();

			final Credential credential = authorize(receiver, redirectUri);

			this.requestFactory = HTTP_TRANSPORT.createRequestFactory(new HttpRequestInitializer() {
				@Override
				public void initialize(HttpRequest request) throws IOException {
					credential.initialize(request);
					request.setParser(new JsonObjectParser(JSON_FACTORY));
				}
			});

			accessToken = credential.getAccessToken();

			System.out.println("Access token:" + accessToken);

		} catch (Exception e) {
			e.printStackTrace();
		} 
		return accessToken;

	}

	public Credential authorize(VerificationCodeReceiver receiver, String redirectUri) throws IOException {

		System.out.println("Waiting for verification code");
		String code = receiver.waitForCode();
		System.out.println("Access token code = "+code);
		AuthorizationCodeFlow codeFlow = new AuthorizationCodeFlow.Builder(
				BearerToken.authorizationHeaderAccessMethod(), HTTP_TRANSPORT, JSON_FACTORY,
				new GenericUrl(TOKEN_SERVER_URL),
				new ClientParametersAuthentication(OAuth2ClientCredentials.CLIENT_ID,
						OAuth2ClientCredentials.CLIENT_SECRET),
				OAuth2ClientCredentials.CLIENT_ID, AUTHORIZATION_SERVER_URL).setScopes(SCOPE).build();

		TokenResponse response = codeFlow.newTokenRequest(code).setRedirectUri(redirectUri).setScopes(SCOPE).execute();

		return codeFlow.createAndStoreCredential(response, null);

	}

	public void getOauthAutomatically(String redirectUrl, String clientId, String scope) throws IOException {

		String authorizationUrl = new AuthorizationCodeRequestUrl(AUTHORIZATION_SERVER_URL, clientId)
				.setRedirectUri(redirectUrl).setScopes(Arrays.asList(scope)).build();

		try {
			loginCloud.connectCloud(authorizationUrl);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Open Link in Browser");
		}

	}

	/**
	 * Use the REST API to find the documentLibrary folder, then return its ID.
	 * 
	 * @param requestFactory
	 * @param homeNetwork
	 * @param site
	 * @return
	 * @throws IOException
	 */
	public String getRootFolderId(HttpRequestFactory requestFactory, String homeNetwork, String site)
			throws IOException {
		GenericUrl containersUrl = new GenericUrl(
				ALFRESCO_API_URL + homeNetwork + SITES_URL + "/" + site + "/containers");

		HttpRequest request = requestFactory.buildGetRequest(containersUrl);
		ContainerList containerList = request.execute().parseAs(ContainerList.class);
		String rootFolderId = null;
		for (ContainerEntry containerEntry : containerList.list.entries) {
			if (containerEntry.entry.folderId.equals("documentLibrary")) {
				rootFolderId = containerEntry.entry.id;
				break;
			}
		}
		return rootFolderId;
	}

}
