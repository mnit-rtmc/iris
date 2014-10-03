/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
 * Camera preset helper methods.
 *
 * @author Douglas Lau
 */
public class CameraPresetHelper extends BaseHelper {

	/** Disallow instantiation */
	protected CameraPresetHelper() {
		assert false;
	}

	/** Get a preset iterator */
	static public Iterator<CameraPreset> iterator() {
		return new IteratorWrapper<CameraPreset>(namespace.iterator(
			CameraPreset.SONAR_TYPE));
	}

	/** Lookup the preset with the specified name */
	static public CameraPreset lookup(String name) {
		return (CameraPreset)namespace.lookupObject(
			CameraPreset.SONAR_TYPE, name);
	}

	/** Lookup the preset with the camera / number */
	static public CameraPreset lookup(Camera c, int pn) {
		Iterator<CameraPreset> it = iterator();
		while (it.hasNext()) {
			CameraPreset cp = it.next();
			if (cp.getCamera() == c && cp.getPresetNum() == pn)
				return cp;
		}
		return null;
	}
}
