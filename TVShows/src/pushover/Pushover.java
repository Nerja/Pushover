package pushover;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Pushover {
	private String appToken, userToken;
	private static final String END_POINT = "https://api.pushover.net/1/messages.json";
	private static final String SETTINGS_FILE = "settings.json";
	private final URL endUrl;

	public Pushover(String appToken, String userToken) throws PushoverException {
		this.appToken = appToken;
		this.userToken = userToken;
		try {
			endUrl = new URL(END_POINT);
		} catch (Exception e) {
			throw new PushoverException("Internal implementation problems!");
		}
	}

	public Pushover() throws PushoverException {
		try {
			endUrl = new URL(END_POINT);
		} catch (Exception e) {
			throw new PushoverException("Internal implementation problems!");
		}
		if (!(new File(SETTINGS_FILE)).exists())
			throw new PushoverException(SETTINGS_FILE + " can not be found!");
		JSONParser parser = new JSONParser();
		try {
			JSONObject settingsObject = (JSONObject) parser.parse(new FileReader(SETTINGS_FILE));
			appToken = (String) settingsObject.get("apptoken");
			userToken = (String) settingsObject.get("usertoken");
		} catch (Exception e) {
			e.printStackTrace();
			throw new PushoverException("Problem parsing settings");
		}
	}

	public void sendMessage(String message) throws PushoverException {
		URLConnection conn;
		try {
			conn = endUrl.openConnection();
		} catch (IOException e) {
			throw new PushoverException("Problems connecting to pushover server...");
		}
		conn.setDoOutput(true);
		HttpsURLConnection https = (HttpsURLConnection) conn;
		try {
			https.setRequestMethod("POST");
		} catch (ProtocolException e) {
			throw new PushoverException("Internal implementation problems!");
		}
		try {
			OutputStreamWriter out = new OutputStreamWriter(https.getOutputStream(), "UTF-8");
			out.write(buildQuery(message));
			out.write("\r\n");
			out.flush();
		} catch (UnsupportedEncodingException e) {
			throw new PushoverException("System does not support UTF-8");
		} catch (IOException e) {
			throw new PushoverException("Problems sending data to pushover");
		}
		int responseCode = 0;
		try {
			responseCode = https.getResponseCode();
		} catch (IOException e) {
			throw new PushoverException("Problems sending data to pushover");
		}
		if (responseCode != 200)
			throw new PushoverException("Server did respond with code: " + responseCode);
	}

	private String buildQuery(String message) throws UnsupportedEncodingException {
		return "token=" + appToken + "&user=" + userToken + "&message=" + URLEncoder.encode(message, "UTF-8");
	}
}
