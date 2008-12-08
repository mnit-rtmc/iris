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
package us.mn.state.dot.tms.utils;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import us.mn.state.dot.tms.Login;
import us.mn.state.dot.tms.DeviceList;
import us.mn.state.dot.tms.TMS;
import us.mn.state.dot.tms.TMSObject;

/**
 * Client-side proxy for the TMS server object.
 *
 * @author Douglas Lau
 */
public class TMSProxy {

	/** Remote TMS */
	protected final TMS tms;

	/** Lane Control Signal list */
	protected final RemoteListModel lcss;

	/** Get the lane control signal list */
	public RemoteListModel getLCSList() { return lcss; }

	/** LCS list */
	protected final DeviceList lcs_list;

	/** Available LCS list */
	protected final RemoteListModel availableLCSs;

	/** Get the available LCS list */
	public RemoteListModel getAvailableLCSs() {
		return availableLCSs;
	}

	/** Create a new TMS proxy */
	public TMSProxy(String server, String user) throws RemoteException,
		NotBoundException, MalformedURLException
	{
		Login l = (Login)Naming.lookup("//" + server + "/login");
		tms = l.login(user);
		lcs_list = tms.getLCSList();
		lcss = new RemoteListModel(lcs_list);
		availableLCSs = new RemoteListModel(
			lcs_list.getAvailableList());
	}

	/** Dispose of all proxied lists */
	public void dispose() {
		availableLCSs.dispose();
	}

	/** Get a TMSObject */
	public TMSObject getTMSObject(int vaultOID){
		try{
			return tms.getObject(vaultOID);
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
}
