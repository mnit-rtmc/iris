/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2007  Minnesota Department of Transportation
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
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * ChangeJob is a simple extension/replacement for ChangeListener
 * which passes off a job to a scheduler.
 *
 * @author Douglas Lau
 */
abstract public class ChangeJob extends GuiJob implements ChangeListener {

	/** Most recent change event */
	protected ChangeEvent event;

	/** Create a new change job */
	public ChangeJob(JComponent f, JSpinner s) {
		super(f, s);
		s.addChangeListener(this);
	}

	/** State changed (from ChangeListener interface) */
	public void stateChanged(ChangeEvent e) {
		start();
		event = e;
		addToScheduler();
	}
}
