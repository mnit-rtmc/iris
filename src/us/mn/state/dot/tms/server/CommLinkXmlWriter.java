/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Berkeley Transportation Systems Inc.
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
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.CommLinkHelper;

/**
 * This class writes the state of all comm links to an XML file.
 *
 * @author Michael Darter
 */
public class CommLinkXmlWriter {

	/** Print the body of the controller list XML file */
	public void print(final PrintWriter out) {
		CommLinkHelper.find(new Checker<CommLink>() {
			public boolean check(CommLink cl) {
				if(cl instanceof CommLinkImpl)
					((CommLinkImpl)cl).printXml(out);
				return false;
			}
		});
	}

}
