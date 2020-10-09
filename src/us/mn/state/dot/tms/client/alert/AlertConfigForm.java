/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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

package us.mn.state.dot.tms.client.alert;

import us.mn.state.dot.tms.IpawsAlertConfig;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying and editing (IPAWS) alert configurations.
 *
 * @author Gordon Parikh
 */
@SuppressWarnings("serial")
public class AlertConfigForm extends ProxyTableForm<IpawsAlertConfig> {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.isWritePermitted(IpawsAlertConfig.SONAR_TYPE);
	}
	
	/** Create a new alert config form */
	public AlertConfigForm(Session s) {
		super(I18N.get("alert.config"), new AlertConfigPanel(
				new AlertConfigModel(s)));
	}
}
