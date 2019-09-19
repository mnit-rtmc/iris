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
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.EssSurfaceStatus;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.PavementSensorError;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.PavementSensorsTable;
import us.mn.state.dot.tms.server.comm.ntcip.mib1204.SubSurfaceSensorError;
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

	/** Table of temperature sensor data read from the controller */
	private final TemperatureSensorsTable ts_table =
		new TemperatureSensorsTable();

	/** Table of pavement sensor data read from the controller */
	private final PavementSensorsTable ps_table =
		new PavementSensorsTable();

	/** Table of subsurface sensor data read from controller */
	private final SubSurfaceSensorsTable ss_table =
		new SubSurfaceSensorsTable();

	/** Record of values read from the controller */
	private final EssRec ess_rec;

	/** Constructor */
	public OpQueryEssStatus(WeatherSensorImpl ws) {
		super(PriorityLevel.DEVICE_DATA, ws);
		ess_rec = new EssRec(ws);
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
			ASN1Integer awd = essAvgWindDirection.makeInt();
			ASN1Integer aws = essAvgWindSpeed.makeInt();
			ASN1Integer mwgs = essMaxWindGustSpeed.makeInt();
			ASN1Integer mwgd = essMaxWindGustDir.makeInt();
			ASN1Integer swd = essSpotWindDirection.makeInt();
			ASN1Integer sws = essSpotWindSpeed.makeInt();
			mess.add(awd);
			mess.add(aws);
			mess.add(mwgs);
			mess.add(mwgd);
			mess.add(swd);
			mess.add(sws);
			mess.queryProps();
			logQuery(awd);
			logQuery(aws);
			logQuery(mwgs);
			logQuery(mwgd);
			logQuery(swd);
			logQuery(sws);
			ess_rec.storeStamp();
			ess_rec.storeAvgWindDir(awd);
			ess_rec.storeAvgWindSpeed(aws);
			ess_rec.storeMaxWindGustSpeed(mwgs);
			ess_rec.storeMaxWindGustDir(mwgd);
			ess_rec.storeSpotWindDir(swd);
			ess_rec.storeSpotWindSpeed(sws);
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
			ess_rec.storeDewpointTemp(ts_table.dew_point_temp);
			ess_rec.storeMaxTemp(ts_table.max_air_temp);
			ess_rec.storeMinTemp(ts_table.min_air_temp);
			return new QueryTemperatureTable();
		}
	}

	/** Phase to query all rows in temperature table */
	protected class QueryTemperatureTable extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			if (ts_table.isDone()) {
				ess_rec.storeAirTemp(ts_table);
				return new QueryPrecipitation();
			}
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
			ASN1Integer rhu = essRelativeHumidity.makeInt();
			ASN1Integer prr = essPrecipRate.makeInt();
			ASN1Integer p01 = essPrecipitationOneHour.makeInt();
			ASN1Integer p03 = essPrecipitationThreeHours.makeInt();
			ASN1Integer p06 = essPrecipitationSixHours.makeInt();
			ASN1Integer p12 = essPrecipitationTwelveHours.makeInt();
			ASN1Integer p24 = essPrecipitation24Hours.makeInt();
			ASN1Enum<EssPrecipSituation> prs = new ASN1Enum<
				EssPrecipSituation>(EssPrecipSituation.class,
				essPrecipSituation.node);
			mess.add(rhu);
			mess.add(prr);
			mess.add(p01);
			mess.add(p03);
			mess.add(p06);
			mess.add(p12);
			mess.add(p24);
			mess.add(prs);
			mess.queryProps();
			logQuery(rhu);
			logQuery(prr);
			logQuery(p01);
			logQuery(p03);
			logQuery(p06);
			logQuery(p12);
			logQuery(p24);
			logQuery(prs);
			ess_rec.storeHumidity(rhu);
			ess_rec.storePrecipRate(prr);
			ess_rec.storePrecipOneHour(p01);
			ess_rec.storePrecipSituation(prs.getEnum());
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
			ess_rec.storeStamp();
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
