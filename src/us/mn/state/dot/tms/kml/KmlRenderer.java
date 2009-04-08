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
 * This class contains default renderers for kml types.
 *
 * @author Michael Darter
 * @created 11/26/08
 * @see KmlObject
 */
public class KmlRenderer 
{
	/** newline */
	final static String R = "\n";

	/** return a rendered placemark, as below...
	 *	<Placemark>
	 *		<name>CMS 3</name>
	 *		<description>description</description>
	 *		<Point>
	 *			<coordinates>lon,lat[,alt]</coordinates>
	 *		</Point>
	 *	</Placemark>
	 */
	public static String render(KmlPlacemark pm) {
		if(pm == null)
			return "";
		StringBuilder sb = new StringBuilder();
		Kml.elementOpen(sb, "Placemark");
		Kml.element(sb, "name", pm.getPlacemarkName());
		Kml.element(sb, "description", pm.getPlacemarkDesc());
		if(pm.getGeometry() != null)
			sb.append(pm.getGeometry().renderKml());
		if(pm.getKmlStyleSelector() != null)
			sb.append(pm.getKmlStyleSelector().renderKml());
		Kml.elementClose(sb, "Placemark");
		sb.append(R);
		return sb.toString();
	}

	/** return a rendered document, as below...
	 *	<Document>
	 *		<name>CMS 3</name>
	 *		...
	 *	</Document>
	 */
	public static String render(KmlDocument doc) {
		if(doc == null)
			return "";
		StringBuilder sb = new StringBuilder();
		Kml.elementOpen(sb, "Document");
		Kml.element(sb, "name", doc.getDocumentName());

		// add features
		ArrayList<KmlFeature> list = doc.getKmlFeatures();
		if(list != null)
			for(KmlFeature f: list)
				sb.append(f.renderKml());

		Kml.elementClose(sb, "Document");
		sb.append(R);
		return sb.toString();
	}

	/** return a rendered folder, as below...
	 *	<Folder>
	 *		<name>CMS 3</name>
	 *		...
	 *	</Folder>
	 */
	public static String render(KmlFolder folder) {
		if(folder == null)
			return "";
		StringBuilder sb = new StringBuilder();
		Kml.elementOpen(sb, "Folder");
		Kml.element(sb, "name", folder.getFolderName());

		// add features
		ArrayList<KmlFeature> list = folder.getKmlFeatures();
		if(list != null)
			for(KmlFeature f: list)
				sb.append(f.renderKml());

		Kml.elementClose(sb, "Folder");
		sb.append(R);
		return sb.toString();
	}

	/** return a rendered point, as below...
	 *	<Point>
	 *		<coordinates>lon,lat[,alt]</coordinates>
	 *	</Point>
	 */
	public static String render(KmlPoint pnt) {
		if(pnt == null)
			return "";
		StringBuilder sb = new StringBuilder();
		Kml.elementOpen(sb, "Point");
		Kml.elementOpen(sb, "coordinates");
		sb.append(Double.toString(pnt.getX()));
		sb.append(",");
		sb.append(Double.toString(pnt.getY()));
		sb.append(",");
		sb.append(Double.toString(pnt.getZ()));
		Kml.elementClose(sb, "coordinates");
		Kml.elementClose(sb, "Point");
		return sb.toString();
	}
}
