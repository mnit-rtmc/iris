/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2017  Minnesota Department of Transportation
 * Copyright (C) 2012  Iteris Inc.
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
package us.mn.state.dot.tms;

/**
 * Report generator enumeration.
 *
 * @author John L. Stanley - SRF Consulting
 */
public enum RptGenEnum {

	RPTGEN_SIGN_EVENTS("Sign Events");

	/** Create a new comm protocol value */
	private RptGenEnum(String guiName) {
		this.guiName = guiName;
	}

	/** Protocol description */
	private final String guiName;

	/** Get the string representation */
	//FIXME: Add i18n method for naming the report (needed once we can select different reports).
	public String getGuiName() {
		return guiName;
	}

	/** Values array */
	static private final RptGenEnum[] VALUES = values();

	/** Get a report generator name from an ordinal value */
	static public RptGenEnum fromOrdinal(short o) {
		if (o >= 0 && o < VALUES.length)
			return VALUES[o];
		else
			return null;
	}
}
