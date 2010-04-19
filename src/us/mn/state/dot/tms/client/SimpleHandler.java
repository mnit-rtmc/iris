/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
import javax.naming.AuthenticationException;
import javax.swing.SwingUtilities;
import us.mn.state.dot.sched.ExceptionHandler;
import us.mn.state.dot.tms.client.widget.ExceptionDialog;

/**
 * A simple exception handler.
 *
 * @author Douglas Lau
 */
public class SimpleHandler implements ExceptionHandler {

	/** Exception dialog */
	protected ExceptionDialog dialog = new ExceptionDialog();

	/** Number of failed login attempts */
	protected int n_failed_login = 0;

	/** Get a count of failed login attempts */
	public int getFailedLoginCount() {
		return n_failed_login;
	}

	/** Handle an exception */
	public boolean handle(final Exception e) {
		if(e instanceof AuthenticationException)
			n_failed_login++;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				dialog.show(e);
			}
		});
		return true;
	}

	/** Set the owner frame */
	public void setOwner(Frame f) {
		dialog.setVisible(false);
		dialog.dispose();
		dialog = new ExceptionDialog(f);
	}
}
