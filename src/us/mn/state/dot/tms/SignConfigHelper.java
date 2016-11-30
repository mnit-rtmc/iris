/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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

/**
 * Helper for dealing with sign configurations.
 *
 * @author Douglas Lau
 */
public class SignConfigHelper extends BaseHelper {

	/** Do not allow objects of this class */
	private SignConfigHelper() {
		assert false;
	}

	/** Lookup the sign config with the specified name */
	static public SignConfig lookup(String name) {
		return (SignConfig) namespace.lookupObject(
			SignConfig.SONAR_TYPE, name);
	}

	/** Get a sign config iterator */
	static public Iterator<SignConfig> iterator() {
		return new IteratorWrapper<SignConfig>(namespace.iterator(
			SignConfig.SONAR_TYPE));
	}

	/** Find a sign config with matching attributes.
	 * @param dt DMS type.
	 * @param p Portable flag.
	 * @param t Technology.
	 * @param sa Sign access.
	 * @param l Legend.
	 * @param bt Beacon type.
	 * @param fw Face width (mm).
	 * @param fh Face height (mm).
	 * @param bh Border -- horizontal (mm).
	 * @param bv Border -- vertical (mm).
	 * @param ph Pitch -- horizontal (mm).
	 * @param pv Pitch -- vertical (mm).
	 * @param pxw Pixel width.
	 * @param pxh Pixel height.
	 * @param cw Character width.
	 * @param ch Character height.
	 * @return Matching sign config, or null if not found. */
	static public SignConfig find(DMSType dt, boolean p, String t,
		String sa, String l, String bt, int fw, int fh, int bh, int bv,
		int ph, int pv, int pxw, int pxh, int cw, int ch)
	{
		int dti = dt.ordinal();
		Iterator<SignConfig> it = iterator();
		while (it.hasNext()) {
			SignConfig sc = it.next();
			if (sc.getDmsType() == dti &&
			    sc.getPortable() == p &&
			    t.equals(sc.getTechnology()) &&
			    sa.equals(sc.getSignAccess()) &&
			    l.equals(sc.getLegend()) &&
			    bt.equals(sc.getBeaconType()) &&
			    sc.getFaceWidth() == fw &&
			    sc.getFaceHeight() == fh &&
			    sc.getBorderHoriz() == bh &&
			    sc.getBorderVert() == bv &&
			    sc.getPitchHoriz() == ph &&
			    sc.getPitchVert() == pv &&
			    sc.getPixelWidth() == pxw &&
			    sc.getPixelHeight() == pxh &&
			    sc.getCharWidth() == cw &&
			    sc.getCharHeight() == ch)
				return sc;
		}
		return null;
	}
}
