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

import java.io.IOException;
import java.io.PrintWriter;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.server.ServerNamespace;

/**
 * This class writes out the current camera configuration to an XML file.
 *
 * @author Tim Johnson
 */
public class CameraXmlWriter extends XmlWriter {

	/** Camera XML file */
	static protected final String CAMERA_XML = "cameras.xml";

	/** SONAR namespace */
	protected final ServerNamespace namespace;

	/** Create a new camera XML writer */
	public CameraXmlWriter(ServerNamespace ns) {
		super(CAMERA_XML, false);
		namespace = ns;
	}

	/** Print the body of the camera list XML file */
	public void print(final PrintWriter out) {
		out.println(XML_DECLARATION);
		out.println("<camera_list>");
		namespace.findObject(Camera.SONAR_TYPE,
			new Checker<CameraImpl>()
		{
			public boolean check(CameraImpl camera) {
				camera.printXmlElement(out);
				return false;
			}
		});
		out.println("</camera_list>");
	}
}
