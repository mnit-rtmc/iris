/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.SwingRunner;
import us.mn.state.dot.tms.client.widget.ExceptionDialog;

/**
 * An exception handler which displays a dialog.
 *
 * @author Douglas Lau
 */
public class DialogHandler extends SimpleHandler {

	/** Exception dialog */
	protected ExceptionDialog dialog = new ExceptionDialog();

	/** Handle an exception */
	public boolean handle(final Exception e) {
		SwingRunner.invoke(new Runnable() {
			public void run() {
				dialog.show(e);
			}
		});
		return super.handle(e);
	}

	/** Set the owner frame */
	public void setOwner(Frame f) {
		dialog.setVisible(false);
		dialog.dispose();
		dialog = new ExceptionDialog(f);
	}
}
