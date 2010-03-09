/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.mndot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.server.comm.AddressedMessage;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.DownloadRequestException;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Mndot protocol message
 *
 * @author Douglas Lau
 */
public class Message implements AddressedMessage {

	/** Status codes for 170 communication protocol */
	static public final int OK = 0;
	static public final int BAD_MESSAGE = 1;
	static public final int BAD_POLL_CHECKSUM = 2;
	static public final int DOWNLOAD_REQUEST = 3;
	static public final int WRITE_PROTECT = 4;
	static public final int MESSAGE_SIZE = 5;
	static public final int NO_DATA = 6;
	static public final int NO_RAM = 7;
	static public final int DOWNLOAD_REQUEST_4 = 8; // 4-bit addressing

	/** Check controller status code and throw an equivalent exception */
	protected void checkStatus(int status) throws IOException {
		switch(status) {
		case OK:
			return;
		case BAD_MESSAGE:
			throw new ParsingException("BAD MESSAGE");
		case BAD_POLL_CHECKSUM:
			throw new ChecksumException(
				"CONTROLLER I/O CHECKSUM ERROR");
		case DOWNLOAD_REQUEST:
		case DOWNLOAD_REQUEST_4:
			throw new DownloadRequestException("CODE: " + status);
		case WRITE_PROTECT:
			throw new ControllerException("WRITE PROTECT");
		case MESSAGE_SIZE:
			throw new ParsingException("MESSAGE SIZE");
		case NO_DATA:
			throw new ControllerException("NO SAMPLE DATA");
		case NO_RAM:
			throw new ControllerException("NO RAM");
		default:
			throw new ParsingException("BAD STATUS: " + status);
		}
	}

	/** Serial output stream */
	protected final OutputStream output;

	/** Serial input stream */
	protected final InputStream input;

	/** Drop address */
	protected final int drop;

	/** Protocol version */
	protected final CommProtocol protocol;

	/** Request object */
	protected Request req;

	/** Create a new Mndot protocol message */
	public Message(OutputStream o, InputStream i, int d, CommProtocol p) {
		output = o;
		input = i;
		drop = d;
		protocol = p;
	}

	/** Add a request object to this message */
	public void add(Object mo) {
		if(mo instanceof Request)
			req = (Request)mo;
		else
			req = null;
	}

	/** Perform a "get" request */
	public void getRequest() throws IOException {
		if(req == null)
			throw new IOException("No request");
		req.doGetRequest(this);
	}

	/** Perform a "set" request */
	public void setRequest() throws IOException {
		if(req == null)
			throw new IOException("No request");
		req.doSetRequest(this);
	}

	/** Get the drop from the response drop/status byte */
	protected int getDrop(byte[] buf) {
		byte drop_stat = buf[Request.OFF_DROP_CAT];
		if(protocol == CommProtocol.MNDOT_5)
			return (drop_stat & 0xFF) >> 3;
		else
			return (drop_stat & 0xFF) >> 4;
	}

	/** Get the stat from the response drop/status byte */
	protected int getStat(byte[] buf) {
		byte drop_stat = buf[Request.OFF_DROP_CAT];
		if(protocol == CommProtocol.MNDOT_5)
			return drop_stat & 0x07;
		else
			return drop_stat & 0x0F;
	}

	/** Validate a response message */
	protected void validateResponse(byte[] req, byte[] res)
		throws IOException
	{
		if(getDrop(res) != getDrop(req))
			throw new ParsingException("DROP ADDRESS MISMATCH");
		if(res.length < 2 || res.length != res[Request.OFF_LENGTH] + 3)
			throw new ParsingException("INVALID LENGTH");
		checkStatus(getStat(res));
	}

	/** Make the initical drop/category byte */
	protected byte dropCat(int cat) {
		if(protocol == CommProtocol.MNDOT_5)
			return (byte)(drop << 3 | cat);
		else
			return (byte)(drop << 4 | cat);
	}
}
