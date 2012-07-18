/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  AHMCT, University of California
 * Copyright (C) 2012  Minnesota Department of Transportation
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

import java.util.Date;
import us.mn.state.dot.tms.Temperature;
import us.mn.state.dot.tms.WeatherSensorHelper;
import us.mn.state.dot.tms.server.WeatherSensorImpl;
import us.mn.state.dot.tms.utils.SCsv;
import us.mn.state.dot.tms.utils.SString;

/**
 * SSI record.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class RwisRec {

	/** Number of expected fields per line */
	static private final int NUM_FIELDS = 16;

	/** Convert missing values (negative) to null */
	static private Integer nmnCheck(Integer v) {
		return v != null && v >= 0 ? v : null;
	}

	/** Convert missing values (negative) to null */
	static private Long nmnCheck(Long v) {
		return v != null && v >= 0 ? v : null;
	}

	/** Convert missing values (null, -1, or empty) to null */
	static private String nmnCheck(String v) {
		return v == null || v.equals("-1") || v.isEmpty() ? null : v;
	}

	/** Validity indicator */
	private final boolean valid_rec;

	/** Raw record, as a line of CSV text. */
	private final String raw_rec;

	/** Rwis site id */
	private String site_id = "";

	/** Creation time */
	private final long create_time;

	/** Observation time, read from file, null if missing */
	private Long obs_time;

	/** Visibility in ft or null if missing */
	private Integer obs_vis;

	/** Wind speed in mph or null if missing */
	private Integer obs_wind_speed;

	/** Air temperature */
	private Temperature obs_air_temp = new Temperature();

	/** Dew point or null if missing */
	private Integer obs_dew_point;

	/** Relative humidity or null if missing */
	private Integer obs_rel_humidity;

	/** Wind speed in MPH or null if missing */
	private Integer obs_speed_gust;

	/** Wind direction in degrees or null if missing */
	private Integer obs_dir_min;

	/** Average wind direction in degrees or null if missing */
	private Integer obs_dir_avg;

	/** Wind direction max in degrees or null if missing  */
	private Integer obs_dir_max;

	/** Pressure or null if missing */
	private int obs_pressure;

	/** Precip intensity or null if missing */
	private String obs_precip_intens;

	/** Precipitation type or null if missing */
	private String obs_precip_type;

	/* Precipitation rate in mm/hr or null if missing */
	private Integer obs_precip_rate;

	/** Precipitation accumulation or null if missing */
	private Integer obs_precip_accum;

	/** Constructor
	 * @param line A single text line (record) */
	public RwisRec(String line) {
		raw_rec = new String(line);
		create_time = new Date().getTime();
		valid_rec = parse();
	}

	/** Parse the raw record that contains an ssi record into fields.
	 * For example:<p>
	 *	365000,02/09/2010 15:52:16,820,-2130,10,2,14,,5,6,
	 *		65535,Other,Other,-1,-1,16767<p>
	 * @return True if the record is valid else false. */
	private boolean parse() {
		if(raw_rec == null)
			return false;
		String[] fs = SCsv.separate(raw_rec);
		if(fs.length != NUM_FIELDS) {
			SsiPoller.log("bogus number of fields read=" +
				fs.length + ", expected=" + NUM_FIELDS);
			return false;
		}
		if(headerRow(fs)) {
			SsiPoller.log("read header row");
			return false;
		}
		// FIXME: parse fields by named column order
		site_id = SCsv.getFieldString(fs, 0);
		obs_time = nmnCheck(SCsv.getFieldTime(fs, 1));
		obs_air_temp = parseAirTemp(SCsv.getFieldString(fs, 2));
		obs_dew_point = nmnCheck(SCsv.getFieldInt(fs, 3));
		obs_rel_humidity = nmnCheck(SCsv.getFieldInt(fs, 4));
		obs_wind_speed = nmnCheck(SCsv.getFieldInt(fs, 5));
		obs_speed_gust = nmnCheck(SCsv.getFieldInt(fs, 6));
		obs_dir_min = nmnCheck(SCsv.getFieldInt(fs, 7));
		obs_dir_avg = nmnCheck(SCsv.getFieldInt(fs, 8));
		obs_dir_max = nmnCheck(SCsv.getFieldInt(fs, 9));
		obs_pressure = nmnCheck(SCsv.getFieldInt(fs, 10));
		obs_precip_intens = nmnCheck(SCsv.getFieldString(fs, 11));
		obs_precip_type = nmnCheck(SCsv.getFieldString(fs, 12));
		obs_precip_rate = nmnCheck(SCsv.getFieldInt(fs, 13));
		obs_precip_accum = nmnCheck(SCsv.getFieldInt(fs, 14));
		obs_vis = nmnCheck(SCsv.getFieldInt(fs, 15));
		SsiPoller.log("parsed rec=" + this);
		return true;
	}

	/** Parse air temperature */
	private Temperature parseAirTemp(String field) {
		if(field == null || field.isEmpty())
			return new Temperature();
		else {
			double t = SString.stringToDouble(field) / 100;
			return new Temperature(t);
		}
	}

	/** Is the row the header row? For example: Siteid, DtTm, AirTemp,
	 * Dewpoint, Rh, SpdAvg, SpdGust, DirMin, DirAvg, DirMax, Pressure,
	 * PcIntens, PcType, PcRate, PcAccum, Visibility. */
	private boolean headerRow(String[] fs) {
		return "Siteid".equals(SCsv.getFieldString(fs, 0));
	}

	/** Is record valid? */
	public boolean valid() {
		return valid_rec;
	}

	/** To string */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("(RwisRec: ");
		sb.append("id=").append(site_id);
		sb.append(", valid=").append(valid_rec);
		sb.append(", create_time=").append(new Date(create_time));
		sb.append(", obs_time=").append(
			obs_time == null ? "null" : new Date(obs_time));
		sb.append(", wspd=").append(obs_wind_speed);
		sb.append(", vis=").append(obs_vis);
		sb.append(", airtemp_F=").append(obs_air_temp.toString());
		sb.append(", dewp=").append(obs_dew_point);
		sb.append(", relhum=").append(obs_rel_humidity);
		sb.append(", wsgust=").append(obs_speed_gust);
		sb.append(", wdirmin=").append(obs_dir_min);
		sb.append(", wdiravg=").append(obs_dir_avg);
		sb.append(", wdirmax=").append(obs_dir_max);
		sb.append(", pressure=").append(obs_pressure);
		sb.append(", precip_intens=").append(obs_precip_intens);
		sb.append(", precip_type=").append(obs_precip_type);
		sb.append(", precip_rate=").append(obs_precip_rate);
		sb.append(", precip_accum=").append(obs_precip_accum);
		sb.append(", raw_rec=").append(raw_rec);
		sb.append(")");
		return sb.toString();
	}

	/** Store the record */
	public void store() {
		if(!valid())
			return;
		WeatherSensorImpl ws = find(site_id);
		if(ws == null) {
			SsiPoller.log("No weather sensor defined " +
				"for id=" + site_id + ", observation " +
				"ignored, rec=" + this);
		} else {
			SsiPoller.log("stored rec=" + this);
			ws.store(obs_vis, obs_wind_speed,
				obs_air_temp.toCInteger(), obs_dir_avg,
				obs_precip_rate);
		}
	}

	/** Find the weather station by id.
	 * @param n Name of weather station, may be null.
	 * @return weather station w/ specified name or null if not found. */
	private WeatherSensorImpl find(String n) {
		if(n == null)
			return null;
		return (WeatherSensorImpl)WeatherSensorHelper.lookup(n);
	}
}
