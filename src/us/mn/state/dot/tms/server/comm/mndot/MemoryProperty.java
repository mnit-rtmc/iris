/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2014  Minnesota Department of Transportation
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
import java.io.OutputStream;
import us.mn.state.dot.tms.utils.HexString;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * Memory Property to read or write 170 controller memory.
 *
 * @author Douglas Lau
 */
public class MemoryProperty extends MndotProperty {

	/** Maximum length of a memory request (128 - 5 octet overhead) */
	static private final int MAX_LENGTH = 123;

	/** Offset for MSB of memory address */
	static private final int OFF_ADDRESS_MSB = 2;

	/** Offset for LSB of memory address */
	static private final int OFF_ADDRESS_LSB = 3;

	/** Offset for memory read length */
	static private final int OFF_READ_LENGTH = 4;

	/** 170 controller memory address */
	private final int address;

	/** Get the MSB of the controller memory address */
	private byte getAddressMsb() {
		return (byte)((address >> 8) & 0xFF);
	}

	/** Get the LSB of the controller memory address */
	private byte getAddressLsb() {
		return (byte)(address & 0xFF);
	}

	/** 170 controller memory payload */
	private final byte[] payload;

	/** Payload position offset */
	private int pos = 0;

	/** Check for a valid payload length */
	private void checkPayloadLength() throws ProtocolException {
		if (payload.length < 1 || payload.length > MAX_LENGTH)
			throw new ProtocolException("INVALID PAYLOAD SIZE");
	}

	/** Create a memory property */
	public MemoryProperty(int a, byte[] buf) throws ProtocolException {
		address = a;
		payload = buf;
		checkPayloadLength();
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] req = createRequest(c, CatCode.READ_MEMORY, 3);
		req[OFF_ADDRESS_MSB] = getAddressMsb();
		req[OFF_ADDRESS_LSB] = getAddressLsb();
		req[OFF_READ_LENGTH] = (byte)payload.length;
		calculateChecksum(req);
		os.write(req);
	}

	/** Parse a query response packet.
	 * @param pkt Response packet.
	 * @throws IOException on parse errors. */
	@Override
	protected void parseQuery(byte[] pkt) throws IOException {
		validateResponseLength(pkt, payload.length + 3);
		System.arraycopy(pkt, OFF_PAYLOAD, payload, 0, payload.length);
	}

	/** Encode a STORE request */
	@Override
	public void encodeStore(ControllerImpl c, OutputStream os)
		throws IOException
	{
		byte[] pkt = createRequest(c, CatCode.WRITE_MEMORY,
			payload.length + 2);
		pkt[OFF_ADDRESS_MSB] = getAddressMsb();
		pkt[OFF_ADDRESS_LSB] = getAddressLsb();
		System.arraycopy(payload, 0, pkt, 4, payload.length);
		calculateChecksum(pkt);
		os.write(pkt);
	}

	/** Format a 2-digit BCD value.
	 * @param v Binary value to convert to BCD. */
	public void formatBCD2(int v) throws IOException {
		formatBCD2(payload, pos, v);
		++pos;
	}

	/** Format a 4-digit BCD value.
	 * @param v Binary value to convert to BCD. */
	public void formatBCD4(int v) throws IOException {
		formatBCD4(payload, pos, v);
		pos += 2;
	}

	/** Format a 16-bit value.
	 * @param v Binary value to format. */
	public void format16(int v) {
		format16(payload, pos, v);
		pos += 2;
	}

	/** Parse a 4-digit BCD value.
	 * @return Parsed value. */
	public int parseBCD4() throws IOException {
		int v = parseBCD4(payload, pos);
		pos += 2;
		return v;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("mem ");
		sb.append(Integer.toHexString(address).toUpperCase());
		sb.append(": ");
		sb.append(HexString.format(payload, ' '));
		return sb.toString();
	}
}
