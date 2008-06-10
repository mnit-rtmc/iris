/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.utils;

import java.util.Properties;

/**
 * Agency class, used in client and server to store agency specific information.
 *
 * @author Michael Darter
 */
public class Agency {

	/** valid agency ids */
	public static final String MNDOT="mndot";
	public static final String CALTRANS_D10="caltrans_d10";

	/** agency id, must match predefined values, used for internal program logic */
	protected static String m_agencyid;

	/** agency name, for descriptive purposes */
	protected static String m_agencyname;

	/** this class can't be instantiated */
	private Agency() {}

	/** read and validate agency id and agency name properties */
	public static boolean readProps(Properties props) {
		m_agencyid = props.getProperty("agencyid");
		assert m_agencyid!=null : "Error: property file: agencyid must be defined.";
		m_agencyname = props.getProperty("agencyname");
		assert m_agencyname!=null : "Error: property file: agencyname must be defined.";
		System.err.println("The agency id is: "+getId()+".");
		return Agency.validate();
	}

	/** validate */
	protected static boolean validate() {
		boolean ok=false;
		ok = ok || getId().equals(MNDOT);
		ok = ok || getId().equals(CALTRANS_D10);
		if (!ok)
			System.err.println("Error: unknown or no agencyid defined: "+
				getId()+". Use "+MNDOT+", or "+CALTRANS_D10+".");
		return ok;
	}

	/** return the agency id */
	public static String getId() {
		return m_agencyid;
	}

	/** return true if the agency id matches the argument */
	public static boolean isId(String agency) {
		return getId().equals(agency);
	}

}

