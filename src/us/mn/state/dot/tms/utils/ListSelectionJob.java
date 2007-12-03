/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.utils;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * ListSelectionJob is a simple extension/replacement for ListSelectionListener
 * which passes off a job to a scheduler.
 *
 * @author Douglas Lau
 */
abstract public class ListSelectionJob extends GuiJob
	implements ListSelectionListener
{
	/** Most recent event */
	protected ListSelectionEvent event;

	/** Create a new list selection job */
	public ListSelectionJob(JComponent f, JList c) {
		super(f, null);
		c.addListSelectionListener(this);
	}

	/** Create a new list selection job */
	public ListSelectionJob(JComponent f, ListSelectionModel m) {
		super(f, null);
		m.addListSelectionListener(this);
	}

	/** List selection changed (from ListSelectionListener interface) */
	public void valueChanged(ListSelectionEvent e) {
		start();
		event = e;
		addToScheduler();
	}
}
