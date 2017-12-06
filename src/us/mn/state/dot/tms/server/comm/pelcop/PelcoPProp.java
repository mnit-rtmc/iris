/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.pelcop;

import java.io.IOException;
import java.nio.ByteBuffer;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.ControllerProp;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * PelcoP property.
 *
 * @author Douglas Lau
 */
abstract public class PelcoPProp extends ControllerProp {

	/** Start transmission byte */
	static private final int STX = 0xA0;

	/** End transmission byte */
	static private final int ETX = 0xAF;

	/** Acknowledge byte */
	static protected final int ACK = 0xA2;

	/** Error display code */
	static public final int ERR_CODE = 0xE1;

	/** Parse a valid packet */
	static public PelcoPProp parse(ByteBuffer rx_buf, boolean logged_in,
		int mon_num) throws ParsingException
	{
		scanPkt(rx_buf);
		if (parse8(rx_buf) != STX)
			throw new ParsingException("STX");
		int mc = parse8(rx_buf);
		switch (mc) {
		case AlarmProp.REQ_CODE:
			return new AlarmProp();
		case AlarmArmProp.REQ_CODE:
			return new AlarmArmProp();
		case AlarmCycleProp.REQ_CODE:
			return new AlarmCycleProp();
		case AliveProp.REQ_CODE:
			return new AliveProp();
		case LoginProp.REQ_CODE:
			return new LoginProp();
		case ReleaseProp.REQ_CODE:
			return new ReleaseProp();
		case MacroCycleProp.REQ_CODE:
			return new MacroCycleProp(logged_in, mon_num);
		case MacroDeleteProp.REQ_CODE:
			return new MacroDeleteProp(logged_in, mon_num);
		case MacroPauseProp.REQ_CODE:
			return new MacroPauseProp(logged_in, mon_num);
		case MacroPlayProp.REQ_CODE:
			return new MacroPlayProp(logged_in, mon_num);
		case MacroSelectProp.REQ_CODE:
			return new MacroSelectProp(logged_in, mon_num);
		case MonStatusProp.REQ_CODE:
		case MonStatusProp.RESP_CODE:
			return new MonStatusProp(logged_in, mon_num);
		case MonCycleProp.REQ_CODE:
			return new MonCycleProp(logged_in, mon_num);
		case CamSelectProp.REQ_CODE:
			return new CamSelectProp(logged_in, mon_num);
		case CamPrevProp.REQ_CODE:
			return new CamPrevProp(logged_in, mon_num);
		case CamNextProp.REQ_CODE:
			return new CamNextProp(logged_in, mon_num);
		case AuxProp.REQ_CODE:
			return new AuxProp(logged_in, mon_num);
		case CamLockProp.REQ_CODE:
			return new CamLockProp(logged_in, mon_num);
		case CamUnlockProp.REQ_CODE:
			return new CamUnlockProp(logged_in, mon_num);
		case CamControlProp.REQ_CODE:
			return new CamControlProp(logged_in, mon_num);
		default:
			throw new ParsingException("Unknown msg code: " + mc);
		}
	}

	/** Scan received data for a valid packet */
	static private void scanPkt(ByteBuffer rx_buf) throws ChecksumException{
		rx_buf.mark();
		while ((rx_buf.get() & 0xFF) != STX)
			rx_buf.mark();
		int xsum = STX;
		while (true) {
			int b = rx_buf.get() & 0xFF;
			xsum ^= b;
			if (b == ETX)
				break;
		}
		int c = rx_buf.get() & 0xFF;
		if (c == xsum)
			rx_buf.reset();
		else {
			rx_buf.mark();
			throw new ChecksumException();
		}
	}

	/** Parse the tail of a packet */
	static public void parseTail(ByteBuffer rx_buf) throws ParsingException{
		if (parse8(rx_buf) != ETX)
			throw new ParsingException("ETX");
		// Parse checksum (already checked)
		parse8(rx_buf);
	}

	/** Error message */
	private ErrorMsg error;

	/** Has error response? */
	public boolean hasError() {
		return error != null;
	}

	/** Encode a QUERY response to keyboard */
	protected void encodeError(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		format8(tx_buf, ERR_CODE);
		formatBCD2(tx_buf, error.code);
	}

	/** Set an error display code */
	protected void setErrMsg(ErrorMsg e) {
		error = e;
	}

	/** Format the head of a packet */
	public void formatHead(ByteBuffer tx_buf) {
		tx_buf.put((byte) STX);
		tx_buf.mark();
	}

	/** Format the tail of a packet */
	public void formatTail(ByteBuffer tx_buf) {
		tx_buf.put((byte) ETX);
		tx_buf.reset();
		int xsum = STX;
		while (true) {
			int b = tx_buf.get() & 0xFF;
			xsum ^= b;
			if (b == ETX)
				break;
		}
		tx_buf.put((byte) xsum);
	}

	/** Get the next property to send */
	public PelcoPProp next() {
		return null;
	}
}
