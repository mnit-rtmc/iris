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

import us.mn.state.dot.sonar.Checker;

/**
 * Helper for comm links.
 * @author Michael Darter
 */
public class CommLinkHelper extends BaseHelper {

	/** Disallow instantiation */
	private CommLinkHelper() {
		assert false;
	}

	/** Get the AWS comm link.
	 *  @return The AWS comm link or null if one is not defined. If 
	 *	    AWS comm links are defined (which shouldn't be) the 
	 *	    1st one found is returned. */
	static public CommLink getAwsCommLink() {
		final CommLink[] ret = new CommLink[1];
		namespace.findObject(CommLink.SONAR_TYPE, 
			new Checker<CommLink>() 
			{
				public boolean check(CommLink c) {
					if(c.getProtocol() == 
						CommLink.PROTO_AWS) 
					{
						ret[0] = c;
						return true;
					}
					return false;
				}
			});
		return ret[0];
	}
}
