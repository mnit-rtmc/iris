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
import us.mn.state.dot.tms.LaneControlSignal;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.SortedList;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.device.DeviceHandlerImpl;
import us.mn.state.dot.tms.client.device.NamedListModel;
import us.mn.state.dot.tms.client.device.TrafficDeviceProxy;
import us.mn.state.dot.tms.client.proxy.LocationProxy;
import us.mn.state.dot.tms.client.proxy.TmsMapLayer;
import us.mn.state.dot.tms.client.proxy.TmsMapProxy;

/**
 * The LcsHandler class provides proxies for LaneControlSignal objects.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class LcsHandler extends DeviceHandlerImpl {

	/** Get the proxy type name of the handler */
	public String getProxyType() {
		return LcsProxy.PROXY_TYPE;
	}

	/** ListModel containing all eastbound LaneControlSignals */
	protected final NamedListModel eastModel =
		new NamedListModel("East");

	/** ListModel containing all westbound LaneControlSignals */
	protected final NamedListModel westModel =
		new NamedListModel("West");

	/** Create a new LCS handler */
	protected LcsHandler(TmsConnection c, SortedList lcs_list)
		throws RemoteException
	{
		super(c, lcs_list, new LcsTheme());
		initialize();
	}

	/** Get the model containing LaneControlSignals for given direction */
	public NamedListModel getDirectionModel(short direction) {
		switch(direction) {
			case Road.EAST:
				return eastModel;
			case Road.WEST:
				return westModel;
			default:
				return null;
		}
	}

	/** Load an LcsProxy by id */
	protected TmsMapProxy loadProxy(Object id) throws RemoteException {
		LaneControlSignal lcs =
			(LaneControlSignal)r_list.getElement((String)id);
		return new LcsProxy(lcs);
	}

	public NamedListModel[] getListModels() {
		return new NamedListModel[] {
			eastModel, westModel
		};
	}

	/** Get the status list model for the specified traffic device */
	protected NamedListModel getStatusModel(TrafficDeviceProxy proxy) {
		if(proxy.isActive()) {
			LocationProxy loc = (LocationProxy)proxy.getLocation();
			return getDirectionModel(loc.getFreeDir());
		} else
			return null;
	}

	/** Create the LCS layer */
	static public TmsMapLayer createLayer(TmsConnection c)
		throws RemoteException
	{
		SortedList lcs_list = (SortedList)
			(c.getProxy().getLCSList().getList());
		return new TmsMapLayer(new LcsHandler(c, lcs_list));
	}
}
