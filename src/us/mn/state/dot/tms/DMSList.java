/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.tms.kml.Kml;
import us.mn.state.dot.tms.kml.KmlColor;
import us.mn.state.dot.tms.kml.KmlColorImpl;
import us.mn.state.dot.tms.kml.KmlFeature;
import us.mn.state.dot.tms.kml.KmlFolder;
import us.mn.state.dot.tms.kml.KmlIconStyle;
import us.mn.state.dot.tms.kml.KmlIconStyleImpl;
import us.mn.state.dot.tms.kml.KmlPlacemark;
import us.mn.state.dot.tms.kml.KmlRenderer;
import us.mn.state.dot.tms.kml.KmlStyle;
import us.mn.state.dot.tms.kml.KmlStyleImpl;
import us.mn.state.dot.tms.kml.KmlStyleSelector;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A DMS List used for KML rendering of all DMS. This class
 * is a singleton and is server side code.
 * @author Michael Darter
 */
public class DMSList implements KmlFolder {

	/** server namespace (assigned in MainServer) */
	static public ServerNamespace namespace;

	/** disallow external instantiation */
	private DMSList() {
	}

	/** single instance */
	private static DMSList m_instance = null;

	/** get single instance */
	public static DMSList get() {
		if(m_instance == null)
			m_instance = new DMSList();
		return m_instance;
	}

	/** get kml document name (KmlFolder interface) */
	public String getFolderName() {
		return I18N.get("dms.abbreviation");
	}

	/** render to kml (KmlFolder interface) */
	public String renderKml() {
		return KmlRenderer.render(this);
	}

	/** render inner elements to kml (KmlPlacemark interface) */
	public String renderInnerKml() {
		return "";
	}

	/** get style selector (KmlFolder interface) */
	public KmlStyleSelector getKmlStyleSelector() {
		return null;
	}

	/** return the kml document features (KmlFolder interface) */
	public ArrayList<KmlFeature> getKmlFeatures()
	{
		final ArrayList<KmlFeature> list = new ArrayList<KmlFeature>();
		if(namespace == null)
			return list;
		namespace.findObject(DMS.SONAR_TYPE, new Checker<DMS>() {
			public boolean check(DMS d) {
				list.add((KmlFeature)d);
				return false;
			}
		});
		return list;
	}
}
