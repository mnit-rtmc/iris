/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.pelco;

import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.OpController;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * OpPelco is the base class for the Pelco comm protocol.
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 */
abstract public class OpPelco extends OpController<PelcoProperty> {

	/** Video monitor to select camera on */
	protected final VideoMonitor monitor;

	/** Camera to perform operation on */
	protected final String camera;

	/** Create a new Pelco operation */
	public OpPelco(ControllerImpl c, VideoMonitor m, CameraImpl cam) {
		super(PriorityLevel.COMMAND, c);
		monitor = m;
		camera = (cam != null)
		       ? cam.getName()
		       : SystemAttrEnum.CAMERA_ID_BLANK.getString();
	}
}
