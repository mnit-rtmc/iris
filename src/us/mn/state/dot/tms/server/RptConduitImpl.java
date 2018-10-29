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

package us.mn.state.dot.tms.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.mn.state.dot.tms.RptConduit;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.reports.RptGenItem;
import us.mn.state.dot.tms.reports.RptRequest;
import us.mn.state.dot.tms.reports.RptResults;
import us.mn.state.dot.tms.server.BaseObjectImpl;
import us.mn.state.dot.tms.server.reports.RptGen;

/**
 * A RptConduit is a client<->server communication-channel
 * for requesting and returning reports.
 * 
 * (Note that a RptConduit is a non-persistent SONAR object.)
 *
 * @author John L. Stanley - SRF Consulting
 */
public class RptConduitImpl extends BaseObjectImpl
		implements RptConduit {

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Load all reports from the database (of which there are none...).
	 * (Just registers the "rpt_conduit" type with the namespace.)
	 */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, RptConduitImpl.class);
	}

	/** Get the database table name. */
	@Override
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name",     name);
		map.put("canceled", canceled);
		map.put("request",  request);
		map.put("results",  results);
		return map;
	}

	//-------------------------------------------

	/** Create a new conduit */
	public RptConduitImpl(String n) {
		super(n);
	}

	//-------------------------------------------

	protected boolean canceled = false;
	
	@Override
	public void setCanceled(boolean b) {
		canceled = b;
	}

	@Override
	public boolean getCanceled() {
		return canceled;
	}

	//-------------------------------------------
	
	protected String request = null;
	
	@Override
	public void setRequest(String sReq) {
		request = sReq;
		if ((sReq == null) || sReq.equals("")) {
			setresults("{}");
			return;
		}

		// generate the report in a new thread
		Thread t = new Thread() {
			public void run() {
				generateReports();
			}
		};
		t.start();
	}

	@Override
	public String getRequest() {
		return request;
	}

	//-------------------------------------------
	
	protected String results = "";
	
	@Override
	public void setresults(String sRes) {
		results = sRes;
	}

	/** Set the results */
	public void doSetresults(String sRes) throws TMSException {
		if (!results.equals(sRes)) {
			setresults(sRes);
		}
	}

	/** Set the results and notify clients of the change */
	private void setresultsNotify(String sRes) throws TMSException {
		doSetresults(sRes);
		notifyAttribute("results");
	}

	@Override
	public String getResults() {
		return results;
	}

	//-------------------------------------------
	
	protected void generateReports() {
		RptRequest req = new RptRequest();
		RptResults res = new RptResults();
		RptGen gen;

		// run report generator(s)
		req.initFromReqString(request);
		for (RptGenItem it : req.getGenItemList()) {
			if (it.getSelected()) {
				try {
					gen = RptGen.newGenerator(it.getGuiName());
					gen.generateReport(store, req, res);
				} catch (Exception e) {
					res.addException(e.getMessage());
				}
			}
		}
		
		// return results
		try {
			if (!canceled) {
				String sRes = res.toResultsString();
				if ((sRes == null) || sRes.isEmpty())
					sRes = "{empty: }";
				setresultsNotify(sRes);
			}
		} catch (TMSException e) {
			e.printStackTrace();
		}
	}
	
	//-------------------------------------------
	// Suppress the following two database operations
	// (because we have NO database table).

	/** Store an object */
	@Override
	public void doStore() throws TMSException {
		// suppress the following operation
//		store.create(this);
		System.out.println("doStore: "+name);
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		// suppress the following operation
//		store.destroy(this);
		System.out.println("doDestroy: "+name);
	}
}
