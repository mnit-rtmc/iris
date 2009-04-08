/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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

package us.mn.state.dot.tms.kml;

import java.util.ArrayList;

/**
 * A KmlFolder is implemented by classes that render kml folders.
 *
 * @author Michael Darter
 * @created 11/26/08
 * @see KmlObject
 */
public interface KmlFolder extends KmlFeature 
{
	/** get name */
	public String getFolderName();

	/** return features */
	public ArrayList<KmlFeature> getKmlFeatures();
}
