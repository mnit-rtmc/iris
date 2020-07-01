/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  SRF Consulting Group
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
import us.mn.state.dot.tms.CameraTemplate;
import us.mn.state.dot.tms.CameraTemplateHelper;
import us.mn.state.dot.tms.VidSourceTemplate;
import us.mn.state.dot.tms.VidSourceTemplateHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.utils.SubnetChecker;

/** VidStreamReq: Video-stream-request parameters.
 * 
 * Data class that contains the parameters needed
 * to open a video stream.
 * 
 * The VidPanel class uses the static
 * VidStreamReq.getVidStreamReqs(...) generator
 * method to convert a Camera reference to a list
 * of VidStreamReq objects that "should" be able
 * to stream video from that camera to the current
 * client.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class VidStreamReq {

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

	//-------------------------------------------
	// static helper methods
	
	static private boolean isNothing(String str) {
		return ((str == null) || str.isEmpty());
	}

	static boolean isMember(String list, String item) {
		if (isNothing(list))
			return true; // an empty list matches everything
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
	 *  if string does not have a port and defPort is not null,
	 *  appends ":<defPort>" to the addr string before returning it.. */
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
	
	/** Get "addr[:port]" from an addr string and port integer value. If port
	 *  is null and defPort is not null, appends ":<defPort>" to the addr
	 *  string before returning it. */
	static private String getAddrPort(String addr, Integer port, Integer defPort) {
		if (isNothing(addr))
			return null;
		if (port == null && defPort != null)
			port = defPort;
		if (port != null)
			addr += ":" + port;
		return addr;
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
	
	//-------------------------------------------
	// static VidStreamReq creation method
	
	static final Pattern pattern = Pattern.compile("\\{(.+?)\\}");
	
	/** Create a VidStreamReq from a
	 *  VidSourceTemplate and Camera.
	 * 
	 * Returns an expanded VidStreamReq
	 * (or a null if expanding the template
	 *  fails some requirement).
	 * 
	 * @param st   VidSourceTemplate
	 * @param cam  Camera
	 * @return an expanded VideoReq or 
	 *         null if the template expansion fails.
	 */
	private static VidStreamReq create(VidSourceTemplate st, Camera cam) {
		Session ses = Session.getCurrent();
		Properties p = ses.getProperties();
		String config = st.getConfig();
//		System.out.println("In: "+config);
//		System.out.println("In: "+st.toString());
		
		// check subnet
		String subnets = st.getSubnets();
		if (!isMember(subnets,
				SubnetChecker.getSubnetName())) {
//			System.out.println("Out: (null); not in template subnet");
			return null;
		}

		// expand config replacement-fields
		Matcher m = pattern.matcher(config);
		StringBuffer sb = new StringBuffer();
		String tok, val;
		while (m.find()) {
			tok = m.group(1);
			val = null;
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
			else
				val = p.getProperty(tok);
			// ignore a template with an unavailable replacement-field
			if (isNothing(val)) {
//				System.out.println("Out: (null); missing substitution for {"+tok+"}");
				return null;
			}
			m.appendReplacement(sb, val);
		}
		m.appendTail(sb);
		config = sb.toString();
		
		if (VidStreamMgrGst.isOkConfig(config)
		 || VidStreamMgrMJPEG.isOkConfig(config))
			return new VidStreamReq(st, config);
		return null;
	}

//	private xVideoReq makeTestVR(String cam, String templateName, String codec, String config) {
//		Camera c = us.mn.state.dot.tms.CameraHelper.lookup(cam);
//		VidSourceTemplate st = new VidSourceTemplateImpl();
//	}

	/** Comparator to sort by cameraStreamOrder.order value */
	public static Comparator<CameraVidSourceOrder> streamOrder =
		new Comparator<CameraVidSourceOrder>() {
			public int compare(CameraVidSourceOrder cso1, CameraVidSourceOrder cso2) {
				return Integer.compare(cso1.getSourceOrder(), cso2.getSourceOrder());
			}
		};

	/** Get list of VidStreamReq(s) for a given camera */
	public static List<VidStreamReq> getVidStreamReqs(Camera c) {
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
		csoList.sort(streamOrder);
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
	public static List<CameraVidSourceOrder> getCamVidSrcOrder(CameraTemplate ct) {
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
		csoList.sort(streamOrder);
		return csoList;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(vst.getLabel());
		sb.append(": ");
		sb.append(config);
		return sb.toString();
	}
	
}
