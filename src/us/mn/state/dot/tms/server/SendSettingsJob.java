/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.WarningSignHelper;

/**
 * Job to send settings to all field controllers.
 *
 * @author Douglas Lau
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
		System.err.println("Sending settings to all controllers @ " +
			TimeSteward.getDateInstance());
		sendSettings();
	}

	/** Send settings to all controllers */
	protected void sendSettings() {
		ControllerHelper.find(new Checker<Controller>() {
			public boolean check(Controller c) {
				c.setDownload(false);
				return false;
			}
		});
		final int req = DeviceRequest.SEND_SETTINGS.ordinal();
		DMSHelper.find(new Checker<DMS>() {
			public boolean check(DMS dms) {
				dms.setDeviceRequest(req);
				dms.setDeviceRequest(DeviceRequest.
					QUERY_PIXEL_FAILURES.ordinal());
				return false;
			}
		});
		LCSArrayHelper.find(new Checker<LCSArray>() {
			public boolean check(LCSArray lcs_array) {
				lcs_array.setDeviceRequest(req);
				return false;
			}
		});
		RampMeterHelper.find(new Checker<RampMeter>() {
			public boolean check(RampMeter meter) {
				meter.setDeviceRequest(req);
				return false;
			}
		});
		WarningSignHelper.find(new Checker<WarningSign>() {
			public boolean check(WarningSign sign) {
				sign.setDeviceRequest(req);
				return false;
			}
		});
	}
}
