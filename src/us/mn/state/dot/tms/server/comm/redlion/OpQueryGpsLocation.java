/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2017  SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.redlion;

import java.io.IOException;
import us.mn.state.dot.tms.server.GeoLocImpl;
import us.mn.state.dot.tms.server.GpsImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Base class for all GPS-modem Query-Location operations
 *
 * @author John L. Stanley
 * @author Douglas Lau
 */
public class OpQueryGpsLocation extends OpDevice<RedLionProperty> {

	/** Log an error msg */
	protected void logError(String msg) {
		if (GpsImpl.GPS_LOG.isOpen())
			GpsImpl.GPS_LOG.log(controller.getName() + "! " + msg);
	}

	/** GPS device */
	private final GpsImpl gps;

	/** Device location */
	private final GeoLocImpl loc;

	/** Property to query */
	private final RedLionProperty prop;

	/** Create a new GPS operation */
	public OpQueryGpsLocation(GpsImpl g, GeoLocImpl l) {
		super(PriorityLevel.DEVICE_DATA, g);
		gps = g;
		loc = l;
		prop = new RedLionProperty();
		gps.setLatestPollNotify();
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<RedLionProperty> phaseTwo() {
		return new QueryGPS();
	}

	/** Phase to send GPS location-query command */
	private class QueryGPS extends Phase<RedLionProperty> {

		/** Add the RedLionProperty cmd to the outbound message */
		protected Phase<RedLionProperty> poll(
			CommMessage<RedLionProperty> mess) throws IOException
		{
			mess.add(prop);
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
		if (prop.gotValidResponse()) {
			if (prop.gotGpsLock()) {
				gps.saveDeviceLocation(loc, prop.getLat(),
					prop.getLon());
			} else
				setErrorStatus("No GPS Lock");
		}
	}
}
