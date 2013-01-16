/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2013  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.Job;
import static us.mn.state.dot.tms.client.IrisClient.WORKER;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.I18N;

/**
 * IRIS Action.
 *
 * @author Douglas Lau
 */
abstract public class IAction extends AbstractAction {

	/** System attribute to enable action */
	private final SystemAttrEnum attr;

	/** Create a new action.
	 * @param text_id Text ID of I18N string.
	 * @param sa Boolean attribute to determine if the action is enabled. */
	protected IAction(String text_id, SystemAttrEnum sa) {
		attr = sa;
		String name = text_id != null ? I18N.get(text_id) : "";
		putValue(Action.NAME, name);
		int m = I18N.getKeyEvent(text_id);
		if(m != 0)
			putValue(Action.MNEMONIC_KEY, m);
		String tt = I18N.getSilent(text_id + ".tooltip");
		if(tt != null) {
			if(m != 0) {
				char c = I18N.getKeyEventChar(text_id);
				tt += " (alt-" + c + ")";
			}
			putValue(Action.SHORT_DESCRIPTION, tt);
		} else if(!name.isEmpty())
			putValue(Action.SHORT_DESCRIPTION, name);
	}

	/** Create a new action.
	 * @param text_id Text ID of I18N string. */
	protected IAction(String text_id) {
		this(text_id, null);
	}

	/** Is the action enabled by system attribute? */
	public boolean getIEnabled() {
		if(attr != null)
			return attr.getBoolean();
		else
			return true;
	}

	/** Schedule the action to be performed */
	@Override public void actionPerformed(ActionEvent e) {
		WORKER.addJob(new Job() {
			@Override public void perform() throws Exception {
				do_perform();
			}
		});
	}

	/** Actually perform the action */
	abstract protected void do_perform() throws Exception;
}
