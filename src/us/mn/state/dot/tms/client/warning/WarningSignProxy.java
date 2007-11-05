/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.warning;

import java.rmi.RemoteException;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.TrafficDevice;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.device.LogDeviceAction;
import us.mn.state.dot.tms.client.device.TrafficDeviceProxy;
import us.mn.state.dot.tms.client.proxy.PropertiesAction;

/**
 * Warning sign proxy
 *
 * @author Douglas Lau
 */
public class WarningSignProxy extends TrafficDeviceProxy
	implements WarningSign
{
	/** Proxy type name */
	static public final String PROXY_TYPE = "Warning_Sign";

	/** Get the proxy type name */
	public String getProxyType() {
		return PROXY_TYPE;
	}

	/** Warning sign that this proxy represents */
	protected final WarningSign sign;

	/** Create a new warning sign proxy */
	public WarningSignProxy(WarningSign s) throws RemoteException {
		super(s);
		sign = s;
		updateUpdateInfo();
		updateStatusInfo();
	}

	/** Text message on the sign */
	protected String text = "";

	/** Update the warning sign update information */
	public void updateUpdateInfo() throws RemoteException {
		super.updateUpdateInfo();
		text = sign.getText();
	}

	/** Get the verification camera */
	public TrafficDevice getCamera() throws RemoteException {
		return sign.getCamera();
	}

	/** Set the verification camera */
	public void setCamera(String id) throws TMSException, RemoteException {
		sign.setCamera(id);
	}

	/** Get the text of the sign */
	public String getText() {
		return text;
	}

	/** Set the text of the sign */
	public void setText(String t) throws TMSException, RemoteException {
		sign.setText(t);
	}

	/** Check if the sign is deployed */
	public boolean isDeployed() throws RemoteException {
		return sign.isDeployed();
	}

	/** Deploy the sign */
	public void setDeployed(boolean d) throws RemoteException {
		sign.setDeployed(d);
	}

	/** Show the properties form for the warning sign */
	public void showPropertiesForm(TmsConnection tc)
		throws RemoteException
	{
		tc.getDesktop().show(new WarningSignProperties(tc, id));
	}

	/** Get a popup for this LCS */
	public JPopupMenu getPopup(TmsConnection c) {
		JPopupMenu popup = makePopup(toString());
		popup.add(new JMenuItem(new DeployAction(this, true)));
		popup.add(new JMenuItem(new DeployAction(this, false)));
		popup.add(new JMenuItem(new LogDeviceAction(this, c)));
		popup.addSeparator();
		popup.add(new JMenuItem(new PropertiesAction(this, c)));
		return popup;
	}
}
