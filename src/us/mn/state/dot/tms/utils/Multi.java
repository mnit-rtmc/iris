/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
 * Copyright (C) 2009  AHMCT, University of California
 * Copyright (C) 2019-2020  SRF Consulting Group
 * Copyright (C) 2021  Iteris Inc.
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
 * MULTI string state interface.
 *
 * @author Douglas Lau
 * @author Michael Darter
 * @author John Stanley - SRF Consulting
 * @author Gordon Parikh - SRF Consulting
 */
public interface Multi {

	/** Handle an unsupported tag */
	void unsupportedTag(String tag);

	/** Add a span of text */
	void addSpan(String span);

	/** Set the (deprecated) message background color.
	 * @param x Background color (0-9; colorClassic value).
	 * Use the sign's default background color if x is null. */
	void setColorBackground(Integer x);

	/** Set the page background color for monochrome1bit, monochrome8bit,
	 * and colorClassic color schemes.
	 * @param x Background color (0-1 for monochrome1bit),
	 *                           (0-255 for monochrome8bit),
	 *                           (0-9 for colorClassic).
	 * Use the sign's default background color if x is null. */
	void setPageBackground(Integer x);

	/** Set the page background color for color24bit color scheme.
	 * @param r Red component (0-255).
	 * @param g Green component (0-255).
	 * @param b Blue component (0-255). */
	void setPageBackground(int r, int g, int b);

	/** Set the foreground color for single-int color tag.  [cfX]
	 * @param x Foreground color (0-1 for monochrome1bit),
	 *                           (0-255 for monochrome8bit),
	 *                           (0-9 for colorClassic &amp; color24bit).
	 * Use the sign's default foreground color if x is null. */
	void setColorForeground(Integer x);

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
	 * @param f_id Font version ID (4-digit hex string)
	 * Use the sign's default font if f_num is null. */
	void setFont(Integer f_num, String f_id);

	/** Add a graphic */
	void addGraphic(int g_num, Integer x, Integer y, String g_id);

	/** Line Justification enumeration */
	enum JustificationLine {
		UNDEFINED, OTHER, LEFT, CENTER, RIGHT, FULL;

		/** Cached values array */
		static private final JustificationLine[] VALUES = values();

		/** Get line justification from an ordinal value */
		static public JustificationLine fromOrdinal(int o) {
			if (o >= 0 && o < VALUES.length)
				return VALUES[o];
			else
				return UNDEFINED;
		}

		/** Get the default line justification */
		static public JustificationLine defaultValue() {
			return CENTER;
		}
	}

	/** Set the line justification.
	 * Use the sign's default line justification if jl is null. */
	void setJustificationLine(JustificationLine jl);

	/** Page Justification enumeration. See NTCIP 1203 as necessary. */
	enum JustificationPage {
		UNDEFINED, OTHER, TOP, MIDDLE, BOTTOM;

		/** Cached values array */
		static private final JustificationPage[] VALUES = values();

		/** Get page justification from an ordinal value */
		static public JustificationPage fromOrdinal(int o) {
			if (o >= 0 && o < VALUES.length)
				return VALUES[o];
			else
				return UNDEFINED;
		}

		/** Get the default page justification */
		static public JustificationPage defaultValue() {
			return TOP;
		}
	}

	/** Set the page justification.
	 * Use the sign's default page justification if jp is null. */
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

	/* IRIS-specific action tags (not part of MULTI) */

	/** Add a ClearGuide advisory.
	 * @param dms DMS name
	 * @param wid Workzone ID
	 * @param min Minimum valid value
	 * @param max Maximum valid value
	 * @param mode Tag replacement mode to use: e.g. tt, delay
	 * @param idx Workzone index, zero based */
	void addClearGuideAdvisory(String dms, int wid, int min, int max,
		String mode, int idx);

	/** Add an exit backup warning.
	 * @param did Exit detector ID.
	 * @param occ Occupancy threshold to activate warning. */
	void addExitWarning(String did, int occ);

	/** Add a feed message */
	void addFeed(String fid);

	/** Add parking area availability.
	 * @param pid Parking area ID.
	 * @param l_txt Text for low availability.
	 * @param c_txt Text for closed area. */
	void addParking(String pid, String l_txt, String c_txt);

	/** Add RWIS condition warning.
	 * @param condition Weather condition.
	 * @param level Warning level. */
	void addRwis(String condition, int level);

	/** Add a slow traffic warning.
	 * @param spd Highest speed to activate warning.
	 * @param dist Distance to search for slow traffic (1/10 mile).
	 * @param mode Tag replacement mode (none, dist or speed). */
	void addSlowWarning(int spd, int dist, String mode);

	/** Add a standby-message tag */
	void addStandby();

	/** Add a time action substitution field.
	 *  @param dir Chronoligical direction ("n" or "p").
	 *  @param format Format pattern (DateTimeFormattter). */
	void addTimeAction(String dir, String format);

	/** Modes for travel time over limit handling */
	enum OverLimitMode {
		blank, prepend, append
	};

	/** Add a travel time destination.
	 * @param sid Destination station ID.
	 * @param mode Over limit mode.
	 * @param o_txt Over limit text. */
	void addTravelTime(String sid, OverLimitMode mode, String o_txt);

	/** Add a tolling message */
	void addTolling(String mode, String[] zones);

	/** Add a speed advisory */
	void addSpeedAdvisory();

	/** Add an incident locator */
	void addLocator(String code);
}
