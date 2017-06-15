/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  AHMCT, University of California
 * Copyright (C) 2012-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ssi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.server.WeatherSensorImpl;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.FEET;
import static us.mn.state.dot.tms.units.Distance.Units.METERS;
import static us.mn.state.dot.tms.units.Distance.Units.MICROMETERS;
import static us.mn.state.dot.tms.units.Distance.Units.MILLIMETERS;
import us.mn.state.dot.tms.units.Speed;
import static us.mn.state.dot.tms.units.Speed.Units.KPH;
import static us.mn.state.dot.tms.units.Speed.Units.MPH;
import us.mn.state.dot.tms.units.Temperature;
import static us.mn.state.dot.tms.units.Temperature.Units.CELSIUS;

/**
 * SSI record.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class RwisRec {

	/** Get the duration of a record in milliseconds */
	static private long durationMs() {
		return 20 * 1000;
	}

	/** Parse the fields of a line */
	static private String[] parseFields(String line) {
		String[] fs = line.split(",");
		for (int i = 0; i < fs.length; i++)
			fs[i] = fs[i].trim();
		return fs;
	}

	/** Get field as long which is a time.
	 * @return Time or null on error. */
	static private Long parseDateTime(String field) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(
				"MM/dd/yyyy HH:mm:ss");
			sdf.setLenient(false);
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date pd = sdf.parse(field);
			return pd.getTime();
		}
		catch (ParseException e) {
			return null;
		}
	}

	/** Parse temperature.
	 * @param field Temp as 1/100 degree Celsius.
	 * @return Parsed temperature. */
	static private Temperature parseTemp(String field) {
		Double t = parseDouble(field);
		if (t != null)
			return new Temperature(t / 100);
		else
			return null;
	}

	/** Parse field as int */
	static private Integer parseInt(String field) {
		try {
			return Integer.parseInt(field);
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

	/** Parse speed.
	 * @param field Speed in MPH.
	 * @return Parsed speed. */
	static private Speed parseMph(String field) {
		Integer mph = parseInt(field);
		if (mph != null)
			return new Speed(mph, MPH);
		else
			return null;
	}

	/** Parse distance.
	 * @param field Distance in feet.
	 * @return Parsed distance. */
	static private Distance parseFt(String field) {
		Integer ft = parseInt(field);
		if (ft != null)
			return new Distance(ft, FEET);
		else
			return null;
	}

	/** Parse distance.
	 * @param field Distance in mm.
	 * @return Parsed distance. */
	static private Distance parseMm(String field) {
		Integer mm = parseInt(field);
		if (mm != null)
			return new Distance(mm, MILLIMETERS);
		else
			return null;
	}

	/** Parse field as double */
	static private Double parseDouble(String field) {
		try {
			return Double.parseDouble(field);
		}
		catch (NumberFormatException ex) {
			return null;
		}
	}

	/** Raw record, as a line of CSV text */
	private final String raw_rec;

	/** Creation time */
	private final long create_time;

	/** RWIS site id (Siteid) */
	private final String site_id;

	/** Get the RWIS site ID */
	public String getSiteId() {
		return site_id;
	}

	/** Observation time (DtTm) */
	private final Long obs_time;

	/** Air temperature (AirTemp) */
	private final Temperature air_temp;

	/** Dew point (Dewpoint) */
	private final Temperature dew_point;

	/** Relative humidity (%) (Rh) */
	private final Integer rel_humidity;

	/** Average wind speed in MPH (SpdAvg) */
	private final Speed wind_speed_avg;

	/** Gust wind speed in MPH (SpdGust) */
	private final Speed wind_speed_gust;

	/** Minimum wind direction in degrees (DirMin) */
	private final Integer wind_dir_min;

	/** Average wind direction in degrees (DirAvg) */
	private final Integer wind_dir_avg;

	/** Maximum wind direction in degrees (DirMax) */
	private final Integer wind_dir_max;

	/** Air pressure (Pressure) */
	private final Integer air_pressure;

	/** Precipitation intensity (PcIntens) */
	private final String precip_intens;

	/** Precipitation type (PcType) */
	private final String precip_type;

	/** Precipitation rate in mm/hr (PcRate) */
	private final Integer precip_rate;

	/** Precipitation accumulation in mm (PcAccum) */
	private final Distance precip_accum;

	/** Visibility (Visibility) */
	private final Distance visibility;

	/** Create a new RWIS record by parsing text that contains an ssi
	 * record into fields.
	 * @param line A single text line (record).  The fields are: Siteid,
	 *             DtTm, AirTemp, Dewpoint, Rh, SpdAvg, SpdGust, DirMin,
	 *             DirAvg, DirMax, Pressure, PcIntens, PcType, PcRate,
	 *             PcAccum, Visibility.  For example:<p>
	 *             365000,02/09/2010 15:52:16,820,-2130,10,2,14,,5,6,
	 *             65535,Other,Other,-1,-1,16767
	 * @throws ParsingException if record cannot be parsed.
	 */
	public RwisRec(String line, RwisHeader header) throws ParsingException {
		raw_rec = line;
		create_time = TimeSteward.currentTimeMillis();
		String[] fs = parseFields(line);
		site_id = header.getField(fs, "Siteid");
		obs_time = parseDateTime(header.getField(fs, "DtTm"));
		air_temp = parseTemp(header.getField(fs, "AirTemp"));
		dew_point = parseTemp(header.getField(fs, "Dewpoint"));
		rel_humidity = parseInt(header.getField(fs, "Rh"));
		wind_speed_avg = parseMph(header.getField(fs, "SpdAvg"));
		wind_speed_gust = parseMph(header.getField(fs, "SpdGust"));
		wind_dir_min = parseInt(header.getField(fs, "DirMin"));
		wind_dir_avg = parseInt(header.getField(fs, "DirAvg"));
		wind_dir_max = parseInt(header.getField(fs, "DirMax"));
		air_pressure = parseInt(header.getField(fs, "Pressure"));
		precip_intens = header.getField(fs, "PcIntens");
		precip_type = header.getField(fs, "PcType");
		precip_rate = parseInt(header.getField(fs, "PcRate"));
		precip_accum = parseMm(header.getField(fs, "PcAccum"));
		visibility = parseFt(header.getField(fs, "Visibility"));
	}

	/** To string */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(RwisRec: ").append(raw_rec);
		sb.append(" create_time=").append(new Date(create_time));
		sb.append(", site_id=").append(site_id);
		sb.append(", obs_time=").append(
			obs_time == null ? "null" : new Date(obs_time));
		sb.append(", air_temp=").append(air_temp);
		sb.append(", dew_point=").append(dew_point);
		sb.append(", rel_humidity=").append(rel_humidity);
		sb.append(", wind_speed_avg=").append(wind_speed_avg);
		sb.append(", wind_speed_gust=").append(wind_speed_gust);
		sb.append(", wind_dir_min=").append(wind_dir_min);
		sb.append(", wind_dir_avg=").append(wind_dir_avg);
		sb.append(", wind_dir_max=").append(wind_dir_max);
		sb.append(", air_pressure=").append(air_pressure);
		sb.append(", precip_intens=").append(precip_intens);
		sb.append(", precip_type=").append(precip_type);
		sb.append(", precip_rate=").append(precip_rate);
		sb.append(", precip_accum=").append(precip_accum);
		sb.append(", visibility=").append(visibility);
		sb.append(")");
		return sb.toString();
	}

	/** Check if the record is expired */
	public boolean isExpired() {
		return create_time + durationMs() <
			TimeSteward.currentTimeMillis();
	}

	/** Store the record */
	public void store(WeatherSensorImpl ws) {
		updateAirTemp(ws);
		updateWindSpeed(ws);
		updateWindDir(ws);
		updateAccumulation(ws);
		updateVisibility(ws);
		ws.setStampNotify(create_time);
		SsiPoller.slog("stored rec=" + this);
	}

	/** Update the air temp */
	private void updateAirTemp(WeatherSensorImpl ws) {
		if (air_temp != null)
			ws.setAirTempNotify(air_temp.round(CELSIUS));
		else
			ws.setAirTempNotify(null);
	}

	/** Update the wind speed */
	private void updateWindSpeed(WeatherSensorImpl ws) {
		if (wind_speed_avg != null)
			ws.setWindSpeedNotify(wind_speed_avg.round(KPH));
		else
			ws.setWindSpeedNotify(null);
	}

	/** Update the wind dir */
	private void updateWindDir(WeatherSensorImpl ws) {
		ws.setWindDirRoundNotify(wind_dir_avg);
	}

	/** Update the weather sensor precip accumulation */
	private void updateAccumulation(WeatherSensorImpl ws) {
		if (precip_accum != null && precip_accum.value >= 0) {
			ws.updateAccumulation(precip_accum.round(MICROMETERS),
				create_time);
		} else
			ws.updateAccumulation(null, create_time);
	}

	/** Update the weather sensor visibility */
	private void updateVisibility(WeatherSensorImpl ws) {
		if (visibility != null && visibility.value >= 0)
			ws.setVisibilityNotify(visibility.round(METERS));
		else
			ws.setVisibilityNotify(null);
	}
}
