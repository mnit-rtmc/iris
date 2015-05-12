/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dr500;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Properties;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * System information property.
 *
 * @author Douglas Lau
 */
public class SysInfoProperty extends DR500Property {

	/** Body of query request */
	static private final byte[] BODY = new byte[] {
		(byte) MsgCode.SYS_INFO.code
	};

	/** System information */
	private final Properties sys_info = new Properties();

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		encodeRequest(os, BODY);
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		Response resp = decodeResponse(is);
		if (resp.msg_code != MsgCode.SYS_INFO)
			throw new ParsingException("MSG CODE");
		// SYS_INFO response is new line delimited set of <tag>=<value>
		StringReader r = new StringReader(new String(resp.body, ASCII));
		sys_info.clear();
		sys_info.load(r);
	}

	/** Get the firmware version */
	public String getVersion() {
		StringBuilder sb = new StringBuilder();
		String brd = sys_info.getProperty("BRD");
		if (brd != null) {
			sb.append("BRD=");
			sb.append(brd);
			sb.append(',');
		}
		String mod = sys_info.getProperty("MOD");
		if (mod != null) {
			sb.append("MOD=");
			sb.append(mod);
			sb.append(',');
		}
		String rev = sys_info.getProperty("REV");
		if (rev != null) {
			sb.append("REV=");
			sb.append(rev);
			sb.append(',');
		}
		if (sb.length() > 0)
			return sb.substring(0, sb.length() - 1);
		else
			return "";
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return sys_info.toString();
	}
}
