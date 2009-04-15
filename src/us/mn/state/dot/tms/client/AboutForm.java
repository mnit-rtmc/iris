/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client;

import java.awt.Font;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.utils.I18NMessages;

/**
 * Provides an about form for the IRIS client.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author Michael Darter
 */
public class AboutForm extends AbstractForm {

	/** 1st title line */
	static protected final String m_title1 =
		"IRIS -- Intelligent Roadway Information System v. @@VERSION@@";

	/** 2nd title line (optional) */
	static protected final String m_title2 =
		I18NMessages.get("AboutForm.Title2");

	/** 3rd title line (optional) */
	static protected final String m_title3 =
		I18NMessages.get("AboutForm.Title3");

	/** Create a new About form */
	public AboutForm() {
		super("About IRIS");
	}

	/** Initialize the About form */
	protected void initialize() {
		FormPanel panel = new FormPanel(true);
		URL url = getClass().getResource("/images/iris.gif");
		panel.setCenter();
		panel.addRow(new JLabel(new ImageIcon(url)));
		panel.addRow(new JLabel(" "));
		panel.setCenter();
		panel.addRow(createTitle(m_title1, 18));
		if(useTitle(m_title2)) {
			panel.setCenter();
			panel.addRow(createTitle(m_title2, 16));
		}
		if(useTitle(m_title3)) {
			panel.setCenter();
			panel.addRow(createTitle(m_title3, 16));
		}
		panel.addRow(new JLabel(" "));
		add(panel);
	}

	/** Create a title label */
	protected JLabel createTitle(String t, int size) {
		JLabel lbl = new JLabel(t);
		lbl.setFont(new Font("Dialog", 1, size));
		return lbl;
	}

	/** return true to use an optional title else false */
	private boolean useTitle(String t) {
		return t != null && t.length() > 0;
	}
}
