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
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.EssRec;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1204.MIB1204.*;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.EssPrecipSituation;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.PavementSensorsTable;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.SubSurfaceSensorsTable;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.TemperatureSensorsTable;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1String;

/**
 * Operation to query the status of a weather sensor.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class OpQueryEssStatus extends OpEss {

	/** Record of values read from the controller */
	private final EssRec ess_rec;

	/** Table of temperature sensor data read from the controller */
	private final TemperatureSensorsTable ts_table;

	/** Table of pavement sensor data read from the controller */
	private final PavementSensorsTable ps_table =
		new PavementSensorsTable();

	/** Table of subsurface sensor data read from controller */
	private final SubSurfaceSensorsTable ss_table =
		new SubSurfaceSensorsTable();

	/** Create new query ESS status operation */
	public OpQueryEssStatus(WeatherSensorImpl ws) {
		super(PriorityLevel.DEVICE_DATA, ws);
		ess_rec = new EssRec(ws);
		ts_table = ess_rec.ts_table;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new QueryWind();
	}

	/** Phase to query wind values */
	protected class QueryWind extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ess_rec.wind_values.avg_wind_dir);
			mess.add(ess_rec.wind_values.avg_wind_speed);
			mess.add(ess_rec.wind_values.spot_wind_dir);
			mess.add(ess_rec.wind_values.spot_wind_speed);
			mess.add(ess_rec.wind_values.gust_wind_dir);
			mess.add(ess_rec.wind_values.gust_wind_speed);
			mess.queryProps();
			logQuery(ess_rec.wind_values.avg_wind_dir);
			logQuery(ess_rec.wind_values.avg_wind_speed);
			logQuery(ess_rec.wind_values.spot_wind_dir);
			logQuery(ess_rec.wind_values.spot_wind_speed);
			logQuery(ess_rec.wind_values.gust_wind_dir);
			logQuery(ess_rec.wind_values.gust_wind_speed);
			return new QueryTemperatureSensors();
		}
	}

	/** Phase to query the temperature sensors and other data */
	protected class QueryTemperatureSensors extends Phase {

		/** Query */
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
			return new QueryTemperatureTable();
		}
	}

	/** Phase to query all rows in temperature table */
	protected class QueryTemperatureTable extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			if (ts_table.isDone())
				return new QueryPrecipitation();
			TemperatureSensorsTable.Row tr = ts_table.addRow();
			mess.add(tr.temperature_sensor_height);
			mess.add(tr.air_temp.node);
			mess.queryProps();
			logQuery(tr.temperature_sensor_height);
			logQuery(tr.air_temp.node);
			return this;
		}
	}

	/** Phase to query precipitation values */
	protected class QueryPrecipitation extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ess_rec.precip_values.relative_humidity);
			mess.add(ess_rec.precip_values.precip_rate);
			mess.add(ess_rec.precip_values.precipitation_1_hour);
			mess.add(ess_rec.precip_values.precipitation_3_hours);
			mess.add(ess_rec.precip_values.precipitation_6_hours);
			mess.add(ess_rec.precip_values.precipitation_12_hours);
			mess.add(ess_rec.precip_values.precipitation_24_hours);
			mess.add(ess_rec.precip_values.precip_situation);
			mess.queryProps();
			logQuery(ess_rec.precip_values.relative_humidity);
			logQuery(ess_rec.precip_values.precip_rate);
			logQuery(ess_rec.precip_values.precipitation_1_hour);
			logQuery(ess_rec.precip_values.precipitation_3_hours);
			logQuery(ess_rec.precip_values.precipitation_6_hours);
			logQuery(ess_rec.precip_values.precipitation_12_hours);
			logQuery(ess_rec.precip_values.precipitation_24_hours);
			logQuery(ess_rec.precip_values.precip_situation);
			return new QueryElevation();
		}
	}

	/** Phase to query elevation values */
	protected class QueryElevation extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer reh = essReferenceHeight.makeInt();
			ASN1Integer prh = essPressureHeight.makeInt();
			ASN1Integer wsh = essWindSensorHeight.makeInt();
			ASN1Integer apr = essAtmosphericPressure.makeInt();
			mess.add(reh);
			mess.add(prh);
			mess.add(wsh);
			mess.add(apr);
			mess.queryProps();
			logQuery(reh);
			logQuery(prh);
			logQuery(wsh);
			logQuery(apr);
			ess_rec.storeAtmosphericPressure(apr);
			return new QueryVisibility();
		}
	}

	/** Phase to query visibility values */
	protected class QueryVisibility extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer vis = essVisibility.makeInt();
			mess.add(vis);
			mess.queryProps();
			logQuery(vis);
			ess_rec.storeVisibility(vis);
			return new QueryPavement();
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
			if (ps_table.isDone()) {
				ess_rec.store(ps_table);
				return new QuerySubsurface();
			}
			PavementSensorsTable.Row pr = ps_table.addRow();
			mess.add(pr.pavement_type);
			mess.add(pr.sensor_type);
			mess.add(pr.surface_status);
			mess.add(pr.surface_temp.node);
			mess.add(pr.pavement_temp.node);
			mess.add(pr.surface_freeze_point.node);
			mess.add(pr.pavement_sensor_error);
			mess.add(pr.surface_water_depth);
			mess.queryProps();
			logQuery(pr.pavement_type);
			logQuery(pr.sensor_type);
			logQuery(pr.surface_status);
			logQuery(pr.surface_temp.node);
			logQuery(pr.pavement_temp.node);
			logQuery(pr.surface_freeze_point.node);
			logQuery(pr.pavement_sensor_error);
			logQuery(pr.surface_water_depth);
			return this;
		}
	}

	/** Phase to query subsurface values */
	protected class QuerySubsurface extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			mess.add(ss_table.num_sensors);
			mess.queryProps();
			logQuery(ss_table.num_sensors);
			return new QuerySubsurfaceTable();
		}
	}

	/** Phase to query all rows in subsurface table */
	protected class QuerySubsurfaceTable extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			if (ss_table.isDone()) {
				ess_rec.store(ss_table);
				return new FinalPhase();
			}
			SubSurfaceSensorsTable.Row sr = ss_table.addRow();
			mess.add(sr.sub_surface_sensor_location);
			mess.add(sr.sub_surface_type);
			mess.add(sr.sub_surface_sensor_depth);
			mess.add(sr.sub_surface_temp.node);
			mess.add(sr.sub_surface_moisture);
			mess.add(sr.sub_surface_sensor_error);
			mess.queryProps();
			logQuery(sr.sub_surface_sensor_location);
			logQuery(sr.sub_surface_type);
			logQuery(sr.sub_surface_sensor_depth);
			logQuery(sr.sub_surface_temp.node);
			logQuery(sr.sub_surface_moisture);
			logQuery(sr.sub_surface_sensor_error);
			return this;
		}
	}

	/** Final phase */
	protected class FinalPhase extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ess_rec.store();
			log("ess_rec=" + ess_rec);
			log("w_sensor=" + w_sensor.toStringDebug());
			return null;
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess()) {
			setMaintStatus("");
			setErrorStatus("");
		}
		super.cleanup();
	}
}
