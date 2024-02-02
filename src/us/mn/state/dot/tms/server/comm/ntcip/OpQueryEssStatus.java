/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017 Iteris Inc.
 * Copyright (C) 2019-2023  Minnesota Department of Transportation
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
import static us.mn.state.dot.tms.server.comm.ntcip.mib1204.MIB1204.essMobileFriction;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.PavementSensorsTable;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.PercentObject;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.SubSurfaceSensorsTable;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.TemperatureSensorsTable;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.WindSensorsTable;
import us.mn.state.dot.tms.server.comm.snmp.NoSuchName;

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
		super(PriorityLevel.POLL_LOW, ws);
		ws_table = ess_rec.ws_table;
		ts_table = ess_rec.ts_table;
		ps_table = ess_rec.ps_table;
		ss_table = ess_rec.ss_table;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new QueryPressure();
	}

	/** Phase to query atmospheric pressure */
	protected class QueryPressure extends Phase {

		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ess_rec.atmospheric_values
				.atmospheric_pressure);
			mess.queryProps();
			logQuery(ess_rec.atmospheric_values
				.atmospheric_pressure);
			return new QueryVisibility();
		}
	}

	/** Phase to query visibility */
	protected class QueryVisibility extends Phase {

		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ess_rec.atmospheric_values.visibility);
			mess.add(ess_rec.atmospheric_values
				.visibility_situation);
			try {
				mess.queryProps();
				logQuery(ess_rec.atmospheric_values.visibility);
				logQuery(ess_rec.atmospheric_values
					.visibility_situation);
			}
			catch (NoSuchName e) {
				// Note: some vendors do not support these
			}
			return queryWindSensors();
		}
	}

	/** Get phase to query wind sensor data */
	private Phase queryWindSensors() {
		// Vaisala LX model RPUs contain a bug which causes objects
		// in tables to update only once every 12 hours or so.  The
		// preferred workaround is to randomize SNMP request-IDs.
		// This lesser workaround is to query the (deprecated) wind
		// sensor objects from 1204v1
		// FIXME: remove this workaround after testing
		return isVaisalaLx()
			? new QueryWindSensorV1()
			: new QueryWindSensorsV2();
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
			mess.add(tr.avg_speed.node);
			mess.add(tr.avg_direction.node);
			mess.add(tr.spot_speed.node);
			mess.add(tr.spot_direction.node);
			mess.add(tr.gust_speed.node);
			mess.add(tr.gust_direction.node);
			try {
				mess.queryProps();
			}
			catch (NoSuchName e) {
				// Some controllers sometimes seem to randomly
				// forget what windSensorGustDirection is
				return new QueryTemperatureSensors();
			}
			logQuery(tr.avg_speed.node);
			logQuery(tr.avg_direction.node);
			logQuery(tr.spot_speed.node);
			logQuery(tr.spot_direction.node);
			logQuery(tr.gust_speed.node);
			logQuery(tr.gust_direction.node);
			return ws_table.isDone()
			      ? new QueryTemperatureSensors()
			      : new QueryWindSensorsV2();
		}
	}

	/** Phase to query wind sensor values (V1) */
	protected class QueryWindSensorV1 extends Phase {

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ws_table.avg_direction.node);
			mess.add(ws_table.avg_speed.node);
			mess.add(ws_table.spot_direction.node);
			mess.add(ws_table.spot_speed.node);
			mess.add(ws_table.gust_direction.node);
			mess.add(ws_table.gust_speed.node);
			try {
				mess.queryProps();
			}
			catch (NoSuchName e) {
				// Note: these objects are deprecated in V2
				return new QueryTemperatureSensors();
			}
			logQuery(ws_table.avg_direction.node);
			logQuery(ws_table.avg_speed.node);
			logQuery(ws_table.spot_direction.node);
			logQuery(ws_table.spot_speed.node);
			logQuery(ws_table.gust_direction.node);
			logQuery(ws_table.gust_speed.node);
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
		private final TemperatureSensorsTable.Row tr =
			ts_table.addRow();

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(tr.air_temp.node);
			try {
				mess.queryProps();
			}
			catch (NoSuchName e) {
				// Some controllers sometimes seem to randomly
				// forget what essAirTemperature is
				return new QueryPrecipitation();
			}
			logQuery(tr.air_temp.node);
			return ts_table.isDone()
			      ? new QueryPrecipitation()
			      : new QueryTemperatureTable();
		}
	}

	/** Phase to query precipitation values */
	protected class QueryPrecipitation extends Phase {

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ess_rec.precip_values.relative_humidity.node);
			mess.add(ess_rec.precip_values.precip_rate);
			mess.add(ess_rec.precip_values.precip_1_hour);
			mess.add(ess_rec.precip_values.precip_3_hours);
			mess.add(ess_rec.precip_values.precip_6_hours);
			mess.add(ess_rec.precip_values.precip_12_hours);
			mess.add(ess_rec.precip_values.precip_24_hours);
			mess.add(ess_rec.precip_values.precip_situation);
			mess.queryProps();
			logQuery(ess_rec.precip_values.relative_humidity.node);
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
			return nextPavementRow();
		}
	}

	/** Get phase to query next pavement sensor row */
	private Phase nextPavementRow() {
		return ps_table.isDone()
		      ? new QuerySubSurface()
		      : new QueryPavementRow();
	}

	/** Phase to query one pavement sensor row */
	protected class QueryPavementRow extends Phase {
		private final PavementSensorsTable.Row pr =
			ps_table.addRow();

		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(pr.surface_status);
			mess.add(pr.surface_temp.node);
			mess.add(pr.pavement_temp.node);
			mess.add(pr.freeze_point.node);
			mess.add(pr.sensor_error);
			mess.add(pr.salinity);
			mess.add(pr.black_ice_signal);
			mess.queryProps();
			logQuery(pr.surface_status);
			logQuery(pr.surface_temp.node);
			logQuery(pr.pavement_temp.node);
			logQuery(pr.freeze_point.node);
			logQuery(pr.sensor_error);
			logQuery(pr.salinity);
			logQuery(pr.black_ice_signal);
			return new QueryPavementRowV2(pr);
		}
	}

	/** Phase to query one pavement sensor row (V2) */
	protected class QueryPavementRowV2 extends Phase {
		private final PavementSensorsTable.Row pr;
		private QueryPavementRowV2(PavementSensorsTable.Row r) {
			pr = r;
		}

		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			// Note: these objects were introduced in V2
			mess.add(pr.ice_or_water_depth);
			mess.add(pr.surface_conductivity_v2);
			try {
				mess.queryProps();
				logQuery(pr.ice_or_water_depth);
				logQuery(pr.surface_conductivity_v2);
				return new QueryPavementRowV4(pr);
			}
			catch (NoSuchName e) {
				// Fallback to V1 water depth
				return new QueryPavementRowV1(pr);
			}
		}
	}

	/** Phase to query one pavement sensor row (V1) */
	protected class QueryPavementRowV1 extends Phase {
		private final PavementSensorsTable.Row pr;
		private QueryPavementRowV1(PavementSensorsTable.Row r) {
			pr = r;
		}

		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(pr.water_depth);
			// Note: essSurfaceConductivity could be polled here
			try {
				mess.queryProps();
				logQuery(pr.water_depth);
			}
			catch (NoSuchName e) {
				// Note: this object was deprecated in V2
			}
			return nextPavementRow();
		}
	}

	/** Phase to query one pavement sensor row (V4) */
	protected class QueryPavementRowV4 extends Phase {
		private final PavementSensorsTable.Row pr;
		private QueryPavementRowV4(PavementSensorsTable.Row r) {
			pr = r;
		}

		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			// Note: this object was added in V4
			mess.add(pr.friction.node);
			try {
				mess.queryProps();
				logQuery(pr.friction.node);
			}
			catch (NoSuchName e) {
				// Fallback to mobile friction (1st row only)
				if (pr.number == 1)
					return new QueryMobileFriction(pr);
			}
			return nextPavementRow();
		}
	}

	/** Phase to query mobile friction (as fallback).
	 * Note: some vendors support essMobileFriction for permanent stations
	 *       (non-mobile).  We'll pretend it's part of the pavement sensors
	 *       table (first row only). */
	protected class QueryMobileFriction extends Phase {
		private final PavementSensorsTable.Row pr;
		private QueryMobileFriction(PavementSensorsTable.Row r) {
			pr = r;
		}

		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			// Note: mobile friction is not part of pavement table
			PercentObject mf = new PercentObject("friction",
				essMobileFriction.makeInt());
			mess.add(mf.node);
			try {
				mess.queryProps();
				logQuery(mf.node);
				pr.friction.node.setInteger(
					mf.node.getInteger()
				);
			}
			catch (NoSuchName e) {
				// Note: some vendors do not support this object
			}
			return nextPavementRow();
		}
	}

	/** Phase to query sub-surface values */
	protected class QuerySubSurface extends Phase {

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ss_table.num_sensors);
			try {
				mess.queryProps();
				logQuery(ss_table.num_sensors);
			}
			catch (NoSuchName e) {
				// Note: some vendors do not support this object
			}
			return ss_table.isDone()
			      ? new QueryTotalSun()
			      : new QuerySubSurfaceTable();
		}
	}

	/** Phase to query rows in sub-surface table */
	protected class QuerySubSurfaceTable extends Phase {
		private final SubSurfaceSensorsTable.Row sr =
			ss_table.addRow();

		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(sr.sensor_error);
			try {
				mess.queryProps();
				logQuery(sr.sensor_error);
			}
			catch (NoSuchName e) {
				// Note: some vendors do not support this object
			}
			return new QuerySubSurfaceTemp(sr);
		}
	}

	/** Phase to query sub-surface temperature */
	protected class QuerySubSurfaceTemp extends Phase {
		private final SubSurfaceSensorsTable.Row sr;
		private QuerySubSurfaceTemp(SubSurfaceSensorsTable.Row r) {
			sr = r;
		}

		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(sr.temp.node);
			try {
				mess.queryProps();
				logQuery(sr.temp.node);
			}
			catch (NoSuchName e) {
				// Note: some vendors do not support this object
			}
			return new QuerySubSurfaceMoisture(sr);
		}
	}

	/** Phase to query sub-surface moisture */
	protected class QuerySubSurfaceMoisture extends Phase {
		private final SubSurfaceSensorsTable.Row sr;
		private QuerySubSurfaceMoisture(SubSurfaceSensorsTable.Row r) {
			sr = r;
		}

		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(sr.moisture.node);
			try {
				mess.queryProps();
				logQuery(sr.moisture.node);
			}
			catch (NoSuchName e) {
				// Note: some vendors do not support this object
			}
			return ss_table.isDone()
			      ? new QueryTotalSun()
			      : new QuerySubSurfaceTable();
		}
	}

	/** Phase to query total sun value */
	protected class QueryTotalSun extends Phase {

		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ess_rec.rad_values.total_sun);
			try {
				mess.queryProps();
				logQuery(ess_rec.rad_values.total_sun);
			}
			catch (NoSuchName e) {
				// Not supported by some vendors...
			}
			return new QueryCloudSituation();
		}
	}

	/** Phase to query cloud situation value */
	protected class QueryCloudSituation extends Phase {

		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ess_rec.rad_values.cloud_situation);
			try {
				mess.queryProps();
				logQuery(ess_rec.rad_values.cloud_situation);
			}
			catch (NoSuchName e) {
				// Not supported by some vendors...
			}
			return new QueryRadiationV2();
		}
	}

	/** Phase to query radiation values (V2) */
	protected class QueryRadiationV2 extends Phase {

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ess_rec.rad_values
				.instantaneous_terrestrial.node);
			mess.add(ess_rec.rad_values.instantaneous_solar.node);
			mess.add(ess_rec.rad_values.total_radiation.node);
			mess.add(ess_rec.rad_values.total_radiation_period);
			try {
				mess.queryProps();
			}
			catch (NoSuchName e) {
				// Note: these objects were introduced in V2
				return new QueryRadiationV1();
			}
			logQuery(ess_rec.rad_values
				.instantaneous_terrestrial.node);
			logQuery(ess_rec.rad_values.instantaneous_solar.node);
			logQuery(ess_rec.rad_values.total_radiation.node);
			logQuery(ess_rec.rad_values.total_radiation_period);
			return null;
		}
	}

	/** Phase to query radiation values (V1) */
	protected class QueryRadiationV1 extends Phase {

		/** Query values */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ess_rec.rad_values.solar_radiation);
			try {
				mess.queryProps();
			}
			catch (NoSuchName e) {
				// Note: this object was deprecated in V2
				return null;
			}
			logQuery(ess_rec.rad_values.solar_radiation);
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess()) {
			w_sensor.setSample(ess_rec.toJson());
			ess_rec.store(w_sensor);
		}
		super.cleanup();
	}
}
