/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.proxy;

import java.rmi.RemoteException;
import javax.swing.Action;
import us.mn.state.dot.tms.client.TmsConnection;

/**
 * Action to access the properties of a TMS map object.
 *
 * @author Douglas Lau
 */
public class PropertiesAction extends TmsMapAction {

	/** Connection to TMS server */
	protected final TmsConnection connection;

	/** Create a new properties action */
	public PropertiesAction(TmsMapProxy p, TmsConnection c) {
		super(p);
		connection = c;
		putValue(Action.NAME, "Properties");
		putValue(Action.SHORT_DESCRIPTION, "Access object properties");
		putValue(Action.LONG_DESCRIPTION, "Access the " +
			proxy.getProxyType() + " properties page");
	}

	/** Invoked when the action is selected */
	protected void do_perform() throws RemoteException {
		proxy.showPropertiesForm(connection);
	}
}
