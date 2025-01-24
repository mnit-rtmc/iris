/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Iteris Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package us.mn.state.dot.tms.server.comm.clearguide;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import us.mn.state.dot.tms.server.CommLinkImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;

/**
 * ClearGuide Property, responsible for authentication and API calls.
 *
 * @author Michael Darter
 */
public class ClearGuideProperty extends ControllerProperty {

	/** Minimum timeout in ms */
	private static final int MIN_TIMEOUT_MS = 5 * 1000;

	/** Get user agent */
	static private String getUserAgent() {
		return "Java/" + System.getProperty("java.version");
	}

	/** Get null-safe string */
	static private String safe(String str) {
		return (str != null ? str : "");
	}

	/** Decode an XML string */
	static private String decodeXml(String xml) {
		xml = xml.replace("&amp;", "&");
		xml = xml.replace("&lt;", "<");
		xml = xml.replace("&gt;", ">");
		xml = xml.replace("&quot;", "\"");
		xml = xml.replace("&apos;", "\'");
		return xml;
	}

	/** Return the specified value for a key value pair.
	 * @param key_value Key value pair in form: "key":"value"
	 * @return The value */
	static public String getValue(String key_value) {
		String[] words = key_value.split(":");
		if (words.length == 2) {
			String value = words[1];
			value = value.trim();
			value = removePostfix(value, ",");
			return deenclose(value, "\"");
		} else {
			return key_value;
		}
	}

	/** Return true if specified string is enclosed by another string */
	static private boolean enclosedBy(String s, String e) {
		return (s != null && e != null ?
			s.startsWith(e) && s.endsWith(e) : false);
	}

	/** Remove enclosing string */
	static public String deenclose(String s, String e) {
		int len_e = e.length();
		if (s.length() > 0 && len_e > 0 && enclosedBy(s, e)) {
			return s.substring(len_e, s.length() - len_e);
		} else {
			return s;
		}
	}

	/** Remove prefix from string */
	static public String removePrefix(String s, String p) {
		if (p.length() > 0 && s.startsWith(p)) {
			int len_p = p.length();
			if (len_p == s.length()) {
				return "";
			}
			return s.substring(len_p);
		}
		return s;
	}

	/** Remove postfix from string */
	static public String removePostfix(String s, String p) {
		if (p.length() > 0 && s.endsWith(p)) {
			int len_p = p.length();
			if (len_p == s.length()) {
				return "";
			}
			return s.substring(0, s.length() - len_p);
		}
		return s;
	}

	/** Calculate time delta in ms */
	static private long timeDelta(long start) {
		long now = System.currentTimeMillis();
		return now - start;
	}

	/** Extract JSON payload from XML response.
	 * @return JSON with payload response or empty string, never null */
	static private String getJsonPayload(String res) {
		String prefix = ">Accept</span></span>";
		String postfix = "}</pre>";
		res = decodeXml(res);
		int si = res.indexOf(prefix);
		if (si > 0) {
			int ei = res.indexOf(postfix);
			if (ei > 0) {
				++ei;
				String ss = res.substring(si, ei);
				ss = removePrefix(ss, prefix);
				ss = removePostfix(ss, postfix);
				return safe(ss);
			}
		}
		return "";
	}

	/** Auth user name */
	private String auth_uname;

	/** Auth password */
	private String auth_pword;

	/** Auth URL */
	private String auth_url;

	/** Auth Connect and read timeout (ms) */
	private int auth_timeout_ms;

	/** Tokens stored by poller */
	private Tokens cg_tokens;

	/** API response JSON, never null */
	public String api_json = "";

	/** Mode: true for authentication else API call */
	private final boolean mode_auth;

	/** Associated controller */
	private ControllerImpl controller;

	/** Constructor */
	public ClearGuideProperty(boolean ma, Tokens toks) {
		super();
		mode_auth = ma;
		cg_tokens = toks;
	}

	/** Logger method */
	private void log(String s) {
		ClearGuidePoller.slog(
			(controller != null ? controller.getName() : "null") +
			" ClearGuideProperty:" + s);
	}

	/** Get the normalized timeout */
	private int getNormalizedTimeout() {
		return Math.max(MIN_TIMEOUT_MS, auth_timeout_ms);
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl ci, OutputStream os)
		throws IOException
	{
		// do nothing
	}

	/** Decode a QUERY response: send, read and decode auth response */
	@Override
	public void decodeQuery(ControllerImpl ci, InputStream is)
		throws IOException
	{
		controller = ci;
		log("decodeQuery: called, mode_auth=" + mode_auth);
		storeAuthParams(ci);
		storeCommParams(ci);
		if (mode_auth) {
			authenticate();
		} else {
			api_json = apiCall(Tokens.getDmsApiUri()); //never null
			if (api_json.isEmpty())
				cg_tokens.clear(); // trigger reauth
		}
		log("decodeQuery: done");
	}

	/** Retrieve and store the username and passwords, which are both
	 * stored in the password field in the form: username:password */
	private void storeAuthParams(ControllerImpl ci) {
		final CommLinkImpl cli = (CommLinkImpl)ci.getCommLink();
		String uname_pword = safe(ci.getPassword()); // uname:pword
		String[] parts = uname_pword.split(":");
		if (parts.length >= 2) {
			auth_uname = safe(parts[0]).trim();
			auth_pword = safe(parts[1]).trim();
			log("storeAuthParams: uname='" + auth_uname +
				"' pword='" + auth_pword + "'");
		} else {
			log("storeAuthParams: unexpected controller " +
				"password field contents");
		}
	}

