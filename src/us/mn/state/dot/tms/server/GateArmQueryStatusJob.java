/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmHelper;

/**
 * Job to periodically query all gate arm status.
 *
 * @author Douglas Lau
 */
public class GateArmQueryStatusJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static private final int OFFSET_SECS = 5;

	/** Create a new job to query gate arm status */
	public GateArmQueryStatusJob() {
		super(Calendar.SECOND, 30, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform the gate arm query status job */
	public void perform() {
		int req = DeviceRequest.QUERY_STATUS.ordinal();
		Iterator<GateArm> it = GateArmHelper.iterator();
		while(it.hasNext()) {
			GateArm ga = it.next();
			if(ga instanceof GateArmImpl) {
				GateArmImpl gate_arm = (GateArmImpl)ga;
				gate_arm.setDeviceRequest(req);
			}
		}
	}
}
