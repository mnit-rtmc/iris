/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017 Iteris Inc.
 * Copyright (C) 2019-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.WindSensorsTable;
import us.mn.state.dot.tms.server.comm.snmp.NoSuchName;

/**
 * Operation to query a weather sensor's settings.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class OpQueryEssSettings extends OpEss {

	/** Record of values read from the controller */
	private final EssRec ess_rec = new EssRec();;

	/** Wind sensors table */
	private final WindSensorsTable ws_table;

	/** Temperature sensors table */
	private final TemperatureSensorsTable ts_table;

	/** Pavement sensors table */
	private final PavementSensorsTable ps_table;

	/** Sub-surface sensors table */
	private final SubSurfaceSensorsTable ss_table;

	/** Create a new query settings object */
	public OpQueryEssSettings(WeatherSensorImpl ws) {
		super(PriorityLevel.POLL_LOW, ws);
		ws_table = ess_rec.ws_table;
		ts_table = ess_rec.ts_table;
		ps_table = ess_rec.ps_table;
		ss_table = ess_rec.ss_table;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new QueryElevation();
	}

	/** Phase to query elevation values */
	protected class QueryElevation extends Phase {

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ess_rec.atmospheric_values
				.reference_height.node);
			mess.add(ess_rec.atmospheric_values
				.pressure_sensor_height.node);
			mess.queryProps();
			logQuery(ess_rec.atmospheric_values
				.reference_height.node);
			logQuery(ess_rec.atmospheric_values
				.pressure_sensor_height.node);
			return new QueryWindSensorsV2();
		}
	}

	/** Phase to query the wind sensor count (V2+) */
	protected class QueryWindSensorsV2 extends Phase {

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			try {
				mess.add(ws_table.num_sensors);
				mess.queryProps();
				logQuery(ws_table.num_sensors);
				return ws_table.isDone()
				      ? new QueryTemperatureSensors()
				      : new QueryWindTableV2();
			}
			catch (NoSuchName e) {
				// Note: this object was introduced in V2
				return new QueryWindSensorV1();
			}
		}
	}

	/** Phase to query all rows in wind table (V2+) */
	protected class QueryWindTableV2 extends Phase {
		private final WindSensorsTable.Row tr = ws_table.addRow();

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(tr.height.node);
			mess.queryProps();
			logQuery(tr.height.node);
			return ws_table.isDone()
			      ? new QueryTemperatureSensors()
			      : new QueryWindTableV2();
		}
	}

	/** Phase to query wind sensor values (V1) */
	protected class QueryWindSensorV1 extends Phase {

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ws_table.height.node);
			try {
				mess.queryProps();
				logQuery(ws_table.height.node);
			}
			catch (NoSuchName e) {
				// Note: this object is deprecated in V2
			}
			return new QueryTemperatureSensors();
		}
	}

	/** Phase to query the temperature sensors and other data */
	protected class QueryTemperatureSensors extends Phase {

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ts_table.num_temp_sensors);
			mess.queryProps();
			logQuery(ts_table.num_temp_sensors);
			return ts_table.isDone()
			      ? new QueryPavement()
			      : new QueryTemperatureTable();
		}
	}

	/** Phase to query all rows in temperature table */
	protected class QueryTemperatureTable extends Phase {
		private final TemperatureSensorsTable.Row tr = ts_table.addRow();

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(tr.height.node);
			mess.queryProps();
			logQuery(tr.height.node);
			return ts_table.isDone()
			      ? new QueryPavement()
			      : new QueryTemperatureTable();
		}
	}

	/** Phase to query pavement values */
	protected class QueryPavement extends Phase {

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ps_table.num_sensors);
			mess.queryProps();
			logQuery(ps_table.num_sensors);
			return ps_table.isDone()
			      ? new QuerySubSurface()
			      : new QueryPavementTable();
		}
	}

	/** Phase to query all rows in pavement table */
	protected class QueryPavementTable extends Phase {
		private final PavementSensorsTable.Row pr = ps_table.addRow();

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(pr.location);
			mess.add(pr.pavement_type);
			mess.add(pr.height.node);
			mess.add(pr.exposure.node);
			mess.add(pr.sensor_type);
			mess.queryProps();
			logQuery(pr.location);
			logQuery(pr.pavement_type);
			logQuery(pr.height.node);
			logQuery(pr.exposure.node);
			logQuery(pr.sensor_type);
			return ps_table.isDone()
			      ? new QuerySubSurface()
			      : new QueryPavementTable();
		}
	}

	/** Phase to query sub-surface values */
	protected class QuerySubSurface extends Phase {

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ss_table.num_sensors);
			mess.queryProps();
			logQuery(ss_table.num_sensors);
			return ss_table.isDone()
			      ? null
			      : new QuerySubSurfaceTable();
		}
	}

	/** Phase to query all rows in sub-surface table */
	protected class QuerySubSurfaceTable extends Phase {
		private final SubSurfaceSensorsTable.Row sr = ss_table.addRow();

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(sr.location);
			mess.add(sr.sub_surface_type);
			mess.add(sr.depth);
			mess.queryProps();
			logQuery(sr.location);
			logQuery(sr.sub_surface_type);
			logQuery(sr.depth);
			return ss_table.isDone()
			      ? null
			      : new QuerySubSurfaceTable();
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			w_sensor.setSettings(ess_rec.toJson());
		super.cleanup();
	}
}
