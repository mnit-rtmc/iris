/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2015  AHMCT, University of California
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cohuptz;

import java.io.IOException;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Cohu PTZ operation to pan/tilt/zoom a camera.
 *
 * @author Travis Swanston
 * @author Douglas Lau
 */
public class OpPTZCamera extends OpCohuPTZ {

	/** Pan vector */
	private final float pan;

	/** Tilt vector */
	private final float tilt;

	/** Zoom vector */
	private final float zoom;

	/**
	 * Create the operation.
	 * @param c the CameraImpl instance
	 * @param p the pan vector [-1..1]
	 * @param t the tilt vector [-1..1]
	 * @param z the zoom vector [-1..1]
	 */
	public OpPTZCamera(CameraImpl c, float p, float t, float z) {
		super(PriorityLevel.COMMAND, c);
		pan  = p;
		tilt = t;
		zoom = z;
	}

	/** Begin the operation */
	@Override
	protected Phase<CohuPTZProperty> phaseTwo() {
		return new PanPhase();
	}

	/** Pan phase, 1/3 */
	protected class PanPhase extends Phase<CohuPTZProperty> {
		protected Phase<CohuPTZProperty> poll(
			CommMessage<CohuPTZProperty> mess) throws IOException
		{
			mess.add(new PanProperty(pan));
			mess.storeProps();
			return new TiltPhase();
		}
	}

	/** Tilt phase, 2/3 */
	protected class TiltPhase extends Phase<CohuPTZProperty> {
		protected Phase<CohuPTZProperty> poll(
			CommMessage<CohuPTZProperty> mess) throws IOException
		{
			mess.add(new TiltProperty(tilt));
			mess.storeProps();
			return new ZoomPhase();
		}
	}

	/** Zoom phase, 3/3 */
	protected class ZoomPhase extends Phase<CohuPTZProperty> {
		protected Phase<CohuPTZProperty> poll(
			CommMessage<CohuPTZProperty> mess) throws IOException
		{
			mess.add(new ZoomProperty(zoom));
			mess.storeProps();
			return null;
		}
	}
}
