/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.LcsLock;
import us.mn.state.dot.tms.client.proxy.ProxyAction;

/**
 * This action sets a lock on the selected LCS array.
 *
 * @author Douglas Lau
 */
public class LockReasonAction extends ProxyAction<Lcs> {

	/** User ID */
	private final String user;

	/** Reason combo box component */
	private final JComboBox<String> reason_cbx;

	/** Create a new action to select a lock reason */
	public LockReasonAction(Lcs p, String u, JComboBox<String> c) {
		super("lcs.locked", p);
		user = u;
		reason_cbx = c;
	}

	/** Actually perform the action */
	@Override
	protected void doActionPerformed(ActionEvent e) {
		if (proxy != null) {
			String reason = (String) reason_cbx.getSelectedItem();
			if ("".equals(reason))
				reason = null;
			if (reason != null) {
				LcsLock lk = new LcsLock(proxy.getLock());
				lk.setReason(reason);
				lk.setUser(user);
				proxy.setLock(lk.toString());
			} else
				proxy.setLock(null);
		}
	}
}
