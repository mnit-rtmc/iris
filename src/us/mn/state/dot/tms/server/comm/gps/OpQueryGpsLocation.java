/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2017  SRF Consulting Group
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

package us.mn.state.dot.tms.server.comm.gps;

import java.io.IOException;
import java.lang.Comparable;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.GpsImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Base class for all GPS-modem Query-Location operations
 * 
 * @author John L. Stanley
 */

//FIXME:  Finish adding olympic-filtering
//FIXME:  Move filtering to a separate memory-cached
//          GpsFilter class (for sample-array persistence).

abstract public class OpQueryGpsLocation extends OpGps {

	/** Log an error msg */
	protected void logError(String msg) {
		if (GpsImpl.GPS_LOG.isOpen())
			GpsImpl.GPS_LOG.log(controller.getName() + "! " + msg);
	}

	/** If set, ignore "jitter tolerance" when saving current result */
	protected boolean bForce;
	
	//----------------------------------------------
	// GPS-sample filtering

	protected class GpsSample implements Comparable<GpsSample> {
		protected double lat;
		protected double lon;
		protected double errorSum;
		
		protected GpsSample(double xlat, double xlon) {
			lat = xlat;
			lon = xlon;
			errorSum = 0;
		}

		@Override
		public int compareTo(GpsSample arg0) {
			double cmp = (this.errorSum - arg0.errorSum);
			return (cmp < 0.0) ? -1 : ((cmp == 0.0) ? 0 : 1);
		}
	}

	protected GpsSample[] samples;
	protected int nextSample = 0;
	protected int useSamples;
	protected int haveSamples = 0;

	protected void initSampleArray(int xmaxSamples, int xuseSamples) {
		samples = new GpsSample[xmaxSamples];
		useSamples = xuseSamples;
	}
	
	/** Add (lat,lon) sample to array of samples.
	 *
	 * @param xlat - latitude
	 * @param xlon - longitude
	 * @return True if sample array isn't full yet.  False if it is.
	 */
	protected boolean addSample(double xlat, double xlon) {
		GpsSample s = new GpsSample(xlat, xlon);
		samples[nextSample] = s;
		// wrap nextSample
		if (++nextSample >= samples.length)
			nextSample = 0;
		// limit haveSamples to size of sample array
		if (haveSamples >= samples.length)
			return false;
		return (++haveSamples != samples.length);
	}

	// filter samples and save the averaged result
	protected void filterSamplesAndSave() {
		int len;
		GpsSample s1 = null;

		len = samples.length;

		// special case - skip olympic filtering
		if ((len == 1) && (haveSamples == 1)) {
			s1 = samples[0];
			GpsImpl.saveDeviceLocation(gps.getDeviceName(),
				s1.lat, s1.lon, bForce);
			return;
		}

		//FIXME:  Add olympic-filtering code here
	}

	//----------------------------------------------

	/** Create a new GPS operation */
	public OpQueryGpsLocation(GpsImpl g, boolean force,
			GpsProperty gprop) {
		super(PriorityLevel.DEVICE_DATA, g, gprop);
		bForce = force;
		if (gps != null) {
			gps.doSetPollDatetime(TimeSteward.currentTimeMillis());
			gps.doSetErrorStatus("");
			gps.doSetCommStatus("Polling");
		}
		initSampleArray(1, 1);
	}

	//----------------------------------------------

	/** Create the second phase of the operation */
	protected Phase<GpsProperty> phaseTwo() {
		return (prop == null)
				? null
				: (new DoPhaseTwo());
	}

	/** Phase to send GPS location-query command */
	protected class DoPhaseTwo extends Phase<GpsProperty> {

		/** Add the GpsProperty cmd to the outbound message */
		protected Phase<GpsProperty> poll(CommMessage<GpsProperty> mess)
			throws IOException
		{
			mess.add(prop);
			mess.queryProps();
			if (!prop.gotValidResponse() || !isSuccess())
				return null;  // error exit

			if (!prop.gotGpsLock()) {
				if (gps != null)
					gps.doSetErrorStatus("No GPS Lock");
				return null;
			}
			
			if (addSample(prop.getLat(), prop.getLon())) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return this;  // ask for another sample
			}

			filterSamplesAndSave();
			return null;
		}
	}

	//----------------------------------------------

	/** Set the error status message. */
	public void setErrorStatus(String s) {
		assert s != null;
		if (gps != null) {
			gps.doSetCommStatus("");
			gps.doSetErrorStatus(s);
		}
		super.setErrorStatus(s);
	}

	/** Handle a communication error */
	@Override
	public void handleCommError(EventType et, String msg) {
		setSuccess(false);
		setErrorStatus(msg);
		super.handleCommError(et, msg);
	}
}
