/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2021  Minnesota Department of Transportation
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

import static us.mn.state.dot.tms.DmsColor.AMBER;
import static us.mn.state.dot.tms.DmsColor.BLACK;
import us.mn.state.dot.tms.utils.ColorClassic;

/**
 * Color scheme enumeration.  Note: these values and ordering are taken from
 * NTCIP 1203.
 *
 * @author Douglas Lau
 */
public enum ColorScheme {
	UNKNOWN("???"),
	MONOCHROME_1_BIT("monochrome1Bit"),
	MONOCHROME_8_BIT("monochrome8Bit"),
	COLOR_CLASSIC("colorClassic"),
	COLOR_24_BIT("color24Bit");

	/** Description string */
	public final String description;

	/** Create a new color scheme */
	private ColorScheme(String d) {
		description = d;
	}

	/** Enumerated values */
	static private final ColorScheme[] VALUES = values();

	/** Get a color scheme from an ordinal value */
	static public ColorScheme fromOrdinal(int o) {
		return (o >= 0 && o < values().length) ? VALUES[o] : UNKNOWN;
	}

	/** Byte array for background 1-bit monochrome color */
	static private final byte[] MONO_1_BACKGROUND = new byte[] { 0 };

	/** Byte array for background 8-bit monochrome color */
	static private final byte[] MONO_8_BACKGROUND = new byte[] { 0 };

	/** Byte array for background classic color */
	static private final byte[] COLOR_CLASSIC_BACKGROUND = new byte[] {
		(byte) ColorClassic.black.ordinal()
	};

	/** Byte array for background 24-bit color */
	static private final byte[] COLOR_24_BACKGROUND = new byte[] {
		(byte) BLACK.red, (byte) BLACK.green, (byte) BLACK.blue
	};

	/** Byte array for foreground 1-bit monochrome color */
	static private final byte[] MONO_1_FOREGROUND = new byte[] { 1 };

	/** Byte array for foreground 8-bit monochrome color */
	static private final byte[] MONO_8_FOREGROUND = new byte[] {
		(byte) 255
	};

	/** Byte array for foreground classic color */
	static private final byte[] COLOR_CLASSIC_FOREGROUND = new byte[] {
		(byte) ColorClassic.amber.ordinal()
	};

	/** Byte array for foreground 24-bit color */
	static private final byte[] COLOR_24_FOREGROUND = new byte[] {
		(byte) AMBER.red, (byte) AMBER.green, (byte) AMBER.blue
	};

	/** Get the default background color for a color scheme */
	public byte[] getDefaultBackgroundBytes() {
		switch (this) {
		case MONOCHROME_1_BIT:
			return MONO_1_BACKGROUND;
		case MONOCHROME_8_BIT:
			return MONO_8_BACKGROUND;
		case COLOR_CLASSIC:
			return COLOR_CLASSIC_BACKGROUND;
		default:
			return COLOR_24_BACKGROUND;
		}
	}

	/** Get the default background color */
	public DmsColor getDefaultBackground() {
		return BLACK;
	}

	/** Get the default foreground color for a color scheme */
	public byte[] getDefaultForegroundBytes() {
		switch (this) {
		case MONOCHROME_1_BIT:
			return MONO_1_FOREGROUND;
		case MONOCHROME_8_BIT:
			return MONO_8_FOREGROUND;
		case COLOR_CLASSIC:
			return COLOR_CLASSIC_FOREGROUND;
		default:
			return COLOR_24_FOREGROUND;
		}
	}

	/** Get the default foreground color */
	public DmsColor getDefaultForeground() {
		return AMBER;
	}

	/** Get a color for the color scheme */
	public DmsColor getColor(int x) {
		switch (this) {
		case MONOCHROME_1_BIT:
			if (x == 0)
				return getDefaultBackground();
			else if (x == 1)
				return getDefaultForeground();
			break;
		case MONOCHROME_8_BIT:
			if (x >= 0 && x <= 255) {
				DmsColor c = getDefaultForeground();
				int r = (c.red * x) >> 8;
				int g = (c.green * x) >> 8;
				int b = (c.blue * x) >> 8;
				return new DmsColor(r, g, b);
			}
			break;
		default:
			ColorClassic cc = ColorClassic.fromOrdinal(x);
			if (cc != null)
				return cc.clr;
			break;
		}
		return null;
	}
}
