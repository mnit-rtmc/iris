/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
 * MULTI string state interface.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public interface Multi {

	/** Add a span of text */
	void addSpan(String span);

	/** Set the (deprecated) message background color.
	 * @param x Background color (0-9; colorClassic value). */
	void setColorBackground(int x);

	/** Set the page background color for monochrome1bit, monochrome8bit,
	 * and colorClassic color schemes.
	 * @param z Background color (0-1 for monochrome1bit),
	 *                           (0-255 for monochrome8bit),
	 *                           (0-9 for colorClassic). */
	void setPageBackground(int z);

	/** Set the page background color for color24bit color scheme.
	 * @param r Red component (0-255).
	 * @param g Green component (0-255).
	 * @param b Blue component (0-255). */
	void setPageBackground(int r, int g, int b);

	/** Set the foreground color for monochrome1bit, monochrome8bit, and
	 * colorClassic color schemes.
	 * @param x Foreground color (0-1 for monochrome1bit),
	 *                           (0-255 for monochrome8bit),
	 *                           (0-9 for colorClassic). */
	void setColorForeground(int x);

	/** Set the foreground color for color24bit color scheme.
	 * @param r Red component (0-255).
	 * @param g Green component (0-255).
	 * @param b Blue component (0-255). */
	void setColorForeground(int r, int g, int b);

	/** Add a color rectangle for monochrome1bit, monochrome8bit, and
	 * colorClassic color schemes.
	 * @param x X pixel position of upper left corner.
	 * @param y Y pixel position of upper left corner.
	 * @param w Width in pixels.
	 * @param h Height in pixels.
	 * @param z Color of rectangle (0-1 for monochrome1bit),
	 *                             (0-255 for monochrome8bit),
	 *                             (0-9 for colorClassic). */
	void addColorRectangle(int x, int y, int w, int h, int z);

	/** Add a color rectangle for color24bit color scheme.
	 * @param x X pixel position of upper left corner.
	 * @param y Y pixel position of upper left corner.
	 * @param w Width in pixels.
	 * @param h Height in pixels.
	 * @param r Red component (0-255).
	 * @param g Green component (0-255).
	 * @param b Blue component (0-255). */
	void addColorRectangle(int x, int y, int w, int h, int r, int g, int b);

	/** Set the font number.
	 * @param f_num Font number (1 to 255)
	 * @param f_id Font version ID (4-digit hex) */
	void setFont(int f_num, String f_id);

	/** Add a graphic */
	void addGraphic(int g_num, Integer x, Integer y, String g_id);

	/** Line Justification enumeration */
	enum JustificationLine {
		UNDEFINED, OTHER, LEFT, CENTER, RIGHT, FULL;

		/** Get line justification from an ordinal value */
		static public JustificationLine fromOrdinal(int v) {
			for(JustificationLine lj: values()) {
				if(lj.ordinal() == v)
					return lj;
			}
			return UNDEFINED;
		}

		/** Default line justification */
		static public final JustificationLine DEFAULT = fromOrdinal(
			SystemAttrEnum.DMS_DEFAULT_JUSTIFICATION_LINE.getInt());
	}

	/** Set the line justification */
	void setJustificationLine(JustificationLine jl);

	/** Page Justification enumeration. See NTCIP 1203 as necessary. */
	enum JustificationPage {
		UNDEFINED, OTHER, TOP, MIDDLE, BOTTOM;

		/** Get page justification from an ordinal value */
		static public JustificationPage fromOrdinal(int v) {
			for(JustificationPage pj: values()) {
				if(pj.ordinal() == v)
					return pj;
			}
			return UNDEFINED;
		}

		/** Default page justification */
		static public final JustificationPage DEFAULT = fromOrdinal(
			SystemAttrEnum.DMS_DEFAULT_JUSTIFICATION_PAGE.getInt());
	}

	/** Set the page justification */
	void setJustificationPage(JustificationPage jp);

	/** Add a new line.
	 * @param spacing Pixel spacing (null means use font spacing) */
	void addLine(Integer spacing);

	/** Add a page */
	void addPage();

	/** Set the page times.
	 * @param pt_on Page on time (deciseconds; null means default)
	 * @param pt_off Page off time (deciseconds; null means default) */
	void setPageTimes(Integer pt_on, Integer pt_off);

	/** Set the character spacing.
	 * @param sc Character spacing (null means use font spacing) */
	void setCharSpacing(Integer sc);

	/** Set the text rectangle */
	void setTextRectangle(int x, int y, int w, int h);

	/* IRIS-specific (not part of MULTI) */

	/** Add a travel time destination */
	void addTravelTime(String sid);

	/** Add a speed advisory */
	void addSpeedAdvisory();

	/** Add a slow traffic warning.
	 * @param spd Highest speed to activate warning.
	 * @param b Distance to end of backup (negative indicates upstream).
	 * @param units Units for speed (mph or kph).
	 * @param dist If true, replace tag with distance to slow station. */
	void addSlowWarning(int spd, int b, String units, boolean dist);

	/** Add a feed message */
	void addFeed(String fid);
}
