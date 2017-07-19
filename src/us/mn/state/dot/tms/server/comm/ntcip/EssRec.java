/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Iteris Inc.
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.PavementSurfaceStatus;
import us.mn.state.dot.tms.PrecipSituation;
import us.mn.state.dot.tms.server.WeatherSensorImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.FEET;
import static us.mn.state.dot.tms.units.Distance.Units.METERS;
import static us.mn.state.dot.tms.units.Distance.Units.MICROMETERS;
import static us.mn.state.dot.tms.units.Distance.Units.MILLIMETERS;
import us.mn.state.dot.tms.units.Speed;
import static us.mn.state.dot.tms.units.Speed.Units.KPH;
import static us.mn.state.dot.tms.units.Speed.Units.MPH;
import static us.mn.state.dot.tms.units.Speed.Units.MPS;
import us.mn.state.dot.tms.units.Temperature;
import static us.mn.state.dot.tms.units.Temperature.Units.CELSIUS;

/**
 * A collection of weather condition values with functionality
 * to convert from NTCIP 1204 units to IRIS units.
 * @author Michael Darter
 */
public class EssRec {

	/** Creation time */
	private long create_time = 0;

	/** Storage time */
	private long storage_time = 0;

	/** Weather sensor */
	private final WeatherSensorImpl w_sensor;

	/** Average wind direction in degrees */
	private Integer wind_dir_avg = null;

	/** Average wind speed */
	private Speed wind_speed_avg = null;

	/** Max wind gust speed */
	private Speed max_wind_gust_speed = null;

	/** Max wind gust direction in degrees */
	private Integer max_wind_gust_dir = null;

	/** Spot wind direction */
	private Integer spot_wind_dir = null;

	/** Spot wind Speed */
	private Speed spot_wind_speed = null;

	/** Dew point temperature */
	private Temperature dew_point_temp = null;

	/** Max temp */
	private Temperature max_temp = null;

	/** Min temp */
	private Temperature min_temp = null;

	/** Air temperature */
	private Temperature air_temp = null;

	/** Relative humidity (%)  */
	private Integer rel_humidity = null;

	/** Precipitation rate in mm/hr */
	private Integer precip_rate = null;

	/** Precipitation situation */
	private Integer precip_situation = null;

	/** Precipitation 1h */
	private Integer precip_one_hour = null;

	/** Air pressure in Pascals  */
	private Integer air_pressure = null;

	/** Visibility */
	private Distance visibility = null;

	/** Pavement surface temperature */
	private Temperature pvmt_surf_temp = null;

	/** Surface temperature */
	private Temperature surf_temp = null;

	/** Pavement surface status */
	private Integer pvmt_surf_status = null;

	/** Pavement surface temperature */
	private Temperature surf_freeze_temp = null;

	/** Subsurface temperature */
	private Temperature subsurf_temp = null;

	/** Constructor */
	public EssRec(WeatherSensorImpl ws) {
		w_sensor = ws;
		create_time = TimeSteward.currentTimeMillis();
	}

	/** Convert a temperature to iris format.
	 * @arg tc Is a temperature in tenths of a degree C. A value
	 *	   of 1001 is an error condition or missing value.
	 * @return Temperature in C rounded to nearest degree or 
	 * 	   null if missing */
	private Temperature convertTemp(ASN1Integer tc) {
		if (tc != null) {
			int itc = tc.getInteger();
			if (itc == 1001)
				return null;
			return new Temperature(.1 * (double)itc);
		}
		return null;
	}

	/** Convert essAvgWindDirection to iris format.
	 * @arg awd Is essAvgWindDirection, which is the two minute 
	 * 	average of direction, measured clockwise in degs from
	 * 	north, with 361 indicating an error or missing value.
	 * @return Angle in degrees or null for missing. */
	private Integer convertAngle(ASN1Integer awd) {
		if (awd != null) {
			int iawd = awd.getInteger();
			if (iawd >= 0 && iawd <= 360)
				return new Integer(iawd);
		}
		return null;
	}

