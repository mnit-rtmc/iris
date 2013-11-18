/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toolbar;

import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A tool panel that controls client "edit" mode.
 *
 * @author Douglas Lau
 */
public class EditModePanel extends ToolPanel {

	/** Is this panel IRIS enabled? */
	static public boolean getIEnabled() {
		return true;
	}

	/** Get button text based on edit mode flag */
	static private String buttonText(boolean m) {
		return m ? I18N.get("mode.edit") : I18N.get("mode.view");
	}

	/** User session */
	private final Session session;

	/** Button to toggle edit mode */
	private final JToggleButton edit_btn = new JToggleButton();

	/** Create a new edit mode panel */
	public EditModePanel(Session s) {
		session = s;
		buttonChanged();
		add(edit_btn);
		edit_btn.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				buttonChanged();
			}
		});
	}

	/** Handler for button changed events */
	private void buttonChanged() {
		boolean m = edit_btn.isSelected();
		edit_btn.setText(buttonText(m));
		session.setEditMode(m);
	}
}
