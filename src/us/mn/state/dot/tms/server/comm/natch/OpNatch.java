/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.natch;

import us.mn.state.dot.tms.server.comm.OpStep;

/**
 * Step for Natch operations
 *
 * @author Douglas Lau
 */
abstract public class OpNatch extends OpStep {

	/** Is step done? */
	protected boolean done;

	/** Set the step to be done */
	public void setDone(boolean d) {
		done = d;
	}

	/** Create a new Natch step */
	protected OpNatch() {
		done = false;
	}

	/** Get the next step */
	@Override
	public OpStep next() {
		return done ? null : this;
	}
}
