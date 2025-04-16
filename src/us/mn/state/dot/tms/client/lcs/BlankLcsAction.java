/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.LcsLock;
import us.mn.state.dot.tms.client.proxy.ProxyAction;

/**
 * Action to blank all selected LCS arrays.
 *
 * @author Douglas Lau
 */
public class BlankLcsAction extends ProxyAction<Lcs> {

	/** User ID */
	private final String user;

	/** Create a new action to blank the selected LCS array */
	public BlankLcsAction(Lcs lcs, String u) {
		super("lcs.blank", lcs);
		user = u;
	}

	/** Actually perform the action */
	@Override
	protected void doActionPerformed(ActionEvent e) {
		if (proxy != null) {
			String lk = proxy.getLock();
			if (lk != null) {
				LcsLock lock = new LcsLock(lk);
				// reason: incident or testing
				if (lock.optExpires() != null)
					proxy.setLock(null);
			}
		}
	}
}
