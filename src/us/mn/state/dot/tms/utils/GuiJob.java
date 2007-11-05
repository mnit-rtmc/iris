/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2006  Minnesota Department of Transportation
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

import java.awt.Cursor;
import javax.swing.JComponent;
import us.mn.state.dot.tms.Scheduler;

/**
 * GuiJob is the superclass of all GUI swing jobs, like ActionJob and ItemJob.
 *
 * @author Douglas Lau
 */
abstract public class GuiJob extends AbstractJob {

	/** Wait cursor */
	static protected final Cursor WAIT_CURSOR =
		Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

	/** Form associated with this job */
	protected final JComponent form;

	/** Component associated with this job */
	protected final JComponent component;

	/** Create a new GUI job */
	protected GuiJob(Scheduler s, JComponent f, JComponent c) {
		super(s);
		form = f;
		component = c;
	}

	/** Create a new GUI job */
	protected GuiJob(JComponent f, JComponent c) {
		this(WORKER, f, c);
	}

	/** Create a new GUI job with no form */
	protected GuiJob(JComponent c) {
		this(WORKER, null, c);
	}

	/** Create a new GUI job for an alternate scheduler */
	protected GuiJob(Scheduler s, JComponent c) {
		this(s, null, c);
	}

	/** Start the job */
	protected void start() {
		if(component != null)
			component.setEnabled(false);
		if(form != null)
			form.setCursor(WAIT_CURSOR);
	}

	/** Complete the job */
	public void complete() {
		if(component != null)
			component.setEnabled(true);
		if(form != null)
			form.setCursor(null);
	}
}
