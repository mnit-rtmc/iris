/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  SRF Consulting Group
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

import us.mn.state.dot.tms.utils.UniqueNameCreator;

/**
 * Helper class for CameraTemplate objects.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class CameraTemplateHelper extends BaseHelper {

	/** Name creator */
	static UniqueNameCreator UNC;
	static {
		UNC = new UniqueNameCreator("CAM_TMPLT_%d", (n)->lookup(n));
		UNC.setMaxLength(20);
	}

	/** Create a unique camera-template record name */
	static public String createUniqueName() {
		return UNC.createUniqueName();
	}

	/** Don't allow instances to be created */
	private CameraTemplateHelper() {
		assert false;
	}

	/** Lookup the CameraTemplate with the specified name */
	static public CameraTemplate lookup(String name) {
		return (CameraTemplate) namespace.lookupObject(
			CameraTemplate.SONAR_TYPE, name);
	}

	/** Get a CameraTemplate iterator */
	static public Iterator<CameraTemplate> iterator() {
		return new IteratorWrapper<CameraTemplate>(namespace.iterator(
			CameraTemplate.SONAR_TYPE));
	}
}
