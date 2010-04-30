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
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.server.BrightnessSample;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ntcip.mib1203.*;

/**
 * Operation to incorporate brightness feedback for a DMS.
 *
 * @author Douglas Lau
 */
public class OpUpdateDMSBrightness extends OpDMS {

	/** Device request (BRIGHTNESS_GOOD, BRIGHTNESS_TOO_DIM or
	 * BRIGHTNESS_TOO_BRIGHT) */
	protected final DeviceRequest request;

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
	public OpUpdateDMSBrightness(DMSImpl d, DeviceRequest r) {
		super(COMMAND, d);
		request = r;
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
			dms.feedbackBrightness(new BrightnessSample(request,
				p_level.getInteger(), light.getInteger()));
			if(request == DeviceRequest.BRIGHTNESS_TOO_DIM ||
			   request == DeviceRequest.BRIGHTNESS_TOO_BRIGHT)
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
			brightness.setTable(calculateTable());
			mess.add(brightness);
//			mess.storeProps();
			DMS_LOG.log(dms.getName() + ":= " + brightness);
			return null;
		}
	}

	/** Get the brightness table as a list of samples */
	protected int[][] calculateTable() throws IOException {
		final BrightnessMapping down = new BrightnessMapping(true);
		final BrightnessMapping up = new BrightnessMapping(false);
		int[][] table = brightness.getTable();
		for(int[] level: table) {
			down.put(level[1], level[0], false);
			up.put(level[2], level[0], false);
		}
		dms.queryBrightnessFeedback(new BrightnessSample.Handler() {
			public void handle(BrightnessSample s) {
				down.put(s.photocell, s.output, s.feedback ==
					DeviceRequest.BRIGHTNESS_TOO_DIM);
				up.put(s.photocell, s.output, s.feedback ==
					DeviceRequest.BRIGHTNESS_TOO_BRIGHT);
			}
		});
		int[][] tbl = new int[table.length][3];
		for(int i = 0; i < table.length; i++) {
			int light = table[i][0];
			tbl[i][0] = light;
			tbl[i][1] = down.getPhotocell(light);
			tbl[i][2] = up.getPhotocell(light);
		}
		return tbl;
	}
}