	/** Convert integer in tenths of m/s to Speed.
	 * @arg aws Is essAvgWindSpeed, which is the two minute 
	 * 	average of speed in tenths of meters per second,
	 * 	with 65,535 indicating an error or missing value.
	 * @return Speed in KPH or null if missing */
	public Speed convertSpeed(ASN1Integer aws) {
		Speed ret;
		if (aws != null) {
			int iaws = aws.getInteger();
			if (iaws != 65535) {
				double mps = .1 * (double)iaws;
				return new Speed(mps, MPS);
			}
		}
		return null;
	}

	/** Convert humidity to an integer.
	 * @arg rhu Relative humidity in percent. A value of 
	 * 	    101 indicates * an error or missing value.
	 * @return Humidity as a percent or null if missing. */
	public Integer convertHumidity(ASN1Integer rhu) {
		if (rhu != null) {
			int irhu = rhu.getInteger();
			if (irhu >= 0 && irhu <= 100)
				return new Integer(irhu);
		}
		return null;
	}

	/** Convert the precipitation rate in 1/10s of gram per square 
	 * meter per second to mm/hr.
	 * @return Precipiration rate in mm/hr or null if missing */
	public Integer convertPrecipRate(ASN1Integer prr) {
		if (prr != null) {
			// 1mm of water over 1 sqm is 1L which is 1Kg
			int tg = prr.getInteger();
			if (tg != 65535) {
				int mmhr = (int)Math.round((double)tg * .36);
				return new Integer(mmhr);
			}
		}
		return null;
	}

	/** Convert the precipitation situation */
	public Integer convertPrecipSituation(ASN1Integer prs) {
		if (prs != null) {
			int i = prs.getInteger();
			PrecipSituation eps = PrecipSituation.fromOrdinal(i);
			if (eps != PrecipSituation.UNDEFINED)
				return new Integer(i);
		}
		return null;
	}

	/** Convert the precipitation 1h from tenths of a mm to mm.
	 * @return Precipitation over 1h in mm or null */
	public Integer convertPrecip(ASN1Integer pr) {
		if (pr != null) {
			int pi = pr.getInteger();
			if (pi != 65535) {
				int cp = (int)Math.round((double)pi * .1);
				return new Integer(cp);
			}
		}
		return null;
	}

	/** Convert essAtmosphericPressure to iris format.
	 * @arg apr Is essAtmosphericPressure in 1/10ths of millibars,
	 *      with 65535 indicating an error or missing value.
	 * @return Pressure in pascals */
	private Integer convertAtmosphericPressure(ASN1Integer apr) {
		if (apr != null) {
			int tmb = apr.getInteger();
			if (tmb == 65535)
				return null;
			double mb = (double)tmb * .1;
			double pa = mb * 100;
			return new Integer((int)Math.round(pa));
		}
		return null;
	}

	/** Convert essVisibility to a distance object.
	 * @arg vis Visibility as essVisibility, which is in one tenth of
	 *      a meter with 1,000,001 indicating an error or missing value.
	 * @return Visibility in meters with null for missing */
	private Distance convertVisibility(ASN1Integer vis) {
		if (vis != null) {
			int iv = vis.getInteger();
			if (iv == 1000001)
				return null;
			iv = (int)Math.round((double)iv / 10);
			return new Distance(iv, METERS);
		}
		return null;
	}

	/** Update the wind dir */
	public void storeAvgWindDir(ASN1Integer angle) {
		wind_dir_avg = convertAngle(angle);
		w_sensor.setWindDirNotify(wind_dir_avg);
	}

	/** Store the average wind speed using essAvgWindSpeed.
	 * @arg awd Is essAvgWindSpeed, which is the two minute 
	 * 	average of speed in tenths of meters per second,
	 * 	with 65,535 indicating an error or missing value. */
	public void storeAvgWindSpeed(ASN1Integer aws) {
		wind_speed_avg = convertSpeed(aws);
		if (wind_speed_avg != null)
			w_sensor.setWindSpeedNotify(wind_speed_avg.round(KPH));
		else
			w_sensor.setWindSpeedNotify(null);
	}

