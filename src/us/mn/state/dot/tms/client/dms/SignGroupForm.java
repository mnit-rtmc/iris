/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.awt.GridLayout;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying and editing sign groups.
 *
 * @author Douglas Lau
 */
public class SignGroupForm extends ProxyTableForm<SignGroup> {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(SignGroup.SONAR_TYPE) &&
		       s.canRead(DmsSignGroup.SONAR_TYPE) &&
		       s.canRead(DMS.SONAR_TYPE);
	}

	/** Create a new sign group form */
	public SignGroupForm(Session s) {
		super(I18N.get("dms.group.plural"), new SignGroupPanel(s));
	}

	/** Initialize the form */
	@Override
	public void initialize() {
		super.initialize();
		setLayout(new GridLayout(1, 2));
		add(((SignGroupPanel) panel).dms_panel);
	}
}
