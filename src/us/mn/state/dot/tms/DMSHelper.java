/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2019  Minnesota Department of Transportation
 * Copyright (C) 2009-2010  AHMCT, University of California
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

import java.util.Iterator;
import static us.mn.state.dot.tms.DmsColor.AMBER;
import static us.mn.state.dot.tms.DmsColor.BLACK;
import us.mn.state.dot.tms.utils.ColorClassic;
import us.mn.state.dot.tms.utils.MultiBuilder;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.SString;

/**
 * Helper class for DMS. Used on the client and server.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSHelper extends BaseHelper {

	/** don't instantiate */
	private DMSHelper() {
		assert false;
	}

	/** Lookup the DMS with the specified name */
	static public DMS lookup(String name) {
		return (DMS) namespace.lookupObject(DMS.SONAR_TYPE, name);
	}

	/** Get a DMS iterator */
	static public Iterator<DMS> iterator() {
		return new IteratorWrapper<DMS>(namespace.iterator(
			DMS.SONAR_TYPE));
	}

	/** Get the maintenance status of a DMS */
	static public String getMaintenance(DMS proxy) {
		return ControllerHelper.getMaintenance(proxy.getController());
	}

	/** Test if a DMS has a critical error */
	static public boolean hasCriticalError(DMS proxy) {
		return !getCriticalError(proxy).isEmpty();
	}

	/** Get the DMS critical error */
	static public String getCriticalError(DMS proxy) {
		SignConfig sc = proxy.getSignConfig();
		if (null == sc ||
		    sc.getFaceWidth() <= 0 ||
		    sc.getFaceHeight() <= 0)
			return "Invalid dimensions";
		else
			return getStatus(proxy);
	}

	/** Get DMS controller communication status */
	static public String getStatus(DMS proxy) {
		return ControllerHelper.getStatus(proxy.getController());
	}

	/** Test if a DMS is active */
	static public boolean isActive(DMS proxy) {
		return ItemStyle.ACTIVE.checkBit(proxy.getStyles());
	}

	/** Test if a DMS is failed */
	static public boolean isFailed(DMS proxy) {
		return ItemStyle.FAILED.checkBit(proxy.getStyles());
	}

	/** Test if a DMS is a dedicated purpose sign */
	static public boolean isPurpose(DMS proxy) {
		return ItemStyle.PURPOSE.checkBit(proxy.getStyles());
	}

	/** Get a string that contains all active DMS styles,
	 * separated by commas. */
	static public String getAllStyles(DMS proxy) {
		StringBuilder sb = new StringBuilder();
		for (ItemStyle style: ItemStyle.toStyles(proxy.getStyles())) {
			if (sb.length() > 0)
				sb.append(", ");
			sb.append(style.toString());
		}
		return sb.toString();
	}

	/** Lookup the camera preset for a DMS */
	static public CameraPreset getPreset(DMS dms) {
		return (dms != null) ? dms.getPreset() : null;
	}

	/** Get the DMS roadway direction from the geo location as a String */
	static public String getRoadDir(DMS proxy) {
		if (proxy != null) {
			GeoLoc loc = proxy.getGeoLoc();
			if (loc != null) {
				short rd = loc.getRoadDir();
				return Direction.fromOrdinal(rd).abbrev;
			}
		}
		return "";
	}

	/** Get the MULTI string currently on the specified dms.
	 * @param dms DMS to lookup. */
	static public String getMultiString(DMS dms) {
		if (dms != null) {
			SignMessage sm = dms.getMsgCurrent();
			if (sm != null)
				return sm.getMulti();
		}
		return "";
	}

	/** Get the operator sent MULTI string currently on a DMS.
	 * @param dms DMS to lookup. */
	static public String getOperatorMulti(DMS dms) {
		if (dms != null) {
			SignMessage sm = dms.getMsgCurrent();
			if (sm != null
			 && SignMsgSource.operator.checkBit(sm.getSource()))
				return sm.getMulti();
		}
		return "";
	}

	/** Get the default font number for a DMS */
	static public int getDefaultFontNumber(DMS dms) {
		Font f = getDefaultFont(dms);
		return (f != null)
		      ? f.getNumber()
		      : FontHelper.DEFAULT_FONT_NUM;
	}

	/** Get the default font for a DMS */
	static public Font getDefaultFont(DMS dms) {
		if (dms != null) {
			SignConfig sc = dms.getSignConfig();
			if (sc != null)
				return sc.getDefaultFont();
		}
		return null;
	}

	/** Get the font number for a DMS */
	static private int getFontNumber(DMS dms) {
		Font f = dms.getOverrideFont();
		return (f != null) ? f.getNumber() : getDefaultFontNumber(dms);
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

	/** Get the default background color for a color scheme */
	static public byte[] getDefaultBackgroundBytes(ColorScheme scheme) {
		switch (scheme) {
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

	/** Get the default background color for a DMS */
	static public byte[] getDefaultBackgroundBytes(DMS dms) {
		return getDefaultBackgroundBytes(getColorScheme(dms));
	}

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

	/** Get the default foreground color for a color scheme */
	static public byte[] getDefaultForegroundBytes(ColorScheme scheme) {
		switch (scheme) {
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

	/** Get the default foreground color for a DMS */
	static public byte[] getDefaultForegroundBytes(DMS dms) {
		return getDefaultForegroundBytes(getColorScheme(dms));
	}

	/** Get the color scheme for a DMS */
	static private ColorScheme getColorScheme(DMS dms) {
		SignConfig sc = dms.getSignConfig();
		return (sc != null)
			? ColorScheme.fromOrdinal(sc.getColorScheme())
		        : ColorScheme.UNKNOWN;
	}

	/** Adjust a MULTI string for a DMS */
	static public String adjustMulti(DMS dms, String multi) {
		ColorScheme scheme = getColorScheme(dms);
		Font f = dms.getOverrideFont();
		Integer fg = dms.getOverrideForeground();
		Integer bg = dms.getOverrideBackground();
		return (f != null || fg != null || bg != null)
		      ? adjustMulti(multi, f, scheme, fg, bg)
		      : multi;
	}

	/** Adjust a MULTI string with override font / colors */
	static private String adjustMulti(String multi, Font f,
		ColorScheme scheme, Integer fg, Integer bg)
	{
		MultiBuilder mb = new MultiBuilder();
		if (f != null)
			mb.setFont(f.getNumber(), null);
		if (ColorScheme.COLOR_24_BIT == scheme) {
			if (fg != null) {
				DmsColor c = new DmsColor(fg);
				mb.setColorForeground(c.red, c.green, c.blue);
			}
			if (bg != null) {
				DmsColor c = new DmsColor(fg);
				mb.setPageBackground(c.red, c.green, c.blue);
			}
		} else {
			if (fg != null)
				mb.setColorForeground(fg);
			if (bg != null)
				mb.setPageBackground(bg);
		}
		mb.append(new MultiString(multi));
		return mb.toString();
	}

	/** Get the number of lines on a DMS.
	 * @param dms DMS to check.
	 * @return Number of text lines on the DMS. */
	static public int getLineCount(DMS dms) {
		if (dms != null) {
			RasterBuilder rb = createRasterBuilder(dms);
			if (rb != null)
				return rb.getLineCount();
		}
		return SystemAttrEnum.DMS_MAX_LINES.getInt();
	}

	/** Create a raster builder for a DMS.
	 * @param dms DMS with proper dimensions for the builder.
	 * @return A pixel map builder, or null if dimensions are invalid. */
	static public RasterBuilder createRasterBuilder(DMS dms) {
		SignConfig sc = dms.getSignConfig();
		if (sc != null) {
			int w = sc.getPixelWidth();
			int h = sc.getPixelHeight();
			int cw = sc.getCharWidth();
			int ch = sc.getCharHeight();
			int f = getFontNumber(dms);
			return new RasterBuilder(w, h, cw, ch, f);
		} else
			return null;
	}

	/** Return a single string which is formated to be readable
	 * by the user and contains all sign message lines on the
	 * specified DMS.
	 * @param dms The DMS containing the message.
	 * @return Text of message on the DMS. */
	static public String buildMsgLine(DMS dms) {
		SignMessage sm = dms.getMsgCurrent();
		if (sm != null) {
			String multi = sm.getMulti();
			if (multi != null)
				return new MultiString(multi).asText();
		}
		return "";
	}

	/** Messages lines that flag no DMS message text available */
	static public final String NOTXT_L1 = "_OTHER_";
	static public final String NOTXT_L2 = "_SYSTEM_";
	static public final String NOTXT_L3 = "_MESSAGE_";

	/** Filter the specified multi. If certain keywords are present then
	 * a blank multi is returned. The keywords indicate no text is
	 * available for the associated bitmap.
	 * @return A blank multi if the argument multi flags no text,
	 *         else the specified multi. */
	static public MultiString ignoreFilter(MultiString ms) {
		String s = ms.toString();
		boolean ignore = s.contains(NOTXT_L1) && s.contains(NOTXT_L2)
			&& s.contains(NOTXT_L3);
		return (ignore) ? new MultiString("") : ms;
	}

	/**
	 * Return true if the specified message line should be ignored.
	 * By convention, a line begining and ending with an underscore
	 * is to be ignored. IRIS assumes non-blank DMS messages have
	 * both a bitmap and multistring, which is not the case for all
	 * DMS protocols.
	 */
	static public boolean ignoreLineFilter(String line) {
		if (line == null)
			return false;
		return SString.enclosedBy(line, "_");
	}

	/** Create a bitmap graphic for the specified DMS */
	static public BitmapGraphic createBitmapGraphic(DMS dms) {
		SignConfig sc = dms.getSignConfig();
		if (sc != null) {
			int pw = sc.getPixelWidth();
			int ph = sc.getPixelHeight();
			return new BitmapGraphic(pw, ph);
		} else
			return null;
	}

	/** Create bitmap graphics for all pages of a specified DMS.
	 * @param dms The sign.
	 * @param ms Message MULTI string.
	 * @return Array of bitmap graphics for the sign, or null.
	 * @throws InvalidMsgException if MULTI string is invalid. */
	static public BitmapGraphic[] createBitmaps(DMS dms, String ms)
		throws InvalidMsgException
	{
		RasterBuilder rb = createRasterBuilder(dms);
		if (rb != null)
			return rb.createBitmaps(new MultiString(ms));
		else
			return null;
	}

	/** Create pixmap graphics for a DMS.
	 * @param dms The sign.
	 * @param ms Message MULTI string.
	 * @return Array of bitmap graphics for the sign, or null.
	 * @throws InvalidMsgException if MULTI string is invalid. */
	static public RasterGraphic[] createPixmaps(DMS dms, MultiString ms)
		throws InvalidMsgException
	{
		RasterBuilder rb = createRasterBuilder(dms);
		if (rb != null)
			return rb.createPixmaps(ms);
		else
			return null;
	}

	/** Get the current raster graphic for page one of the specified DMS.
	 * @param dms The sign.
	 * @return RasterGraphic for page one, or null on error.
	 */
	static public RasterGraphic getPageOne(DMS dms) {
		RasterGraphic[] rasters = getRasters(dms);
		if (rasters != null && rasters.length > 0)
			return rasters[0];
		else
			return null;
	}

	/** Create the page one raster graphic for a DMS with a MULTI string.
	 * @param dms The sign.
	 * @param multi MULTI string.
	 * @return RasterGraphic for page one, or null on error. */
	static public RasterGraphic createPageOne(DMS dms, String multi) {
		RasterGraphic[] rasters = getRasters(dms, multi);
		if (rasters != null && rasters.length > 0)
			return rasters[0];
		else
			return null;
	}

	/** Get the current raster graphics for all pages of the specified DMS.
	 * @param dms Sign in question.
	 * @return RasterGraphic array, one for each page, or null on error.
	 */
	static public RasterGraphic[] getRasters(DMS dms) {
		if (dms != null) {
			SignMessage sm = dms.getMsgCurrent();
			if (sm != null)
				return getRasters(dms, sm.getMulti());
		}
		return null;
	}

	/** Get the current raster graphics for all pages of the specified DMS.
	 * @param dms Sign in question.
	 * @param multi MULTI string.
	 * @return RasterGraphic array, one for each page, or null on error.
	 */
	static private RasterGraphic[] getRasters(DMS dms, String multi) {
		RasterBuilder rb = createRasterBuilder(dms);
		return (rb != null) ? createRasters(rb, multi) : null;
	}

	/** Create raster graphics using a raster builder and multi string.
	 * @return RasterGraphic array, one for each page, or null on error.
	 */
	static private RasterGraphic[] createRasters(RasterBuilder rb,
		String multi)
	{
		try {
			return rb.createPixmaps(new MultiString(multi));
		}
		catch (InvalidMsgException e) {
			return null;
		}
	}

	/** Get the owner of the current message */
	static public String getOwner(DMS dms) {
		SignMessage sm = dms.getMsgCurrent();
		return (sm != null) ? sm.getOwner() : null;
	}

	/** Lookup the associated incident */
	static public Incident lookupIncident(DMS dms) {
		if (null == dms)
			return null;
		SignMessage sm = dms.getMsgCurrent();
		return (sm != null)
		      ? IncidentHelper.lookupOriginal(sm.getIncident())
		      : null;
	}

	/** Check if a MULTI string fits on a DMS.
	 * @param dms Sign in question.
	 * @param multi MULTI string.
	 * @param abbrev Check word abbreviations.
	 * @return Best fit MULTI string, or null if message does not fit. */
	static public String checkMulti(DMS dms, String multi, boolean abbrev) {
		return (createPageOne(dms, multi) != null)
		      ? multi
		      : (abbrev) ? checkMultiAbbrev(dms, multi) : null;
	}

	/** Check if a MULTI string fits on a DMS (with abbreviations).
	 * @param dms Sign in question.
	 * @param multi MULTI string.
	 * @return Best fit MULTI string, or null if message does not fit. */
	static private String checkMultiAbbrev(DMS dms, String multi) {
		String[] words = multi.split(" ");
		// Abbreviate words with non-blank abbreviations
		for (int i = words.length - 1; i >= 0; i--) {
			String abbrev = WordHelper.abbreviate(words[i]);
			if (abbrev != null && abbrev.length() > 0) {
				words[i] = abbrev;
				String ms = String.join(" ", words);
				if (createPageOne(dms, ms) != null)
					return ms;
			}
		}
		// Abbreviate words with blank abbreviations
		for (int i = words.length - 1; i >= 0; i--) {
			String abbrev = WordHelper.abbreviate(words[i]);
			if (abbrev != null) {
				words[i] = abbrev;
				String ms = String.join(" ", words);
				if (createPageOne(dms, ms) != null)
					return ms;
			}
		}
		return null;
	}
}
