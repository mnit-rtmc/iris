/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.manchester;

import java.io.IOException;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;

/**
 * Manchester operation to handle DeviceRequest requests.
 *
 * @author Douglas Lau
 */
public class OpDeviceRequest extends OpManchester {

	/** Get property associated with a device request.
	 * @param dr Device request.
	 * @return Associated property. */
	static private ManchesterProperty getProperty(DeviceRequest dr) {
		switch (dr) {
		case CAMERA_FOCUS_NEAR:
			return new FocusProperty(-1);
		case CAMERA_FOCUS_FAR:
			return new FocusProperty(1);
		case CAMERA_IRIS_CLOSE:
			return new IrisProperty(-1);
		case CAMERA_IRIS_OPEN:
			return new IrisProperty(1);
		case CAMERA_WIPER_ONESHOT:
			return new AuxProperty(6);
		case RESET_DEVICE:
			return new AuxProperty(1);
		default:
			return null;
		}
	}

	/** Property for request */
	private final ManchesterProperty prop;

	/** Flag for reset request */
	private final boolean is_reset;

	/** Create a new device request operation.
	 * @param c CameraImpl instance.
	 * @param dr Device request. */
	public OpDeviceRequest(CameraImpl c, DeviceRequest dr) {
		super(c);
		prop = getProperty(dr);
		is_reset = dr == DeviceRequest.RESET_DEVICE;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<ManchesterProperty> phaseTwo() {
		return (prop != null) ? new DeviceRequestPhase() : null;
	}

	/** Phase to make device request */
	protected class DeviceRequestPhase extends Phase<ManchesterProperty> {
		protected Phase<ManchesterProperty> poll(
			CommMessage<ManchesterProperty> mess) throws IOException
		{
			mess.add(prop);
			mess.storeProps();
			return (is_reset) ? new PowerOnPhase() : null;
		}
	}

	/** Phase to power device back on */
	protected class PowerOnPhase extends Phase<ManchesterProperty> {

		/** Power on property */
		private final AuxProperty p = new AuxProperty(4);

		/** Number of times this request was sent */
		private int n_sent = 0;

		/** Set power-on property */
		protected Phase<ManchesterProperty> poll(
			CommMessage<ManchesterProperty> mess) throws IOException
		{
			if (n_sent > 0)
				sleepUntilReady();
			else
				TimeSteward.sleep_well(2000);
			mess.add(p);
			mess.storeProps();
			n_sent++;
			return (n_sent < 4) ? this : null;
		}
	}
}
