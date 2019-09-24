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

import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.WeatherSensorImpl;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;

/**
 * A collection of weather condition values which can be converted to JSON.
 * Only values which have been successfully read will be included.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class EssRec {

	/** Atmospheric values */
	public final AtmosphericValues atmospheric_values =
		new AtmosphericValues();

	/** Wind sensor values */
	public final WindSensorValues wind_values = new WindSensorValues();

	/** Temperature sensors table */
	public final TemperatureSensorsTable ts_table =
		new TemperatureSensorsTable();

	/** Precipitation sensor values */
	public final PrecipitationValues precip_values =
		new PrecipitationValues();

	/** Pavement sensors table */
	public final PavementSensorsTable ps_table = new PavementSensorsTable();

	/** Sub-surface sensors table */
	public final SubSurfaceSensorsTable ss_table =
		new SubSurfaceSensorsTable();

	/** Create a new ESS record */
	public EssRec() { }

	/** Store the atmospheric values */
	private void storeAtmospheric(WeatherSensorImpl ws) {
		ws.setPressureNotify(atmospheric_values
			.getAtmosphericPressure());
		Float vis = atmospheric_values.getVisibility();
		Integer v = (vis != null) ? Math.round(vis) : null;
		ws.setVisibilityNotify(v);
	}

	/** Store the wind sensor samples */
	private void storeWinds(WeatherSensorImpl ws) {
		ws.setWindDirNotify(wind_values.getAvgWindDir());
		ws.setWindSpeedNotify(wind_values.getAvgWindSpeedKPH());
		ws.setSpotWindDirNotify(wind_values.getSpotWindDir());
		ws.setSpotWindSpeedNotify(wind_values.getSpotWindSpeedKPH());
		ws.setMaxWindGustDirNotify(wind_values.getGustWindDir());
		ws.setMaxWindGustSpeedNotify(wind_values.getGustWindSpeedKPH());
	}

	/** Store the temperatures */
	private void storeTemps(WeatherSensorImpl ws) {
		ws.setDewPointTempNotify(ts_table.getDewPointTempC());
		ws.setMaxTempNotify(ts_table.getMaxTempC());
		ws.setMinTempNotify(ts_table.getMinTempC());
		// Air temperature is assumed to be the first sensor
		// in the table.  Additional sensors are ignored.
		TemperatureSensorsTable.Row row = ts_table.getRow(1);
		Integer t = (row != null) ? row.getAirTempC() : null;
		ws.setAirTempNotify(t);
	}

	/** Store precipitation samples */
	private void storePrecip(WeatherSensorImpl ws) {
		ws.setHumidityNotify(precip_values.getRelativeHumidity());
		ws.setPrecipRateNotify(precip_values.getPrecipRate());
		ws.setPrecipOneHourNotify(precip_values.getPrecip1Hour());
		EssPrecipSituation ps = precip_values.getPrecipSituation();
		ws.setPrecipSituationNotify((ps != null) ? ps.toString() : null);
	}

	/** Store pavement sensor related values */
	private void storePavement(WeatherSensorImpl ws) {
		PavementSensorsTable.Row row = ps_table.getRow(1);
		if (row != null) {
			ws.setPvmtSurfTempNotify(row.getPvmtTempC());
			ws.setSurfTempNotify(row.getSurfTempC());
			EssSurfaceStatus ess = row.getSurfStatus();
			ws.setPvmtSurfStatusNotify((ess != null)
				? ess.toString()
				: EssSurfaceStatus.undefined.toString());
			ws.setSurfFreezeTempNotify(row.getSurfFreezePointC());
		} else {
			ws.setPvmtSurfTempNotify(null);
			ws.setSurfTempNotify(null);
			ws.setPvmtSurfStatusNotify(null);
			ws.setSurfFreezeTempNotify(null);
		}
	}

	/** Store subsurface sensor values */
	private void storeSubSurface(WeatherSensorImpl ws) {
		SubSurfaceSensorsTable.Row row = ss_table.getRow(1);
		Integer t = (row != null) ? row.getTempC() : null;
		ws.setSubSurfTempNotify(t);
	}

	/** Store all sample values */
	public void store(WeatherSensorImpl ws) {
		storeAtmospheric(ws);
		storeWinds(ws);
		storeTemps(ws);
		storePrecip(ws);
		storePavement(ws);
		storeSubSurface(ws);
		long st = TimeSteward.currentTimeMillis();
		ws.setStampNotify(st);
	}

	/** Get JSON representation */
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		sb.append(atmospheric_values.toJson());
		sb.append(wind_values.toJson());
		sb.append(ts_table.toJson());
		sb.append(precip_values.toJson());
		sb.append(ps_table.toJson());
		sb.append(ss_table.toJson());
		// remove trailing comma
		if (sb.charAt(sb.length() - 1) == ',')
			sb.setLength(sb.length() - 1);
		sb.append('}');
		return sb.toString();
	}
}
