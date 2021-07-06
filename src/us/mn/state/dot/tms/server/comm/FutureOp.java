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
package us.mn.state.dot.tms.server.comm;

import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.server.DeviceImpl;
import us.mn.state.dot.tms.server.comm.DevicePoller;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.ThreadedPoller;

/**
 * Schedule a DeviceOp to be run sometime in the future.
 *
 * @author John Stanley - SRF Consulting
 */
@SuppressWarnings("rawtypes")
public class FutureOp extends Job 
{
	static private final Scheduler FUTUREOPS = new Scheduler("futureOps");

	final private DeviceImpl dev;
	final private OpDevice op;
	
	private FutureOp(DeviceImpl adev, int delaySec, OpDevice aop) {
		super(delaySec * 1000);
		dev = adev;
		op = aop;
	}

	@SuppressWarnings("unchecked")
	public void perform() {
		DevicePoller dp = dev.getPoller();
		if (dp instanceof ThreadedPoller) {
			ThreadedPoller tp = (ThreadedPoller)dp;
			tp.addOp(op);
		}
	}

	/** Schedule an OpDevice to be run sometime in the future. */
	public static void queueOp(DeviceImpl adev, int delaySec, OpDevice op) {
		FutureOp foo = new FutureOp(adev, delaySec, op);
		//FIXME: Add code to prevent adding duplicate op for same device 
		FUTUREOPS.addJob(foo);
	}
}
