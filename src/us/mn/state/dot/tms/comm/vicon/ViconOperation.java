/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm.vicon;

import us.mn.state.dot.tms.CameraImpl;
import us.mn.state.dot.tms.MonitorImpl;
import us.mn.state.dot.tms.comm.DeviceOperation;

/**
 * ViconOperation is the base class for the Vicon comm protocol.
 *
 * @author <a href="mailto:timothy.a.johnson@dot.state.mn.us">Tim Johnson</a>
 * @author Douglas Lau
 */
abstract public class ViconOperation extends DeviceOperation {

	/** Monitor to select camera on */
	protected final MonitorImpl monitor;

	/** Camera to perform operation on */
	protected final CameraImpl camera;

	/** Create a new Vicon operation */
	public ViconOperation(int p, MonitorImpl m, CameraImpl c) {
		super(p, c);
		monitor = m;
		camera = c;
	}
}