	/** Store the max wind gust speed. */
	public void storeMaxWindGustSpeed(ASN1Integer mwgs) {
		max_wind_gust_speed = convertSpeed(mwgs);
		if (max_wind_gust_speed != null) {
			w_sensor.setMaxWindGustSpeedNotify(
				max_wind_gust_speed.round(KPH));
		} else {
			w_sensor.setMaxWindGustSpeedNotify(null);
		}
	}

	/** Store the max wind gust direction */
	public void storeMaxWindGustDir(ASN1Integer mwgd) {
		max_wind_gust_dir = convertAngle(mwgd);
		w_sensor.setMaxWindGustDirNotify(max_wind_gust_dir);
	}

	/** Store spot wind direction */
	public void storeSpotWindDir(ASN1Integer swd) {
		spot_wind_dir = convertAngle(swd);
		w_sensor.setSpotWindDirNotify(spot_wind_dir);
	}

	/** Store spot wind speed */
	public void storeSpotWindSpeed(ASN1Integer sws) {
		spot_wind_speed = convertSpeed(sws);
		if (spot_wind_speed != null) {
			w_sensor.setSpotWindSpeedNotify(
				spot_wind_speed.round(KPH));
		} else {
			w_sensor.setSpotWindSpeedNotify(null);
		}
	}

	/** Store the dew point temperature */
	public void storeDewpointTemp(ASN1Integer dpt) {
		dew_point_temp = convertTemp(dpt);
		w_sensor.setDewPointTempNotify(dew_point_temp != null ? 
			dew_point_temp.round(CELSIUS) : null);
	}

	/** Store the max temperature */
	public void storeMaxTemp(ASN1Integer mt) {
		max_temp = convertTemp(mt);
		w_sensor.setMaxTempNotify(max_temp != null ?
			max_temp.round(CELSIUS) : null);
	}

	/** Store the min temperature */
	public void storeMinTemp(ASN1Integer mt) {
		min_temp = convertTemp(mt);
		w_sensor.setMinTempNotify(min_temp != null ?
			min_temp.round(CELSIUS) : null);
	}

	/** Store the air temperature, which is assumed to be the
	 * first sensor in the table. Additional sensors are ignored */
	public void storeAirTemp(TemperatureSensorsTable tst) {
		// even if no table rows present, set values
		air_temp = convertTemp(tst.getTemp(1));
		w_sensor.setAirTempNotify(air_temp != null?
			air_temp.round(CELSIUS) : null);
	}

	/** Store humidity */
	public void storeHumidity(ASN1Integer rhu) {
		rel_humidity = convertHumidity(rhu);
		w_sensor.setHumidityNotify(rel_humidity);
	}

	/** Store the precipitation rate */
	public void storePrecipRate(ASN1Integer prr) {
		precip_rate = convertPrecipRate(prr);
		w_sensor.setPrecipRateNotify(precip_rate);
	}

	/** Store the precipitation stuation */
	public void storePrecipSituation(ASN1Integer ps) {
		precip_situation = convertPrecipSituation(ps);
		w_sensor.setPrecipSituationNotify(precip_situation);
	}

	/** Store the precipitation 1h */
	public void storePrecipOneHour(ASN1Integer pr) {
		precip_one_hour = convertPrecip(pr);
		w_sensor.setPrecipOneHourNotify(precip_one_hour);
	}

	/** Store the atmospheric pressure */
	public void storeAtmosphericPressure(ASN1Integer apr) {
		air_pressure = convertAtmosphericPressure(apr);
		w_sensor.setPressureNotify(air_pressure);
	}

	/** Store visibility */
	public void storeVisibility(ASN1Integer vis) {
		visibility = convertVisibility(vis);
		w_sensor.setVisibilityNotify(visibility.round(METERS));
	}

	/** Store current time */
	public void storeStamp() {
		storage_time = TimeSteward.currentTimeMillis();
		w_sensor.setStampNotify(storage_time);
	}

