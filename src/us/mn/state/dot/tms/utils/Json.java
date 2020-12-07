/*
 * IRIS -- Intelligent Roadway Information System
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
package us.mn.state.dot.tms.utils;

/**
 * JSON writer.
 *
 * @author Douglas Lau
 */
public class Json {

	/** Make a JSON key/value pair for a number type */
	static public String num(String key, Object value) {
		if (value != null) {
			StringBuilder sb = new StringBuilder();
			sb.append('"');
			sb.append(key);
			sb.append("\":");
			sb.append(value);
			sb.append(',');
			return sb.toString();
		} else
			return "";
	}

	/** Make a JSON key/value pair for a string type */
	static public String str(String key, Object value) {
		if (value != null) {
			StringBuilder sb = new StringBuilder();
			sb.append('"');
			sb.append(key);
			sb.append("\":\"");
			sb.append(value);
			sb.append("\",");
			return sb.toString();
		} else
			return "";
	}
	
	/** Make a JSON key/array pair for a string type */
	static public String arr(String key, Object[] values) {
		if (values != null && values.length > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append('"');
			sb.append(key);
			sb.append("\":");
			sb.append('[');
			for (Object v: values) {
				sb.append('"');
				sb.append(v);
				sb.append("\",");
			}
			// remove trailing comma and add closing bracket
			if (sb.charAt(sb.length() - 1) == ',')
				sb.setLength(sb.length() - 1);
			sb.append("],");
			return sb.toString();
		} else
			return "";
	}
	
	/** Make a JSON key/sub object for a string type */
	static public String sub(String key, Object value) {
		if (value != null) {
			StringBuilder sb = new StringBuilder();
			sb.append('"');
			sb.append(key);
			sb.append("\":");
			sb.append(value);
			sb.append("");
			return sb.toString();
		} else
			return "";
	}
}
