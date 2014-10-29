/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.stc;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Control property is used to control the gate arm.
 *
 * @author Douglas Lau
 */
public class ControlProperty extends STCProperty {

	/** Byte offsets from beginning of control request */
	static private final int OFF_PUSH_BUTTON_OPEN = 1;
	static private final int OFF_PUSH_BUTTON_CLOSE = 2;
	static private final int OFF_PUSH_BUTTON_STOP = 3;
	static private final int OFF_OPEN_PARTIAL = 4;
	static private final int OFF_EMERGENCY_OPEN = 5;
	static private final int OFF_EMERGENCY_CLOSE = 6;
	static private final int OFF_OPEN_INTERLOCK = 7;
	static private final int OFF_BLOCK_EXIT_VEHICLE_DETECTOR = 8;

	/** Create a new control property */
	public ControlProperty(String pw) {
		super(pw);
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] data = new byte[9];
		data[0] = 'C';
		formatBoolean(data, OFF_PUSH_BUTTON_OPEN, pbo);
		formatBoolean(data, OFF_PUSH_BUTTON_CLOSE, pbc);
		formatBoolean(data, OFF_PUSH_BUTTON_STOP, pbs);
		formatBoolean(data, OFF_OPEN_PARTIAL, op);
		formatBoolean(data, OFF_EMERGENCY_OPEN, eo);
		formatBoolean(data, OFF_EMERGENCY_CLOSE, ec);
		formatBoolean(data, OFF_OPEN_INTERLOCK, oi);
		formatBoolean(data, OFF_BLOCK_EXIT_VEHICLE_DETECTOR, bevd);
		os.write(formatRequest(c.getDrop(), data));
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is)
		throws IOException
	{
		parseFrame(is, c.getDrop());
	}

	/** Parse a received message */
	@Override
	protected void parseMessage(byte[] msg, int len)
		throws IOException
	{
		if(msg[0] != 'C')
			super.parseMessage(msg, len);
		else if(len != 1)
			throw new ParsingException("INVALID LENGTH:" + len);
	}

	/** Push button open */
	private boolean pbo = false;

	/** Set open request */
	public void setOpen(boolean o) {
		pbo = o;
	}

	/** Push button close */
	private boolean pbc = false;

	/** Set close request */
	public void setClose(boolean c) {
		pbc = c;
	}

	/** Push button stop */
	private boolean pbs = false;

	/** Set stop request */
	public void setStop(boolean s) {
		pbs = s;
	}

	/** Open partial */
	private boolean op = false;

	/** Emergency open */
	private boolean eo = false;

	/** Emergency close */
	private boolean ec = false;

	/** Open interlock */
	private boolean oi = false;

	/** Set open interlock */
	public void setInterlock(boolean i) {
		oi = i;
	}

	/** Block exit vehicle detector */
	private boolean bevd = false;

	/** Get a string representation */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("pbo:");
		sb.append(pbo);
		sb.append(" pbc:");
		sb.append(pbc);
		sb.append(" pbs:");
		sb.append(pbs);
		sb.append(" op:");
		sb.append(op);
		sb.append(" eo:");
		sb.append(eo);
		sb.append(" ec:");
		sb.append(ec);
		sb.append(" oi:");
		sb.append(oi);
		sb.append(" bevd:");
		sb.append(bevd);
		return sb.toString();
	}
}
