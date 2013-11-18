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
import us.mn.state.dot.sched.ExceptionHandler;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.MainClient;
import us.mn.state.dot.tms.utils.I18N;

/**
 * IRIS Action, which sets i18n'd values and handles exceptions.
 *
 * @author Douglas Lau
 */
abstract public class IAction2 extends AbstractAction {

	/** Get the exception handler */
	static private ExceptionHandler getHandler() {
		return MainClient.getHandler();
	}

	/** System attribute to enable action */
	private final SystemAttrEnum attr;

	/** Create a new action.
	 * @param text_id Text ID of I18N string.
	 * @param sa Boolean attribute to determine if the action is enabled. */
	protected IAction2(String text_id, SystemAttrEnum sa) {
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
	protected IAction2(String text_id) {
		this(text_id, null);
	}

	/** Is the action enabled by system attribute? */
	public boolean getIEnabled() {
		if(attr != null)
			return attr.getBoolean();
		else
			return true;
	}

	/** Perform the action */
	@Override
	public final void actionPerformed(ActionEvent ev) {
		long start = System.currentTimeMillis();
		try {
			doActionPerformed(ev);
		}
		catch(Exception ex) {
			getHandler().handle(ex);
		}
		finally {
			long e = System.currentTimeMillis() - start;
			if(e > 50) {
				System.err.println("IAction took " + e + " ms");
				System.err.println("  from: " + getClass());
			}
		}
	}

	/** Actually perform the action */
	abstract protected void doActionPerformed(ActionEvent ev)
		throws Exception;
}
