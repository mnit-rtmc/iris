/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2014  Minnesota Department of Transportation
 * Copyright (C) 2014-2015  AHMCT, University of California
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
package us.mn.state.dot.tms.server.comm.pelcod;

import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Pelco D operation.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
abstract public class OpPelcoD extends OpDevice<PelcoDProperty> {

	/** Operation description */
	private final String op_desc;

	/**
	 * Create a new Pelco D operation.
	 * @param c the CameraImpl instance
	 * @param d the op description
	 */
	protected OpPelcoD(CameraImpl c, String d) {
		super(PriorityLevel.COMMAND, c);
		op_desc = d;
		device.setOpStatus("sending cmd");
	}

	/** Return operation description */
	@Override
	public String getOperationDescription() {
		return (op_desc == null ? "Unnamed operation" : op_desc);
	}

	/**
	 * Update device op status.
	 * We bundle the operation description into the status because camera
	 * ops are generally so short that, as far as I can tell, by the time
	 * the client gets the SONAR "operation" notification and requests the
	 * op's description via SONAR, the device has already been released,
	 * and thus Device.getOperation() returns "None".
	*/
	protected void updateOpStatus(String stat) {
		String s = getOperationDescription() + ": " + stat;
		device.setOpStatus(s);
	}

}
