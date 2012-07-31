/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.tms.utils.I18N;

/**
 * IRIS Action.
 *
 * @author Douglas Lau
 */
abstract public class IAction extends AbstractAction {

	/** Create a new action */
	protected IAction(String text_id) {
		putValue(Action.NAME, I18N.get(text_id));
		String s = I18N.getSilent(text_id + ".tooltip");
		if(s != null)
			putValue(Action.SHORT_DESCRIPTION, s);
		String l = I18N.getSilent(text_id + ".long");
		if(l != null)
			putValue(Action.LONG_DESCRIPTION, l);
	}

	/** Schedule the action to be performed */
	public void actionPerformed(ActionEvent e) {
		new AbstractJob() {
			public void perform() throws Exception {
				do_perform();
			}
		}.addToScheduler();
	}

	/** Actually perform the action */
	abstract protected void do_perform() throws Exception;
}
