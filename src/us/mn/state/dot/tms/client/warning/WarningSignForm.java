/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.warning;

import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;

/**
 * A form for displaying and editing warning signs
 *
 * @author Douglas Lau
 */
public class WarningSignForm extends ProxyTableForm<WarningSign> {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(WarningSign.SONAR_TYPE);
	}

	/** Create a new warning sign form */
	public WarningSignForm(Session s) {
		super("Warning Signs", new WarningSignModel(s));
	}

	/** Get the visible row count */
	protected int getVisibleRowCount() {
		return 12;
	}
}
