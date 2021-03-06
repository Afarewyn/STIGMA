package info.mineshafter.datasources;

import info.mineshafter.intercept.TextureHandler;
import info.mineshafter.models.Profile;
import info.mineshafter.util.Streams;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class MineshafterProfileAuthority implements ProfileAuthority {
	private static final String API_URL = "http://mineshafter.info/mcapi/profile";
	private static TextureHandler textureHandler = TextureHandler.getInstance();

	private static ProfileAuthority instance;

	private MineshafterProfileAuthority() {}

	public static synchronized ProfileAuthority getInstance() {
		if (instance == null) {
			instance = new MineshafterProfileAuthority();
		}

		return instance;
	}

	// id is the uuid of the user, returns a Profile object
	public Profile getProfile(String id) {
		System.out.println("MineshafterProfileClient.getProfile(" + id + ")");
		URL u;
		try {
			u = new URL(API_URL + "?uuid=" + id);

			HttpURLConnection conn = (HttpURLConnection) u.openConnection();

			InputStream in = conn.getInputStream();
			String profileJSON = Streams.toString(in);
			Streams.close(in);

			System.out.println("MS.getProfile: " + profileJSON);

			if (profileJSON == null || profileJSON.length() == 0) { return null; }

			JsonObject pj = JsonObject.readFrom(profileJSON);

			// TODO: md5 the username/email
			// server will return a profile for the email if there is one
			// then it will
			Profile p = new Profile(id);
			p.setName(pj.get("username").asString());
			JsonValue skinVal = pj.get("skin");
			JsonValue capeVal = pj.get("cape");
			String url;

			if (skinVal != null && !skinVal.isNull()) {
				url = textureHandler.addSkin(id, skinVal.asString());
				p.setSkin(url);
			}
			if (capeVal != null && !capeVal.isNull()) {
				url = textureHandler.addCape(id, capeVal.asString());
				p.setCape(url);
			}

			return p;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	// name is the username, returns a Profile object
	public Profile searchProfile(String name) {
		try {
			name = URLEncoder.encode(name, "UTF-8");
			URL u = new URL(API_URL + "?username=" + name);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();

			InputStream in = conn.getInputStream();
			String uuid = Streams.toString(in);
			Streams.close(in);

			if (uuid == null || uuid.length() == 0) { return null; }

			Profile p = new Profile(uuid);
			p.setName(name);

			return p;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
