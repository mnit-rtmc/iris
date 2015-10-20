/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dr500;

/**
 * Enum of radar mode flags.
 *
 * @author Douglas Lau
 */
public enum ModeFlag {
	CONSOLE0(1),		/* enable master serial port output */
	CONSOLE1(2),		/* enable slave serial port output */
	SLOW_FILTER(10),	/* enable slow target filtering */
	AVG_SPEED(11),		/* enable average speed output */
	RAIN_FILTER(15);	/* enable rain filtering */

	/** Create a mode flag value */
	private ModeFlag(int f) {
		flag = 1 << f;
	}

	/** Bit flag */
	public final int flag;
}