	/** Store pavement sensor related values.
	 * @arg pst Pavement sensor table, which might contain observations
	 *          from multiple sensors. Only the first sensor is used. */
	public void store(PavementSensorsTable pst) {
		// Even if no table rows present, set values
		// Ignore rows > 1
		final int row = 1;
		pvmt_surf_temp = convertTemp(pst.getSurfTemp(row));
		w_sensor.setPvmtSurfTempNotify(pvmt_surf_temp != null ? 
			pvmt_surf_temp.round(CELSIUS) : null);

		surf_temp = convertTemp(pst.getSurfTemp(row));
		w_sensor.setSurfTempNotify(surf_temp != null ? 
			surf_temp.round(CELSIUS) : null);

		pvmt_surf_status = pst.getPvmtSurfStatus(row);
		w_sensor.setPvmtSurfStatusNotify(pvmt_surf_status);

		surf_freeze_temp = convertTemp(pst.getSurfFreezeTemp(row));
		w_sensor.setSurfFreezeTempNotify(surf_freeze_temp != null ?
			surf_freeze_temp.round(CELSIUS) : null);
	}

	/** Store subsurface sensor related values.
	 * @arg sst Subsurface sensor table, which might 
	 *          contain observations from multiple 
	 *          sensors. Only the first sensor is used. */
	public void store(SubsurfaceSensorsTable sst) {
		// Even if no table rows present, set values
		// Ignore rows > 1
		final int row = 1;
		subsurf_temp = convertTemp(sst.getTemp(row));
		w_sensor.setSubSurfTempNotify(subsurf_temp != null ? 
			subsurf_temp.round(CELSIUS) : null);
	}

	/** Get the an enum from an ordinal value */
	private PavementSurfaceStatus getPvmtSurfStatus() {
		if (pvmt_surf_status == null)
			return PavementSurfaceStatus.UNDEFINED;
		else {
			return PavementSurfaceStatus.fromOrdinal(
				pvmt_surf_status);
		}
        }

	/** To string */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(EssRec:");
		sb.append(" w_sensor_name=").append(w_sensor.getName());
		sb.append(" create_time=").append(new Date(create_time));
		sb.append(" air_temp_c=").append(air_temp);
		sb.append(" dew_point_temp_c=").append(dew_point_temp);
		sb.append(" max_temp_c=").append(max_temp);
		sb.append(" min_temp_c=").append(min_temp);
		sb.append(" rel_humidity_perc=").append(rel_humidity);
		sb.append(" wind_speed_avg_mph=").append(
			(wind_speed_avg != null ? 
			wind_speed_avg.convert(MPH) : "null"));
		sb.append(" wind_dir_avg_degs=").append(wind_dir_avg);
		sb.append(" max_wind_gust_speed_mph=").append(
			(max_wind_gust_speed != null ?
			max_wind_gust_speed.convert(MPH) : "null"));
		sb.append(" max_wind_gust_dir_degs=").append(
			max_wind_gust_dir);
		sb.append(" spot_wind_speed_mph=").append(
			(spot_wind_speed != null ?
			spot_wind_speed.convert(MPH) : "null"));
		sb.append(" spot_wind_dir_degs=").append(
			spot_wind_dir);
		sb.append(" air_pressure_pa=").append(air_pressure);
		sb.append(" precip_rate_mmhr=").append(precip_rate);
		sb.append(" precip_situation=").append(precip_situation);
		sb.append(" precip_1h=").append(precip_one_hour);
		sb.append(" visibility_m=").append(visibility);
		sb.append(" pvmt_surf_temp_c=").append(pvmt_surf_temp);
		sb.append(" surf_temp_c=").append(surf_temp);
		sb.append(" pvmt_surf_status=").append(getPvmtSurfStatus());
		sb.append(" pvmt_surf_freeze_temp=").append(surf_freeze_temp);
		sb.append(" subsurf_temp=").append(subsurf_temp);
		sb.append(")");
		return sb.toString();
	}
}
