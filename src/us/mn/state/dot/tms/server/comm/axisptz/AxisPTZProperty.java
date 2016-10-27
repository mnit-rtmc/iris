/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2015  AHMCT, University of California
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
package us.mn.state.dot.tms.server.comm.axisptz;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.utils.Base64;


/**
 * Axis PTZ Property
 *
 * @author Travis Swanston
 */
abstract public class AxisPTZProperty extends ControllerProperty {

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
	}

	/** Decode a STORE request */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is)
		throws IOException
	{
	}

	/** Get a short description of the property */
	public abstract String getDesc();

	/**
	 * Issue a VAPIX request to the Axis controller.
	 * NOTE: Currently, this method performs no special URL encoding.
	 *
	 * @param ci     The ControllerImpl
	 * @param os     The OutputStream
	 * @param cmd    The VapixCmd
	 */
	protected void issueRequest(ControllerImpl ci, OutputStream os,
		VapixCmd c)
	{
		String reqpath = c.getCommand();
		Map<String, String> pmap = c.getParams();

		boolean first = true;
		for (String p : pmap.keySet()) {
			reqpath += (first ? "?" : "&");
			reqpath += p + "=" + pmap.get(p);
			first = false;
		}

		String host = getHost(ci);
		if (host == null)
			return;

		Integer port = getPort(ci);
		if (port == null)
			return;

		String auth = getAuth(ci);

		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(os, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			AxisPTZPoller.log("UnsupportedEncodingException: "
				+ e);
			return;
		}

		String msg = ""
			+ "GET " + reqpath + " HTTP/1.0\r\n"
			+ "Host: " + host + ":" + port + "\r\n"
			+ ( (auth != null)
				? ("Authorization: Basic " + auth + "\r\n")
				: "")
			+ "\r\n"
			;
		try {
			writer.write(msg);
			writer.flush();
		}
		catch (IOException e) {
			AxisPTZPoller.log("IOException: " + e);
			return;
		}
	}

	private String getHost(ControllerImpl ci) {
		AxisPTZPoller poller = (AxisPTZPoller) ci.getPoller();
		if (poller == null)
			return null;
		String host = poller.getHost();
		return host;
	}

	private Integer getPort(ControllerImpl ci) {
		AxisPTZPoller poller = (AxisPTZPoller) ci.getPoller();
		if (poller == null)
			return null;
		Integer port = poller.getPort();
		if ((port == null) || (port.intValue() < 1))
			return null;
		return port;
	}

	private static String getAuth(ControllerImpl ci) {
		String userpass = ci.getPassword();
		if (userpass == null)
			return null;
		if (!userpass.matches("^[^:]+:[^:]+$"))
			return null;
		return Base64.encode(userpass.getBytes());
	}

}

