/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2024  SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.sierrassh;

import java.io.IOException;
import us.mn.state.dot.tms.server.GeoLocImpl;
import us.mn.state.dot.tms.server.GpsImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.SshMessenger;

/**
 * This operation logs into a Sierra Wireless GX GPS modem
 * and then queries the GPS coordinates of the modem.
 *
 * @author John L. Stanley
 * @author Douglas Lau
 */
public class OpQueryGpsLocation extends OpDevice<GpsLocationProperty> {

	/** GPS device */
	private final GpsImpl gps;

	/** GPS location property */
	private final GpsLocationProperty gps_prop;

	/** Create a new query GPS operation */
	public OpQueryGpsLocation(GpsImpl g) {
		super(PriorityLevel.POLL_LOW, g);
		gps = g;
		gps_prop = new GpsLocationProperty();
		gps.setLatestPollNotify();
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<GpsLocationProperty> phaseTwo() {
		return new QueryGps();
	}

	/** Phase to query GPS location */
	private class QueryGps extends Phase<GpsLocationProperty> {

		protected Phase<GpsLocationProperty> poll(
			CommMessage<GpsLocationProperty> mess) throws IOException
		{
			mess.add(gps_prop);
			mess.queryProps();
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
		if (gps_prop.gotValidResponse()) {
			if (gps_prop.gotGpsLock()) {
				gps.saveDeviceLocation(gps_prop.getLat(),
					gps_prop.getLon());
			} else
				setErrorStatus("No GPS Lock");
		}
	}
}
