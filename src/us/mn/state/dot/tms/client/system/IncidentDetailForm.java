/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.system;

import us.mn.state.dot.tms.IncidentDetail;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;

/**
 * A form for displaying and editing incident details.
 *
 * @author Douglas Lau
 */
public class IncidentDetailForm extends ProxyTableForm<IncidentDetail> {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(IncidentDetail.SONAR_TYPE);
	}

	/** Create a new incident detail form */
	public IncidentDetailForm(Session s) {
		super("Incident Details", new IncidentDetailModel(s));
	}

	/** Get the visible row count */
	protected int getVisibleRowCount() {
		return 12;
	}
}
