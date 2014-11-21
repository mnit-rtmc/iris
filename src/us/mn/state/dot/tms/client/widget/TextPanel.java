/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.widget;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A text panel is a simple panel to display text messages centered nicely.
 *
 * @author Douglas Lau
 */
public class TextPanel extends JPanel {

	/** Create a new text panel */
	public TextPanel() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(Widgets.UI.border);
	}

	/** Add glue to take excess space */
	public void addGlue() {
		add(Box.createVerticalGlue());
	}

	/** Add spacing between lines of text */
	public void addSpacing() {
		add(Box.createVerticalStrut(6));
	}

	/** Add a line of text to the text panel */
	public void addText(String text) {
		add(new CenteredLabel(text));
	}

	/** Centered label component */
	static protected class CenteredLabel extends Box {
		protected CenteredLabel(String s) {
			super(BoxLayout.X_AXIS);
			add(Box.createHorizontalGlue());
			add(new JLabel(s));
			add(Box.createHorizontalGlue());
		}
	}
}
