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
package us.mn.state.dot.tms.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.tms.kml.KmlDocument;
import us.mn.state.dot.tms.kml.KmlFolder;
import us.mn.state.dot.tms.kml.KmlFeature;
import us.mn.state.dot.tms.kml.KmlFile;
import us.mn.state.dot.tms.kml.KmlRenderer;
import us.mn.state.dot.tms.kml.KmlStyleSelector;

/**
 * Job to write out KML file periodically.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class KmlWriterJob extends Job implements KmlDocument {

	/** Seconds to offset each poll from start of interval */
	static protected final int OFFSET_SECS = 45;

	/** Create a new KML writer job */
	public KmlWriterJob() {
		super(Calendar.MINUTE, 1, Calendar.SECOND, OFFSET_SECS);
	}

	/** Perform the KML writer job */
	public void perform() throws IOException {
		KmlFile.writeServerFile(this);
	}

	/** get kml document name (KmlDocument interface) */
	public String getDocumentName() {
		return "IRIS ATMS";
	}

	/** render to kml (KmlDocument interface) */
	public String renderKml() {
		return KmlRenderer.render(this);
	}

	/** render innert elements to kml (KmlPoint interface) */
	public String renderInnerKml() {
		return "";
	}

	/** return the kml document features (KmlDocument interface) */
	public ArrayList<KmlFeature> getKmlFeatures() {
		ArrayList<KmlFeature> ret = new ArrayList<KmlFeature>();
		ret.add((KmlFolder)DMSList.get());
		return ret;
	}

	/** return kml style selector (KmlDocument interface) */
	public KmlStyleSelector getKmlStyleSelector() {
		return null;
	}
}
