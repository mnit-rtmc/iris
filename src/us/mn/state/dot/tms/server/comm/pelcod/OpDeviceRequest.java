/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  AHMCT, University of California
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

import java.io.IOException;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;

/**
 * Pelco D operation to handle DeviceRequest requests.
 *
 * @author Travis Swanston
 */
public class OpDeviceRequest extends OpPelcoD {

	protected final DeviceRequest devReq;

	/**
	 * Create the operation.
	 * @param c the CameraImpl instance
	 * @param dr the DeviceRequest representing the desired op
	 */
	public OpDeviceRequest(CameraImpl c, DeviceRequest dr) {
		super(c);
		devReq = dr;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<PelcoDProperty> phaseTwo() {
		return new DeviceRequestPhase();
	}

	/**
	 * Create and return the appropriate PelcoDProperty to store, based on
	 * this op's devReq value.
	 */
	protected PelcoDProperty getProperty() {
		switch (devReq) {
			case CAMERA_FOCUS_NEAR:
				return new CommandProperty(0, 0, 0, -1, 0);
			case CAMERA_FOCUS_FAR:
				return new CommandProperty(0, 0, 0, 1, 0);
			case CAMERA_FOCUS_STOP:
				return new CommandProperty(0, 0, 0, 0, 0);
			case CAMERA_IRIS_CLOSE:
				return new CommandProperty(0, 0, 0, 0, -1);
			case CAMERA_IRIS_OPEN:
				return new CommandProperty(0, 0, 0, 0, 1);
			case CAMERA_IRIS_STOP:
				return new CommandProperty(0, 0, 0, 0, 0);
			case CAMERA_FOCUS_MANUAL:
				return new AutoFocusModeProperty(false);
			case CAMERA_FOCUS_AUTO:
				return new AutoFocusModeProperty(true);
			case CAMERA_IRIS_MANUAL:
				return new AutoIrisModeProperty(false);
			case CAMERA_IRIS_AUTO:
				return new AutoIrisModeProperty(true);
			case RESET_DEVICE:
				return new ResetCameraProperty();

			// FIXME: the following have not yet been implemented
			// for this driver.
			case CAMERA_FOCUS_TOGGLE:
			case CAMERA_IRIS_TOGGLE:
			case CAMERA_WIPER_ON:
			case CAMERA_WIPER_OFF:
			case CAMERA_WIPER_TOGGLE:
			case CAMERA_WIPER_ONESHOT:
			default:
				return null;
		}

	}

	/** Main phase. */
	protected class DeviceRequestPhase extends Phase<PelcoDProperty> {
		protected Phase<PelcoDProperty> poll(
			CommMessage<PelcoDProperty> mess) throws IOException
		{
			PelcoDProperty prop = getProperty();
			if (prop != null) {
				mess.add(prop);
				mess.storeProps();
			}
			return null;
		}
	}

}
