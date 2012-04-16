/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.g4;

import java.util.Date;
import java.util.LinkedList;
import us.mn.state.dot.sched.TimeSteward;
import static us.mn.state.dot.tms.Constants.MISSING_DATA;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.utils.SString;

/**
 * This represents a record received from a G4 controller.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class G4Rec {

	/** Maximum number of scans for one sample */
	static private final int MAX_SCANS = 1000;

	/** G4 unknown speed */
	static private final int UNKNOWN_SPEED = 240;

	/** Sensor id */
	private int sensor_id;

	/** Message number, 0 - 255 */
	private int msg_num;

	/* Number of classes */
	private int num_classes;

	/** Number of lanes (zones) */
	private int num_lanes;

	/** Lane samples */
	private LaneSamples lane_samples = new LaneSamples();

	/** Message period */
	private int msg_period;

	/** Units */
	private boolean si_unit;

	/** Record creation time */
	private long create_time;

	/** Controller time */
	private long field_ctrl_time;

	/** Constructor */
	protected G4Rec() {
	}

	/** Parse the records.
	 * @throws ParsingException if data is in an unexpected format. */
	protected void parse(G4Blob b) throws ParsingException {
		create_time = TimeSteward.currentTimeMillis();
		G4Poller.info("G4Rec.parse() called");
		if(!b.minRecLength())
			throw new ParsingException("record too small");
		if(!b.validLeader(0))
			throw new ParsingException("bad record leader");
		LinkedList<G4Blob> recs = b.subdivide();		
		for(G4Blob r : recs) {
			G4Poller.info("G4Rec.parse(): r=" + r);
			int q = r.getQualifier(); // throws ParsingException
			if(q == G4Blob.QUAL_STATSHEADER)
				parseStatsHeaderRec(r);
			else if(q == G4Blob.QUAL_VOL)
				parseVolRec(r);
			else if(q == G4Blob.QUAL_OCC)
				parseOccRec(r);
			else if(q == G4Blob.QUAL_SPEED)
				parseSpeedRec(r);
			else if(q == G4Blob.QUAL_C1)
				parseCRec(r);
			else if(q == G4Blob.QUAL_C2)
				parseCRec(r);
			else if(q == G4Blob.QUAL_C3)
				parseCRec(r);
			else if(q == G4Blob.QUAL_C4)
				parseCRec(r);
			else if(q == G4Blob.QUAL_C5)
				parseCRec(r);
			else if(q == G4Blob.QUAL_STATSEND)
				parseStatsEndRec(r);
			else
				G4Poller.warn("Unknown qualifier=" + q); 
		}
	}

	/** Parse a statistics header record */
	private void parseStatsHeaderRec(G4Blob b) 
		throws ParsingException
	{
		sensor_id = b.getSensorId();
		msg_num = b.getMsgNum();
		si_unit = b.getUnits();
		num_lanes = b.getNumZones();
		num_classes = b.getNumClasses();
		lane_samples = new LaneSamples(si_unit, num_lanes);
		msg_period = b.getMsgPeriod();
		field_ctrl_time = b.getControllerTime();

		if(msg_period != 30)
			throw new ParsingException("Invalid period");

		G4Poller.info("sid=" + sensor_id);
		G4Poller.info("msg_num=" + msg_num);
		G4Poller.info("SI units=" + si_unit);
		G4Poller.info("nzones=" + num_lanes);
		G4Poller.info("numclasses=" + num_classes);
		G4Poller.info("poll period=" + msg_period);
		G4Poller.info("voltage=" + b.getVoltage());
		G4Poller.info("fctime=" + new Date(field_ctrl_time));
	}

	/** Parse a volume record */
	private void parseVolRec(G4Blob b) throws ParsingException {
		if(!b.minRecLength())
			throw new ParsingException("record too small");
		if(sensor_id != b.getSensorId())
			throw new ParsingException("Unexpected sensor id");
		if(b.getDataLength() != 2 + 2 * num_lanes) {
			G4Poller.warn("invalid # lanes in vol rec");
			throw new ParsingException("invalid #lanes vol rec");
		}
		for(int i = 0; i < num_lanes; ++i) {
			int val = b.getSampleValue(i);
			if(val < 0 || val > 3000)
				throw new ParsingException("invalid volume");
			lane_samples.setVolume(i, val);
			G4Poller.info("lane_sample["+i+"]=" + val);
		}
	}

	/** Parse an occupancy record */
	private void parseOccRec(G4Blob b) throws ParsingException {
		if(!b.minRecLength())
			throw new ParsingException("record too small");
		if(sensor_id != b.getSensorId())
			throw new ParsingException("Unexpected sensor id");
		if(b.getDataLength() != 2 + 2 * num_lanes) {
			G4Poller.warn("invalid # lanes in occ rec: dl=" + 
				b.getDataLength() + ", x=" + 
				new Integer(2 + 2 * num_lanes));
			throw new ParsingException("invalid #lanes occ rec");
		}
		for(int i = 0; i < num_lanes; ++i) {
			int val = b.getSampleValue(i);
			if(val < 0 || val > MAX_SCANS)
				throw new ParsingException("invalid occ");
			lane_samples.setScans(i, val);
			G4Poller.info("lane_sample["+i+"]=" + (val / 10f));
		}
	}

	/** Parse a speed record */
	private void parseSpeedRec(G4Blob b) throws ParsingException {
		if(!b.minRecLength())
			throw new ParsingException("record too small");
		if(sensor_id != b.getSensorId())
			throw new ParsingException("Unexpected sensor id");
		if(b.getDataLength() != 2 + 2 * num_lanes) {
			G4Poller.warn("invalid # lanes in speed rec: dl=" + 
				b.getDataLength() + ", x=" + 
				new Integer(2 + 2 * num_lanes));
			throw new ParsingException("wrong #lanes speed rec");
		}
		for(int i = 0; i < num_lanes; ++i) {
			int val = b.getSampleValue(i);
			if(val == UNKNOWN_SPEED)
				val = MISSING_DATA;
			else if(val > maxSpeed())
				throw new ParsingException("invalid speed");
			lane_samples.setSpeed(i, val);
			G4Poller.info("lane_sample["+i+"]=" + val);
		}
	}

	/** Get the maximum valid speed as a function of the units. The
	 * G4 protocol defines the maximum speed. */
	private int maxSpeed() {
		return (si_unit ? 200 : 120);
	}

	/** Parse a C record */
	private void parseCRec(G4Blob b) throws ParsingException {
		if(!b.minRecLength())
			throw new ParsingException("record too small");
		if(sensor_id != b.getSensorId())
			throw new ParsingException("Unexpected sensor id");
		if(b.getDataLength() != 2 + 2 * num_lanes) {
			G4Poller.warn("invalid # lanes in C rec");
			G4Poller.warn("invalid # lanes in C rec: dl=" + 
				b.getDataLength() + ", x=" + 
				new Integer(2 + 2*num_lanes));
			throw new ParsingException("wrong #lanes C rec");
		}
	}

	/** Parse a stats end record */
	private void parseStatsEndRec(G4Blob b) throws ParsingException {
		if(sensor_id != b.getSensorId())
			throw new ParsingException("Unexpected sensor id");
		if(msg_num != b.getMsgNum())
			throw new ParsingException("Unexpected end msg_num");
	}

	/** Update the controller object with the record's sample data. */
	public void store(ControllerImpl ci) {
		G4Poller.info("G4Rec.store(" + ci + ") called");
		if(ci == null)
			return;
		int nel = ci.getNumberIoPins();
		if(num_lanes != nel)
			G4Poller.info("G4Rec.store(): controller " + 
				ci.getName() + " received " + num_lanes + 
				" zones of data, is configured for " + nel + 
				" lanes.");
		G4Poller.info("storing rec: controller=" + ci +
			", create_time=" + new Date(create_time) +
			", numlanes=" + lane_samples.getNumLanes() +
			", vol=" + SString.toString(lane_samples.getVolumes()) + 
			", spd=" + SString.toString(lane_samples.getSpeeds()) + 
			", sca=" + SString.toString(lane_samples.getScans()));
		final int STARTPIN = 1;
		ci.storeData30Second(create_time, STARTPIN,
			lane_samples.getVolumes(), lane_samples.getScans(), 
			lane_samples.getSpeeds(), MAX_SCANS);
	}
}
