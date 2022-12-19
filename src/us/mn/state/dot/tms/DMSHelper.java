/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2022  Minnesota Department of Transportation
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
import org.json.JSONException;
import org.json.JSONObject;
import us.mn.state.dot.tms.utils.Base64;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.SString;

/**
 * Helper class for DMS.  Used on the client and server.
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
	static public int getDefaultFontNum(DMS dms) {
		return (dms != null)
		      ? SignConfigHelper.getDefaultFontNum(dms.getSignConfig())
		      : FontHelper.DEFAULT_FONT_NUM;
	}

	/** Get the default font for a DMS */
	static public Font getDefaultFont(DMS dms) {
		return (dms != null)
		      ? SignConfigHelper.getDefaultFont(dms.getSignConfig())
		      : null;
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
		if (dms != null) {
			RasterBuilder rb = createRasterBuilder(dms);
			if (rb != null)
				return rb.getLineCount();
		}
		return SignMessage.MAX_LINES;
	}

	/** Create a raster builder for a DMS */
	static public RasterBuilder createRasterBuilder(DMS dms) {
		return SignConfigHelper.createRasterBuilder(
			dms.getSignConfig());
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
	static public RasterGraphic[] createRasters(DMS dms) {
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
	static public RasterGraphic[] createRasters(DMS dms, String multi) {
		RasterBuilder rb = createRasterBuilder(dms);
		return (rb != null) ? rb.createRasters(multi) : null;
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

	/** Get DMS status attribute */
	static public Object getStatus(DMS dms, String key) {
		String status = (dms != null) ? dms.getStatus() : null;
		if (status != null) {
			try {
				JSONObject jo = new JSONObject(status);
				return jo.opt(key);
			}
			catch (JSONException e) {
				// malformed JSON
				e.printStackTrace();
			}
		}
		return null;
	}

	/** Create stuck pixel bitmap */
	static public BitmapGraphic createStuckBitmap(DMS dms, String key)
		throws InvalidMsgException
	{
		String stuck = (dms != null) ? dms.getStuckPixels() : null;
		if (stuck != null) {
			BitmapGraphic bg = createBitmapGraphic(dms);
			if (bg != null) {
				try {
					JSONObject jo = new JSONObject(stuck);
					return setStuckPixels(bg, jo.opt(key));
				}
				catch (JSONException e) {
					throw new InvalidMsgException(
						"Malformed JSON");
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

	/** Get stuck pixel bitmap */
	static private BitmapGraphic setStuckPixels(BitmapGraphic bg,
		Object bm) throws InvalidMsgException
	{
		if (bm instanceof String) {
			String bmap = (String) bm;
			try {
				byte[] pixels = Base64.decode(bmap);
				if (pixels.length == bg.length()) {
					bg.setPixelData(pixels);
					return bg;
				}
			}
			catch (IOException e) {
				throw new InvalidMsgException("Base64 decode");
			}
			catch (IndexOutOfBoundsException e) {
				// stuck bitmap doesn't match current dimensions
			}
		}
		return null;
	}
}
