/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.help;

import java.awt.Font;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.FormPanel;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Provides an about form for the IRIS client.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author Michael Darter
 */
public class AboutForm extends AbstractForm {

	/** Create a new About form */
	public AboutForm() {
		super(I18N.get("iris.about"));
	}

	/** Initialize the About form */
	protected void initialize() {
		FormPanel panel = new FormPanel(true);
		URL url = getClass().getResource("/images/iris.gif");
		panel.setCenter();
		panel.addRow(new JLabel(new ImageIcon(url)));
		panel.addRow(new JLabel(" "));
		panel.setCenter();
		panel.addRow(createTitle("iris.about1", 1.8f));
		panel.setCenter();
		panel.addRow(createTitle("iris.about2", 1.5f));
		panel.setCenter();
		panel.addRow(createTitle("iris.about3", 1.5f));
		panel.addRow(new JLabel(" "));
		add(panel);
	}

	/** Create a title label */
	private JLabel createTitle(String text_id, float scale) {
		return new ILabel(text_id, Font.BOLD, scale);
	}
}
