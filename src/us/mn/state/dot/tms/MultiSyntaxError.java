/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2015  Minnesota Department of Transportation
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
 * Enumeration of MULTI syntax errors.  This is specified by NTCIP 1203, as part
 * of the MULTI markup language.
 *
 * @author Douglas Lau
 */
public enum MultiSyntaxError {
	undefined,		/*  0     */
	other,			/*  1     */
	none,			/*  2     */
	unsupportedTag,		/*  3     */
	unsupportedTagValue,	/*  4     */
	textTooBig,		/*  5     */
	fontNotDefined,		/*  6     */
	characterNotDefined,	/*  7     */
	fieldDeviceNotExist,	/*  8     */
	fieldDeviceError,	/*  9     */
	flashRegionError,	/* 10     */
	tagConflict,		/* 11     */
	tooManyPages,		/* 12     */
	fontVersionID,		/* 13  V2 */
	graphicID,		/* 14  V2 */
	graphicNotDefined;	/* 15  V2 */

	/** Get MULTI syntax error from an ordinal value */
	static public MultiSyntaxError fromOrdinal(int o) {
		MultiSyntaxError v[] = values();
		if (o >= 0 && o < v.length)
			return v[o];
		else
			return undefined;
	}
}
