/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2019  Minnesota Department of Transportation
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
 * Helper for dealing with sign details.
 *
 * @author Douglas Lau
 */
public class SignDetailHelper extends BaseHelper {

	/** Do not allow objects of this class */
	private SignDetailHelper() {
		assert false;
	}

	/** Lookup the sign detail with the specified name */
	static public SignDetail lookup(String name) {
		return (SignDetail) namespace.lookupObject(
			SignDetail.SONAR_TYPE, name);
	}

	/** Get a sign detail iterator */
	static public Iterator<SignDetail> iterator() {
		return new IteratorWrapper<SignDetail>(namespace.iterator(
			SignDetail.SONAR_TYPE));
	}

	/** Find a sign detail with matching attributes.
	 * @param dt DMS type.
	 * @param p Portable flag.
	 * @param t Technology.
	 * @param sa Sign access.
	 * @param l Legend.
	 * @param bt Beacon type.
	 * @param mf Monochrome foreground color (24-bit).
	 * @param mb Monochrome background color (24-bit).
	 * @param hmk Hardware make.
	 * @param hmd Hardware model.
	 * @param smk Software make.
	 * @param smd Software model.
	 * @return Matching sign detail, or null if not found. */
	static public SignDetail find(DMSType dt, boolean p, String t,
		String sa, String l, String bt, int mf, int mb, String hmk,
		String hmd, String smk, String smd)
	{
		int dti = dt.ordinal();
		Iterator<SignDetail> it = iterator();
		while (it.hasNext()) {
			SignDetail sd = it.next();
			if (sd.getDmsType() == dti &&
			    sd.getPortable() == p &&
			    t.equals(sd.getTechnology()) &&
			    sa.equals(sd.getSignAccess()) &&
			    l.equals(sd.getLegend()) &&
			    bt.equals(sd.getBeaconType()) &&
			    sd.getMonochromeForeground() == mf &&
			    sd.getMonochromeBackground() == mb &&
			    hmk.equals(sd.getHardwareMake()) &&
			    hmd.equals(sd.getHardwareModel()) &&
			    smk.equals(sd.getSoftwareMake()) &&
			    smd.equals(sd.getSoftwareModel()))
			{
				return sd;
			}
		}
		return null;
	}
}
