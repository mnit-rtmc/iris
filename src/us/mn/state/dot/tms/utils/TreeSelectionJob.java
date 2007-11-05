/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

/**
 * TreeSelectionJob is a simple extension/replacement for TreeSelectionListener
 * which passes off a job to a scheduler.
 *
 * @author Douglas Lau
 */
abstract public class TreeSelectionJob extends GuiJob
	implements TreeSelectionListener
{
	/** Most recent event */
	protected TreeSelectionEvent event;

	/** Create a new tree selection job */
	public TreeSelectionJob(JComponent f, JTree c) {
		super(f, null);
		c.addTreeSelectionListener(this);
	}

	/** Tree selection changed (from TreeSelectionListener interface) */
	public void valueChanged(TreeSelectionEvent e) {
		start();
		event = e;
		addToScheduler();
	}
}
