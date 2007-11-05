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
import us.mn.state.dot.tms.SortedList;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.device.DeviceHandlerImpl;
import us.mn.state.dot.tms.client.proxy.TmsMapLayer;
import us.mn.state.dot.tms.client.proxy.TmsMapProxy;

/**
 * Warning sign handler
 *
 * @author Douglas Lau
 */
public class WarningSignHandler extends DeviceHandlerImpl {

	/** Get the proxy type name of the handler */
	public String getProxyType() {
		return WarningSignProxy.PROXY_TYPE;
	}

	/** Create a new warning sign handler */
	protected WarningSignHandler(TmsConnection c, SortedList warn_list)
		throws RemoteException
	{
		super(c, warn_list, new WarningSignTheme());
		initialize();
	}

	/** Load a warning sign proxy by id */
	protected TmsMapProxy loadProxy(Object id) throws RemoteException {
		WarningSign sign = (WarningSign)r_list.getElement((String)id);
		return new WarningSignProxy(sign);
	}

	/** Create the warning sign layer */
	static public TmsMapLayer createLayer(TmsConnection c)
		throws RemoteException
	{
		SortedList warn_list = (SortedList)
			(c.getProxy().getWarningSignList().getList());
		return new TmsMapLayer(new WarningSignHandler(c,
			warn_list));
	}
}
