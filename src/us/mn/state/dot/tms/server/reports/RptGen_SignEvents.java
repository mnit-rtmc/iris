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

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import us.mn.state.dot.tms.RptGenEnum;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.reports.RptDeviceItem;
import us.mn.state.dot.tms.reports.RptRequest;
import us.mn.state.dot.tms.reports.RptResultItem;
import us.mn.state.dot.tms.reports.RptResults;
import us.mn.state.dot.tms.server.ResultFactory;
import us.mn.state.dot.tms.server.SQLConnection;

/**
 * Report generator for sign events.
 *
 * @author John L. Stanley - SRF Consulting
 * @author Michael Janson - SRF Consulting
 */
public class RptGen_SignEvents extends RptGen {

	/** Returns RptGenEnum that corresponds to this class. */
	@Override
	public RptGenEnum getEnum() {
		return RptGenEnum.RPTGEN_SIGN_EVENTS;
	}
	
	//-------------------------------------------
	
	/** Generate sign-event report.
	 * 
	 * Called from RptConduitImpl.generateReports().
	 * Adds generated rptResultItem(s) to rptResults.
	 * This method is ONLY run in the server context.
	 */
	public void generateReport(SQLConnection store, 
			RptRequest request, 
			final RptResults results)
					throws TMSException {

		/** Lookup all sign events for date range and device list */
		Long start_date = request.getStartDatetime();
		Long end_date   = request.getEndDatetime();
		List<RptDeviceItem> device_list = request.getDeviceList();

		// build string containing list of selected device names
		List<String> device_list_escaped = new ArrayList<String>();
		String devName;
		for (RptDeviceItem device : device_list) {
			if (!device.getSelected())
				continue;
			devName = device.getName();
			// escape single quotes in device names
			device_list_escaped.add(devName.replaceAll("'", "''"));
		}
		String str_device_list = String.join("','", device_list_escaped);
			
		// build SQL WHERE segment (if any is required)
		String sWhere = "";
		if (start_date != 0)
			sWhere = addWhere(sWhere, "event_date >= '" + new Date(start_date) + "'");
		if (end_date != 0)
			sWhere = addWhere(sWhere, "event_date < '" + new Date(end_date) + "'");
		if (!str_device_list.isEmpty())
			sWhere = addWhere(sWhere, "device_id in ('" + str_device_list + "')");

		// run query and gather results
		store.query("SELECT event_date, description, device_id, multi, msg_owner "
				+ "FROM public.sign_event_view"
				+ sWhere
				+ " ORDER BY event_date, device_id;",
			new ResultFactory() {
				public void create(ResultSet row) throws Exception {
					Timestamp tsTime = row.getTimestamp(1);	// event_date
					String sDescr    = row.getString(2);	// event_description
					String sDevName  = row.getString(3);	// device_id
					String sMulti    = row.getString(4);	// MULTI string
					String sOwner     = row.getString(5);	// message owner
					Long lTime = tsTime.getTime();
					if (sMulti != null)
						sDescr = sDescr + ": \"" + sMulti + "\"";
					if (sOwner == null)
						sOwner = "";
					results.addRptRecord(new RptResultItem(
						lTime, sDevName, sOwner, sDescr));
				}
			}
		);
	}
}
