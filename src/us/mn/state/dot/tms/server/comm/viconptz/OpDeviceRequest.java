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
package us.mn.state.dot.tms.server.comm.viconptz;

import java.io.IOException;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;

/**
 * Vicon operation to handle DeviceRequest requests.
 *
 * @author Douglas Lau
 */
public class OpDeviceRequest extends OpViconPTZ {

	/** Get property associated with a device request.
	 * @param dr Device request.
	 * @return Associated property. */
	static private ViconPTZProperty getProperty(DeviceRequest dr) {
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
		case RESET_DEVICE:
			return new ExPresetProperty(true,
				ExPresetProperty.SOFT_RESET);
		case CAMERA_WIPER_ONESHOT:
			// For actual Vicon cameras, this should be AUX 6, but
			// Pelco cameras require AUX 1 here.
			return new AuxProperty(1);
		default:
			return null;
		}
	}

	/** Property for request */
	private final ViconPTZProperty prop;

	/** Create a new device request operation.
	 * @param c CameraImpl instance.
	 * @param dr Device request. */
	public OpDeviceRequest(CameraImpl c, DeviceRequest dr) {
		super(c);
		prop = getProperty(dr);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<ViconPTZProperty> phaseTwo() {
		if (prop instanceof AuxProperty)
			return new AuxSetPhase();
		else if (prop != null)
			return new DeviceRequestPhase();
		else
			return null;
	}

	/** Phase to make device request */
	protected class DeviceRequestPhase extends Phase<ViconPTZProperty> {

		/** Make device request */
		protected Phase<ViconPTZProperty> poll(
			CommMessage<ViconPTZProperty> mess) throws IOException
		{
			mess.add(prop);
			mess.storeProps();
			return null;
		}
	}

	/** Phase to set AUX device request */
	protected class AuxSetPhase extends Phase<ViconPTZProperty> {

		/** Number of times this request was sent */
		private int n_sent = 0;

		/** Set AUX property */
		protected Phase<ViconPTZProperty> poll(
			CommMessage<ViconPTZProperty> mess) throws IOException
		{
			mess.add(prop);
			mess.storeProps();
			n_sent++;
			return (n_sent < 2) ? this : new AuxClearPhase();
		}
	}

	/** Phase to clear AUX device request */
	protected class AuxClearPhase extends Phase<ViconPTZProperty> {

		/** Clear AUX property */
		protected Phase<ViconPTZProperty> poll(
			CommMessage<ViconPTZProperty> mess) throws IOException
		{
			AuxProperty p = new AuxProperty(0);
			mess.add(p);
			mess.storeProps();
			return null;
		}
	}
}
