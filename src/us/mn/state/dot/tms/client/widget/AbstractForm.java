/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2014  Minnesota Department of Transportation
 * Copyright (C) 2018       SRF Consulting Group
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

import javax.swing.JPanel;

/**
 * An abstract form is a panel which contains a titled form. Most of the work
 * is done in the initialize method so the desktop can check the title before
 * initializing a duplicate form.
 *
 * @author Douglas Lau
 * @author John L. Stanley - SRF Consulting
 */
abstract public class AbstractForm extends JPanel {

	/** Form title */
	protected String title;

	/** Form resizable flag */
	private final boolean resizable;

	/** Create a new abstract form */
	protected AbstractForm(String t) {
		title = t;
		resizable = false;
	}

	/** Create a new abstract form */
	protected AbstractForm(String t, boolean resiz) {
		title = t;
		resizable = resiz;
	}

	/** Get the title of the form */
	public String getTitle() {
		return title;
	}

	/** Get the resizable flag for the form */
	public boolean isResizable() {
		return resizable;
	}

	/** Initialize the form */
	protected void initialize() {
		setBorder(Widgets.UI.border);
	}

	/** Dispose of the form */
	protected void dispose() {
		removeAll();
	}

	/** Close the form */
	protected void close(SmartDesktop desktop) {
		desktop.closeForm(this);
	}

	/** Help page name */
	private String helpPageName;

	/** Get the help page name for the form */
	public final String getHelpPageName() {
		return helpPageName;
	}

	/** Set the help page name for the form */
	public final void setHelpPageName(String n) {
		helpPageName = n;
	}
}
