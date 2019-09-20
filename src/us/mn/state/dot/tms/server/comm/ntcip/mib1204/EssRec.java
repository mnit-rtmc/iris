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

	/** Table of temperature sensor data read from the controller */
	public final TemperatureSensorsTable ts_table =
		new TemperatureSensorsTable();

	/** Precipitation sensor values */
	public final PrecipitationValues precip_values =
		new PrecipitationValues();

	/** Atmospheric values */
	public final AtmosphericValues atmospheric_values =
		new AtmosphericValues();

	/** Pavement surface temperature */
	private Temperature pvmt_surf_temp = null;

	/** Surface temperature */
	private Temperature surf_temp = null;

	/** Pavement surface status */
	private EssSurfaceStatus pvmt_surf_status = null;

	/** Pavement surface temperature */
	private Temperature surf_freeze_temp = null;

	/** Subsurface temperature */
	private Temperature subsurf_temp = null;

	/** Create a new ESS record */
	public EssRec(WeatherSensorImpl ws) {
		w_sensor = ws;
		create_time = TimeSteward.currentTimeMillis();
	}

	/** Get the dew point temp */
	private Temperature getDewPointTemp() {
		return ts_table.dew_point_temp.getTemperature();
	}

	/** Get the max temp */
	private Temperature getMaxTemp() {
		return ts_table.max_air_temp.getTemperature();
	}

	/** Get the min temp */
	private Temperature getMinTemp() {
		return ts_table.min_air_temp.getTemperature();
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
		Temperature dpt = getDewPointTemp();
		w_sensor.setDewPointTempNotify((dpt != null) ?
			dpt.round(CELSIUS) : null);
		Temperature mxt = getMaxTemp();
		w_sensor.setMaxTempNotify((mxt != null) ?
			mxt.round(CELSIUS) : null);
		Temperature mnt = getMinTemp();
		w_sensor.setMinTempNotify((mnt != null) ?
			mnt.round(CELSIUS) : null);
		// Air temperature is assumed to be the first sensor
		// in the table.  Additional sensors are ignored.
		Temperature t = ts_table.getAirTemp(1);
		w_sensor.setAirTempNotify((t != null) ?
			t.round(CELSIUS) : null);
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

	/** Store all sample values */
	public void store() {
		storeWinds();
		storeTemps();
		storePrecip();
		storeAtmospheric();
		long storage_time = TimeSteward.currentTimeMillis();
		w_sensor.setStampNotify(storage_time);
	}

	/** Store pavement sensor related values.
	 * @param pst Pavement sensor table, which might contain observations
	 *            from multiple sensors.  Only the first sensor is used. */
	public void store(PavementSensorsTable pst) {
		// Even if no table rows present, set values
		// Ignore rows > 1
		final int row = 1;
		pvmt_surf_temp = pst.getSurfTemp(row);
		w_sensor.setPvmtSurfTempNotify((pvmt_surf_temp != null) ?
			pvmt_surf_temp.round(CELSIUS) : null);

		surf_temp = pst.getSurfTemp(row);
		w_sensor.setSurfTempNotify((surf_temp != null) ?
			surf_temp.round(CELSIUS) : null);

		pvmt_surf_status = pst.getPvmtSurfStatus(row);
		w_sensor.setPvmtSurfStatusNotify((pvmt_surf_status != null)
			? pvmt_surf_status.ordinal()
			: EssSurfaceStatus.undefined.ordinal());

		surf_freeze_temp = pst.getSurfFreezeTemp(row);
		w_sensor.setSurfFreezeTempNotify((surf_freeze_temp != null) ?
			surf_freeze_temp.round(CELSIUS) : null);
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



// FIXME
	sb.append(" pvmt_surf_temp_c=").append(pvmt_surf_temp);
	sb.append(" surf_temp_c=").append(surf_temp);
	sb.append(" pvmt_surf_status=").append(pvmt_surf_status);
	sb.append(" pvmt_surf_freeze_temp=").append(surf_freeze_temp);
	sb.append(" subsurf_temp=").append(subsurf_temp);




		// remove trailing comma
		if (sb.length() > 1)
			sb.setLength(sb.length() - 1);
		sb.append('}');
		return sb.toString();
	}
}
