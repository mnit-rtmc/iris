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

import java.io.IOException;
import java.util.HashMap;
import us.mn.state.dot.tms.server.WeatherSensorImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to read the SSI file.
 *
 * @author Michael Darter
 */
public class OpRead extends OpDevice {

	/** Weather sensor to read */
	private final WeatherSensorImpl sensor;

	/** RWIS site ID */
	private final String site_id;

	/** Mapping of site_id to most recent RWIS records */
	private final HashMap<String, RwisRec> records;

	/** Create a new device operation */
	protected OpRead(WeatherSensorImpl ws, HashMap<String, RwisRec> recs) {
		super(PriorityLevel.DATA_30_SEC, ws);
		records = recs;
		sensor = ws;
		site_id = sensor.getName();
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
		return new PhaseCheck();
	}

	/** Phase to check records mapping */
	private class PhaseCheck extends Phase {

		/** Check the records mapping */
		protected Phase poll(CommMessage cm) {
			RwisRec rec = records.get(site_id);
			if(rec == null || rec.isExpired()) {
				// Add a null mapping for site_id
				records.put(site_id, null);
				return new PhaseRead();
			} else
				return new PhaseUpdate();
		}
	}

	/** Phase to read the file */
	private class PhaseRead extends Phase {

		/** Execute the phase
		 * @throws IOException received from queryProps call. */
		protected Phase poll(CommMessage cm) throws IOException {
			SsiMessage m = (SsiMessage)cm;
			m.add(new SsiProperty(records));
			m.queryProps();
			return new PhaseUpdate();
		}
	}

	/** Phase to update the sensor */
	private class PhaseUpdate extends Phase {

		/** Update the sensor */
		protected Phase poll(CommMessage cm) throws IOException {
			RwisRec rec = records.get(site_id);
			if(rec == null || rec.isExpired()) {
				rec = new RwisRec(site_id, new RwisHeader());
				records.put(site_id, rec);
			}
			rec.store(sensor);
			return null;
		}
	}
}
