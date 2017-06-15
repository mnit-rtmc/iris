/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017 Iteris Inc.
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
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;

/**
 * Operation to query the status of a weather sensor.
 *
 * @author Michael Darter
 */
public class OpQueryEssStatus extends OpEss {

	/** Table of temperature sensor data read from the controller */
	private final TemperatureSensorsTable ts_table = 
		new TemperatureSensorsTable();

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
			mess.add(awd);
			mess.add(aws);
			mess.add(mwgs);
			mess.add(mwgd);
			mess.queryProps();
			logQuery(awd);
			logQuery(aws);
			logQuery(mwgs);
			logQuery(mwgd);
			ess_rec.storeAvgWindDir(awd);
			ess_rec.storeAvgWindSpeed(aws);
			ess_rec.storeWindGustSpeed(mwgs);
			ess_rec.storeWindGustDir(mwgd);
			return new QueryNumTemperatureSensors();
		}
	}

	/** Phase to query the number of temperature sensors and other data */
	protected class QueryNumTemperatureSensors extends Phase {

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer dpt = essDewpointTemp.makeInt();
			ASN1Integer mat = essMaxTemp.makeInt();
			ASN1Integer mit = essMinTemp.makeInt();
			mess.add(ts_table.num_temp_sensors);
			mess.add(dpt);
			mess.add(mat);
			mess.add(mit);
			mess.queryProps();
			logQuery(ts_table.num_temp_sensors);
			if (ts_table.size() > 1)
				log("Ignoring additional temp sensors");
			logQuery(dpt);
			logQuery(mat);
			logQuery(mit);
			ess_rec.storeDewpointTemp(dpt);
			ess_rec.storeMaxTemp(mat);
			ess_rec.storeMinTemp(mit);
			return new QueryTemperatureTable();
		}
	}

	/** Phase to query all rows in temperature table */
	protected class QueryTemperatureTable extends Phase {

		/** Row to query */
		private int row = 1;

		/** Query */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			if (ts_table.size() <= 0) {
				ess_rec.storeAirTemp(ts_table);
				return new QueryPrecipitation();
			}
			ASN1Integer tsi = 
				essTemperatureSensorIndex.makeInt(row);
			ASN1Integer tsh = 
				essTemperatureSensorHeight.makeInt(row);
			ASN1Integer tsa = 
				essAirTemperature.makeInt(row);
			mess.add(tsi);
			mess.add(tsh);
			mess.add(tsa);
			mess.queryProps();
			logQuery(tsi);
			logQuery(tsh);
			logQuery(tsa);
			ts_table.addRow(row, tsh, tsa);
			if (row < ts_table.size()) {
				row++;
				return this;
			} else {
				ess_rec.storeAirTemp(ts_table);
				return new QueryPrecipitation();
			}
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
			mess.add(rhu);
			mess.add(prr);
			mess.add(p01);
			mess.add(p03);
			mess.add(p06);
			mess.add(p12);
			mess.add(p24);
			mess.queryProps();
			logQuery(rhu);
			logQuery(prr);
			logQuery(p01);
			logQuery(p03);
			logQuery(p06);
			logQuery(p12);
			logQuery(p24);
			ess_rec.storeHumidity(rhu);
			ess_rec.storePrecipRate(prr);
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
			return new FinalPhase();
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
