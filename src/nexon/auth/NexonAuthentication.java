package nexon.auth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.stream.Collectors;

import javax.net.ssl.HttpsURLConnection;

public class NexonAuthentication {
	private static final String LOGIN_HOST = "jp.nexon.com";
	private static final String LOGIN_HOST_BASE = "https://" + LOGIN_HOST + "/signin/";
	private static final String LOGIN_HOST_GET = LOGIN_HOST_BASE + "login/";
	private static final String LOGIN_HOST_POST = LOGIN_HOST_BASE + "login/login";
	private static final String REQUEST_VERIFICATION_TOKEN_PATTERN = "<input name=\"__RequestVerificationToken\" type=\"hidden\"";
	private static final String VALUE_PATTERN = "value=\"";
	private static final String MESSAGE_ERROR_LOGIN = "NEXON ERROR: login fail";
	private static CookieManager cookieManager = new CookieManager();

	static {
		CookieHandler.setDefault(cookieManager);
	}

	/**
	 * トークン取得
	 *
	 * @return 認証時に必要なトークン
	 */
	public static String getRequestVerificationToken() {
		HttpURLConnection http = null;
		try {
			// HTTP取得
			URL url = new URL(LOGIN_HOST_GET);
			http = (HttpURLConnection) url.openConnection();
			http.setRequestMethod("GET");
			http.connect();
			BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()));
			String html = "", line = "";
			while ((line = reader.readLine()) != null) {
				html += line;
			}
			reader.close();

			// トークン取得
			int indexOf = html.indexOf(REQUEST_VERIFICATION_TOKEN_PATTERN);
			int lastIndexOf = html.indexOf(">", indexOf) + 1;
			String reqTokenHtml = html.substring(indexOf, lastIndexOf);
			indexOf = reqTokenHtml.indexOf(VALUE_PATTERN) + VALUE_PATTERN.length();
			lastIndexOf = reqTokenHtml.indexOf("\"", indexOf);
			return reqTokenHtml.substring(indexOf, lastIndexOf);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (http != null) {
				http.disconnect();
			}
		}
	}

	/**
	 * 認証
	 *
	 * @param nexonId
	 *            NEOXN_ID
	 * @param password
	 *            パスワード
	 * @return NPP（ネクソンパスポート）
	 */
	public String login(String nexonId, String password) {
		return login(nexonId, password, "");
	}

	/**
	 * 認証
	 *
	 * @param nexonId
	 *            NEXON_ID
	 * @param password
	 *            パスワード
	 * @param otp
	 *            ワンタイムパスワード
	 * @return NPP（ネクソンパスポート）
	 */
	public String login(String nexonId, String password, String otp) {

		HttpsURLConnection https = null;
		try {
			// トークン取得＋認証情報構築
			String data = "__RequestVerificationToken=";
			data += getRequestVerificationToken();
			data += "&NexonID=" + URLEncoder.encode(nexonId, "UTF-8");
			data += "&Password=" + URLEncoder.encode(password, "UTF-8");
			data += "&OTP=" + otp;
			data += "&SaveID=false";
			data += "&X-Requested-With=XMLHttpRequest";

			// HTTPS取得
			URL url = new URL(LOGIN_HOST_POST);
			https = (HttpsURLConnection) url.openConnection();
			https.setDoInput(true);
			https.setDoOutput(true);
			https.setUseCaches(false);
			https.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			https.setRequestProperty("x-kl-ajax-request", "Ajax_Request");
			https.setRequestProperty("x-requested-with", "XMLHttpRequest");
			https.setRequestMethod("POST");
			OutputStreamWriter out = new OutputStreamWriter(https.getOutputStream());
			out.write(data);
			out.close();
			https.connect();

			if (https.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new IOException(MESSAGE_ERROR_LOGIN);
			}

			List<HttpCookie> list = cookieManager.getCookieStore().getCookies();

			List<String> nppList = list.stream().filter(i -> {
				return i.getName().equals("NPP");
			}).map(i -> i.getValue()).collect(Collectors.toList());

			if (nppList == null || nppList.isEmpty()) {
				throw new IOException(MESSAGE_ERROR_LOGIN);
			}

			String npp = nppList.get(0);

			npp = URLDecoder.decode(npp, "UTF-8");

			return npp;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (https != null) {
				https.disconnect();
			}
		}
	}
}
