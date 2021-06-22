/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2021  SRF Consulting Group
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
package us.mn.state.dot.tms.client.camera;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraVidSourceOrder;
import us.mn.state.dot.tms.CameraVidSourceOrderHelper;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.CameraTemplate;
import us.mn.state.dot.tms.CameraTemplateHelper;
import us.mn.state.dot.tms.VidSourceTemplate;
import us.mn.state.dot.tms.VidSourceTemplateHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.utils.SubnetChecker;

/** VidStreamReq: Video-stream-request parameters.
 *
 * Data class that contains the parameters needed to open a video stream.
 *
 * The VidPanel class uses the static VidStreamReq.getVidStreamReqs(...)
 * generator method to convert a Camera reference to a list of VidStreamReq
 * objects that "should" be able to stream video from that camera to the current
 * client.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class VidStreamReq {

	static private boolean isNothing(String str) {
		return ((str == null) || str.isEmpty());
	}

	static boolean isMember(String list, String item) {
		// an empty list matches everything
		if (isNothing(list))
			return true;
		String[] items = list.split("[,;]");
		int len = items.length;
		for (String it : items) {
			if (it.isEmpty())
				continue;
			if (it.trim().equalsIgnoreCase(item))
				return true;
		}
		return false;
	}

	/** Get address portion of "<addr>[:<port>]" string. */
	static private String getAddr(String addrport) {
		if (isNothing(addrport))
			return null;
		try {
			URL url = new URL("http://"+addrport);
			return url.getHost();
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/** Get port portion of "<addr>[:<port>]" string.
	 *  If no port in the string, returns "<defPort>". */
	static private String getPort(String addrport, Integer defPort) {
		if (isNothing(addrport))
			return null;
		try {
			URL url = new URL("http://"+addrport);
			Integer port = url.getPort();
			if (port == -1) {
				if (defPort == null)
					return null;
				port = defPort;
			}
			return Integer.toString(port);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/** Get "addr[:port]" portion of "<addr>[:<port>]" string.
	 * if string does not have a port and defPort is not null,
	 * appends ":<defPort>" to the addr string before returning it.. */
	static private String getAddrPort(String addrport, Integer defPort) {
		if (isNothing(addrport))
			return null;
		try {
			URL url = new URL("http://"+addrport);
			String nuAddrport = url.getHost();
			if (isNothing(nuAddrport))
				return null;
			Integer port = url.getPort();
			if (port == -1) {
				if (defPort != null)
					port = defPort;
			}
			if (port != -1)
				nuAddrport += ":" + port;
			return nuAddrport;
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/** Get "addr[:port]" from an addr string and port integer value. If
	 * port is null and defPort is not null, appends ":<defPort>" to the
	 * addr string before returning it. */
	static private String getAddrPort(String addr, Integer port,
		Integer defPort)
	{
		if (isNothing(addr))
			return null;
		if (port == null && defPort != null)
			port = defPort;
		if (port != null)
			addr += ":" + port;
		return addr;
	}

	/** Get username from controller password field.
	 * Returns null if the password string is null,
	 * empty, or doesn't have a colon separator.
	 */
	static private String getUsername(String unpw) {
		if (isNothing(unpw))
			return null;
		String[] parts = unpw.split(":", 2);
		if (parts.length < 2)
			return null;
		return parts[0];
	}
	
	/** Get password from controller password field.
	 * Returns null if the password string is null or empty.
	 */
	static private String getPassword(String unpw) {
		if (isNothing(unpw))
			return null;
		String[] parts = unpw.split(":", 2);
		switch (parts.length) {
			case 0:
				return null;
			case 1:
				return parts[1];
		}
		return parts[2];
	}

	/** Get an RFC-2396 compliant un + pw + address + port
	 *  string in the form "{un}:{pw}@{addr}:{port}", with
	 *  appropriate reformatting if certain fields are null.
	 *  Returns null if there's no address or if there's a
	 *  password but no username.
	 */
	static private String getUnPwAddr(String unpw, String addr, Integer port,
			Integer defPort) {
		String addrport = getAddrPort(addr, port, defPort);
		if (isNothing(addrport))
			return null; // no address
		if (isNothing(unpw))
			return addrport;   // no un or pw
		String[] parts = unpw.split(":", 2);
		switch (parts.length) {
			case 0: // no un or pw
				return addrport;
			case 1: // found pw, but no un
				return null;
		}
		// username and password
		return parts[0] + ":" + parts[1] + "@" + addrport;
	}
	
	/** Get camera name modified for use with
	  * a live555 videoProxy rtsp uri string */
	static private String getLive555CamName(Camera cam) {
		String camName = cam.getName();
		int len = camName.length();
		StringBuilder newCamName = new StringBuilder(len);
		char ch;
		for (int i = 0; (i < len); ++i) {
			ch = camName.charAt(i);
			if (ch >= 127) // replace any non-ASCII character
				newCamName.append('_');
			else if (Character.isLetterOrDigit(ch)
			      || (ch == '.')
			      || (ch == '-')
			      || (ch == '_')
			      || (ch == '~'))
				newCamName.append(ch);
			else
				newCamName.append('_');
		}
		return newCamName.toString();
	}

	/** Pattern for config parameter */
	static final Pattern PATTERN = Pattern.compile("\\{(.+?)\\}");

	/** Create a VidStreamReq from a VidSourceTemplate and Camera.
	 *
	 * @param st VidSourceTemplate
	 * @param cam Camera
	 * @return An expanded VidStreamReq (or null if expanding the template
	 *         fails some requirement).
	 */
	static private VidStreamReq create(VidSourceTemplate st, Camera cam) {
		Session ses = Session.getCurrent();
		Properties p = ses.getProperties();
		String config = st.getConfig();
		Controller con = cam.getController();
		String unpw = (con == null) ? null : con.getPassword();

		// check subnet
		String subnets = st.getSubnets();
		if (!isMember(subnets, SubnetChecker.getSubnetName())) {
			return null;
		}

		// expand substitution-fields...
		Matcher m = PATTERN.matcher(config);
		StringBuffer sb = new StringBuffer();
		String tok, val, defVal;
		String[] ds;
		while (m.find()) {
			tok = m.group(1);
			val = null;
			
			// parse default value from substitution field
			ds = tok.split("=", 2);
			defVal = null;
			if (ds.length == 2) {
				tok = ds[0];
				defVal = ds[1];
			}

			// find value for substitution field
			if (tok.equalsIgnoreCase("addr"))
				val = cam.getEncAddress();
			else if (tok.equalsIgnoreCase("port")) {
				val = Integer.toString(cam.getEncPort() != null
					  ? cam.getEncPort() : st.getDefaultPort());
			} else if (tok.equalsIgnoreCase("addrport")) {
				val = getAddrPort(cam.getEncAddress(), cam.getEncPort(),
						st.getDefaultPort());
			} else if (tok.equalsIgnoreCase("maddr"))
				val = getAddr(cam.getEncMcast());
			else if (tok.equalsIgnoreCase("mport"))
				val = getPort(cam.getEncMcast(), st.getDefaultPort());
			else if (tok.equalsIgnoreCase("maddrport"))
				val = getAddrPort(cam.getEncMcast(), st.getDefaultPort());
			else if (tok.equalsIgnoreCase("chan"))
				val = Integer.toString(cam.getEncChannel());
			else if (tok.equalsIgnoreCase("name"))
				val = cam.getName();
			else if (tok.equalsIgnoreCase("dist"))
				val = p.getProperty("district");
			else if (tok.equalsIgnoreCase("session-id"))
				val = Long.toString(ses.getSessionId());
			else if (tok.equalsIgnoreCase("pname"))
				val = getLive555CamName(cam);
			else if (tok.equalsIgnoreCase("sizecode"))
				val = "s"; //TODO: change when panel is resized
			else if (tok.equalsIgnoreCase("unpw@addr")) {
				val = getUnPwAddr(unpw, cam.getEncAddress(),
						cam.getEncPort(), st.getDefaultPort());
			}
			else if (tok.equalsIgnoreCase("un"))
				val = getUsername(unpw);
			else if (tok.equalsIgnoreCase("pw"))
				val = getPassword(unpw);
			else // look in the iris-client.properties file
				val = p.getProperty(tok);

			// If we couldn't find a value...
			if (isNothing(val)) {
				if (defVal == null)
					return null; // ignore template
				val = defVal; // use default value
			}

			m.appendReplacement(sb, val);
		}
		m.appendTail(sb);
		config = sb.toString();
		return isOkConfig(config) ? new VidStreamReq(st, config) : null;
	}

	/** Check if config is OK */
	static private boolean isOkConfig(String config) {
		try {
			return VidStreamMgrGst.isOkConfig(config)
			    || VidStreamMgrMJPEG.isOkConfig(config);
		}
		catch (NoClassDefFoundError e) {
			return false;
		}
	}

	/** Comparator to sort by cameraStreamOrder.order value */
	static private Comparator<CameraVidSourceOrder> STREAM_ORDER =
		new Comparator<CameraVidSourceOrder>()
	{
		public int compare(CameraVidSourceOrder a, CameraVidSourceOrder b) {
			return Integer.compare(a.getSourceOrder(), b.getSourceOrder());
		}
	};

	/** Get list of VidStreamReq(s) for a given camera */
	static public List<VidStreamReq> getVidStreamReqs(Camera c) {
		List<VidStreamReq> vrList = new ArrayList<VidStreamReq>();

		// Get camera's CameraTemplate
		if (c.getCameraTemplate() == null)
			return vrList;

		String cstName = c.getCameraTemplate().getName();
		CameraTemplate ct = CameraTemplateHelper.lookup(cstName);

		if (ct == null)
			return vrList;
		// Iterate through CameraStreamOrder(s)
		// (collecting those that match the CameraTemplate)
		Iterator<CameraVidSourceOrder> itCSO = CameraVidSourceOrderHelper.iterator();
		List<CameraVidSourceOrder> csoList = new ArrayList<CameraVidSourceOrder>();
		CameraVidSourceOrder cso;
		while (itCSO.hasNext()) {
			cso = itCSO.next();
			if (cso.getCameraTemplate().equals(cstName))
				csoList.add(cso);
		}
		if (csoList.size() == 0)
			return vrList;
		// sort the resulting list of CSO(s) by stream-order
		csoList.sort(STREAM_ORDER);
		// Iterate through sorted sublist
		// (collecting VidStreamReq(s) generated from VidSourceTemplate(s))

		itCSO = csoList.iterator();
		VidStreamReq vsr;
		VidSourceTemplate vst;
		while (itCSO.hasNext()) {
			cso = itCSO.next();
			vst = VidSourceTemplateHelper.lookup(cso.getVidSourceTemplate());
			if (vst == null)
				continue;
			// expand the template to a request
			vsr = VidStreamReq.create(vst, c);
			if (vsr != null)
				vrList.add(vsr);
		}
		return /*(vrList.size() == 0) ? null :*/ vrList;
	}

	/** Get list of CameraVidSourceOrder objects for a given camera template */
	static public List<CameraVidSourceOrder> getCamVidSrcOrder(CameraTemplate ct) {
		List<CameraVidSourceOrder> csoList = new ArrayList<CameraVidSourceOrder>();
		if (ct == null)
			return csoList;
		// Iterate through CameraStreamOrder(s)
		// (collecting those that match the CameraTemplate)
		Iterator<CameraVidSourceOrder> itCSO = CameraVidSourceOrderHelper.iterator();
		CameraVidSourceOrder cso;
		while (itCSO.hasNext()) {
			cso = itCSO.next();
			if (cso.getCameraTemplate().equals(ct.getName()))
				csoList.add(cso);
		}
		if (csoList.size() == 0)
			return csoList;
		// sort the resulting list of CSO(s) by stream-order
		csoList.sort(STREAM_ORDER);
		return csoList;
	}

	/** Create a new stream request */
	private VidStreamReq(VidSourceTemplate vst, String config) {
		this.vst    = vst;
		this.config = config;
	}

	/** source VidSourceTemplate */
	private final VidSourceTemplate vst;

	/** Get the source VidSourceTemplate */
	public VidSourceTemplate getVidSourceTemplate() {
		return vst;
	}

	/** expanded config string */
	private final String config;

	/** Get the expanded config string */
	public String getConfig() {
		return config;
	}

	/** is this a GStreamer request? */
	public boolean isGst() {
		return config.contains("!");
	}

	/** is this an old-style MJPEG request? */
	public boolean isMJPEG() {
		return !isGst();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(vst.getLabel());
		sb.append(": ");
		sb.append(config);
		return sb.toString();
	}
}
