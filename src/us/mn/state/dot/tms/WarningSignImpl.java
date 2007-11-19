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
package us.mn.state.dot.tms;

import java.rmi.RemoteException;
import us.mn.state.dot.tms.comm.MessagePoller;
import us.mn.state.dot.tms.comm.WarningSignPoller;
import us.mn.state.dot.vault.FieldMap;

/**
 * WarningSignImpl is a traffic device can display one fixed message. It can
 * only be turned on or off.
 *
 * @author Douglas Lau
 */
public class WarningSignImpl extends TrafficDeviceImpl implements WarningSign,
	Storable
{
	/** ObjectVault table name */
	static public final String tableName = "warning_sign";

	/** Get the database table name */
	public String getTable() {
		return tableName;
	}

	/** Create a new warning sign */
	public WarningSignImpl(String i) throws ChangeVetoException,
		RemoteException
	{
		super(i);
		deviceList.add(id, this);
	}

	/** Create a warning sign from an ObjectVault field map */
	protected WarningSignImpl(FieldMap fields) throws RemoteException {
		super(fields);
	}

	/** Set the controller to which this sign is assigned */
	public void setController(ControllerImpl c) throws TMSException {
		super.setController(c);
		if(c == null)
			deviceList.add(id, this);
		else
			deviceList.remove(id);
	}

	/** Camera from which this can be seen */
	protected CameraImpl camera;

	/** Set the verification camera */
	public void setCamera(String id) throws TMSException {
		setCamera((CameraImpl)cameraList.getElement(id));
	}

	/** Set the verification camera */
	protected synchronized void setCamera(CameraImpl c)
		throws TMSException
	{
		if(c == camera)
			return;
		// FIXME: use toString() instead of getOID()
		if(c == null)
			store.update(this, "camera", "0");
		else
			store.update(this, "camera", c.getOID());
		camera = c;
	}

	/** Get verification camera */
	public TrafficDevice getCamera() { return camera; }

	/** Message text of the sign */
	protected String text;

	/** Get the message text */
	public String getText() { return text; }

	/** Set the message text */
	public synchronized void setText(String t) throws TMSException {
		if(t.equals(text))
			return;
		store.update(this, "text", t);
		text = t;
	}

	/** Flag for deployed status */
	protected transient boolean deployed;

	/** Check if the warning sign is deployed */
	public boolean isDeployed() { return deployed; }

	/** Set the actual deployed status from the controller */
	public void setDeployedStatus(boolean d) {
		if(d != deployed) {
			deployed = d;
			notifyStatus();
		}
	}

	/** Get a warning sign poller */
	protected WarningSignPoller getWarningSignPoller() {
		if(isActive()) {
			MessagePoller p = getPoller();
			if(p instanceof WarningSignPoller)
				return (WarningSignPoller)p;
		}
		return null;
	}

	/** Set the deployed status of the sign */
	public void setDeployed(boolean d) {
		WarningSignPoller p = getWarningSignPoller();
		if(p != null)
			p.setDeployed(this, d);
	}

	/** Get the current status code */
	public int getStatusCode() {
		if(!isActive())
			return STATUS_INACTIVE;
		if(isFailed())
			return STATUS_FAILED;
		if(isDeployed())
			return STATUS_DEPLOYED;
		else
			return STATUS_AVAILABLE;
	}
}
