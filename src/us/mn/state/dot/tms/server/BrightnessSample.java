/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import us.mn.state.dot.tms.DeviceRequest;

/**
 * A brightness feedback sample point
 *
 * @author Douglas Lau
 */
public class BrightnessSample {

	/** Feedback request */
	public final DeviceRequest feedback;

	/** Photocell value (0-65535) */
	public final int photocell;

	/** Light output value (0-65535) */
	public final int output;

	/** Create a new brightness feedback sample point */
	public BrightnessSample(DeviceRequest f, int p, int o) {
		feedback = f;
		photocell = p;
		output = o;
	}

	/** Callback interface for brightness feedback samples */
	static public interface Handler {
		public void handle(BrightnessSample s);
	}
}
