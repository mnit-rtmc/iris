/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2018  SRF Consulting Group
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

package us.mn.state.dot.tms.server.reports;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Iterator;

import us.mn.state.dot.tms.RptGenEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.reports.RptRequest;
import us.mn.state.dot.tms.reports.RptResults;
import us.mn.state.dot.tms.server.SQLConnection;

/**
 * This is the parent class for
 * classes that generate reports.
 *
 * @author John L. Stanley - SRF Consulting
 */
public abstract class RptGen {

	/** Returns RptGenEnum that corresponds to this class. */
	abstract public RptGenEnum getEnum();

	//-------------------------------------------
	
	/** Get report-generator name to be displayed in GUI checklist.
	 * - Must be implemented by each child generator class. */
	private String getGuiName() {
		return getEnum().getGuiName();
	};

	//-------------------------------------------
	
	/** Strip all characters up to the last dot */
	static private String stripToLastDot(String v) {
		int i = v.lastIndexOf('.');
		return (i >= 0) ? v.substring(i + 1) : v;
	}

	/** Get short-name (class name) of generator. */
	public String getName() {
		return stripToLastDot(getClass().getName());
	}
	
	//-------------------------------------------
	
	/** Uses info from request to generate report.
	 * Adds generated RptResultItem(s) to rptResults.
	 * This method is ONLY run in the server context.
	 * Must be implemented by each child generator class. 
	 * 
	 * @param store     Access to database
	 * @param request   Request parameters
	 * @param results   Results to be passed back to client
	 * @throws TMSException
	 */
	public abstract void generateReport(
			SQLConnection store,
			RptRequest request,
			RptResults results)
					throws TMSException;

	//-------------------------------------------
	
	/** Helper method for building dynamic SQL
	 *  "WHERE ... and ..." strings.
	 *   
	 * For the first call, prev should be null or "".
	 *
	 * @param prev String containing previous WHERE segments
	 * @param where Conditional to add
	 * @return String containing resulting WHERE segments
	 */
	protected String addWhere(String prev, String where) {
		if (prev == null)
			prev = "";
		if ((where == null) || where.isEmpty())
			return prev;
		StringBuilder sb = new StringBuilder();
		if (prev.isEmpty())
			sb.append(" WHERE ");
		else {
			sb.append(prev);
			sb.append(" and ");
		}
		sb.append(where);
		return sb.toString();
	}

	//-------------------------------------------
	// Code to create generator objects from
	// generator GUI names...
	//-------------------------------------------

	/** LinkedHashMap containing class(es) of all
	 *  available report generators, indexed by
	 *  the generator's guiName */
	static private LinkedHashMap<String,Class<RptGen>> generatorClasses = null;

	/** Add RptGen class to generatorClasses */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static private void addGenClass(Class c) {
//		if (RptGen.class.isAssignableFrom(c)) {
			Object o;
			try {
				o = c.newInstance();
				RptGen gen = (RptGen)o;
				String key = gen.getGuiName();
				generatorClasses.put(key, c);
			} catch (Exception e) {
				e.printStackTrace();
			}
//		}
	}

	/** populate generatorClasses */
	static private void populateGeneratorMap() {
		synchronized (RptGen.class) {
			if (generatorClasses == null) {
				generatorClasses = new LinkedHashMap<String,Class<RptGen>>();

				addGenClass(RptGen_SignEvents.class);
				// <add more generators here when we get around to creating them>
			}
		}
	}

	/** create a new instance of a named report generator */
	static public RptGen newGenerator(String guiName) {
		populateGeneratorMap();
		Class<RptGen> c = generatorClasses.get(guiName);
		try {
			return c.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
