/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dxm;

import java.io.IOException;
import java.nio.ByteBuffer;
import us.mn.state.dot.sched.TimeSteward;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerProp;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.ProtocolException;

/**
 * Sample property.
 *
 * @author Douglas Lau
 */
public class SampleProp extends ControllerProp {

	/** Slave Modbus ID base for detector pins */
	static private final int SLAVE_MODBUS_BASE_PIN = 59;

	/** Parse an integer value */
	static private int parseInt(String v) throws ParsingException {
		try {
			return Integer.parseInt(v);
		}
		catch (NumberFormatException e) {
			throw new ParsingException("Invalid value: " + v);
		}
	}

	/** Error code for invalid puck sample */
	static private final int PUCK_ERR = 65000;

	/** Filter valid sample data */
	static private int filterSample(int s) {
		return (s >= 0 && s < PUCK_ERR) ? s : MISSING_DATA;
	}

	/** Maximum number of scans in 30 seconds */
	static private final int MAX_C30 = 1800;

	/** Filter sample to valid scans (for diagnostics) */
	static private int filterScans(int s) {
		return (s < MAX_C30) ? s : MAX_C30;
	}

	/** Time stamp */
	private final long stamp;

	/** Sample perdiod */
	private final int period;

	/** Create a sample property */
	public SampleProp(int p) {
		stamp = TimeSteward.currentTimeMillis();
		period = p;
	}

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(Operation op, ByteBuffer tx_buf)
		throws IOException
	{
		int p = op.getController().getDetectorPinFirst();
		int pl = op.getController().getDetectorPinLast();
		if (p > 0 && pl >= p) {
			int len = 1 + pl - p;
			tx_buf.put(formatReq(p, len).getBytes("US-ASCII"));
		} else
			throw new ProtocolException("No Detector");
	}

	/** Format a query request */
	private String formatReq(int p, int len) {
		StringBuilder sb = new StringBuilder();
		sb.append("CMD0001 ");
		sb.append(p + SLAVE_MODBUS_BASE_PIN);
		sb.append(",");
		sb.append(len);
		sb.append(",0,0,0\n");
		return sb.toString();
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(Operation op, ByteBuffer rx_buf)
		throws IOException
	{
		ControllerImpl controller = op.getController();
		int p = controller.getDetectorPinFirst();
		int pl = controller.getDetectorPinLast();
		if (p > 0 && pl >= p) {
			int len = 1 + pl - p;
			int[] occ = parseOcc(rx_buf, p, len);
			controller.storeOccupancy(stamp, period, p,occ,MAX_C30);
		} else
			throw new ProtocolException("No Detector");
	}

	/** Parse an occupancy response */
	private int[] parseOcc(ByteBuffer rx_buf, int p, int len)
		throws IOException
	{
		byte[] buf = new byte[rx_buf.remaining()];
		rx_buf.get(buf);
		String resp = new String(buf, "US-ASCII");
		String[] par = resp.trim().split(",", 2 + len);
		if (par.length != 2 + len)
			throw new ParsingException("Wrong # of params");
		int reg = p + SLAVE_MODBUS_BASE_PIN;
		if (!par[0].equals("RSP0001" + reg))
			throw new ParsingException("Invalid response code");
		if (!"".equals(par[1 + len]))
			throw new ParsingException("Invalid response tail");
		int[] occ = new int[len];
		for (int i = 0; i < len; i++) {
			int s = parseInt(par[i + 1]);
			occ[i] = filterScans(filterSample(s));
		}
		return occ;
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "sample";
	}
}
