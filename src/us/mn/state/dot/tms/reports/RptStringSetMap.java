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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Manages a collection of named rows of data for
 *  the report generator.<p>
 * 
 * This class provides a uniform way to convert a collection
 * of named rows of data to/from a serialization String.
 * 
 * @author John L. Stanley - SRF Consulting
 */

@SuppressWarnings("serial")
public class RptStringSetMap extends LinkedHashMap<String,RptStringSet> {
	
	public RptStringSetMap() {
		super();
	}

	public RptStringSetMap(String fromString) throws IOException {
		super();
		initFromCompositeString(fromString.trim());
	}

	enum state {
		PRE_BRACE,
		IN_NAME,
		POST_NAME,
		IN_TOKEN,
		AFTER_TOKEN
	}

	public void add(RptStringSet rss) {
		String name = rss.getName();
		this.put(name, rss);
	}
	
	/** Initializes RptStringSetMap from a serialization string.
	 * (generates a series of RptStringSet objects) */
	public void initFromCompositeString(String compositeStr) throws IOException {
		clear();
		boolean escape = false;
		state st = state.PRE_BRACE;
		StringBuilder token = new StringBuilder();
		RptStringSet rss = null;
		for (char ch : compositeStr.toCharArray()) {
			if (!escape) {
				if (ch == '\\') {
					escape = true;
					continue;
				}
				switch (st) {
					case PRE_BRACE:
						if (ch == '{')
							st = state.IN_NAME;
						continue; // ignore any other characters

					case IN_NAME:
						if (ch == ':') {
							if (token.length() == 0)
								throw new IOException("Empty RptStringSet name");

							// apply name to the set
							String name = token.toString();
							rss = new RptStringSet(name);
							token.setLength(0);
							st = state.POST_NAME;
							continue;
						}
						break; // add character to token

					case POST_NAME:
						if (ch == ' ') {
							// ignore space after separating colon
							st = state.IN_TOKEN;
							continue;
						}
						throw new IOException("Missing space after RptStringSet colon");
						
					case IN_TOKEN:
						if (ch == '}')
							st = state.PRE_BRACE;
						else if (ch == ';')
							st = state.AFTER_TOKEN;
						else 
							break; // add character to token

						// add token to the set
						rss.add(token.toString());
						token.setLength(0);
						
						// if we're done with this set, add it to the map
						if (st == state.PRE_BRACE) {
							this.add(rss);
							rss = null;
						}
						continue;

					case AFTER_TOKEN:
						if (ch == ' ') {
							// ignore space after separating semicolon
							st = state.IN_TOKEN;
							continue;
						}
						throw new IOException("Missing space after RptStringSet semicolon");
				}
			}
			// add character to the token
			token.append(ch);
			escape = false;
		}
	}
	
	/** Converts RptStringSetMap to a composite string.
	 * @return composite-string
	 */
	public String toCompositeString() {
		StringBuilder sb = new StringBuilder();
		RptStringSet rss;
		for (Map.Entry<String, RptStringSet> entry : this.entrySet()) {
			rss = entry.getValue();
			sb.append(rss.toString());
		}
		return sb.toString();
	}
}
