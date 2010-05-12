/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2010  Minnesota Department of Transportation
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

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;

/**
 * Operation to incorporate brightness feedback for a DMS.
 *
 * @author Douglas Lau
 */
public class OpUpdateDMSBrightness extends OpDMS {

	/** Event type (DMS_BRIGHT_GOOD, DMS_BRIGHT_LOW or DMS_BRIGHT_HIGH) */
	protected final EventType event_type;

	/** Maximum photocell level */
	protected final DmsIllumMaxPhotocellLevel max_level =
		new DmsIllumMaxPhotocellLevel();

	/** Photocell level status */
	protected final DmsIllumPhotocellLevelStatus p_level =
		new DmsIllumPhotocellLevelStatus();

	/** Light output status */
	protected final DmsIllumLightOutputStatus light =
		new DmsIllumLightOutputStatus();

	/** Total number of supported brightness levels */
	protected final DmsIllumNumBrightLevels b_levels =
		new DmsIllumNumBrightLevels();

	/** Brightness table */
	protected final DmsIllumBrightnessValues brightness =
		new DmsIllumBrightnessValues();

	/** Create a new DMS brightness feedback operation */
	public OpUpdateDMSBrightness(DMSImpl d, EventType et) {
		super(PriorityLevel.COMMAND, d);
		event_type = et;
	}

	/** Create the first real phase of the operation */
	protected Phase phaseOne() {
		return new QueryBrightness();
	}

	/** Phase to query the brightness status */
	protected class QueryBrightness extends Phase {

		/** Query the DMS brightness status */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(max_level);
			mess.add(p_level);
			mess.add(light);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + max_level);
			DMS_LOG.log(dms.getName() + ": " + p_level);
			DMS_LOG.log(dms.getName() + ": " + light);
			dms.feedbackBrightness(event_type,
				p_level.getInteger(), light.getInteger());
			if(event_type == EventType.DMS_BRIGHT_LOW ||
			   event_type == EventType.DMS_BRIGHT_HIGH)
				return new QueryBrightnessTable();
			else
				return null;
		}
	}

	/** Phase to get the brightness table */
	protected class QueryBrightnessTable extends Phase {

		/** Get the brightness table */
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(b_levels);
			mess.add(brightness);
			DmsIllumControl control = new DmsIllumControl();
			mess.add(control);
			mess.queryProps();
			DMS_LOG.log(dms.getName() + ": " + b_levels);
			DMS_LOG.log(dms.getName() + ": " + brightness);
			DMS_LOG.log(dms.getName() + ": " + control);
			if(!control.isPhotocell())
				return new SetPhotocellControl();
			else
				return new SetBrightnessTable();
		}
	}

	/** Phase to set photocell control mode */
	protected class SetPhotocellControl extends Phase {

		/** Set the photocell control mode */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsIllumControl control = new DmsIllumControl();
			control.setEnum(DmsIllumControl.Enum.photocell);
			mess.add(control);
			mess.storeProps();
			DMS_LOG.log(dms.getName() + ":= " + control);
			return new SetBrightnessTable();
		}
	}

	/** Phase to set a new brightness table */
	protected class SetBrightnessTable extends Phase {

		/** Set the brightness table */
		protected Phase poll(CommMessage mess) throws IOException {
			// NOTE: if the existing table is not valid, don't mess
			//       with it.  This check is needed for a certain
			//       vendor, which has a wacky brightness table.
			if(brightness.isValid()) {
				brightness.setTable(calculateTable());
				mess.add(brightness);
//				mess.storeProps();
				DMS_LOG.log(dms.getName() + ":= " + brightness);
			}
			return null;
		}
	}

	/** Get the brightness table as a list of samples.  This is done by
	 * adjusting the current brightness table light output values. */
	protected int[][] calculateTable() throws IOException {
		final int[][] table = brightness.getTable();
		dms.queryBrightnessFeedback(new DMSImpl.BrightnessHandler() {
			public void feedback(EventType et, int photo,
				int output)
			{
				switch(et) {
				case DMS_BRIGHT_LOW:
					feedbackLow(table, photo, output);
					break;
				case DMS_BRIGHT_GOOD:
					feedbackGood(table, photo, output);
					break;
				case DMS_BRIGHT_HIGH:
					feedbackHigh(table, photo, output);
					break;
				default:
					break;
				}
			}
		});
		return table;
	}

	/** Adjust a brightness table with DMS_BRIGHT_LOW feedback */
	static protected void feedbackLow(int[][] table, int photo, int output){
		int light = 0;		// highest light output so far
		for(int[] lvl: table) {
			light = Math.max(lvl[0], light);
			if(lvl[1] <= photo && photo <= lvl[2])
				light = Math.max(light, output) + 256;
			lvl[0] = Math.min(light, 65535);
		}
	}

	/** Adjust a brightness table with DMS_BRIGHT_GOOD feedback */
	static protected void feedbackGood(int[][] table, int photo,
		int output)
	{
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
	static protected void feedbackHigh(int[][] table, int photo,int output){
		int light = 65535;	// lowest light output so far
		for(int i = table.length - 1; i >= 0; i--) {
			int[] lvl = table[i];
			light = Math.min(lvl[0], light);
			if(lvl[1] <= photo && photo <= lvl[2])
				light = Math.min(light, output) - 256;
			lvl[0] = Math.max(light, 0);
		}
	}
}
