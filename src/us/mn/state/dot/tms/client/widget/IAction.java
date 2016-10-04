/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2016  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.ExceptionHandler;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.MainClient;
import static us.mn.state.dot.tms.client.widget.SwingRunner.MAX_ELAPSED;
import us.mn.state.dot.tms.utils.I18N;

/**
 * IRIS Action, which sets i18n'd values and handles exceptions.
 *
 * @author Douglas Lau
 */
abstract public class IAction extends AbstractAction {

	/** Get the exception handler */
	static private ExceptionHandler getHandler() {
		return MainClient.getHandler();
	}

	/** Log a message */
	static private void log(String msg, long e) {
		System.err.println("IAction took " + e + " ms");
		System.err.println("  from: " + msg);
	}

	/** System attribute to enable action */
	private final SystemAttrEnum attr;

	/** Adjusting count */
	private int adjusting = 0;

	/** Create a new action.
	 * @param text_id Text ID of I18N string.
	 * @param sa Boolean attribute to determine if the action is enabled. */
	protected IAction(String text_id, SystemAttrEnum sa) {
		attr = sa;
		String name = (text_id != null) ? I18N.get(text_id) : "";
		putValue(Action.NAME, name);
		int m = I18N.getKeyEvent(text_id);
		if (m != 0)
			putValue(Action.MNEMONIC_KEY, m);
		String tt = I18N.getSilent(text_id + ".tooltip");
		if (tt != null) {
			if (m != 0) {
				char c = I18N.getKeyEventChar(text_id);
				tt += " (alt-" + c + ")";
			}
			putValue(Action.SHORT_DESCRIPTION, tt);
		}
	}

	/** Create a new action.
	 * @param text_id Text ID of I18N string. */
	protected IAction(String text_id) {
		this(text_id, null);
	}

	/** Is the action enabled by system attribute? */
	public boolean getIEnabled() {
		return (attr != null) ? attr.getBoolean() : true;
	}

	/** Perform the action */
	@Override
	public final void actionPerformed(ActionEvent ev) {
		try {
			if (adjusting == 0)
				actionPerformedThrows(ev);
		}
		catch (Exception ex) {
			getHandler().handle(ex);
		}
	}

	/** Perform the action, possibly throwing an exception */
	private void actionPerformedThrows(ActionEvent ev) throws Exception {
		long st = TimeSteward.currentTimeMillis();
		try {
			doActionPerformed(ev);
		}
		finally {
			long e = TimeSteward.currentTimeMillis() - st;
			if (e > MAX_ELAPSED)
				log(getClass().toString(), e);
		}
	}

	/** Actually perform the action */
	abstract protected void doActionPerformed(ActionEvent ev)
		throws Exception;

	/** Update selected item */
	public void updateSelected() {
		adjusting++;
		doUpdateSelected();
		adjusting--;
	}

	/** Update the selected item */
	protected void doUpdateSelected() { }
}
