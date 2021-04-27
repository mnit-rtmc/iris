/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.g4;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.units.Speed;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DetectorImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Vehicle events are parsed using this property.
 *
 * @author Douglas Lau
 */
public class VehicleEventProperty extends G4Property {

	/** Byte offsets from beginning of per vehicle header data */
	static private final int OFF_ZONE = 0;
	static private final int OFF_CLASSIFICATION = 1;
	static private final int OFF_LENGTH = 2;
	static private final int OFF_SPEED = 3;
	static private final int OFF_DURATION = 4; // dwell
	static private final int OFF_STAMP = 6; // 8 bytes, first is millis
	static private final int OFF_MPH = 14;

	/** Valid age of vehicle events (1 hour) */
	static private final long VALID_AGE_MS = 60 * 60 * 1000;

	/** Detection zone */
	private int zone;

	/** Get the zone number */
	public int getZone() {
		return zone;
	}

	/** Vehicle classification */
	private int classification;

	/** Get vehicle classification */
	public int getClassification() {
		return classification;
	}

	/** Vehicle length (decimeters) */
	private int length;

	/** Get the vehicle length (ft) */
	public int getLengthFt() {
		Distance len = new Distance(length, Distance.Units.DECIMETERS);
		return len.round(Distance.Units.FEET);
	}

	/** Vehicle speed (units depends on "mph") */
	private int speed;

	/** Get the vehicle speed (mph) */
	public int getSpeedMph() {
		if (mph)
			return speed;
		else {
			Speed s = new Speed(speed, Speed.Units.KPH);
			return s.round(Speed.Units.MPH);
		}
	}

	/** Get the vehicle duration (ms) */
	private int duration;

	/** Get the vehicle duration (ms) */
	public int getDuration() {
		return duration;
	}

	/** Time stamp */
	private long stamp;

	/** Time stamp */
	public long getStamp() {
		return stamp;
	}

	/** Is time stamp valid? */
	public boolean isValidStamp() {
		long now = TimeSteward.currentTimeMillis();
		return (stamp > now - VALID_AGE_MS) && (stamp < now);
	}

	/** Mph nnits (false for kph) */
	private boolean mph;

	/** Create a new vehicle event property */
	public VehicleEventProperty() { }

	/** Encode a QUERY request */
	@Override
	public void encodeQuery(ControllerImpl c, OutputStream os)
		throws IOException
	{
		// no poll; events arrive asynchronously
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		parseFrame(is, c.getDrop());
	}

	/** Parse the data from one frame.
	 * @param qual Qualifier code.
	 * @param data Data packet. */
	@Override
	protected void parseData(QualCode qual, byte[] data) throws IOException {
		if (qual == QualCode.VEHICLE_EVENT)
			parseVehicleEvent(data);
		else
			super.parseData(qual, data);
	}

	/** Parse vehicle event */
	private void parseVehicleEvent(byte[] data) throws ParsingException {
		if (data.length != 15)
			throw new ParsingException("INVALID HEADER LENGTH");
		zone = parse8(data, OFF_ZONE);
		classification = parse8(data, OFF_CLASSIFICATION);
		length = parse8(data, OFF_LENGTH);
		speed = parse8(data, OFF_SPEED);
		duration = parse16(data, OFF_DURATION);
		stamp = parseStampMs(data, OFF_STAMP);
		int unit = parse8(data, OFF_MPH);
		if (unit < 0 || unit > 1)
			throw new ParsingException("INVALID UNIT");
		mph = (unit == 1);
	}

	/** Get a string representation */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("zone:");
		sb.append(zone);
		sb.append(" classification:");
		sb.append(classification);
		sb.append(" length:");
		sb.append(getLengthFt());
		sb.append(" speed:");
		sb.append(getSpeedMph());
		sb.append(" duration:");
		sb.append(duration);
		sb.append(" stamp:");
		sb.append(new Date(stamp));
		return sb.toString();
	}

	/** Log a vehicle detection event */
	public void logVehicle(ControllerImpl controller) {
		DetectorImpl det = controller.getDetectorAtPin(zone + 1);
		if (det != null) {
			det.logVehicle(duration, 0, stamp, getSpeedMph(),
				getLengthFt());
		}
	}
}
