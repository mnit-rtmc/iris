/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2025  Minnesota Department of Transportation
 * Copyright (C) 2009-2010  AHMCT, University of California
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
package us.mn.state.dot.tms;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.RleTable;
import us.mn.state.dot.tms.utils.SString;

/**
 * Helper class for DMS.  Used on the client and server.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSHelper extends BaseHelper {

	/** Stuck ON pixels */
	static public final int STUCK_ON = 1;

	/** Stuck OFF */
	static public final int STUCK_OFF = 2;

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

	/** Check if a DMS is hidden (#Hidden hashtag) */
	static public boolean isHidden(DMS dms) {
		return new Hashtags(dms.getNotes()).contains("#Hidden");
	}

	/** Reserved hashtags for dedicated-purpose signs */
	static private final String[] DEDICATED_PURPOSE_TAGS = new String[] {
	     "#Hidden",
	     "#LaneUse",
	     "#Parking",
	     "#Safety",
	     "#Tolling",
	     "#TravelTime",
	     "#VSL",
	     "#Wayfinding"
	};

	/** Check if a DMS is general-purpose */
	static public boolean isGeneralPurpose(DMS dms) {
		Hashtags tags = new Hashtags(dms.getNotes());
		for (String tag: DEDICATED_PURPOSE_TAGS) {
			if (tags.contains(tag))
				return false;
		}
		return true;
	}

	/** Get optional DMS faults, or null */
	static public String optFaults(DMS proxy) {
		SignConfig sc = proxy.getSignConfig();
		if (null == sc ||
		    sc.getFaceWidth() <= 0 ||
		    sc.getFaceHeight() <= 0)
			return "Invalid dimensions";
		Object faults = optStatus(proxy, DMS.FAULTS);
		return (faults != null) ? faults.toString() : null;
	}

	/** Test if a DMS has faults */
	static public boolean hasFaults(DMS proxy) {
		return optFaults(proxy) != null;
	}

	/** Test if a DMS is active */
	static public boolean isActive(DMS proxy) {
		return ItemStyle.ACTIVE.checkBit(proxy.getStyles());
	}

	/** Test if a DMS is offline */
	static public boolean isOffline(DMS proxy) {
		return ItemStyle.OFFLINE.checkBit(proxy.getStyles());
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

	/** Get the user sent MULTI string currently on a DMS.
	 * @param dms DMS to lookup. */
	static public String getUserMulti(DMS dms) {
		if (dms != null) {
			SignMessage sm = dms.getMsgUser();
			if (sm != null)
				return sm.getMulti();
		}
		return "";
	}

	/** Get the default font number for a DMS */
	static public int getDefaultFontNum(DMS dms) {
		SignConfig sc = (dms != null) ? dms.getSignConfig() : null;
		return (sc != null)
		      ? sc.getDefaultFont()
		      : FontHelper.DEFAULT_FONT_NUM;
	}

	/** Get the default font for a DMS */
	static public Font getDefaultFont(DMS dms) {
		return FontHelper.find(getDefaultFontNum(dms));
	}

	/** Get the default background color for a DMS */
	static public byte[] getDefaultBackgroundBytes(DMS dms) {
		return getColorScheme(dms).getDefaultBackgroundBytes();
	}

	/** Get the default foreground color for a DMS */
	static public byte[] getDefaultForegroundBytes(DMS dms) {
		return getColorScheme(dms).getDefaultForegroundBytes();
	}

	/** Get the color scheme for a DMS */
	static private ColorScheme getColorScheme(DMS dms) {
		SignConfig sc = dms.getSignConfig();
		return (sc != null)
			? ColorScheme.fromOrdinal(sc.getColorScheme())
		        : ColorScheme.UNKNOWN;
	}

	/** Get the number of lines on a DMS.
	 * @param dms DMS to check.
	 * @return Number of text lines on the DMS. */
	static public int getLineCount(DMS dms) {
		RasterBuilder rb = createRasterBuilder(dms);
		return (rb != null)
		      ? rb.getLineCount()
		      : SignMessage.MAX_LINES;
	}

	/** Create a raster builder for a DMS */
	static public RasterBuilder createRasterBuilder(DMS dms) {
		if (dms != null) {
			return SignConfigHelper.createRasterBuilder(
				dms.getSignConfig());
		} else
			return null;
	}

	/** Return a single string which is formated to be readable
	 * by the user and contains all sign message lines on the
	 * specified DMS.
	 * @param dms The DMS containing the message.
	 * @return Text of message on the DMS. */
	static public String buildMsgText(DMS dms) {
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

	/** Create bitmap graphics for all pages of a specified DMS.
	 * @param dms The sign.
	 * @param ms Message MULTI string.
	 * @return Array of bitmap graphics for the sign, or null.
	 * @throws InvalidMsgException if MULTI string is invalid. */
	static public BitmapGraphic[] createBitmaps(DMS dms, String ms)
		throws InvalidMsgException
	{
		RasterBuilder rb = createRasterBuilder(dms);
		return (rb != null)
		      ? rb.createBitmaps(new MultiString(ms))
		      : null;
	}

	/** Get the current raster graphic for page one of the specified DMS.
	 * @param dms The sign.
	 * @return RasterGraphic for page one, or null on error.
	 */
	static public RasterGraphic getPageOne(DMS dms) {
		RasterGraphic[] rasters = createRasters(dms);
		return (rasters != null && rasters.length > 0)
		      ? rasters[0]
		      : null;
	}

	/** Create the page one raster graphic for a DMS with a MULTI string.
	 * @param dms The sign.
	 * @param multi MULTI string.
	 * @return RasterGraphic for page one, or null on error. */
	static public RasterGraphic createPageOne(DMS dms, String multi) {
		RasterGraphic[] rasters = createRasters(dms, multi);
		return (rasters != null && rasters.length > 0)
		      ? rasters[0]
		      : null;
	}

	/** Get the current raster graphics for all pages of the specified DMS.
	 * @param dms Sign in question.
	 * @return RasterGraphic array, one for each page, or null on error.
	 */
	static private RasterGraphic[] createRasters(DMS dms) {
		if (dms != null) {
			SignMessage sm = dms.getMsgCurrent();
			if (sm != null)
				return createRasters(dms, sm.getMulti());
		}
		return null;
	}

	/** Get the current raster graphics for all pages of the specified DMS.
	 * @param dms Sign in question.
	 * @param multi MULTI string.
	 * @return RasterGraphic array, one for each page, or null on error.
	 */
	static private RasterGraphic[] createRasters(DMS dms, String multi) {
		RasterBuilder rb = createRasterBuilder(dms);
		return (rb != null) ? rb.createRasters(multi) : null;
	}

	/** Lookup the associated incident */
	static public Incident lookupIncident(DMS dms) {
		if (null == dms)
			return null;
		DmsLock lk = new DmsLock(dms.getLock());
		String inc = lk.optIncident();
		return (inc != null)
		      ? IncidentHelper.lookupByOriginal(inc)
		      : null;
	}

	/** Get optional DMS status attribute, or null */
	static public Object optStatus(DMS dms, String key) {
		String status = (dms != null) ? dms.getStatus() : null;
		return optJson(status, key);
	}

	/** Create stuck pixel bitmap */
	static public BitmapGraphic createStuckBitmap(DMS dms, int stuck)
		throws InvalidMsgException
	{
		String fail = (dms != null) ? dms.getPixelFailures() : null;
		if (fail != null) {
			BitmapGraphic bg = createBitmapGraphic(dms);
			if (bg != null) {
				try {
					setStuckPixels(bg, fail, stuck);
					return bg;
				}
				catch (Exception e) {
					throw new InvalidMsgException(
						"Malformed pixel_failures");
				}
			}
		}
		return null;
	}

	/** Create a bitmap graphic for the specified DMS */
	static private BitmapGraphic createBitmapGraphic(DMS dms) {
		SignConfig sc = dms.getSignConfig();
		if (sc != null) {
			int pw = sc.getPixelWidth();
			int ph = sc.getPixelHeight();
			return new BitmapGraphic(pw, ph);
		} else
			return null;
	}

	/** Get stuck pixels in bitmap */
	static private void setStuckPixels(BitmapGraphic bg, String fail,
		int stuck)
	{
		RleTable table = new RleTable(fail);
		for (int y = 0; y < bg.getHeight(); y++) {
			for (int x = 0; x < bg.getWidth(); x++) {
				int pf = table.decode();
				if ((stuck == STUCK_ON) && (pf & 0x01) > 0)
					bg.setPixel(x, y, DmsColor.WHITE);
				if ((stuck == STUCK_OFF) && (pf & 0x10) > 0)
					bg.setPixel(x, y, DmsColor.WHITE);
			}
		}
	}

	/** Check if a MULTI string is rasterizable for a sign */
	static public boolean isRasterizable(DMS dms, String ms) {
		RasterBuilder rb = createRasterBuilder(dms);
		return (rb != null) && rb.isRasterizable(ms);
	}

	/** Validate free-form message lines */
	static public String validateFreeFormLines(DMS dms, String ms) {
		if (dms == null || ms == null)
			return "NULL VALUE";
		String err = "NO PATTERN";
		for (MsgPattern pat: MsgPatternHelper.findAllCompose(dms)) {
			String e = MsgPatternHelper
				.validateLines(pat, dms, ms);
			if (e == null)
				return null;
			else if (!e.isEmpty())
				err = e;
		}
		return err;
	}

	/** Validate free-form message words */
	static public String validateFreeFormWords(DMS dms, String ms) {
		if (dms == null || ms == null)
			return "NULL VALUE";
		String err = "NO PATTERN";
		for (MsgPattern pat: MsgPatternHelper.findAllCompose(dms)) {
			String e = MsgPatternHelper
				.validateWords(pat, dms, ms);
			if (e == null)
				return null;
			else if (!e.isEmpty())
				err = e;
		}
		return err;
	}
}
