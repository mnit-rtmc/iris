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

package us.mn.state.dot.tms.server.comm.cohuptz;

import java.io.IOException;
import java.lang.Float;
import us.mn.state.dot.tms.server.CameraImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Cohu PTZ operation to pan/tilt/zoom a camera.
 *
 * @author Travis Swanston
 */
public class OpPTZCamera extends OpCohuPTZ {

	/** pan vector */
	protected final Float pan;

	/** tilt vector */
	protected final Float tilt;

	/** zoom vector */
	protected final Float zoom;

	/**
	 * Create the operation.
	 * @param c the CameraImpl instance
	 * @param cp the CohuPTZPoller instance
	 * @param p the pan vector [-1..1] or null for NOP
	 * @param t the tilt vector [-1..1] or null for NOP
	 * @param z the zoom vector [-1..1] or null for NOP
	 */
	public OpPTZCamera(CameraImpl c, CohuPTZPoller cp, Float p, Float t, Float z) {
		super(PriorityLevel.COMMAND, c, cp);
		pan  = p;
		tilt = t;
		zoom = z;
	}

	/** Begin the operation. */
	@Override
	public Phase phaseTwo() {
		return new PanPhase();
	}

	/** pan phase, 1/3 */
	protected class PanPhase extends Phase {
		protected Phase poll(CommMessage mess) throws IOException {
			if (pan != null) {
				mess.add(new PanProperty(pan.floatValue()));
				doStoreProps(mess);
			}
			return new TiltPhase();
		}
	}

	/** tilt phase, 2/3 */
	protected class TiltPhase extends Phase {
		protected Phase poll(CommMessage mess) throws IOException {
			if (tilt != null) {
				mess.add(new TiltProperty(tilt.floatValue()));
				doStoreProps(mess);
			}
			return new ZoomPhase();
		}
	}

	/** zoom phase, 3/3 */
	protected class ZoomPhase extends Phase {
		protected Phase poll(CommMessage mess) throws IOException {
			if (zoom != null) {
				mess.add(new ZoomProperty(zoom.floatValue()));
				doStoreProps(mess);
			}
			return null;
		}
	}

}
