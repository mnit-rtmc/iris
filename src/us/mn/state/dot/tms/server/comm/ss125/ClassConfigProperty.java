/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss125;

import java.io.IOException;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.FEET;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Vehicle Classification Configuration Property.
 *
 * @author Douglas Lau
 */
public class ClassConfigProperty extends SS125Property {

	/** Message sub ID for the older fixed 4-class format.  Any other
	 * sub ID will trigger a variable (0-8) class format response. */
	static private final int FIXED_4_CLASS_FORMAT = 0xFF;

	/** Message ID for vehicle class configuration */
	protected MessageID msgId() {
		return MessageID.CLASS_CONFIG;
	}

	/** Format a QUERY request */
	@Override protected byte[] formatQuery() throws IOException {
		byte[] body = new byte[4];
		msg_sub_id = FIXED_4_CLASS_FORMAT;
		formatBody(body, MessageType.READ);
		return body;
	}

	/** Format a STORE request */
	@Override protected byte[] formatStore() throws IOException {
		byte[] body = new byte[12];
		formatBody(body, MessageType.WRITE);
		for(SS125VehClass vc: SS125VehClass.values()) {
			int pos = 3 + vc.ordinal() * 2;
			format16Fixed(body, pos, getClassLen(vc).asFloat(FEET));
		}
		return body;
	}

	/** Parse a QUERY response */
	@Override protected void parseQuery(byte[] body) throws IOException {
		if(body.length != 12)
			throw new ParsingException("BODY LENGTH");
		for(SS125VehClass vc: SS125VehClass.values()) {
			int pos = 3 + vc.ordinal() * 2;
			setClassLen(vc, new Distance(parse16Fixed(body, pos),
				FEET));
		}
	}

	/** Vehicle classificaiton lengths */
	private Distance[] class_len = new Distance[SS125VehClass.size];

	/** Get the length of a vehicle class */
	public Distance getClassLen(SS125VehClass vc) {
		return class_len[vc.ordinal()];
	}

	/** Set the length of a vehicle class */
	public void setClassLen(SS125VehClass vc, Distance l) {
		class_len[vc.ordinal()] = l;
	}

	/** Get a string representation of the property */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for(SS125VehClass vc: SS125VehClass.values()) {
			if(sb.length() > 0)
				sb.append(' ');
			sb.append(vc);
			sb.append(':');
			sb.append(getClassLen(vc));
		}
		return sb.toString();
	}
}
