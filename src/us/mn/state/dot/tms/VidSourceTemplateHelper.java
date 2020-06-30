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
 * @author John L. Stanley - SRF Consulting
 *
 */
public class VidSourceTemplateHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private VidSourceTemplateHelper() {
		assert false;
	}

	/** Lookup the StreamTemplate with the specified name */
	static public VidSourceTemplate lookup(String name) {
		return (VidSourceTemplate) namespace.lookupObject(VidSourceTemplate.SONAR_TYPE,
			name);
	}
	
	/** Lookup the StreamTemplate with the specified label. */
	static public VidSourceTemplate lookupLabel(String label) {
		Iterator<VidSourceTemplate> it = iterator();
		while (it.hasNext()) {
			VidSourceTemplate vst = it.next();
			if (vst.getLabel() != null && vst.getLabel().equals(label))
				return vst;
		}
		return null;
	}
	
	/** Get a StreamTemplate iterator */
	static public Iterator<VidSourceTemplate> iterator() {
		return new IteratorWrapper<VidSourceTemplate>(namespace.iterator(
			VidSourceTemplate.SONAR_TYPE));
	}

	static UniqueNameCreator unc;

	static {
		unc = new UniqueNameCreator("VID_SRC_%d", (n)->lookup(n));
		unc.setMaxLength(20);
	}
	
	/** Create a unique video-source-template record name */
	static public String createUniqueName() {
		return unc.createUniqueName();
	}
}
