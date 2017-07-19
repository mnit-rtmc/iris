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
import java.util.Iterator;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.WeatherSensorHelper;

/**
 * Write the current weather sensors and attributes to an XML file.
 *
 * @author Michael Darter
 */
public class WeatherSensorXmlWriter extends XmlWriter {

	/** XML file */
	static private final String WEATHER_SENSOR_XML = "weather_sensor.xml";

	/** Constructor */
	public WeatherSensorXmlWriter() {
		super(WEATHER_SENSOR_XML, true);
	}

	/** Write the weather sensor XML file */
	@Override protected void write(Writer w) throws IOException {
		writeHead(w);
		writeBody(w);
		writeTail(w);
	}

	/** Write the head of the XML file */
	private void writeHead(Writer w) throws IOException {
		w.write(XML_DECLARATION);
		writeDtd(w);
		w.write("<weather_sensors time_stamp='" +
			TimeSteward.getDateInstance() + "'>\n");
	}

	/** Write the DTD */
	private void writeDtd(Writer w) throws IOException {
		w.write("<!DOCTYPE weather_sensors [\n");
		w.write("<!ELEMENT weather_sensors (weather_sensor)*>\n");
		w.write("<!ATTLIST weather_sensors " + 
			"time_stamp CDATA #IMPLIED>\n");
		w.write("<!ELEMENT weather_sensor EMPTY>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"WeatherSensor CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"name CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"description CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"lon CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"lat CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"air_temp_c CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"humidity_perc CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"dew_point_temp_c CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"max_temp_c CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"min_temp_c CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"avg_wind_speed_kph CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"wind_gust_speed_kph CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"wind_gust_dir_degs CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"avg_wind_dir_degs CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"spot_wind_speed_kph CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"spot_wind_dir_degs CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"precip_rate_mmhr CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"precip_situation CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"precip_1h CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"visibility_m CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"atmos_pressure_pa CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"pvmt_surf_temp CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"surf_temp_c CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"pvmt_surf_status CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"surf_freeze_temp_c CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"subsurf_temp_c CDATA #IMPLIED>\n");
		w.write("<!ATTLIST weather_sensor " + 
			"time_stamp CDATA #IMPLIED>\n");
		w.write("]>\n");
	}

	/** Write the body of the XML file */
	private void writeBody(Writer w) throws IOException {
		Iterator<WeatherSensor> it = WeatherSensorHelper.iterator();
		while(it.hasNext()) {
			WeatherSensor ws = it.next();
			if(ws instanceof WeatherSensorImpl) {
				((WeatherSensorImpl)ws).
					writeWeatherSensorXml(w);
			}
		}
	}

	/** Write the tail of the XML file */
	private void writeTail(Writer w) throws IOException {
		w.write("</weather_sensors>\n");
	}
}
