/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.ntcip;

import java.io.IOException;

import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.GpsImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.NoSuchName;

/**
 * Operation to query the location of a DMS.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class OpQueryGpsLocationNtcip extends OpDMS {

	/** Should we ignore "jitter tolerance" when saving results? */
	protected boolean bForce;
	
	/** Use this to report/save the results */
	GpsImpl gps;

	/** Create a new DMS query configuration object */
	public OpQueryGpsLocationNtcip(DMSImpl d, GpsImpl g, boolean force) {
		super(PriorityLevel.DOWNLOAD, d);
		bForce = force;
		gps = g;
		if (gps != null) {
			gps.doSetPollDatetime(TimeSteward.currentTimeMillis());
			gps.doSetErrorStatus("");
			gps.doSetCommStatus("Polling");
		}
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new QueryGpsLocation();
	}

	/** Phase to query the location */
	protected class QueryGpsLocation extends Phase {

		/** Query the GPS location */
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer aiLat = MIB1204.essLatitude.makeInt();
			ASN1Integer aiLon = MIB1204.essLongitude.makeInt();
			mess.add(aiLat);
			mess.add(aiLon);
			try {
				mess.queryProps();
			}
			catch (NoSuchName e) {
				dms.setBlockAutoGps(true);
				if (gps != null)
					gps.doSetErrorStatus("GPS Not Available");
				return null;
			}
			catch (ParsingException e) {
				// Some NDOR sign controllers throw
				// this error instead of NoSuchName...
				dms.setBlockAutoGps(true);
				if (gps != null)
					gps.doSetErrorStatus("GPS Not Available");
				return null;
			}
			logQuery(aiLat);
			logQuery(aiLon);
			// check for special NTCIP No-GPS-Lock values
			if ((aiLat.getInteger() == 90000001)
			 || (aiLon.getInteger() == 180000001)) {
				if (gps != null)
					gps.doSetErrorStatus("No GPS Lock");
			}
			else {
				// record results
				Double dLat = aiLat.getInteger() / 1000000.0;
				Double dLon = aiLon.getInteger() / 1000000.0;
				GpsImpl.saveDeviceLocation(dms.getName(), dLat, dLon, bForce);
				if (gps != null)
					gps.doSetErrorStatus("");
			}
			return null;
		}
	}

	//----------------------------------------------

	/** Set the error status message. */
	public void setErrorStatus(String s) {
		assert s != null;
		if (gps != null)
			gps.setErrorStatus(s);
		super.setErrorStatus(s);
	}

	/** Handle a communication error */
	@Override
	public void handleCommError(EventType et, String msg) {
		setSuccess(false);
		if (gps != null)
			gps.doSetErrorStatus(msg);
		super.handleCommError(et, msg);
	}
}
