/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022  Minnesota Department of Transportation
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

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import us.mn.state.dot.tms.server.DeviceImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;
import us.mn.state.dot.tms.server.comm.ntcip.mib1201.*;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1201.MIB1201.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1String;

/**
 * Operation to query the hardware/software modules.
 *
 * @author Douglas Lau
 */
public class OpQueryModules extends OpNtcip {

	/** Trim and truncate a string, with null checking.
	 * @param value String to be truncated (may be null).
	 * @param maxlen Maximum length of string (characters).
	 * @return Trimmed, truncated string, or null. */
	static private String trimTruncate(String value, int maxlen) {
		if (value != null) {
			String v = value.trim();
			if (v.length() > 0) {
				return (v.length() <= maxlen)
				      ? v
				      : v.substring(0, maxlen);
			}
		}
		return null;
	}

	/** Controller setup */
	private final JSONObject setup = new JSONObject();

	/** Create a new module query operation */
	public OpQueryModules(DeviceImpl d) {
		super(PriorityLevel.DOWNLOAD, d);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase phaseTwo() {
		return new QueryModuleCount();
	}

	/** Phase to query the number of modules */
	protected class QueryModuleCount extends Phase {

		/** Query the number of modules */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1Integer modules = globalMaxModules.makeInt();
			mess.add(modules);
			mess.queryProps();
			logQuery(modules);
			return new QueryModules(modules.getInteger());
		}
	}

	/** Phase to query the module information */
	protected class QueryModules extends Phase {

		/** Count of rows in the module table */
		private final int count;

		/** Module row number to query */
		private int mod = 1;

		/** Create a QueryModules phase */
		private QueryModules(int c) {
			count = c;
		}

		/** Query the module make, model and version */
		@SuppressWarnings("unchecked")
		protected Phase poll(CommMessage mess) throws IOException {
			ASN1String make = moduleMake.makeStr(mod);
			ASN1String model = moduleModel.makeStr(mod);
			ASN1String version = moduleVersion.makeStr(mod);
			ASN1Enum<ModuleType> m_type = new ASN1Enum<ModuleType>(
				ModuleType.class, moduleType.node, mod);
			mess.add(make);
			mess.add(model);
			mess.add(version);
			mess.add(m_type);
			mess.queryProps();
			logQuery(make);
			logQuery(model);
			logQuery(version);
			logQuery(m_type);
			if (m_type.getEnum() == ModuleType.hardware) {
				setSetup("hardware_make", make.getValue());
				setSetup("hardware_model", model.getValue());
				setSetup("hardware_version", version.getValue());
			}
			if (m_type.getEnum() == ModuleType.software) {
				setSetup("software_make", make.getValue());
				setSetup("software_model", model.getValue());
				setSetup("software_version", version.getValue());
			}
			mod += 1;
			return (mod <= count) ? this : null;
		}
	}

	/** Set a JSON setup value */
	private void setSetup(String key, String value) {
		try {
			setup.put(key, trimTruncate(value, 64));
		}
		catch (JSONException e) {
			// malformed JSON
			e.printStackTrace();
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			controller.setSetupNotify(setup.toString());
		super.cleanup();
	}
}
