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
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.SortedList;
import us.mn.state.dot.tms.utils.TMSProxy;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.device.DeviceHandlerImpl;
import us.mn.state.dot.tms.client.proxy.TmsMapLayer;
import us.mn.state.dot.tms.client.proxy.TmsMapProxy;

/**
 * Camera device handler
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class CameraHandler extends DeviceHandlerImpl {

	/** Get the proxy type name for the handler */
	public String getProxyType() {
		return CameraProxy.PROXY_TYPE;
	}

	/** Create a new camera handler */
	protected CameraHandler(TmsConnection tc, SortedList camera_list)
		throws RemoteException
	{
		super(tc, camera_list, new CameraTheme());
		addStatusModel(Camera.STATUS_AVAILABLE);
		addStatusModel(Camera.STATUS_INACTIVE);
		initialize();
	}

	/** Load a Camera Proxy by id */
	protected TmsMapProxy loadProxy(Object id) throws RemoteException {
		Camera camera = (Camera)r_list.getElement((String)id);
		return new CameraProxy(camera);
	}

	/** Create the camera layer */
	static public TmsMapLayer createLayer(TmsConnection tc)
		throws RemoteException
	{
		TMSProxy tms = tc.getProxy();
		SortedList camera_list = tms.getCameraList();
		CameraHandler handler = new CameraHandler(tc, camera_list);
		tms.setCameras(handler);
		return new TmsMapLayer(handler);
	}
}
