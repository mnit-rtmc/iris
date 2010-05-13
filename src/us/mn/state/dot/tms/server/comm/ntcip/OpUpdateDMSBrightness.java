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
			return new QueryBrightnessTable();
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
			if(control.isPhotocell())
				return new SetManualControl();
			else
				return new SetBrightnessTable();
		}
	}

	/** Phase to set manual control mode */
	protected class SetManualControl extends Phase {

		/** Set the manual control mode */
		protected Phase poll(CommMessage mess) throws IOException {
			DmsIllumControl control = new DmsIllumControl();
			control.setEnum(DmsIllumControl.Enum.manual);
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
				mess.storeProps();
				DMS_LOG.log(dms.getName() + ":= " + brightness);
			}
			return new SetPhotocellControl();
		}
	}

	/** Calculate a new brightness table */
	protected int[][] calculateTable() throws IOException {
		int[][] table = brightness.getTable();
		dms.queryBrightnessFeedback(new BrightnessTable(table));
		return table;
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
			return null;
		}
	}
}
