/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.org815;

import java.io.IOException;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Version property
 *
 * @author Douglas Lau
 */
public class VersionProperty extends Org815Property {

	/** Property value */
	private String value = "";

	/** Get value of property */
	public String getValue() {
		return value;
	}

	/** Get a string value of the property */
	@Override
	public String toString() {
		return "version: " + value;
	}

	/** Get the QUERY request byte code */
	@Override
	protected byte requestQueryByte() {
		return (char)'V';
	}

	/** Parse a QUERY response */
	@Override
	protected void parseQuery(String line) throws IOException {
		if (line.length() > 32)
			throw new ParsingException("Invalid version: " + line);
		value = line.trim();
	}
}
