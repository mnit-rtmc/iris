/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2023  Minnesota Department of Transportation
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.json.JSONException;
import org.json.JSONObject;

import us.mn.state.dot.tms.units.Distance;
import us.mn.state.dot.tms.utils.Base64;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;
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

	/** Normalize a hashtag value */
	static public String normalizeHashtag(String ht) {
		if (ht != null) {
			ht = ht.trim();
			return ht.matches("#[A-Za-z0-9]+") ? ht : null;
		} else
			return null;
	}

	/** Make an ordered array of hashtags */
	static public String[] makeHashtags(String[] ht) {
		TreeSet<String> tags = new TreeSet<String>();
		for (String tag: ht) {
			tag = normalizeHashtag(tag);
			if (tag != null)
				tags.add(tag);
		}
		return tags.toArray(new String[0]);
	}

	/** Hashtag filter iterator */
	static public class DmsHashtagIterator implements Iterator<DMS> {
		private final String hashtag;
		private final Iterator<DMS> wrapped;
		private DMS next;
		public DmsHashtagIterator(String ht, Iterator<DMS> it) {
			hashtag = ht;
			wrapped = it;
			next = null;
		}
		@Override public boolean hasNext() {
			while (next == null && wrapped.hasNext()) {
				next = wrapped.next();
				if (hasHashtag(next, hashtag))
					return true;
				next = null;
			}
			return (next != null);
		}
		@Override public DMS next() {
			DMS n = next;
			next = null;
			return n;
		}
		@Override public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/** Get a DMS iterator for a given hashtag */
	static public Iterator<DMS> hashtagIterator(String ht) {
		return new DmsHashtagIterator(ht, iterator());
	}

	/** Find all DMS with a given hashtag */
	static public Set<DMS> findAllTagged(String ht) {
		TreeSet<DMS> signs = new TreeSet<DMS>(
			new NumericAlphaComparator<DMS>());
		Iterator<DMS> it = hashtagIterator(ht);
		while (it.hasNext())
			signs.add(it.next());
		return signs;
	}

	/** Check if a DMS has a hashtag */
	static public boolean hasHashtag(DMS dms, String hashtag) {
		if (hashtag != null) {
			for (String tag: dms.getHashtags()) {
				if (hashtag.equalsIgnoreCase(tag))
					return true;
			}
		}
		return false;
	}

	/** Check if a DMS is hidden (#Hidden hashtag) */
	static public boolean isHidden(DMS dms) {
		return hasHashtag(dms, "#Hidden");
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
		for (String tag: dms.getHashtags()) {
			for (String ht: DEDICATED_PURPOSE_TAGS) {
				if (ht.equalsIgnoreCase(tag))
					return false;
			}
		}
		return true;
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
		SignMessage sm = dms.getMsgCurrent();
		return (sm != null)
		      ? IncidentHelper.lookupOriginal(sm.getIncident())
		      : null;
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

	/** Get distance in meters between a WeatherSensor and a DMS.
	 *  Returns null if distance is unknown. */
	static public Integer calcDistanceMeters(WeatherSensor ws, DMS dms) {
		if ((ws == null) || (dms == null))
			return null;
		GeoLoc g1 = ws.getGeoLoc();
		GeoLoc g2 = dms.getGeoLoc();
		Distance d = GeoLocHelper.distanceTo(g1, g2);
		return (d == null) ? null : d.round(Distance.Units.METERS);
	}

	/** Find the WeatherSensor that's closest to the DMS.
	 * @param dms
	 * @param bAny If true, returns the closest
	 * weather sensor, regardless of distance.
	 * If false, returns the closest weather sensor
	 * less than RWIS_AUTO_MAX_M meters away. 
	 * @return A two-Object-array containing the
	 *  WeatherSensor and the distance to that
	 *  WeatherSensor in meters as an Integer.
	 *  If no WeatherSensor qualifies, returns
	 *  a two Object array containing nulls. 
	 */
	public static Object[] findClosestWeatherSensor(DMS dms, boolean bAny) {
		Object[] retVals = new Object[2];
		retVals[0] = null;
		retVals[1] = null;
		int cd;
		if (bAny)
			cd = Integer.MAX_VALUE;
		else
			cd = SystemAttrEnum.RWIS_AUTO_MAX_M.getInt() + 1;
		WeatherSensor closestWs = null;
		WeatherSensor ws;
		Integer closestDist = cd;
		Integer dist;
		Iterator<WeatherSensor> it = WeatherSensorHelper.iterator();
		while (it.hasNext()) {
			ws = it.next();
			if (WeatherSensorHelper.isSampleExpired(ws)) {
				//FIXME:  Don't skip if we have test data for this WeatherSensor.
				continue; // Skip those where sample has expired.
			}
			dist = calcDistanceMeters(ws, dms);
			if ((dist == null) || (dist > closestDist))
				continue;
			if (dist < closestDist) {
				closestDist = dist;
				closestWs   = ws;
			}
		}
		if (closestWs != null) {
			retVals[0] = closestWs;
			retVals[1] = closestDist;
		}
		return retVals;
	}

	public static Object[] findClosestWeatherSensor(DMS dms) {
		return findClosestWeatherSensor(dms, false);
	}
		
	/** Convert WeatherSensorOverride string (semicolon
	 * separated list of WeatherSensor names) into an
	 * ArrayList of WeatherSensors.
	 * 
	 * Throws TMSException if one of the listed
	 * WeatherSensors doesn't exist.
	 * Otherwise, returns an ArrayList of WeatherSensors.
	 * @param wsNames
	 * @return ArrayList of WeatherSensors
	 * @throws TMSException
	 */
	static public ArrayList<WeatherSensor> parseWeatherSensorList(String wsNames) throws TMSException {
		ArrayList<WeatherSensor> wsList = new ArrayList<WeatherSensor>();
		if ((wsNames == null) || wsNames.isEmpty())
			return wsList;
		wsNames = wsNames.trim();
		if (wsNames.isEmpty())
			return wsList;
		String[] wsNamesArray = wsNames.split(";");
		Arrays.parallelSetAll(wsNamesArray, (i) -> wsNamesArray[i].trim());
		WeatherSensor ws;
		for (String name : wsNamesArray) {
			if (name.isEmpty())
				continue;
			ws = WeatherSensorHelper.lookup(name);
			if (ws == null)
				throw new ChangeVetoException("Unknown WeatherSensor name: "+name);
			wsList.add(ws);
		}
		return wsList;
	}
	
	/** Get array-list of WeatherSensors associated
	 *  with a DMS.  Returns empty list if no
	 *  WeatherSensors are associated. */
	static public ArrayList<WeatherSensor> getAssociatedWeatherSensors(DMS dms) {
		ArrayList<WeatherSensor> wsList = new ArrayList<WeatherSensor>();
		if (dms == null)
			return wsList;
		WeatherSensor ws;
		String wsOverride = dms.getWeatherSensorOverride().trim();
		if ((wsOverride != null) && !wsOverride.isEmpty()) {
			// Get WeatherSensor name(s) from dms.weatherSensorOverride...
			try {
				wsList = parseWeatherSensorList(wsOverride);
			} catch (TMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			// Find closest WeatherSensor...
			Object[] o = findClosestWeatherSensor(dms);
			WeatherSensor closestEss  = (WeatherSensor) o[0];
			if (closestEss != null)
				wsList.add(closestEss);
		}
		return wsList;
	}
}
