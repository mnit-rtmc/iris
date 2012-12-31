/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Berkeley Transportation Systems Inc.
 * Copyright (C) 2011-2012  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerHelper;

/**
 * This class writes the state of all controllers to an XML file.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class ControllerXmlWriter {

	/** Print the body of the controller list XML file */
	public void print(final PrintWriter out) {
		Iterator<Controller> it = ControllerHelper.iterator();
		while(it.hasNext()) {
			Controller c = it.next();
			if(c instanceof ControllerImpl)
				((ControllerImpl)c).printXml(out);
		}
	}

}
