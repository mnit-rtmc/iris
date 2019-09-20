/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1204;

import java.text.SimpleDateFormat;
import java.util.Date;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.WeatherSensorImpl;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.units.Temperature;
import static us.mn.state.dot.tms.units.Temperature.Units.CELSIUS;

/**
 * A collection of weather condition values with functionality
 * to convert from NTCIP 1204 units to IRIS units.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class EssRec {

	/** Creation time */
	private final long create_time;

	/** Weather sensor */
	private final WeatherSensorImpl w_sensor;

	/** Wind sensor values */
	public final WindSensorValues wind_values = new WindSensorValues();

	/** Temperature sensors table */
	public final TemperatureSensorsTable ts_table =
		new TemperatureSensorsTable();

	/** Precipitation sensor values */
	public final PrecipitationValues precip_values =
		new PrecipitationValues();

	/** Atmospheric values */
	public final AtmosphericValues atmospheric_values =
		new AtmosphericValues();

	/** Pavement sensors table */
	public final PavementSensorsTable ps_table = new PavementSensorsTable();

	/** Subsurface temperature */
	private Temperature subsurf_temp = null;

	/** Create a new ESS record */
	public EssRec(WeatherSensorImpl ws) {
		w_sensor = ws;
		create_time = TimeSteward.currentTimeMillis();
	}

	/** Store the wind sensor samples */
	private void storeWinds() {
		w_sensor.setWindDirNotify(wind_values.getAvgWindDir());
		w_sensor.setWindSpeedNotify(wind_values.getAvgWindSpeedKPH());
		w_sensor.setSpotWindDirNotify(wind_values.getSpotWindDir());
		w_sensor.setSpotWindSpeedNotify(wind_values
			.getSpotWindSpeedKPH());
		w_sensor.setMaxWindGustDirNotify(wind_values.getGustWindDir());
		w_sensor.setMaxWindGustSpeedNotify(wind_values
			.getGustWindSpeedKPH());
	}

	/** Store the temperatures */
	private void storeTemps() {
		w_sensor.setDewPointTempNotify(ts_table.getDewPointTempC());
		w_sensor.setMaxTempNotify(ts_table.getMaxTempC());
		w_sensor.setMinTempNotify(ts_table.getMinTempC());
		// Air temperature is assumed to be the first sensor
		// in the table.  Additional sensors are ignored.
		TemperatureSensorsTable.Row row = ts_table.getRow(1);
		Integer t = (row != null) ? row.getAirTempC() : null;
		w_sensor.setAirTempNotify(t);
	}

	/** Store precipitation samples */
	private void storePrecip() {
		w_sensor.setHumidityNotify(precip_values.getRelativeHumidity());
		w_sensor.setPrecipRateNotify(precip_values.getPrecipRate());
		w_sensor.setPrecipOneHourNotify(precip_values.getPrecip1Hour());
		EssPrecipSituation ps = precip_values.getPrecipSituation();
		w_sensor.setPrecipSituationNotify((ps != null)
			? ps.ordinal()
		        : null);
	}

	/** Store the atmospheric values */
	private void storeAtmospheric() {
		w_sensor.setPressureNotify(atmospheric_values
			.getAtmosphericPressure());
		Float vis = atmospheric_values.getVisibility();
		Integer v = (vis != null) ? Math.round(vis) : null;
		w_sensor.setVisibilityNotify(v);
	}

	/** Store pavement sensor related values */
	private void storePavement() {
		PavementSensorsTable.Row row = ps_table.getRow(1);
		if (row != null) {
			w_sensor.setPvmtSurfTempNotify(row.getPvmtTempC());
			w_sensor.setSurfTempNotify(row.getSurfTempC());
			EssSurfaceStatus ess = row.getSurfStatus();
			w_sensor.setPvmtSurfStatusNotify((ess != null)
				? ess.ordinal()
				: EssSurfaceStatus.undefined.ordinal());
			w_sensor.setSurfFreezeTempNotify(row
				.getSurfFreezePointC());
		} else {
			w_sensor.setPvmtSurfTempNotify(null);
			w_sensor.setSurfTempNotify(null);
			w_sensor.setPvmtSurfStatusNotify(null);
			w_sensor.setSurfFreezeTempNotify(null);
		}
	}

	/** Store all sample values */
	public void store() {
		storeWinds();
		storeTemps();
		storePrecip();
		storeAtmospheric();
		storePavement();
		long storage_time = TimeSteward.currentTimeMillis();
		w_sensor.setStampNotify(storage_time);
	}

	/** Store subsurface sensor related values.
	 * @param sst Subsurface sensor table, which might contain observations
	 *            from multiple sensors. Only the first sensor is used. */
	public void store(SubSurfaceSensorsTable sst) {
		// Even if no table rows present, set values
		// Ignore rows > 1
		final int row = 1;
		subsurf_temp = sst.getTemp(row);
		w_sensor.setSubSurfTempNotify((subsurf_temp != null) ?
			subsurf_temp.round(CELSIUS) : null);
	}

	/** Get JSON representation */
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		sb.append("\"create_time\":\"");
		// FIXME: format as standard date/time
		sb.append(new Date(create_time));
		sb.append("\",");
		sb.append(wind_values.toJson());
		sb.append(ts_table.toJson());
		sb.append(precip_values.toJson());
		sb.append(atmospheric_values.toJson());
		sb.append(ps_table.toJson());



// FIXME
	sb.append(" subsurf_temp=").append(subsurf_temp);




		// remove trailing comma
		if (sb.length() > 1)
			sb.setLength(sb.length() - 1);
		sb.append('}');
		return sb.toString();
	}
}
