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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Helper class for CameraVidSourceOrder objects
 * 
 * @author John L. Stanley - SRF Consulting
 *
 */
public class CameraVidSourceOrderHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private CameraVidSourceOrderHelper() {
		assert false;
	}

	/** Lookup the camera video source order object with the specified name */
	static public CameraVidSourceOrder lookup(String name) {
		return (CameraVidSourceOrder)namespace.lookupObject(CameraVidSourceOrder.SONAR_TYPE,
			name);
	}
	
	/** Lookup the camera video source order object with the specified camera
	 *  template and source order. Required in some cases because the source
	 *  order of an object may change while the name will not.
	 */
	static public CameraVidSourceOrder lookupFromTemplateOrder(
			String camTemplateName, int srcOrder) {
		Iterator<CameraVidSourceOrder> it = iterator();
		while (it.hasNext()) {
			CameraVidSourceOrder cvo = it.next();
			if (cvo.getCameraTemplate() == camTemplateName
					&& cvo.getSourceOrder() == srcOrder)
				return cvo;
		}
		return null;
	}
	
	/** Return a list of CameraVidSourceOrder objects associated with the
	 *  given VidSourceTemplate named vidSrcName. If none are found, an empty
	 *  list is returned.
	 */
	static public ArrayList<CameraVidSourceOrder> listForVidSource(
			String vidSrcName) {
		ArrayList<CameraVidSourceOrder> cvos =
				new ArrayList<CameraVidSourceOrder>();
		Iterator<CameraVidSourceOrder> it = iterator();
		while (it.hasNext()) {
			CameraVidSourceOrder cvo = it.next();
			if (cvo.getVidSourceTemplate().equals(vidSrcName))
				cvos.add(cvo);
		}
		return cvos;
	}
	
	/** Return a list of CameraVidSourceOrder objects associated with the
	 *  given CameraTemplate named camTemplateName. If none are found, an
	 *  empty list is returned.
	 */
	static public ArrayList<CameraVidSourceOrder> listForCameraTemplate(
			String camTemplateName) {
		ArrayList<CameraVidSourceOrder> cvos =
				new ArrayList<CameraVidSourceOrder>();
		Iterator<CameraVidSourceOrder> it = iterator();
		while (it.hasNext()) {
			CameraVidSourceOrder cvo = it.next();
			if (cvo.getCameraTemplate().equals(camTemplateName))
				cvos.add(cvo);
		}
		return cvos;
	}
	
	/** Return the first available name for a CameraVidSourceOrder object
	 *  associated with the given CameraTemplate name. Note that we won't
	 *  allow more than 10,000 objects due to the database name length limit
	 *  (though this is just an artificial limit).
	 */
	static public String getFirstAvailableName(String camTemplateName) {
		for (int i = 0; i <= 9999; ++i) {
			// generate a name with this number and try to lookup an object
			String n = camTemplateName + "_" + String.valueOf(i);
			CameraVidSourceOrder cvo = lookup(n);
			
			// if we don't get one, return the name
			if (cvo == null)
				return n;
		}
		return null;
	}
	
	/** Get a camera video source order object iterator */
	static public Iterator<CameraVidSourceOrder> iterator() {
		return new IteratorWrapper<CameraVidSourceOrder>(namespace.iterator(
				CameraVidSourceOrder.SONAR_TYPE));
	}
}
