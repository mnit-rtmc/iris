/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2018  SRF Consulting Group
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

package us.mn.state.dot.tms.reports;

import java.util.Iterator;
import java.util.LinkedHashSet;

/** Named row of data for report generator
 * 
 * @author John L. Stanley - SRF Consulting
 */

@SuppressWarnings("serial")
public class RptStringSet extends LinkedHashSet<String> {

	// name of this string-set
	private String name;

	public RptStringSet(String name) {
		super();
		this.name = name;
	}

	// prefix any reserved characters with '\'
	protected static String escape(String str) {
		StringBuilder sb = new StringBuilder();
		for (char ch : str.toCharArray()) {
			if ("\\{:;}".indexOf(ch) != -1)
				sb.append('\\');
			sb.append(ch);
		}
		return sb.toString();
	}
	
	@Override
	// create: "{<setName>: <str1>; <str2>; ... <strN>}\n"
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{" + name + ": ");
		Iterator<String> it = this.iterator();
		String str;
		while (it.hasNext()) {
			str = it.next();
			sb.append(escape(str));
			if (it.hasNext())
				sb.append("; ");
		}
		sb.append("}\n");
		return sb.toString();
	}
	
	public void name(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	
}
