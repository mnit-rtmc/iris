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
package us.mn.state.dot.tms.client.dms;

import java.rmi.RemoteException;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.SortedList;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.device.DeviceHandlerImpl;
import us.mn.state.dot.tms.client.proxy.TmsMapLayer;
import us.mn.state.dot.tms.client.proxy.TmsMapProxy;
import us.mn.state.dot.tms.utils.TMSProxy;

import us.mn.state.dot.tms.utils.I18NMessages;

/**
 * The DMSHandler class provides proxies for DMS objects.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class DMSHandler extends DeviceHandlerImpl {

	/** Get the proxy type name */
	public String getProxyType() {
		return (I18NMessages.get("MesgSignLabel"));
	}

	/** Create a new DMS handler */
	protected DMSHandler(TmsConnection tc, SortedList dms_list)
		throws RemoteException
	{
		super(tc, dms_list, new DmsTheme());
		addStatusModel(DMS.STATUS_AVAILABLE);
		addStatusModel(DMS.STATUS_DEPLOYED);
		addStatusModel(DMS.STATUS_TRAVEL_TIME);
		addStatusModel(DMS.STATUS_UNAVAILABLE);
		addStatusModel(DMS.STATUS_FAILED);
		addStatusModel(DMS.STATUS_INACTIVE);
		initialize();
	}

	/** Load a DMSProxy by id */
	protected TmsMapProxy loadProxy(Object id) throws RemoteException {
		DMS sign = (DMS)r_list.getElement((String)id);
		return new DMSProxy(sign);
	}

	/** Create the DMS layer */
	static public TmsMapLayer createLayer(TmsConnection tc)
		throws RemoteException
	{
		TMSProxy tms = tc.getProxy();
		SortedList dms_list = tms.getDMSList();
		DMSHandler handler = new DMSHandler(tc, dms_list);
		tms.setDMSListModel(handler);
		return new TmsMapLayer(handler);
	}
}
