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
package us.mn.state.dot.tms.client.camera;

import java.rmi.RemoteException;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.device.LogDeviceAction;
import us.mn.state.dot.tms.client.device.TrafficDeviceProxy;
import us.mn.state.dot.tms.client.proxy.PropertiesAction;

/**
 * Camera proxy
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class CameraProxy extends TrafficDeviceProxy {

	/** Proxy type name */
	static public final String PROXY_TYPE = "Camera";

	/** Get the proxy type name */
	public String getProxyType() {
		return PROXY_TYPE;
	}

	/** Remote camera */
	protected final Camera camera;

	/** Create a new camera proxy */
	public CameraProxy(Camera c) throws RemoteException {
		super(c);
		camera = c;
		updateUpdateInfo();
		updateStatusInfo();
	}

	/** Get the integer id of the camera */
	public int getUID() {
		try { return Integer.parseInt(getId().substring(1)); }
		catch(NumberFormatException e) {
			return 0;
		}
	}

	/** Show the properties form for the camera */
	public void showPropertiesForm(TmsConnection tc) throws RemoteException
	{
		tc.getDesktop().show(new CameraProperties(tc, id));
	}

	/** Get a popup for this camera */
	public JPopupMenu getPopup(TmsConnection tc) {
		JPopupMenu popup = makePopup(toString());
		popup.add(new JMenuItem(new LogDeviceAction(this, tc)));
		popup.addSeparator();
		popup.add(new JMenuItem(new PropertiesAction(this, tc)));
		return popup;
	}
}
