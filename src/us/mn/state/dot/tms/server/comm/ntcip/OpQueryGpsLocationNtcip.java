/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  SRF Consulting Group
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

	/** Use this to report/save the results */
	private final GpsImpl gps;

	/** Create a new DMS query configuration object */
	public OpQueryGpsLocationNtcip(DMSImpl d, GpsImpl g) {
		super(PriorityLevel.DOWNLOAD, d);
		gps = g;
		if (gps != null) {
			gps.setPollDatetimeNotify(TimeSteward.currentTimeMillis());
			gps.setErrorStatusNotify("");
			gps.setCommStatusNotify("Polling");
		}
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new QueryGpsLocation();
	}

	/** Phase to query the location */
	private class QueryGpsLocation extends Phase {

		/** Query the GPS location */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer aiLat = MIB1204.essLatitude.makeInt();
			ASN1Integer aiLon = MIB1204.essLongitude.makeInt();
			mess.add(aiLat);
			mess.add(aiLon);
			try {
				mess.queryProps();
			}
			catch (NoSuchName e) {
				setErrorStatus("GPS Not Available");
				return null;
			}
			catch (ParsingException e) {
				// Some NDOR sign controllers throw
				// this error instead of NoSuchName...
				setErrorStatus("GPS Not Available");
				return null;
			}
			logQuery(aiLat);
			logQuery(aiLon);
			// check for special NTCIP No-GPS-Lock values
			if ((aiLat.getInteger() == 90000001)
			 || (aiLon.getInteger() == 180000001))
			{
				setErrorStatus("No GPS Lock");
			} else {
				// record results
				Double dLat = aiLat.getInteger() / 1000000.0;
				Double dLon = aiLon.getInteger() / 1000000.0;
				if (gps != null)
					gps.saveDeviceLocation(dLat, dLon, false);
				setErrorStatus("");
			}
			return null;
		}
	}

	/** Set the error status message. */
	@Override
	public void setErrorStatus(String s) {
		assert s != null;
		if (gps != null)
			gps.setErrorStatusNotify(s);
		super.setErrorStatus(s);
	}

	/** Handle a communication error */
	@Override
	public void handleCommError(EventType et, String msg) {
		setSuccess(false);
		if (gps != null)
			gps.setErrorStatusNotify(msg);
		super.handleCommError(et, msg);
	}
}
