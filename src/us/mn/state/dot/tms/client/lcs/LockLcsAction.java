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
package us.mn.state.dot.tms.client.lcs;

import javax.swing.Action;
import javax.swing.JComboBox;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.client.proxy.ProxyAction;
import us.mn.state.dot.tms.utils.I18N;

/**
 * This action sets a lock on the selected LCS array.
 *
 * @author Douglas Lau
 */
public class LockLcsAction extends ProxyAction<LCSArray> {

	/** Lock combo box component */
	protected final JComboBox lockCmb;

	/** Create a new action to lock the selected LCS array */
	public LockLcsAction(LCSArray p, JComboBox c) {
		super(p);
		lockCmb = c;
		putValue(Action.NAME, I18N.get("lcs.locked"));
		putValue(Action.SHORT_DESCRIPTION, I18N.get(
			"lcs.locked.tooltip"));
		putValue(Action.LONG_DESCRIPTION, I18N.get("lcs.locked.long"));
	}

	/** Actually perform the action */
	protected void do_perform() {
		int s = lockCmb.getSelectedIndex();
		if(s >= 0) {
			Integer lk = new Integer(s);
			if(s == 0)
				lk = null;
			proxy.setLcsLock(lk);
		}
	}
}
