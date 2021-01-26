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

import us.mn.state.dot.tms.CapUrgencyField;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying and editing CAP urgency substitution values.
 *
 * @author Gordon Parikh
 */
@SuppressWarnings("serial")
public class CapUrgencyFieldForm extends ProxyTableForm<CapUrgencyField> {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.isWritePermitted(CapUrgencyField.SONAR_TYPE);
	}

	/** Create a new CAP urgency form */
	public CapUrgencyFieldForm(Session s) {
		super(I18N.get("alert.cap.urgency_substitutions"),
			new CapUrgencyFieldPanel(new CapUrgencyFieldModel(s)));
	}
}
