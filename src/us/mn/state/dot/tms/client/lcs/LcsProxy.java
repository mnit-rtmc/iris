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
package us.mn.state.dot.tms.client.lcs;

import java.rmi.RemoteException;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import us.mn.state.dot.tms.LCSModule;
import us.mn.state.dot.tms.LaneControlSignal;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.TrafficDevice;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.device.LogDeviceAction;
import us.mn.state.dot.tms.client.device.TrafficDeviceProxy;
import us.mn.state.dot.tms.client.proxy.PropertiesAction;

/**
 * The LcsProxy class provides a proxy representation of a LaneControlSignal
 * object.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class LcsProxy extends TrafficDeviceProxy {

	/** Proxy type name */
	static public final String PROXY_TYPE = "LCS";

	/** Get the proxy type name */
	public String getProxyType() {
		return PROXY_TYPE;
	}

	/** The LaneControlSignal that this proxy represents */
	protected final LaneControlSignal lcs;

	/** ID of the validation camera */
	protected String camera_id = "";

	/** Get the ID of the validation camera */
	public String getCameraId() {
		return camera_id;
	}

	/** Signal states */
	protected int[] signals;

	/** Create a new LCSProxy */
	public LcsProxy(LaneControlSignal lcs) throws RemoteException {
		super( lcs );
		this.lcs = lcs;
		updateUpdateInfo();
		updateStatusInfo();
	}

	/** Update the update information */
	public void updateUpdateInfo() throws RemoteException {
		super.updateUpdateInfo();
		String camera = lcs.getCamera();
		if(camera == null)
			camera_id = "";
		else
			camera_id = camera;
	}

	/** Called when status information has changed */
	public void updateStatusInfo() throws RemoteException {
		super.updateStatusInfo();
		signals = lcs.getSignals();
	}

	/** Get the number of lanes */
	public int getLanes() throws RemoteException {
		return lcs.getLanes();
	}

	/** Get the individual LCS modules */
	public LCSModule[] getModules() throws RemoteException {
		return lcs.getModules();
	}

	/** Get the states of each module */
	public int[] getSignals() {
		return signals;
	}

	/**
	 * Set the states of each module of this signal.
	 *
	 * @param states                Module states
	 * @param user                 The person who changed the module states
	 * @exception TMSException     If there is a database error
	 * @exception RemoteException  If there is an RMI error
	 */
	public void setSignals(int[] states, String user)
		throws TMSException, RemoteException
	{
		lcs.setSignals( states, user );
	}

	/** Show the properties form for the LCS */
	public void showPropertiesForm(TmsConnection tc) throws RemoteException
	{
		tc.getDesktop().show(new LcsProperties(tc, id));
	}

	/** Get a popup for this LCS */
	public JPopupMenu getPopup(TmsConnection tc) {
		JPopupMenu popup = makePopup(toString());
		popup.add(new JMenuItem(new ClearLcsAction(this, tc)));
		popup.add(new JMenuItem(new LogDeviceAction(this, tc)));
		popup.addSeparator();
		popup.add(new JMenuItem(new PropertiesAction(this, tc)));
		return popup;
	}
}
