/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import us.mn.state.dot.tms.Scheduler;

/**
 * ActionJob is a simple extension/replacement for ActionListener
 * which passes off a job to a scheduler.
 *
 * @author Douglas Lau
 */
abstract public class ActionJob extends GuiJob implements ActionListener {

	/** Create a new action job */
	public ActionJob(JComponent f, AbstractButton c) {
		super(f, c);
		c.addActionListener(this);
	}

	/** Create a new action job */
	public ActionJob(JComponent f, JComboBox c) {
		super(f, c);
		c.addActionListener(this);
	}

	/** Create a new action job with no form */
	public ActionJob(AbstractButton c) {
		super(c);
		c.addActionListener(this);
	}

	/** Create a new action job for an alternate scheduler */
	public ActionJob(Scheduler s, AbstractButton c) {
		super(s, c);
		c.addActionListener(this);
	}

	/** Action performed (from ActionListener interface) */
	public void actionPerformed(ActionEvent e) {
		start();
		addToScheduler();
	}
}
