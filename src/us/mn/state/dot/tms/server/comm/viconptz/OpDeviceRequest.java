/*
 * IRIS -- Intelligent Roadway Information System
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
package us.mn.state.dot.tms.server.comm.viconptz;

import java.io.IOException;
import java.nio.ByteBuffer;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.comm.Operation;
import us.mn.state.dot.tms.server.comm.OpStep;

/**
 * Vicon operation to handle DeviceRequest requests.
 *
 * @author Douglas Lau
 */
public class OpDeviceRequest extends OpStep {

	/** Check if a device request is implemented */
	static public boolean isImplemented(DeviceRequest dr) {
		return createProp(dr, 0) != null;
	}

	/** Create property associated with a device request.
	 * @param dr Device request.
	 * @return Associated property. */
	static private ViconPTZProp createProp(DeviceRequest dr, int n_sent) {
		switch (dr) {
		case CAMERA_FOCUS_NEAR:
			return new CommandProp(0, 0, 0, -1, 0);
		case CAMERA_FOCUS_FAR:
			return new CommandProp(0, 0, 0, 1, 0);
		case CAMERA_FOCUS_STOP:
			return new CommandProp(0, 0, 0, 0, 0);
		case CAMERA_IRIS_CLOSE:
			return new CommandProp(0, 0, 0, 0, -1);
		case CAMERA_IRIS_OPEN:
			return new CommandProp(0, 0, 0, 0, 1);
		case CAMERA_IRIS_STOP:
			return new CommandProp(0, 0, 0, 0, 0);
		case RESET_DEVICE:
			return new ExPresetProp(true, ExPresetProp.SOFT_RESET);
		case CAMERA_WIPER_ONESHOT:
			if (n_sent < 3) {
				// For Vicon cameras, this should be AUX 6, but
				// Pelco cameras require AUX 1 here.
				return new AuxProp(1);
			} else
				return new AuxProp(0);
		case CAMERA_MENU_ENTER:
		case CAMERA_MENU_CANCEL:
			return new MenuProp(dr);
		default:
			return null;
		}
	}

	/** Device request */
	private final DeviceRequest req;

	/** Create a new device request operation.
	 * @param dr Device request. */
	public OpDeviceRequest(DeviceRequest dr) {
		req = dr;
	}

	/** Number of times this request was sent */
	private int n_sent = 0;

	/** Poll the controller */
	@Override
	public void poll(Operation op, ByteBuffer tx_buf) throws IOException {
		ViconPTZProp prop = createProp(req, n_sent);
		if (prop != null)
			prop.encodeStore(op, tx_buf);
		n_sent++;
	}

	/** Get the next step */
	@Override
	public OpStep next() {
		return shouldResend() ? this : null;
	}

	/** Should we resend the property? */
	private boolean shouldResend() {
		return (req == DeviceRequest.CAMERA_WIPER_ONESHOT)
		    && (n_sent < 4);
	}
}
