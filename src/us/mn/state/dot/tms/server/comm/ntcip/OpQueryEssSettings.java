/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017 Iteris Inc.
 * Copyright (C) 2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.WeatherSensorImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1204.MIB1204.*;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.EssRec;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.PavementSensorsTable;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.SubSurfaceSensorsTable;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.TemperatureSensorsTable;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.DisplayString;
import static us.mn.state.dot.tms.server.comm.snmp.MIB.*;

/**
 * Operation to query a weather sensor's settings.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class OpQueryEssSettings extends OpEss {

	/** Record of values read from the controller */
	private final EssRec ess_rec = new EssRec();;

	/** Temperature sensors table */
	private final TemperatureSensorsTable ts_table;

	/** Pavement sensors table */
	private final PavementSensorsTable ps_table;

	/** Sub-surface sensors table */
	private final SubSurfaceSensorsTable ss_table;

	/** Create a new query status object */
	public OpQueryEssSettings(WeatherSensorImpl ws) {
		super(PriorityLevel.DEVICE_DATA, ws);
		ts_table = ess_rec.ts_table;
		ps_table = ess_rec.ps_table;
		ss_table = ess_rec.ss_table;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new QuerySettings();
	}

	/** Phase to query settings */
	protected class QuerySettings extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			// FIXME: move to EssRec
			DisplayString sdes = new DisplayString(sysDescr.node);
			DisplayString scon = new DisplayString(sysContact.node);
			DisplayString snam = new DisplayString(sysName.node);
			DisplayString sloc = new DisplayString(sysLocation.node);
			mess.add(sdes);
			mess.add(scon);
			mess.add(snam);
			mess.add(sloc);
			mess.queryProps();
			controller.setVersionNotify(sdes.toString());
			logQuery(sdes);
			logQuery(scon);
			logQuery(snam);
			logQuery(sloc);
			return new QueryElevation();
		}
	}

	/** Phase to query elevation values */
	protected class QueryElevation extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ess_rec.atmospheric_values.reference_elevation);
			mess.add(ess_rec.atmospheric_values
				.pressure_sensor_height);
			mess.add(ess_rec.wind_values.wind_sensor_height);
			mess.queryProps();
			logQuery(ess_rec.atmospheric_values.reference_elevation);
			logQuery(ess_rec.atmospheric_values
				.pressure_sensor_height);
			logQuery(ess_rec.wind_values.wind_sensor_height);
			return new QueryTemperatureSensors();
		}
	}

	/** Phase to query the temperature sensors and other data */
	protected class QueryTemperatureSensors extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ts_table.num_temp_sensors);
			mess.queryProps();
			logQuery(ts_table.num_temp_sensors);
			return new QueryTemperatureTable();
		}
	}

	/** Phase to query all rows in temperature table */
	protected class QueryTemperatureTable extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			if (ts_table.isDone())
				return new QueryPavement();
			TemperatureSensorsTable.Row tr = ts_table.addRow();
			mess.add(tr.height);
			mess.queryProps();
			logQuery(tr.height);
			return this;
		}
	}

	/** Phase to query pavement values */
	protected class QueryPavement extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ps_table.num_sensors);
			mess.queryProps();
			logQuery(ps_table.num_sensors);
			return new QueryPavementTable();
		}
	}

	/** Phase to query all rows in pavement table */
	protected class QueryPavementTable extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			if (ps_table.isDone())
				return new QuerySubSurface();
			PavementSensorsTable.Row pr = ps_table.addRow();
			mess.add(pr.location);
			mess.add(pr.pavement_type);
			mess.add(pr.height);
			mess.add(pr.exposure);
			mess.add(pr.sensor_type);
			mess.queryProps();
			logQuery(pr.location);
			logQuery(pr.pavement_type);
			logQuery(pr.height);
			logQuery(pr.exposure);
			logQuery(pr.sensor_type);
			return this;
		}
	}

	/** Phase to query sub-surface values */
	protected class QuerySubSurface extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ss_table.num_sensors);
			mess.queryProps();
			logQuery(ss_table.num_sensors);
			return new QuerySubSurfaceTable();
		}
	}

	/** Phase to query all rows in sub-surface table */
	protected class QuerySubSurfaceTable extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			if (ss_table.isDone())
				return null;
			SubSurfaceSensorsTable.Row sr = ss_table.addRow();
			mess.add(sr.location);
			mess.add(sr.sub_surface_type);
			mess.add(sr.sensor_depth);
			mess.queryProps();
			logQuery(sr.location);
			logQuery(sr.sub_surface_type);
			logQuery(sr.sensor_depth);
			return this;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			w_sensor.setSettingsNotify(ess_rec.toJson());
		super.cleanup();
	}
}
