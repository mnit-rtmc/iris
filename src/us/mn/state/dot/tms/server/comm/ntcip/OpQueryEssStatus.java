/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017 Iteris Inc.
 * Copyright (C) 2019-2022  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.EssRec;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.PavementSensorsTable;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.SubSurfaceSensorsTable;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.TemperatureSensorsTable;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.WindSensorsTable;

/**
 * Operation to query the status of a weather sensor.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class OpQueryEssStatus extends OpEss {

	/** Record of values read from the controller */
	private final EssRec ess_rec = new EssRec();

	/** Wind sensors table */
	private final WindSensorsTable ws_table;

	/** Temperature sensors table */
	private final TemperatureSensorsTable ts_table;

	/** Pavement sensors table */
	private final PavementSensorsTable ps_table;

	/** Sub-surface sensors table */
	private final SubSurfaceSensorsTable ss_table;

	/** Create new query ESS status operation */
	public OpQueryEssStatus(WeatherSensorImpl ws) {
		super(PriorityLevel.DEVICE_DATA, ws);
		ws_table = ess_rec.ws_table;
		ts_table = ess_rec.ts_table;
		ps_table = ess_rec.ps_table;
		ss_table = ess_rec.ss_table;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new QueryAtmospheric();
	}

	/** Phase to query atmospheric values */
	protected class QueryAtmospheric extends Phase {

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ess_rec.atmospheric_values
				.atmospheric_pressure);
			mess.add(ess_rec.atmospheric_values.visibility);
			mess.add(ess_rec.atmospheric_values
				.visibility_situation);
			mess.queryProps();
			logQuery(ess_rec.atmospheric_values
				.atmospheric_pressure);
			logQuery(ess_rec.atmospheric_values.visibility);
			logQuery(ess_rec.atmospheric_values
				.visibility_situation);
			return new QueryWind();
		}
	}

	/** Phase to query wind values */
	protected class QueryWind extends Phase {

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ws_table.avg_wind_dir.node);
			mess.add(ws_table.avg_wind_speed.node);
			mess.add(ws_table.spot_wind_dir.node);
			mess.add(ws_table.spot_wind_speed.node);
			mess.add(ws_table.gust_wind_dir.node);
			mess.add(ws_table.gust_wind_speed.node);
			mess.queryProps();
			logQuery(ws_table.avg_wind_dir.node);
			logQuery(ws_table.avg_wind_speed.node);
			logQuery(ws_table.spot_wind_dir.node);
			logQuery(ws_table.spot_wind_speed.node);
			logQuery(ws_table.gust_wind_dir.node);
			logQuery(ws_table.gust_wind_speed.node);
			return new QueryTemperatureSensors();
		}
	}

	/** Phase to query the temperature sensors and other data */
	protected class QueryTemperatureSensors extends Phase {

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ts_table.num_temp_sensors);
			mess.add(ts_table.wet_bulb_temp.node);
			mess.add(ts_table.dew_point_temp.node);
			mess.add(ts_table.max_air_temp.node);
			mess.add(ts_table.min_air_temp.node);
			mess.queryProps();
			logQuery(ts_table.num_temp_sensors);
			logQuery(ts_table.wet_bulb_temp.node);
			logQuery(ts_table.dew_point_temp.node);
			logQuery(ts_table.max_air_temp.node);
			logQuery(ts_table.min_air_temp.node);
			return ts_table.isDone()
			      ? new QueryPrecipitation()
			      : new QueryTemperatureTable();
		}
	}

	/** Phase to query all rows in temperature table */
	protected class QueryTemperatureTable extends Phase {

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			TemperatureSensorsTable.Row tr = ts_table.addRow();
			mess.add(tr.air_temp.node);
			mess.queryProps();
			logQuery(tr.air_temp.node);
			return ts_table.isDone()
			      ? new QueryPrecipitation()
			      : this;
		}
	}

	/** Phase to query precipitation values */
	protected class QueryPrecipitation extends Phase {

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ess_rec.precip_values.relative_humidity);
			mess.add(ess_rec.precip_values.precip_rate);
			mess.add(ess_rec.precip_values.precip_1_hour);
			mess.add(ess_rec.precip_values.precip_3_hours);
			mess.add(ess_rec.precip_values.precip_6_hours);
			mess.add(ess_rec.precip_values.precip_12_hours);
			mess.add(ess_rec.precip_values.precip_24_hours);
			mess.add(ess_rec.precip_values.precip_situation);
			mess.queryProps();
			logQuery(ess_rec.precip_values.relative_humidity);
			logQuery(ess_rec.precip_values.precip_rate);
			logQuery(ess_rec.precip_values.precip_1_hour);
			logQuery(ess_rec.precip_values.precip_3_hours);
			logQuery(ess_rec.precip_values.precip_6_hours);
			logQuery(ess_rec.precip_values.precip_12_hours);
			logQuery(ess_rec.precip_values.precip_24_hours);
			logQuery(ess_rec.precip_values.precip_situation);
			return new QueryPavement();
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

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			PavementSensorsTable.Row pr = ps_table.addRow();
			mess.add(pr.surface_status);
			mess.add(pr.surface_temp.node);
			mess.add(pr.pavement_temp.node);
			mess.add(pr.surface_freeze_point.node);
			mess.add(pr.sensor_error);
			mess.add(pr.surface_water_depth);
			mess.add(pr.salinity);
			mess.add(pr.black_ice_signal);
			mess.queryProps();
			logQuery(pr.surface_status);
			logQuery(pr.surface_temp.node);
			logQuery(pr.pavement_temp.node);
			logQuery(pr.surface_freeze_point.node);
			logQuery(pr.sensor_error);
			logQuery(pr.surface_water_depth);
			logQuery(pr.salinity);
			logQuery(pr.black_ice_signal);
			return ps_table.isDone()
			      ? new QuerySubSurface()
			      : this;
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

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			SubSurfaceSensorsTable.Row sr = ss_table.addRow();
			mess.add(sr.temp.node);
			mess.add(sr.moisture);
			mess.add(sr.sensor_error);
			mess.queryProps();
			logQuery(sr.temp.node);
			logQuery(sr.moisture);
			logQuery(sr.sensor_error);
			return ss_table.isDone() ? null : this;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess()) {
			w_sensor.setSampleNotify(ess_rec.toJson());
			ess_rec.store(w_sensor);
		}
		super.cleanup();
	}
}
