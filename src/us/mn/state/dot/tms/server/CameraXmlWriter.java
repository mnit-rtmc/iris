/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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

import java.io.PrintWriter;
import java.util.Iterator;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;

/**
 * This class writes out the current camera configuration to an XML file.
 *
 * @author Tim Johnson
 * @author Douglas Lau
 */
public class CameraXmlWriter {

	/** Print the body of the camera list XML file */
	public void print(final PrintWriter out) {
		Iterator<Camera> it = CameraHelper.iterator();
		while(it.hasNext()) {
			Camera c = it.next();
			if(c instanceof CameraImpl)
				((CameraImpl)c).printXml(out);
		}
	}
}
