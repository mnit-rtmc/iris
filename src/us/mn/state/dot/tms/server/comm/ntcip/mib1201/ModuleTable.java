/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2022-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ntcip.mib1201;

import java.util.ArrayList;
import static us.mn.state.dot.tms.server.comm.ntcip.mib1201.MIB1201.*;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Enum;
import us.mn.state.dot.tms.server.comm.snmp.ASN1Integer;
import us.mn.state.dot.tms.server.comm.snmp.ASN1String;
import us.mn.state.dot.tms.utils.Json;

/**
 * Module table, where each table row contains a hardware/software module.
 *
 * @author Douglas Lau
 */
public class ModuleTable {

	/** Maximum length of string values (characters) */
	static private final int MAXLEN = 64;

	/** Trim and truncate a string, with null checking.
	 * @param s String to be truncated.
	 * @return Trimmed, truncated string, or null. */
	static private String trimTruncate(ASN1String s) {
		String value = s.getValue();
		String v = value.trim();
		if (v.length() > 0) {
			return (v.length() <= MAXLEN)
			      ? v
			      : v.substring(0, MAXLEN);
		}
		return null;
	}

	/** Module row */
	static public class Row {
		public final ASN1String make;
		public final ASN1String model;
		public final ASN1String version;
		public final ASN1Enum<ModuleType> m_type;

		/** Create a table row */
		private Row(int row) {
			make = moduleMake.makeStr(row);
			model = moduleModel.makeStr(row);
			version = moduleVersion.makeStr(row);
			m_type = new ASN1Enum<ModuleType>(ModuleType.class,
				moduleType.node, row);
		}

		/** Get JSON representation */
		private String toJson() {
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			sb.append(Json.str("make", trimTruncate(make)));
			sb.append(Json.str("model", trimTruncate(model)));
			sb.append(Json.str("version", trimTruncate(version)));
			// remove trailing comma
			if (sb.charAt(sb.length() - 1) == ',')
				sb.setLength(sb.length() - 1);
			sb.append('}');
			return sb.toString();
		}
	}

	/** Number of modules in table */
	public final ASN1Integer modules = globalMaxModules.makeInt();

	/** Check if all rows have been read */
	public boolean isDone() {
		return table_rows.size() >= size();
	}

	/** Get number of modules in table */
	private int size() {
		return modules.getInteger();
	}

	/** Rows in table */
	private final ArrayList<Row> table_rows = new ArrayList<Row>();

	/** Add a row to the table */
	public Row addRow() {
		Row tr = new Row(table_rows.size() + 1);
		table_rows.add(tr);
		return tr;
	}

	/** Get software version (first software module in table) */
	private String getVersion() {
		for (Row row : table_rows) {
			if (row.m_type.getEnum() == ModuleType.software)
				return trimTruncate(row.version);
		}
		return null;
	}

	/** Get JSON representation */
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		sb.append(Json.arr("hw", typeRowsJson(ModuleType.hardware)));
		sb.append(Json.arr("sw", typeRowsJson(ModuleType.software)));
		sb.append(Json.str("version", getVersion()));
		// remove trailing comma
		if (sb.charAt(sb.length() - 1) == ',')
			sb.setLength(sb.length() - 1);
		sb.append('}');
		return sb.toString();
	}

	/** Get all module rows of the given type as JSON */
	private String[] typeRowsJson(ModuleType m_type) {
		ArrayList<String> rows = new ArrayList<String>();
		for (Row row : table_rows) {
			if (row.m_type.getEnum() == m_type)
				rows.add(row.toJson());
		}
		return rows.toArray(new String[0]);
	}
}
