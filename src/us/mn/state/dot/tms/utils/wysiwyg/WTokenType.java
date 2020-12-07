/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  SRF Consulting Group
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

package us.mn.state.dot.tms.utils.wysiwyg;

import us.mn.state.dot.tms.utils.MultiConfig;

/** Expanded list of all types of tags supported by IRIS
 * @author John L. Stanley - SRF Consulting
 */
public enum WTokenType {

	//---------------------------
	//=== Standard MULTI tags ===
	// {Bit numbers match bits in dmsSupportedMultiTags.}

	// [cb] [cbN]
	colorBackground(      0, "Message background color (deprecated)"),

	// [cf] [cfN] [cfR,G,B]
	colorForeground(      1, "Foreground color"),

//	// [fltXoY] [floYtX] (not supported by current version of IRIS)
//	flashing(             2, "Flashing"),

	// [fo] [foN] [foN,CCCC]
	font(                 3, "Font"),

	// [gN], [gN,X,Y] [gN,X,Y,CCCC]
	graphic(              4, "Graphic"),

//	// [hcX] (not supported by current version of IRIS)
//	hexChar(              5, "Hex character"),

	// [jl] [jlN]
	//    Multi.JustificationLine {UNDEFINED, OTHER, LEFT, CENTER, RIGHT, FULL}
	justificationLine(    6, "Line justification"),

	// [jp] [jpN] page justification
	//	   Multi.JustificationPage {UNDEFINED, OTHER, TOP, MIDDLE, BOTTOM}
	justificationPage(    7, "Page justification"),

//	// [msX,Y]  (not supported by current version of IRIS)
//	manufactTag(          8, "Manufacturer tag"),

//	// [mvtdw,s,r,text] (not supported by current version of IRIS)
//	movingText(           9, "Moving text"),

	// [nl] [nlN]
	newLine(             10, "New line"),

	// [np]
	newPage(             11, "New page"),

	// [pt] [ptN] [ptNoO]
	pageTime(            12, "Page timing"),

	// [scN] [/sc]
	spacingChar(         13, "Character spacing"),
	
// Field-data tags - (not supported by current version of IRIS)
	//	[f1] [f1,Y]
//	localTime12hr(       14, "Local hour - 12hr"),
	//	[f2] [f2,Y]
//	localTime24hr(       15, "Local hour - 24hr"),
	//	[f3] [f3,Y]
//	ambientTempCelsius  (16, "Ambient temp - Celsius"),
	//	[f4] [f4,Y]
//	ambientTempFarenheit(17, "Ambient temp - Fahrenheit"),
	//	[f5] [f5,Y]
//	speedKPH(            18, "Speed - KPH"),
	//	[f6] [f6,Y]
//	speedMPH(            19, "Speed - MPH"),
	//	[f7] [f7,Y]
//	dayOfWeek(           20, "Local day of week"),
	//	[f8] [f8,Y]
//	dateOfMonth(         21, "Local date of month"),
	//	[f9] [f9,Y]
//	monthOfYear(         22, "Local month"),
	//	[f10] [f10,Y]
//	year2digits(         23, "Local year - 2 digits"),
	//	[f11] [f11,Y]
//	year4digits(         24, "Local year - 4 digits"),
	//	[f12] [f12,Y]
//	time12_AMPM(         25, "Local time - AM/PM"),
	//	[f13] [f13,Y]
//	time12_ampm(         26, "Local time - am/pm"),

	// [trX,Y,W,H]
	textRectangle(       27, "Text rectangle"),

	// [crX,Y,W,H,N] [crX,Y,W,H,R,G,B]
	colorRectangle(      28, "Color rectangle"),

	// [pb] [pbN] [pbR,G,B]
	pageBackground(      29, "Page background color"),

	//--------------------------------------------------------------------
	//=== Special IRIS-specific tags (not part of NTCIP) ===

	// Modes for travel time over limit handling
	//    Multi.OverLimitMode {blank, prepend, append}

// [tts], [tts,m] or [tts,m,t]
//	/** Add a travel time destination.
//	 * @param stat_id Destination station ID.
//	 * @param mode Over limit mode.
//	 * @param o_txt Over limit text. */
	travelTime(              "Travel Time"),

//	/** Add a speed advisory */
	speedAdvisory(           "Speed Advisory"),
//
//	/** Add a slow traffic warning.
//	 * @param spd Highest speed to activate warning.
//	 * @param dist Distance to search for slow traffic (1/10 mile).
//	 * @param mode Tag replacement mode (none, dist or speed). */
	slowWarning(             "Slow Warning"),
//
//	/** Add a feed message */
	feedMsg(                 "Feed Message"),
//
//	/** Add a tolling message */
	tolling(                 "Tolling"),
//
//	/** Add parking area availability.
//	 * @param pid Parking area ID.
//	 * @param l_txt Text for low availability.
//	 * @param c_txt Text for closed area. */
	parkingAvail(            "Parking Availability"),
//
//	/** Add an incident locator */
	incidentLoc(             "Incident Locator"),
	
	/** Add an alert time (start or end) field from a CAP message */
	capTime(                 "CAP Time"),
	
	/** Add an alert response type field from a CAP message */
	capResponse(             "CAP Response Type"),
	
	/** Add an alert urgency field from a CAP message */
	capUrgency(              "CAP Urgency"),

	//-------------------------------------
	//=== Special WYSIWYG-editor tokens ===

	// one character in a span of text
	textChar(                "Text Character"),
	
	//-------------------------------------
	//=== unsupported tag ===

	// something we don't recognize that looks like a MULTI tag
	unsupportedTag,      
	;

	//-----------------------------------------------------

	/** bit number used to flag particular
	 *  non-MULTI tokens as supported-by-IRIS */
	protected static final int SUPPORTED = -2;

	/** bit number used to flag unsupported tags */
	protected static final int UNSUPPORTED = -1;

	/** bit number in dmsSupportedMultiTags */
	private int bit;

	/** tag label */
	private String label;

	//-----------------------------------------------------
	// WTokenType Constructors

	/** Unsupported tag */
	private WTokenType() {
		bit = UNSUPPORTED;
		label = "Unsupported Tag";
	}
	
	/** Standard MULTI tags */
	private WTokenType(int abit, String alabel) {
		bit = abit;
		label = alabel;
	}

	/** Special IRIS-specific tags
	 *  or single MULTI text char */
	private WTokenType(String alabel) {
		bit = SUPPORTED;
		label = alabel;
	}

	//-----------------------------------------------------
	
	/** Values array */
	static private final WTokenType[] VALUES = values();

	/** Get a token type from an ordinal value */
	static public WTokenType fromOrdinal(short o) {
		if (o >= 0 && o < VALUES.length)
			return VALUES[o];
		else
			return null;
	}

	/** Is token supported by a specific MultiConfig?
	 * @param cfg MultiConfig */
	public boolean supportedBy(MultiConfig cfg) {
		if (bit == SUPPORTED)
			return true;
		if (bit == UNSUPPORTED)
			return false;
		int mask = 1 << bit;
		return (cfg.getSupportedTags() & mask) != 0;
	}
	
	//-----------------------------------------------------
	
	public String getLabel() {
		return label;
	}
}

