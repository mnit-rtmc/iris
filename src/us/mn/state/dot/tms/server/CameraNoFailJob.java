/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;

/**
 * Job to clear camera failures.
 *
 * @author Douglas Lau
 */
public class CameraNoFailJob extends Job {

	/** Seconds to offset each poll from start of interval */
	static protected final int OFFSET_SECS = 10;

	/** Create a new camera no-fail job */
	public CameraNoFailJob() {
		super(Calendar.MINUTE, 3, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform the camera no-fail job */
	public void perform() {
		CameraHelper.find(new Checker<Camera>() {
			public boolean check(Camera c) {
				if(c instanceof CameraImpl) {
					CameraImpl cam = (CameraImpl)c;
					cam.clearFailed();
				}
				return false;
			}
		});
	}
}
