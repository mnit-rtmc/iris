/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.adectdc;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DetectorImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Traffic data property
 *
 * @author Douglas Lau
 */
public class TrafficProperty extends StatusProperty {

	/** Parse from one to four vehicle informations */
	static private ArrayList<VehicleInfo> parseVehicleInfo(byte[] buf)
		throws ParsingException
	{
		if (isLengthValid(buf.length, 6))
			return parse6(buf);
		else if (isLengthValid(buf.length, 7))
			return parse7(buf);
		else if (isLengthValid(buf.length, 11))
			return parse11(buf);
		else
			throw new ParsingException("Wrong len: " + buf.length);
	}

	/** Check if data length is valid for vehicle information */
	static private boolean isLengthValid(int len, int vlen) {
		return (len == 7 + vlen) ||
		       (len == 7 + vlen * 2) ||
		       (len == 7 + vlen * 3) ||
		       (len == 7 + vlen * 4);
	}

	/** Parse vehicle information in 6-byte format */
	static private ArrayList<VehicleInfo> parse6(byte[] buf) {
		ArrayList<VehicleInfo> infos = new ArrayList<VehicleInfo>();
		for (int i = 12; i < buf.length; i += 6)
			infos.add(parse6(buf, i));
		return infos;
	}

	/** Parse vehicle information in 6-byte format */
	static private VehicleInfo parse6(byte[] buf, int pos) {
		int speed = parse8(buf, pos);
		int class_lane = parse8(buf, pos + 1);
		int occupancy = parse16(buf, pos + 2);
		int gap = parse16(buf, pos + 4);
		return new VehicleInfo(speed, class_lane, occupancy, gap,
			0, 0);
	}

	/** Parse vehicle information in 7-byte format */
	static private ArrayList<VehicleInfo> parse7(byte[] buf) {
		ArrayList<VehicleInfo> infos = new ArrayList<VehicleInfo>();
		for (int i = 12; i < buf.length; i += 7)
			infos.add(parse7(buf, i));
		return infos;
	}

	/** Parse vehicle information in 7-byte format */
	static private VehicleInfo parse7(byte[] buf, int pos) {
		int speed = parse8(buf, pos);
		int class_lane = parse8(buf, pos + 1);
		int occupancy = parse16(buf, pos + 2);
		int gap = parse16(buf, pos + 4);
		int length = parse8(buf, pos + 6);
		return new VehicleInfo(speed, class_lane, occupancy, gap,
			length, 0);
	}

	/** Parse vehicle information in 11-byte format */
	static private ArrayList<VehicleInfo> parse11(byte[] buf) {
		ArrayList<VehicleInfo> infos = new ArrayList<VehicleInfo>();
		for (int i = 12; i < buf.length; i += 11)
			infos.add(parse11(buf, i));
		return infos;
	}

	/** Parse vehicle information in 11-byte format */
	static private VehicleInfo parse11(byte[] buf, int pos) {
		int speed = parse8(buf, pos);
		int class_lane = parse8(buf, pos + 1);
		int occupancy = parse16(buf, pos + 2);
		int gap = parse16(buf, pos + 4);
		int length = parse8(buf, pos + 6);
		int sync_stamp = parse16(buf, pos + 8);
		return new VehicleInfo(speed, class_lane, occupancy, gap,
			length, sync_stamp);
	}

	/** Vehicle information */
	private final ArrayList<VehicleInfo> vehicles =
		new ArrayList<VehicleInfo>();

	/** Control byte */
	private byte ctrl = CTRL_FCB | CTRL_FCV | CTRL_VEHICLE;

	/** Toggle the frame control bit (FCB) */
	private void toggleFrameControlBit() {
		ctrl ^= CTRL_FCB;
	}

	/** "Lifetime" vehicle count */
	private long count;

	/** Did we miss any vehicles? */
	public boolean missed = true;

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		os.write(formatShort(ctrl, c));
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		byte[] buf = parseLong(is, c);
		if (buf == null) {
			vehicles.clear();
			toggleFrameControlBit();
			return;
		}
		if (buf[0] != (CTRL_VEHICLE))
			throw new ParsingException("Wrong CTRL: " + buf[0]);
		if (buf.length == 3) {
			toggleFrameControlBit();
			parseStatus(buf);
			return;
		}
		ArrayList<VehicleInfo> info = parseVehicleInfo(buf);
		parseStatus(buf);
		long vc = count + info.size();
		parseVehicleCount(buf);
		missed = (count != vc);
		vehicles.clear();
		vehicles.addAll(info);
		toggleFrameControlBit();
	}

	/** Parse the "lifetime" vehicle count */
	private void parseVehicleCount(byte[] buf) {
		count = parseU32(buf, 3);
	}

	/** Get events as a string */
	@Override
	public String toString() {
		return "vehicles: " + vehicles.size() + ", " +
			super.toString();
	}

	/** Log vehicle detection events */
	public void logVehicles(ControllerImpl controller) {
		DetectorImpl det = controller.getDetectorAtPin(1);
		if (det != null)
			logVehicles(det);
	}

	/** Log vehicle detection events */
	private void logVehicles(DetectorImpl det) {
		long st = TimeSteward.currentTimeMillis();
		for (VehicleInfo info: vehicles)
			st -= info.getHeadway();
		for (VehicleInfo info: vehicles) {
			info.logVehicle(det, st);
			st += info.getHeadway();
		}
	}
}
