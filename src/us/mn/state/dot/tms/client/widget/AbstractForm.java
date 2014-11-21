/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2014  Minnesota Department of Transportation
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

import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import us.mn.state.dot.tms.client.help.Help;
import us.mn.state.dot.tms.utils.I18N;

/**
 * An abstract form is a panel which contains a titled form. Most of the work
 * is done in the initialize method so the desktop can check the title before
 * initializing a duplicate form.
 *
 * @author Douglas Lau
 */
abstract public class AbstractForm extends JPanel {

	/** Form title */
	private final String title;

	/** Create a new abstract form */
	protected AbstractForm(String t) {
		title = t;
		setBorder(Widgets.UI.border);
	}

	/** Get the title of the form */
	public String getTitle() {
		return title;
	}

	/** Initialize the form */
	abstract protected void initialize();

	/** Dispose of the form */
	protected void dispose() {}

	/** Close the form */
	protected void close() {
		JInternalFrame f = frame;
		if(f != null)
			f.dispose();
		frame = null;
	}

	/** Frame holding the form */
	private JInternalFrame frame;

	/** Set the frame holding the form */
	public final void setFrame(JInternalFrame f) {
		frame = f;
	}

	/** Help page name, which is an I18N string */
	private String helpPageName = Help.DEFAULT_HELP_PAGE_NAME;

	/** Get the form's help URL */
	public final String getHelpPageUrl() {
		return I18N.get(helpPageName);
	}

	/** Set the form's help page name, which is an I18N name */
	public final void setHelpPageName(String n) {
		helpPageName = n;
	}
}
