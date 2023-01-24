/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2023  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying and editing message patterns.
 *
 * @author Doug Lau
 * @author Michael Darter
 */
public class MsgPatternForm extends ProxyTableForm<MsgPattern> {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(MsgPattern.SONAR_TYPE);
	}

	/** Create a new message pattern form.
	 * @param s Session. */
	public MsgPatternForm(Session s) {
		super(I18N.get("msg.patterns"), new MsgPatternTablePanel(s));
	}

	/** Initialize the form */
	@Override
	public void initialize() {
		super.initialize();
		setLayout(new GridLayout(1, 2));
		add(((MsgPatternTablePanel) panel).pat_pnl);
	}
}
