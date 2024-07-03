/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.rwis;

import us.mn.state.dot.tms.RwisSign;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying a table of RWIS status
 * conditions for RWIS enabled signs.
 *
 * @author John L. Stanley - SRF Consulting
 */
@SuppressWarnings("serial")
public class RwisSignStatusForm extends ProxyTableForm<RwisSign> {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
//		return true;
		return s.canRead(RwisSign.SONAR_TYPE);
	}

	/** Create a new sign config form */
	public RwisSignStatusForm(Session s) {
		super(I18N.get("rwis.status"), new RwisSignModel(s));
	}
}
