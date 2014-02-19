/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2014  Minnesota Department of Transportation
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
 * This encapsulates a DMS brightness table and allows it to be adjusted with
 * feedbask samples from DMSImpl.BrightnessHandler.  A brightness table has a
 * fixed set of rows, each representing a brightness level.  Each level has
 * three values: light output, photocell down threshold and photocell up
 * threshold.
 *
 * @author Douglas Lau
 */
public class BrightnessTable implements DMSImpl.BrightnessHandler {

	/** Max light output */
	static private final int MAX_OUTPUT = 65535;

	/** Amount to adjust light output on feedback */
	static private final int ADJ_OUTPUT = 1024;

	/** Brightness table values */
	private final BrightnessLevel[] table;

	/** Create a new brightness table */
	public BrightnessTable(BrightnessLevel[] tbl) {
		table = tbl;
	}

	/** Adjust the table with feedback.
	 * @see DMSImpl.BrightnessHandler */
	@Override
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
	private void feedbackLow(int photo, int output) {
		int light = 0;		// highest light output so far
		for(BrightnessLevel lvl: table) {
			light = Math.max(lvl.output, light);
			if(lvl.pc_down <= photo && photo <= lvl.pc_up)
				light = Math.max(light, output + ADJ_OUTPUT);
			lvl.output = Math.min(light, MAX_OUTPUT);
		}
	}

	/** Adjust a brightness table with DMS_BRIGHT_GOOD feedback */
	private void feedbackGood(int photo, int output) {
		final int max_photo = getMaxPhotocell();
		for(int i = 0; i < table.length; i++) {
			BrightnessLevel lvl = table[i];
			if(lvl.pc_down <= photo && photo <= lvl.pc_up) {
				int prev = 0;
				int next = max_photo;
				if(i > 0)
					prev = table[i - 1].pc_up;
				if(i < table.length - 1)
					next = table[i + 1].pc_down;
				if(prev < photo && photo < next) {
					lvl.output = output;
					fixNegativeSlopes(i);
				}
			}
		}
	}

	/** Get the maximum photocell level in the table */
	private int getMaxPhotocell() {
		return table.length > 0 ? table[table.length - 1].pc_up : 0;
	}

	/** Fix negative slopes by adjusting light output around a level */
	private void fixNegativeSlopes(int n_lvl) {
		int output = table[n_lvl].output;
		for(int i = 0; i < table.length; i++) {
			BrightnessLevel lvl = table[i];
			if(i < n_lvl && lvl.output > output)
				lvl.output = output;
			if(i > n_lvl && lvl.output < output)
				lvl.output = output;
		}
	}

	/** Adjust a brightness table with DMS_BRIGHT_HIGH feedback */
	private void feedbackHigh(int photo, int output) {
		int light = MAX_OUTPUT;	// lowest light output so far
		for(int i = table.length - 1; i >= 0; i--) {
			BrightnessLevel lvl = table[i];
			light = Math.min(lvl.output, light);
			if(lvl.pc_down <= photo && photo <= lvl.pc_up)
				light = Math.min(light, output - ADJ_OUTPUT);
			lvl.output = Math.max(light, 0);
		}
	}
}