	/** Store comm parameters */
	private void storeCommParams(ControllerImpl ci) {
		final CommLinkImpl cli = (CommLinkImpl)ci.getCommLink();
		auth_url = cli.getUri();
		auth_timeout_ms = cli.getCommConfig().getTimeoutMs();
	}

	/** Encode a STORE request, write to device */
	@Override
	public void encodeStore(ControllerImpl ci, OutputStream os)
		throws IOException
	{
		// do nothing
	}

	/** Decode a STORE response, read from device */
	@Override
	public void decodeStore(ControllerImpl ci, InputStream is)
		throws IOException
	{
		// do nothing
	}

	/** Authenticate with a ClearGuide server, which retrieves API
	 * tokens and stores them. */
	private boolean authenticate() throws IOException {
		log("authenticate: called");
		final long start_t = System.currentTimeMillis();
		HttpURLConnection con = sendAuthRequest();
		boolean ok;
		if (con != null) {
			parseAuthResponse(con);
			con.disconnect();
			ok = true;
		} else {
			ok = false;
		}
		log("authenticate: done in ms=" + timeDelta(start_t));
		return ok;
	}

	/** Send auth request, set the output stream
	 * @return Connection to ClearGuide authentication server */
	private HttpURLConnection sendAuthRequest() throws IOException {
		String creds = getCreds();
		if (creds == null)
			return null;
		log("sendAuthRequest: URL=" + auth_url);
		log("sendAuthRequest: connect_timeout_ms=read_timeout_ms=" +
			auth_timeout_ms);
		URL url = new URL(auth_url);
		HttpURLConnection con =
			(HttpURLConnection)url.openConnection();
		con.setConnectTimeout(getNormalizedTimeout());
		con.setReadTimeout(getNormalizedTimeout());
		con.setRequestMethod("POST");
		String ua = getUserAgent();
		log("sendAuthRequest: agent=" + ua);
		con.setRequestProperty("User-Agent", ua);
		con.setDoOutput(true);
		final OutputStream os = con.getOutputStream();
		os.write(creds.getBytes());
		os.flush();
		os.close();
		return con;
	}

	/** Get credentials as a string or null on error */
	private String getCreds() {
		if (auth_uname == null || auth_pword == null) {
			log("getCreds: username/password null");
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("username=");
		try {
			sb.append(URLEncoder.encode(auth_uname, "UTF-8"));
			sb.append("&");
			sb.append("password=");
			sb.append(URLEncoder.encode(auth_pword, "UTF-8"));
		} catch (UnsupportedEncodingException ex) {
			log("getCreds: ex=" + ex);
			return null;
		}
		return sb.toString();
	}

	/** Parse auth response, setting token values */
	private boolean parseAuthResponse(HttpURLConnection con)
		throws IOException
	{
		final int rcode = con.getResponseCode();
		log("parseAuthResponse: POST Response rcode=" + rcode);
		if (rcode == HttpURLConnection.HTTP_OK) {
			cg_tokens.clear();
			InputStream is = con.getInputStream();
			BufferedReader bris = new BufferedReader(
				new InputStreamReader(is));
			String line;
			String token_access = "";
			String token_refresh = "";
			while ((line = bris.readLine()) != null) {
				line = decodeXml(line).trim();
				if (line.startsWith("\"access"))
					token_access = getValue(line);
				if (line.startsWith("\"refresh"))
					token_refresh = getValue(line);
			}
			bris.close();
			cg_tokens.store(token_access, token_refresh);
			log("parseAuthResponse: new " + cg_tokens);
		} else {
			log("parseAuthResponse: POST req fail rcode=" + rcode);
			return false;
		}
		return true;
	}

	/** ClearGuide api call
	 * @param surl API call URL
	 * @return JSON response or empty to reauthenticate, never null */
	private String apiCall(String surl) throws IOException {
		long start_t = System.currentTimeMillis();
		log("apiCall: " + cg_tokens);
		if (!cg_tokens.valid())
			return "";
		log("apiCall: url=" + surl);
		log("apiCall: connect_timeout_ms=read_timeout_ms=" +
			auth_timeout_ms);
		URL cgurl = new URL(surl);
		HttpURLConnection con =
			(HttpURLConnection)cgurl.openConnection();
		con.setConnectTimeout(getNormalizedTimeout());
		con.setReadTimeout(getNormalizedTimeout());
		con.setRequestProperty("Authorization", "Bearer " +
			cg_tokens.getAccess());
		con.setRequestMethod("GET");
		con.setRequestProperty("User-Agent", getUserAgent());
		int rcode = con.getResponseCode();
		log("apiCall: response_code=" + rcode);
		String json = "";
		if (rcode == HttpURLConnection.HTTP_OK) {
			BufferedReader br_api =
				new BufferedReader(new InputStreamReader(
				con.getInputStream()));
			String line;
			StringBuffer res = new StringBuffer();
			while ((line = br_api.readLine()) != null)
				res.append(line);
			br_api.close();
			json = getJsonPayload(res.toString()); // never null
			log("apiCall: len_json=" + json.length());
			con.disconnect();
		} else {
			// 400, 401, 403 etc
			json = ""; // trigger reauth
			log("apiCall: need to reauth, rcode=" + rcode);
			con.disconnect();
		}
		log("apiCall: done in ms=" + timeDelta(start_t));
		return safe(json);
	}
}
