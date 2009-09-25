/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client;

import java.awt.Frame;
import javax.swing.SwingUtilities;
import us.mn.state.dot.sched.ExceptionHandler;
import us.mn.state.dot.tms.client.widget.ExceptionDialog;

/**
 * A simple exception handler.
 *
 * @author Douglas Lau
 */
public class SimpleHandler implements ExceptionHandler {

	/** Handle an exception */
	public boolean handle(final Exception e) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new ExceptionDialog(e).setVisible(true);
			}
		});
		return true;
	}

	/** Set the owner frame */
	public void setOwner(Frame f) {
		ExceptionDialog.setOwner(f);
	}
}
