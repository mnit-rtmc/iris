/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2026  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.MsgLine;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying and editing message lines.
 *
 * @author Doug Lau
 */
public class MsgLineForm extends AbstractForm {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(MsgLine.SONAR_TYPE);
	}

	/** Message line panel */
	private final MsgLinePanel ml_panel;

	/** Create a new message line form.
	 * @param s Session. */
	public MsgLineForm(Session s) {
		super(I18N.get("msg.lines"));
		ml_panel = new MsgLinePanel(s);
	}

	/** Initialize the form */
	@Override
	public void initialize() {
		super.initialize();
		ml_panel.initialize();
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		ml_panel.dispose();
		super.dispose();
	}
}
