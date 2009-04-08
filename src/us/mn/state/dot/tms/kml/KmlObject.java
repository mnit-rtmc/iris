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

/**
 * A Kml object base interface, which is abstract.
 *
 * The classes and interfaces in this namespace provide kml rendering
 * functionality. A subset of the kml class hierarchy is implemented (see
 * the kml reference documentation). To add kml rendering ability to 
 * an existing class, implement the KmlPlacemark interface (for points),
 * KmlFolder (for container classes), and KmlDocument. Default rendering
 * to kml is provided by KmlRenderer for interfaces that are implemented
 * by classes outside this namespace. If an interface in this namespace 
 * has an associated implementation in this namespace, it provides 
 * rendering rendering methods. The rendering methods are: renderKml() 
 * and renderInnerKml(). 
 *
 * @author Michael Darter
 * @created 11/27/08
 */
public interface KmlObject 
{
	/** render to kml */
	public String renderKml();

	/** render inner elements to kml */
	public String renderInnerKml();
}
