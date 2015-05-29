/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2015  Minnesota Department of Transportation
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
import static us.mn.state.dot.tms.server.comm.ntcip.mib1203.MIB1203.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;

/**
 * Operation to incorporate brightness feedback for a DMS.
 *
 * @author Douglas Lau
 */
public class OpUpdateDMSBrightness extends OpDMS {

	/** Event type (DMS_BRIGHT_GOOD, DMS_BRIGHT_LOW or DMS_BRIGHT_HIGH) */
	private final EventType event_type;

	/** Illumination control */
	private final ASN1Enum<DmsIllumControl> control =
		new ASN1Enum<DmsIllumControl>(DmsIllumControl.class,
		dmsIllumControl.node);

	/** Maximum photocell level */
	private final ASN1Integer max_level =
		dmsIllumMaxPhotocellLevel.makeInt();

	/** Photocell level status */
	private final ASN1Integer p_level =
		dmsIllumPhotocellLevelStatus.makeInt();

	/** Light output status */
	private final ASN1Integer light = dmsIllumLightOutputStatus.makeInt();

	/** Total number of supported brightness levels */
	private final ASN1Integer b_levels = dmsIllumNumBrightLevels.makeInt();

	/** Brightness table */
	private final DmsIllumBrightnessValues brightness =
		new DmsIllumBrightnessValues();

	/** Create a new DMS brightness feedback operation */
	public OpUpdateDMSBrightness(DMSImpl d, EventType et) {
		super(PriorityLevel.COMMAND, d);
		event_type = et;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
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
			logQuery(max_level);
			logQuery(p_level);
			logQuery(light);
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
			mess.add(control);
			mess.queryProps();
			logQuery(b_levels);
			logQuery(brightness);
			logQuery(control);
			if (control.getEnum() == DmsIllumControl.photocell)
				return new SetManualControl();
			else
				return new SetBrightnessTable();
		}
	}

	/** Phase to set manual control mode */
	protected class SetManualControl extends Phase {

		/** Set the manual control mode */
		protected Phase poll(CommMessage mess) throws IOException {
			control.setEnum(DmsIllumControl.manual);
			mess.add(control);
			logStore(control);
			mess.storeProps();
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
			if (brightness.isValid()) {
				brightness.setTable(calculateTable());
				mess.add(brightness);
				logStore(brightness);
				mess.storeProps();
			}
			return new SetPhotocellControl();
		}
	}

	/** Calculate a new brightness table */
	private BrightnessLevel[] calculateTable() {
		BrightnessLevel[] table = brightness.getTable();
		dms.queryBrightnessFeedback(new BrightnessTable(table));
		return table;
	}

	/** Phase to set photocell control mode */
	protected class SetPhotocellControl extends Phase {

		/** Set the photocell control mode */
		protected Phase poll(CommMessage mess) throws IOException {
			control.setEnum(DmsIllumControl.photocell);
			mess.add(control);
			logStore(control);
			mess.storeProps();
			return null;
		}
	}
}
