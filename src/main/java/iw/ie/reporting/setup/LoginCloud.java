package iw.ie.reporting.setup;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Component
public class LoginCloud {

	private static final String cloudAuthorizationUrl = "https://api.alfresco.com/auth/oauth/versions/2/authorize";

	private List<String> cookies;
	private HttpsURLConnection conn;

	private final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36";

	public void connectCloud(String referralUrl) throws Exception {

		// make sure cookies is turn on
		CookieHandler.setDefault(new CookieManager());

		// 1. Send a "GET" request, so that you can extract the form's data.
		String page = GetPageContent(referralUrl);
		String postParams = getFormParams(page, "gsethi@water.ie", "MyCloud@1234");

		// 2. Construct above post's content and then send a POST request for
		// authentication
		sendPost(cloudAuthorizationUrl, postParams,referralUrl);
	}

	private void sendPost(String url, String postParams, String referralUrl) throws Exception {

		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();

		// Acts like a browser
		conn.setUseCaches(false);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Origin","https://api.alfresco.com");
		conn.setRequestProperty("Accept-Encoding","gzip, deflate, br");
		conn.setRequestProperty("Accept-Language","en-US,en;q=0.8,hi;q=0.6,br;q=0.4,sq;q=0.2,ur;q=0.2,su;q=0.2,pt-PT;q=0.2,pt;q=0.2,th;q=0.2,km;q=0.2,fr-FR;q=0.2,fr;q=0.2,en-GB;q=0.2");
		conn.setRequestProperty("Upgrade-Insecure-Requests","1");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		conn.setRequestProperty("Cache-Control", "max-age=0");
		conn.setRequestProperty("Referer", referralUrl);
		conn.setRequestProperty("Connection", "keep-alive");
		conn.setInstanceFollowRedirects(false);
		
		
		if (cookies != null) {
			for (String cookie : this.cookies) {
				System.out.println(cookies);
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		}

		conn.setDoOutput(true);
		conn.setDoInput(true);

		
		
		
		// Send post request
		DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
		wr.writeBytes(postParams);
		wr.flush();
		wr.close();

		
		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + postParams);
		System.out.println();
		System.out.println("Response Code : " + responseCode);
		URL redirectUrl = new URL(conn.getHeaderField("Location"));
		
		redirectUrl.openStream();
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	public String GetPageContent(String url) throws Exception {

		URL obj = new URL(url);
		conn = (HttpsURLConnection) obj.openConnection();

		// default is GET
		conn.setRequestMethod("GET");

		conn.setUseCaches(false);

		// act like a browser
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		if (cookies != null) {
			for (String cookie : this.cookies) {
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		}
		int responseCode = conn.getResponseCode();
		System.out.println("\nSending 'GET' request to URL : " + url);
		System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// Get the response cookies
		setCookies(conn.getHeaderFields().get("Set-Cookie"));

		return response.toString();

	}

	public String getFormParams(String html, String username, String password) throws UnsupportedEncodingException {

		System.out.println("Extracting form's data...");

		Document doc = Jsoup.parse(html);

		// Google form id
		Element loginform = doc.getElementsByTag("form").first();
		Elements inputElements = loginform.getElementsByTag("input");
		List<String> paramList = new ArrayList<String>();
		for (Element inputElement : inputElements) {
			String key = inputElement.attr("name");
			String value = inputElement.attr("value");
			if (!(key.equals("action") && value.equals("Deny"))) {
				if (key.equals("username"))
					value = username;
				else if (key.equals("password"))
					value = password;
				paramList.add(key + "=" + URLEncoder.encode(value,"utf-8"));
			}
		}

		// build parameters list
		StringBuilder result = new StringBuilder();
		for (String param : paramList) {
			if (result.length() == 0) {
				result.append(param);
			} else {
				result.append("&" + param);
			}
		}
		return result.toString();
	}

	public List<String> getCookies() {
		return cookies;
	}

	public void setCookies(List<String> cookies) {
		this.cookies = cookies;
	}

}