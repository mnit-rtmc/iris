/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cap;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import org.json.JSONObject;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.CommProtocol;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.server.CapAlert;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Container for a CAP property.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class CapProperty extends ControllerProperty {

	/** Date formatter for formatting error file names */
	static private final SimpleDateFormat DT_FMT =
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	/** Get save document enabled setting */
	static private boolean getSaveEnabled() {
		return SystemAttrEnum.CAP_SAVE_ENABLE.getBoolean();
	}

	/** Timer thread for CAP jobs */
	static private final Scheduler SCHED = new Scheduler("cap");

	/** Feed name */
	private final String feed;

	/** Alert parser */
	private final AlertParser parser;

	/** Create a new CAP property */
	public CapProperty(String fd, CommProtocol cp) {
		feed = fd;
		parser = (cp == CommProtocol.CAP_IPAWS)
		       ? new IpawsParser()
		       : new NwsParser();
	}

	/** Get a string representation of the property */
	@Override
	public String toString() {
		return "feed " + feed;
	}

	/** Decode a QUERY response */
	@Override
	public void decodeQuery(ControllerImpl c, InputStream is)
		throws IOException
	{
		List<JSONObject> alerts = parseAlerts(is);
		for (JSONObject alert : alerts) {
			processAlert(alert);
		}
	}

	/** Parse alerts document */
	private List<JSONObject> parseAlerts(InputStream is)
		throws IOException
	{
		ByteArrayOutputStream doc = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len;
		while ((len = is.read(buf)) > -1)
			doc.write(buf, 0, len);
		doc.flush();
		try {
			return parser.parse(doc.toString("UTF-8"));
		}
		catch (ParsingException e) {
			CapPoller.slog("parse error: " + e.getMessage());
			if (getSaveEnabled())
				saveDoc(doc);
			throw e;
		}
	}

	/** Save the response document */
	private void saveDoc(ByteArrayOutputStream doc) throws IOException {
		String fn = "/var/log/iris/cap_err_" + DT_FMT.format(
			TimeSteward.getDateInstance()) + ".xml";
		doc.writeTo(new FileOutputStream(fn));
	}

	/** Process one alert */
	private void processAlert(JSONObject alert) {
		String id = alert.getString("identifier");
		if (id != null) {
			CapAlert ca = new CapAlert(id, alert);
			SCHED.addJob(new Job() {
				public void perform() {
					ca.process();
				}
			});
		} else
			CapPoller.slog("identifier not found!");
	}
}
