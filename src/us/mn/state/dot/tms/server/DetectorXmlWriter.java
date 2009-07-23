/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
import java.io.PrintWriter;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.server.ServerNamespace;
import us.mn.state.dot.tms.Detector;

/**
 * This class writes out the current detector configuration to an XML file.
 *
 * @author Douglas Lau
 */
public class DetectorXmlWriter extends XmlWriter {

	/** Detector list XML file */
	static protected final String DETS_XML = "detectors.xml";

	/** SONAR namespace */
	protected final ServerNamespace namespace;

	/** Create a new detector XML writer */
	public DetectorXmlWriter(ServerNamespace ns) {
		super(DETS_XML, false);
		namespace = ns;
	}

	/** Print the body of the detector list XML file */
	public void print(final PrintWriter out) {
		namespace.findObject(Detector.SONAR_TYPE,
			new Checker<DetectorImpl>()
		{
			public boolean check(DetectorImpl det) {
				det.printXmlElement(out);
				return false;
			}
		});
	}
}
