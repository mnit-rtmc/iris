/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  SRF Consulting Group
 * Copyright (C) 2018-2022  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.GeoLocImpl;
import us.mn.state.dot.tms.server.GpsImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.NoSuchName;

/**
 * Operation to query the GPS location.
 *
 * @author John L. Stanley - SRF Consulting
 * @author Douglas Lau
 */
public class OpQueryGpsLocation extends OpNtcip {

	/** Latitude to indicate no GPS lock */
	static private final int NO_GPS_LOCK_LAT = 90000001;

	/** Longitude to indicate no GPS lock */
	static private final int NO_GPS_LOCK_LON = 180000001;

	/** GPS device being queried */
	private final GpsImpl gps;

	/** Location of device */
	private final GeoLocImpl loc;

	/** GPS latitude */
	private final ASN1Integer lat = MIB1204.essLatitude.makeInt();

	/** GPS longitude */
	private final ASN1Integer lon = MIB1204.essLongitude.makeInt();

	/** Create a new query GPS location operation */
	public OpQueryGpsLocation(GpsImpl g, GeoLocImpl l) {
		super(PriorityLevel.POLL_LOW, g);
		gps = g;
		loc = l;
		gps.setLatestPollNotify();
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
			mess.add(lat);
			mess.add(lon);
			try {
				mess.queryProps();
			}
			catch (NoSuchName | ParsingException e) {
				// Some NDOR sign controllers throw
				// ParsingException instead of NoSuchName...
				setErrorStatus("GPS Not Available");
				setFailed();
				return null;
			}
			logQuery(lat);
			logQuery(lon);
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			updateGpsLocation();
		super.cleanup();
	}

	/** Update the GPS location */
	private void updateGpsLocation() {
		int lt = lat.getInteger();
		int ln = lon.getInteger();
		if ((NO_GPS_LOCK_LAT != lt) && (NO_GPS_LOCK_LON != ln)) {
			gps.saveDeviceLocation(loc, lt / 1000000.0,
				ln / 1000000.0);
		} else
			setErrorStatus("No GPS Lock");
	}
}
