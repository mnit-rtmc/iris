/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2016  Minnesota Department of Transportation
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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.DownloadRequestException;
import us.mn.state.dot.tms.server.comm.InvalidAddressException;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Mndot Property.
 *
 * @author Douglas Lau
 */
abstract public class MndotProperty extends ControllerProperty {

	/** Offset for DROP/CAT or DROP/STAT field */
	static private final int OFF_DROP_CAT = 0;

	/** Offset for message length field */
	static private final int OFF_LENGTH = 1;

	/** Offset for message payload field */
	static protected final int OFF_PAYLOAD = 2;

	/** Maximum data bytes */
	static private final int MAX_DATA_BYTES = 125;

	/** Calculate the checksum of a packet.
	 * @param pkt Packet.
	 * @return Calculated checksum of packet. */
	static private byte checksum(byte[] pkt) {
		byte xsum = 0;
		for (int i = 0; i < pkt.length - 1; i++)
			xsum ^= pkt[i];
		return xsum;
	}

	/** Create a request packet.
	 * @param c Controller.
	 * @param cat Category code.
	 * @param n_bytes Number of additional bytes.
	 * @return Request packet. */
	static protected final byte[] createRequest(ControllerImpl c,
		CatCode cat, int n_bytes) throws IOException
	{
		byte[] pkt = new byte[3 + n_bytes];
		pkt[OFF_DROP_CAT] = dropCat(c, cat);
		pkt[OFF_LENGTH] = (byte)n_bytes;
		return pkt;
	}

	/** Make the initical drop/category byte.
	 * @param c Controller.
	 * @param cat Category code.
	 * @return Combined drop/category byte. */
	static private byte dropCat(ControllerImpl c, CatCode cat)
		throws IOException
	{
		int drop = c.getDrop();
		CommProtocol cp = c.getProtocol();
		if (cp == CommProtocol.MNDOT_5) {
			if (drop < 1 || drop > 31)
				throw new InvalidAddressException(drop);
			return (byte) (drop << 3 | cat.ordinal());
		} else {
			if (drop < 1 || drop > 15)
				throw new InvalidAddressException(drop);
			return (byte) (drop << 4 | cat.ordinal());
		}
	}

	/** Calculate the checksum for a request packet */
	static protected final void calculateChecksum(byte[] pkt) {
		pkt[pkt.length - 1] = checksum(pkt);
	}

	/** Read to the end of a buffer.
	 * @param is Input stream to read.
	 * @param buf Buffer to store data.
	 * @param off Offset to start reading.
	 * @throws EOFException at end of input stream.
	 * @throws ParsingException when packet is not fully read. */
	static private void readFully(InputStream is, byte[] buf, int off)
		throws IOException
	{
		assert buf.length > off;
		int len = buf.length - off;
		int b = is.read(buf, off, len);
		if (b < 0)
			throw new EOFException("END OF STREAM");
		if (b != len)
			throw new ParsingException("BAD LENGTH");
	}

	/** Read a response from an input stream.
	 * @param is InputStream to read from.
	 * @return Packet read from stream.
	 * @throws IOException on parse errors or end of stream. */
	static private byte[] readResponse(InputStream is) throws IOException {
		byte[] header = new byte[3];
		readFully(is, header, 0);
		int len = header[OFF_LENGTH];
		if (len < 0 || len > MAX_DATA_BYTES)
			throw new ParsingException("INVALID LENGTH: " + len);
		if (len > 0) {
			byte[] pkt = new byte[3 + len];
			System.arraycopy(header, 0, pkt, 0, 3);
			readFully(is, pkt, 3);
			return pkt;
		} else
			return header;
	}

	/** Parse the drop address from a response packet.
	 * @param pkt Response packet.
	 * @param cp Communication protocol.
	 * @return Drop address. */
	static private int parseDrop(byte[] pkt, CommProtocol cp) {
		int drop_sh = pkt[OFF_DROP_CAT] & 0xFF;
		return (cp == CommProtocol.MNDOT_5)
		     ? (drop_sh >> 3)
		     : (drop_sh >> 4);
	}

	/** Validate a response checksum.
	 * @param pkt Response packet.
	 * @throws ChecksumException if checksum is invalid. */
	static private void validateChecksum(byte[] pkt)
		throws ChecksumException
	{
		byte xsum = pkt[pkt.length - 1];
		if (xsum != checksum(pkt))
			throw new ChecksumException(pkt);
	}

	/** Validate a response packet.
	 * @param c Controller receiving response.
	 * @param pkt Response packet.
	 * @throws IOException on errors parsing the packet. */
	static private void validateResponse(ControllerImpl c, byte[] pkt)
		throws IOException
	{
		if (pkt.length < 3)
			throw new ParsingException("TOO SHORT");
		validateChecksum(pkt);
		if (pkt.length != pkt[OFF_LENGTH] + 3)
			throw new ParsingException("INVALID LENGTH");
		CommProtocol cp = c.getProtocol();
		if (parseDrop(pkt, cp) != c.getDrop())
			throw new ParsingException("DROP ADDRESS MISMATCH");
		parseStatus(parseStat(pkt, cp));
	}

	/** Parse the stat code from a response packet.
	 * @param pkt Response packet.
	 * @param cp Communication protocol.
	 * @return Status code. */
	static private int parseStat(byte[] pkt, CommProtocol cp) {
		byte drop_stat = pkt[OFF_DROP_CAT];
		return (cp == CommProtocol.MNDOT_5)
		     ? (drop_stat & 0x07)
		     : (drop_stat & 0x0F);
	}

	/** Parse packet status code.
	 * @param status Recieved status code.
	 * @throws IOException for status errors from controller. */
	static private void parseStatus(int status) throws IOException {
		switch (StatCode.fromOrdinal(status)) {
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

	/** Validate response length.
	 * @param pkt Response packet.
	 * @param len Expected packet length.
	 * @throws ParsingException if length is not valid. */
	protected final void validateResponseLength(byte[] pkt, int len)
		throws ParsingException
	{
		if (pkt.length != len) {
			throw new ParsingException("BAD RESPONSE LENGTH: " +
				pkt.length + " FOR " + getClass());
		}
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		byte[] pkt = readResponse(is);
		validateResponse(c, pkt);
		parseQuery(pkt);
	}

	/** Parse a query response packet.
	 * @param pkt Response packet.
	 * @throws IOException on parse errors. */
	protected void parseQuery(byte[] pkt) throws IOException {
		// Override if necessary
	}

	/** Decode a STORE response */
	@Override
	public void decodeStore(ControllerImpl c, InputStream is)
		throws IOException
	{
		byte[] pkt = readResponse(is);
		validateResponse(c, pkt);
		parseStore(pkt);
	}

	/** Parse a store response packet.
	 * @param pkt Response packet.
	 * @throws IOException on parse errors. */
	protected void parseStore(byte[] pkt) throws IOException {
		// Override if necessary
	}
}
