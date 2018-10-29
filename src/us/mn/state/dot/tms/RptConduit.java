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

package us.mn.state.dot.tms;

import us.mn.state.dot.sonar.SonarObject;

/**
 * Conduit for transmitting a report request to the
 * server and for transmitting results back to the
 * client.  This is done (in both directions) by
 * explicitly converting the request or result to a
 * single string.  This reduces the SONAR/reflection
 * overhead of transmitting up to several megabytes
 * of data as hundreds of thousands of individual
 * SONAR fields.
 * 
 * The sReq and sResp strings are constructed/parsed
 * using RptStringListMap.
 *
 * @author John L. Stanley - SRF Consulting
 */
public interface RptConduit extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "rpt_conduit";

	//-------------------------------------------
	// cancel the operation

	void setCanceled(boolean b);

	boolean getCanceled();

	//-------------------------------------------
	// send report-request to server

	void setRequest(String sReq);

	String getRequest();

	//-------------------------------------------
	// send report-results to client

	void setresults(String sResp);

	String getResults();
}

