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
package us.mn.state.dot.tms.server;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.PrecipSituation;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.WeatherSensorHelper;
import us.mn.state.dot.tms.units.Pressure;
import us.mn.state.dot.tms.units.Speed;
import us.mn.state.dot.tms.utils.SString;

/**
 * Write SSI CSV weather export files.
 *
 * @author Michael Darter
 */
public class WeatherSensorCsvWriter extends XmlWriter {

	/** CSV file name */
	static private final String OUTPUT_FNAME_1 = "weather_sensor1.csv";

	/** CSV file name */
	static private final String OUTPUT_FNAME_2 = "weather_sensor2.csv";

	/** File type to generate */
	final private int f_type;

	/** Return the specified date as a string in local time.
	 * @param stamp A time stamp, null or < 0 for missing
	 * @return A string in local time as MM-dd-yyyy HH:mm:ss */
	static private String formatDate(Long stamp) {
		if (stamp == null || stamp < 0)
			return "";
		Date d = new Date(stamp);
		return new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(d);
	}

	/* Factory to create a new CSV file writer and write the file.
	 * @arg ft File type
	 * @returns Null on error or a new file writer */
	static public WeatherSensorCsvWriter createWrite(int ft) 
		throws IOException
	{
		WeatherSensorCsvWriter wsw = null;
		// atmospheric data
		if (ft == 1)
			wsw = new WeatherSensorCsvWriter(OUTPUT_FNAME_1, 1);
		// surface data
		else if (ft == 2)
			wsw = new WeatherSensorCsvWriter(OUTPUT_FNAME_2, 2);
		if (wsw != null)
			wsw.write();
		return wsw;
	}

	/** Constructor */
	public WeatherSensorCsvWriter(String fn, int ft) {
		super(fn, true);
		f_type = ft;
	}

	/** Write the weather sensor CSV file */
	@Override protected void write(Writer w) throws IOException {
		writeHead(w);
		writeBody(w);
	}

	/** Write the head of the CSV file */
	private void writeHead(Writer wr) throws IOException {
		if (f_type == 1) {
			writeLine(wr, "Siteid,DtTm,AirTemp,Dewpoint,Rh," + 
				"SpdAvg,SpdGust,DirMin,DirAvg,DirMax," + 
				"Pressure,PcIntens,PcType,PcRate,PcAccum," + 
				"Visibility");
		} else if (f_type == 2) {
			writeLine(wr, "Siteid,senid,DtTm,sfcond,sftemp," + 
				"frztemp,chemfactor,chempct,depth,icepct," + 
				"subsftemp,waterlevel");
		}
	}

	/** Write the body of the XML file */
	private void writeBody(Writer wr) throws IOException {
		Iterator<WeatherSensor> it = WeatherSensorHelper.iterator();
		while(it.hasNext()) {
			WeatherSensor ws = it.next();
			if(ws instanceof WeatherSensorImpl)
				writeLine(wr, (WeatherSensorImpl)ws);
		}
	}

	/** Convert a temperature to an NTCIP temperature.
	 * @arg v Temperature in C or null if missing.
	 * @return Temperature as tenths of a degree C or 1001 for missing. */
	private String tToN(Integer v) {
		if (v != null) {
			int i = v.intValue() * 10;
			return String.valueOf(i);
		} else {
			return "1001"; // missing
		}
	}

	/** Convert pavement surface status to NTCIP string
	 * @arg w Weather sensor
	 * @return Pavement surface status description or empty if missing. */
	private String pssToN(WeatherSensorImpl w) {
		String v = SString.splitCamel(WeatherSensorHelper.
			getPvmtSurfStatus(w).description);
		return (v.equals("???") ? "" : v);
	}

	/** Convert pressure in pascals to NTCIP pressure.
	 * @arg v Pressure in Pascals or null.
	 * @return Pressure Pressure in 1/10ths of millibar, which are tenths 
	 *                  of hectoPascal or 65535 if missing. */
	private String prToN(Integer v) {
		if (v != null) {
			return String.valueOf(
				new Pressure((double)v).ntcip());
		} else
			return "65535";
	}

	/** Convert precip rate to NTCIP units (essPrecipRate).
	 * @arg v Precip rate in mm/hr, null for missing.
	 * @return Precip rate as .36 mm/hr or 65535 for missing. */
	private String praToN(Integer v) {
		if (v != null)
			return String.valueOf(Math.round((double)v / .36));
		else
			return "65535";
	}

	/** Convert distance to NTCIP units (e.g. essVisibility).
	 * @arg v Distance in meters, null for missing.
	 * @return Distance in tenths of a meter or 1000001 for missing. */
	private String dToN(Integer v) {
		return (v != null ? String.valueOf(v * 10) : "65535");
	}

