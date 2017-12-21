/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2017  Minnesota Department of Transportation
 * Copyright (C) 2017  Iteris Inc.
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
package us.mn.state.dot.tms.server;

import java.util.Calendar;
import java.util.Iterator;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.BeaconHelper;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;
import us.mn.state.dot.tms.TagReader;
import us.mn.state.dot.tms.TagReaderHelper;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.VideoMonitorHelper;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.WeatherSensorHelper;

/**
 * Job to send settings to all field controllers.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SendSettingsJob extends Job {

	/** Create a new send settings job */
	public SendSettingsJob() {
		super(Calendar.DATE, 1, Calendar.HOUR, 4);
	}

	/** Create a new one-shot send settings job */
	public SendSettingsJob(int ms) {
		super(ms);
	}

	/** Perform the send settings job */
	public void perform() {
		sendSettings();
	}

	/** Send settings to all controllers */
	private void sendSettings() {
		Iterator<Controller> it = ControllerHelper.iterator();
		while (it.hasNext()) {
			Controller c = it.next();
			c.setDownload(false);
		}
		requestDMS(DeviceRequest.SEND_SETTINGS);
		requestDMS(DeviceRequest.QUERY_PIXEL_FAILURES);
		requestLCS(DeviceRequest.SEND_SETTINGS);
		requestRampMeters(DeviceRequest.SEND_SETTINGS);
		requestBeacons(DeviceRequest.SEND_SETTINGS);
		requestTagReaders(DeviceRequest.SEND_SETTINGS);
		requestWeatherSensors(DeviceRequest.SEND_SETTINGS);
	}

	/** Send a request to all DMS */
	private void requestDMS(DeviceRequest req) {
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS dms = it.next();
			dms.setDeviceRequest(req.ordinal());
		}
	}

	/** Send a request to all LCS */
	private void requestLCS(DeviceRequest req) {
		Iterator<LCSArray> it = LCSArrayHelper.iterator();
		while (it.hasNext()) {
			LCSArray lcs_array = it.next();
			lcs_array.setDeviceRequest(req.ordinal());
		}
	}

	/** Send a request to all ramp meters */
	private void requestRampMeters(DeviceRequest req) {
		Iterator<RampMeter> it = RampMeterHelper.iterator();
		while (it.hasNext()) {
			RampMeter meter = it.next();
			meter.setDeviceRequest(req.ordinal());
		}
	}

	/** Send a request to all beacons */
	private void requestBeacons(DeviceRequest req) {
		Iterator<Beacon> it = BeaconHelper.iterator();
		while (it.hasNext()) {
			Beacon b = it.next();
			b.setDeviceRequest(req.ordinal());
		}
	}

	/** Send a request to all tag readers */
	private void requestTagReaders(DeviceRequest req) {
		Iterator<TagReader> it = TagReaderHelper.iterator();
		while (it.hasNext()) {
			TagReader tr = it.next();
			tr.setDeviceRequest(req.ordinal());
		}
	}

	/** Send a request to all weather sensors */
	private void requestWeatherSensors(DeviceRequest req) {
		Iterator<WeatherSensor> it = WeatherSensorHelper.iterator();
		while (it.hasNext()) {
			WeatherSensor ws = it.next();
			ws.setDeviceRequest(req.ordinal());
		}
	}
}
