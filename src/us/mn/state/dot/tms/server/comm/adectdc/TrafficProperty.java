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

	/** Application data buffer offsets */
	static private final int OFF_STATUS = 2;
	static private final int OFF_COUNT = 3;
	static private final int OFF_VEHICLE = 7;

	/** Parse from one to four vehicle informations.
	 *
	 * If multiple vehicles are reported, they are in chronological order;
	 * vehicle 1 first, then 2, 3 and 4. */
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
		// Up to 4 vehicle can be reported in a packet
		return (len == OFF_VEHICLE + vlen * 1) ||
		       (len == OFF_VEHICLE + vlen * 2) ||
		       (len == OFF_VEHICLE + vlen * 3) ||
		       (len == OFF_VEHICLE + vlen * 4);
	}

	/** Parse vehicle information in 6-byte format */
	static private ArrayList<VehicleInfo> parse6(byte[] buf) {
		ArrayList<VehicleInfo> infos = new ArrayList<VehicleInfo>();
		for (int i = OFF_VEHICLE; i < buf.length; i += 6)
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
		for (int i = OFF_VEHICLE; i < buf.length; i += 7)
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
		for (int i = OFF_VEHICLE; i < buf.length; i += 11)
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
	private boolean missed = true;

	/** Previous time stamp */
	private long stamp0 = 0;

	/** Current time stamp */
	private long stamp1 = 0;

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
		status = 0;
		vehicles.clear();
		byte[] buf = parseLong(is, c);
		if (buf == null) {
			toggleFrameControlBit();
			return;
		}
		// Is response always "Code 0" when detector in SiTOS mode?
		if (buf[0] != (CTRL_VEHICLE) && buf[0] != (CTRL_RESET))
			throw new ParsingException("Wrong CTRL: " + buf[0]);
		if (buf.length < 3)
			throw new ParsingException("Wrong len: " + buf.length);
		parseStatus(buf);
		if (buf.length == 3) {
			toggleFrameControlBit();
			return;
		}
		ArrayList<VehicleInfo> info = parseVehicleInfo(buf);
		long vc = count + info.size();
		parseVehicleCount(buf);
		missed = (count > vc);
		vehicles.addAll(info);
		estimateTimeStamps();
		toggleFrameControlBit();
	}

	/** Parse the "lifetime" vehicle count */
	private void parseVehicleCount(byte[] buf) {
		count = parseU32(buf, OFF_COUNT);
	}

	/** Get events as a string */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("traf");
		for (VehicleInfo info: vehicles) {
			sb.append(' ');
			sb.append(info.toString());
		}
		sb.append(", ");
		sb.append(super.toString());
		return sb.toString();
	}

	/** Log vehicle detection events */
	public void logVehicles(ControllerImpl controller) {
		if (missed)
			controller.logGap();
		DetectorImpl det = controller.getDetectorAtPin(1);
		if (det != null)
			logVehicles(det);
	}

	/** Estimate the time stamps */
	private void estimateTimeStamps() {
		final long st = (!missed) ? stamp1 : 0;
		long now = TimeSteward.currentTimeMillis();
		long dur = getDuration();
		if (st > 0 && dur > 0)
			stamp1 = Math.min(st + dur, now);
		else {
			// estimated age of logged event: 1.5 sec
			stamp1 = now - 1500;
		}
		stamp0 = stamp1 - dur;
	}

	/** Get duration of vehicle events */
	private long getDuration() {
		long dur = 0;
		for (VehicleInfo info: vehicles) {
			int h = info.getHeadway();
			if (h > 0)
				dur += h;
			else
				return 0;
		}
		return dur;
	}

	/** Log vehicle detection events */
	private void logVehicles(DetectorImpl det) {
		long st = stamp0;
		for (VehicleInfo info: vehicles) {
			st += info.getHeadway();
			info.logVehicle(det, st);
		}
	}
}
