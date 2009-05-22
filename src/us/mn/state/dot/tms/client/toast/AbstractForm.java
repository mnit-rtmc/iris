/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toast;

import java.awt.Cursor;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JPanel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * An abstract form is a panel which contains a titled form. Most of the work
 * is done in the initialize method so the desktop can check the title before
 * initializing a duplicate form.
 *
 * @author Douglas Lau
 */
abstract public class AbstractForm extends JPanel implements TmsForm {

	/** Form title */
	protected final String title;

	/** Help page name, which is an I18N string */
	protected String helpPageName = Help.DEFAULT_HELP_PAGE_NAME;

	/** Create a new abstract form */
	protected AbstractForm(String t) {
		title = t;
		setBorder(BORDER);
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
		fireFormClosed(new FormCloseEvent(this));
		listeners.clear();
	}

	/** The listeners of this form */
	protected final List<FormCloseListener> listeners =
		new LinkedList<FormCloseListener>();

	/** Add a FormCloseListener */
	public void addFormCloseListener(FormCloseListener l) {
		listeners.add(l);
	}

	/** Remove a FormCloseListener */
	public void removeFormCloseListener(FormCloseListener l) {
		listeners.remove(l);
	}

	/** Fire a form closed event to all listeners */
	protected void fireFormClosed(FormCloseEvent e) {
		for(FormCloseListener l: listeners) 
			l.formClosed(e);
	}

	/** get the form's help URL */
	public String getHelpPageUrl() {
		return I18N.get(helpPageName);
	}

	/** set the form's help page name, which is an I18N name */
	public void setHelpPageName(String n) {
		helpPageName = n;
	}
}
