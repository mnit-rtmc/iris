/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.rmi.RemoteException;
import javax.swing.Action;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.device.TrafficDeviceAction;

/**
 * Action to clear the selected DMS.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class ClearDmsAction extends TrafficDeviceAction {

	/** Name of logged-in user */
	protected final String userName;

	/** Create a new action to clear the selected DMS */
	public ClearDmsAction(DMSProxy p, String user) {
		super(p);
		putValue(Action.NAME, "Clear");
		putValue(Action.SHORT_DESCRIPTION, "Blank the sign.");
		putValue(Action.LONG_DESCRIPTION,
			"Remove any message from the sign.");
		userName = user;
	}

	/** Create a new action to clear the selected DMS */
	public ClearDmsAction(DMSProxy p, TmsConnection c) {
		this(p, c.getUser().getName());
	}

	/** Actually perform the action */
	protected void do_perform() throws RemoteException {
		DMSProxy p = (DMSProxy)proxy;
		p.dms.clearMessage(userName);
	}
}
