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
package us.mn.state.dot.tms.server.comm.ntcip;

/**
 * A brightness level has three values: light output, photocell down threshold
 * and photocell up threshold.
 *
 * @author Douglas Lau
 */
public class BrightnessLevel {

	/** Light output */
	public int output;

	/** Photocell down threshold */
	public int pc_down;

	/** Photocell up threshold */
	public int pc_up;
}
