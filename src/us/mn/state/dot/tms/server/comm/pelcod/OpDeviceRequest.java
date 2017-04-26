/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  AHMCT, University of California
 * Copyright (C) 2014-2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.pelcod.ExtendedProperty.Command;

/**
 * Pelco D operation to handle DeviceRequest requests.
 *
 * @author Travis Swanston
 * @author Douglas Lau
 */
public class OpDeviceRequest extends OpPelcoD {

	/** Get property associated with a device request.
	 * @param dr Device request.
	 * @return Associated property. */
	static private PelcoDProperty createProp(DeviceRequest dr, int n_sent) {
		switch (dr) {
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
			return new ExtendedProperty(Command.AUTO_FOCUS, 1);
		case CAMERA_FOCUS_AUTO:
			return new ExtendedProperty(Command.AUTO_FOCUS, 0);
		case CAMERA_IRIS_MANUAL:
			return new ExtendedProperty(Command.AUTO_IRIS, 1);
		case CAMERA_IRIS_AUTO:
			return new ExtendedProperty(Command.AUTO_IRIS, 0);
		case RESET_DEVICE:
			return new ExtendedProperty(Command.REMOTE_RESET);
		case CAMERA_WIPER_ONESHOT:
			return (n_sent < 1)
			      ? new ExtendedProperty(Command.SET_AUX, 1)
			      :	new ExtendedProperty(Command.CLEAR_AUX, 1);
		default:
			return null;
		}
	}

	/** Device request */
	private final DeviceRequest req;

	/**
	 * Create the operation.
	 * @param c the CameraImpl instance.
	 * @param dr the DeviceRequest representing the desired op.
	 */
	public OpDeviceRequest(CameraImpl c, DeviceRequest dr) {
		super(c);
		req = dr;
	}

	/** Number of times this request was sent */
	private int n_sent = 0;

	/** Create the second phase of the operation */
	@Override
	protected Phase<PelcoDProperty> phaseTwo() {
		return new DeviceRequestPhase();
	}

	/** Main phase. */
	protected class DeviceRequestPhase extends Phase<PelcoDProperty> {
		protected Phase<PelcoDProperty> poll(
			CommMessage<PelcoDProperty> mess) throws IOException
		{
			PelcoDProperty prop = createProp(req, n_sent);
			if (prop != null) {
				mess.add(prop);
				mess.storeProps();
			}
			n_sent++;
			return shouldResend() ? this : null;
		}
	}

	/** Should we resend the property? */
	private boolean shouldResend() {
		return (req == DeviceRequest.CAMERA_WIPER_ONESHOT)
		    && (n_sent < 2);
	}
}