	/** Convert precipitation accumulation to NTCIP units.
	 * @arg v Precip accum in mm, null for missing.
	 * @return Precip accumulation in tenths of mm or 65535 for 
	 *         missing. See essPrecipitationOneHour */
	private String pToN(Integer v) {
		return (v != null ? String.valueOf(v * 10) : "65535");
	}

	/** Convert speed to NTCIP units.
	 * @arg v Speed in KPH, null for missing.
	 * @return Speed in tenths of a meter per second or 65535 
	 *         for missing. See essAvgWindSpeed. */
	private String sToN(Integer v) {
		if (v != null)
			return String.valueOf(new Speed((double)v).ntcip());
		else
			return "65535";
	}

	/** Append a CSV value to a StringBuffer */
	private StringBuilder append(StringBuilder sb, String value) {
		if (value != null)
			sb.append(value);
		sb.append(",");
		return sb;
	}

	/** Append a CSV value to a StringBuffer */
	private StringBuilder append(StringBuilder sb, Integer value) {
		if (value != null)
			sb.append(value);
		sb.append(",");
		return sb;
	}

	/** Append a CSV value to a StringBuffer */
	private StringBuilder append(StringBuilder sb, Long value) {
		if (value != null)
			sb.append(value);
		sb.append(",");
		return sb;
	}

	/** Append a CSV value to a StringBuffer */
	private StringBuilder append(StringBuilder sb, Double value) {
		if (value != null)
			sb.append(value);
		sb.append(",");
		return sb;
	}

	/** Append a CSV value to a StringBuffer */
	private StringBuilder append(StringBuilder sb, PrecipSituation value) {
		if (value != null && value != PrecipSituation.UNDEFINED)
			sb.append(value);
		sb.append(",");
		return sb;
	}

	/** Write a terminated line */
	private void writeLine(Writer wr, String line) 
		throws IOException
	{
		if (line != null)
			wr.write(line + "\n");
	}

	/** Write a CSV line */
	private void writeLine(Writer wr, WeatherSensorImpl w) 
		throws IOException
	{
		if (w == null)
			return;
		if (f_type == 1)
			writeLine1(wr, w);
		else if (f_type == 2)
			writeLine2(wr, w);
	}

	/** Write a CSV line for the atmospheric file */
	private void writeLine1(Writer wr, WeatherSensorImpl w) 
		throws IOException
	{
		StringBuilder sb = new StringBuilder();
		append(sb, w.getName());		//Siteid
		append(sb, formatDate(w.getStamp()));	//DtTm
		append(sb, tToN(w.getAirTemp()));	//AirTemp
		append(sb, tToN(w.getDewPointTemp()));	//Dewpoint
		append(sb, w.getHumidity());		//Rh
		append(sb, sToN(w.getWindSpeed()));	//SpdAvg
		append(sb, sToN(
			w.getMaxWindGustSpeed()));	//SpdGust
		append(sb, "");				//DirMin
		append(sb, w.getWindDir());		//DirAvg
		append(sb, w.getMaxWindGustDir());	//DirMax
		append(sb, prToN(w.getPressure()));	//Pressure
		append(sb, WeatherSensorHelper.
			getPrecipRateIntensity(w));	//PcIntens
		append(sb, WeatherSensorHelper.
			getPrecipSituation(w));		//PcType
		append(sb, praToN(w.getPrecipRate()));	//PcRate
		append(sb, pToN(w.getPrecipOneHour()));	//PcAccum
		append(sb, dToN(w.getVisibility()));	//Visibility
		sb.setLength(sb.length() - 1);
		writeLine(wr, sb.toString());
	}

	/** Write a CSV line for the surface file */
	private void writeLine2(Writer wr, WeatherSensorImpl w) 
		throws IOException
	{
		StringBuilder sb = new StringBuilder();
		append(sb, w.getName());		//Siteid
		append(sb, "0");			//senid
		append(sb, formatDate(w.getStamp()));	//DtTm
		append(sb, pssToN(w));			//sfcond
		append(sb, tToN(w.getPvmtSurfTemp()));	//sftemp
		append(sb, tToN(w.getSurfFreezeTemp()));//frztemp
		append(sb, "101");			//chemfactor
		append(sb, "101");			//chempct
		append(sb, "32767");			//depth
		append(sb, "101");			//icepct
		append(sb, tToN(w.getSubSurfTemp()));	//subsftemp
		append(sb, "");				//waterlevel
		sb.setLength(sb.length() - 1);
		writeLine(wr, sb.toString());
	}
}
