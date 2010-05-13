/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.DMSImpl;

/**
 * Brightness table encapsulates a DMS brightness table and allows it to be
 * adjusted with feedbask samples from DMSImpl.BrightnessHandler.
 *
 * @author Douglas Lau
 */
public class BrightnessTable implements DMSImpl.BrightnessHandler {

	/** Max light output */
	static protected final int MAX_OUTPUT = 65535;

	/** Amount to adjust light output on feedback */
	static protected final int ADJ_OUTPUT = 256;

	/** Brightness table values */
	protected final int[][] table;

	/** Create a new brightness table */
	public BrightnessTable(int[][] tbl) {
		table = tbl;
	}

	/** Adjust the table with feedback.
	 * @see DMSImpl.BrightnessHandler */
	public void feedback(EventType et, int photo, int output) {
		switch(et) {
		case DMS_BRIGHT_LOW:
			feedbackLow(photo, output);
			break;
		case DMS_BRIGHT_GOOD:
			feedbackGood(photo, output);
			break;
		case DMS_BRIGHT_HIGH:
			feedbackHigh(photo, output);
			break;
		default:
			break;
		}
	}

	/** Adjust a brightness table with DMS_BRIGHT_LOW feedback */
	protected void feedbackLow(int photo, int output) {
		int light = 0;		// highest light output so far
		for(int[] lvl: table) {
			light = Math.max(lvl[0], light);
			if(lvl[1] <= photo && photo <= lvl[2])
				light = Math.max(light, output) + ADJ_OUTPUT;
			lvl[0] = Math.min(light, MAX_OUTPUT);
		}
	}

	/** Adjust a brightness table with DMS_BRIGHT_GOOD feedback */
	protected void feedbackGood(int photo, int output) {
		final int max_photo = table[table.length - 1][2];
		for(int i = 0; i < table.length; i++) {
			int prev = 0;
			int next = max_photo;
			if(i > 0)
				prev = table[i - 1][2];
			if(i < table.length - 1)
				next = table[i + 1][1];
			if(prev < photo && photo < next) {
				int[] lvl = table[i];
				if(lvl[1] <= photo && photo <= lvl[2]) {
					lvl[0] = output;
					// Fix any negative slope problems
					for(int j = 0; j < table.length; j++) {
						int[] ol = table[j];
						if(j < i && ol[0] > output)
							ol[0] = output;
						if(j > i && ol[0] < output)
							ol[0] = output;
					}
				}
			}
		}
	}

	/** Adjust a brightness table with DMS_BRIGHT_HIGH feedback */
	protected void feedbackHigh(int photo,int output) {
		int light = MAX_OUTPUT;	// lowest light output so far
		for(int i = table.length - 1; i >= 0; i--) {
			int[] lvl = table[i];
			light = Math.min(lvl[0], light);
			if(lvl[1] <= photo && photo <= lvl[2])
				light = Math.min(light, output) - ADJ_OUTPUT;
			lvl[0] = Math.max(light, 0);
		}
	}
}
