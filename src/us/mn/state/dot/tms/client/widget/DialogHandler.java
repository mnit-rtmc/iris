/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2017  Minnesota Department of Transportation
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

import us.mn.state.dot.sched.ExceptionHandler;
import us.mn.state.dot.tms.client.IrisClient;
import static us.mn.state.dot.tms.client.widget.SwingRunner.runSwing;
import us.mn.state.dot.tms.client.help.ExceptionDialog;

/**
 * An exception handler which displays a dialog.
 *
 * @author Douglas Lau
 */
public class DialogHandler implements ExceptionHandler {

	/** Exception dialog */
	private ExceptionDialog dialog = new ExceptionDialog();

	/** Handle an exception */
	@Override
	public boolean handle(final Exception e) {
		runSwing(new Runnable() {
			public void run() {
				dialog.show(e);
			}
		});
		return true;
	}

	/** Set the owner frame */
	public void setOwner(IrisClient ic) {
		dialog.setVisible(false);
		dialog.dispose();
		dialog = new ExceptionDialog(ic);
	}
}
