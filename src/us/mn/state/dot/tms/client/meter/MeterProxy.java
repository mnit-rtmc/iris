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
package us.mn.state.dot.tms.client.meter;

import java.rmi.RemoteException;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterLock;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.device.LogDeviceAction;
import us.mn.state.dot.tms.client.device.TrafficDeviceProxy;
import us.mn.state.dot.tms.client.proxy.PropertiesAction;

/**
 * The MeterProxy class provides a proxy representation of a RampMeter object.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class MeterProxy extends TrafficDeviceProxy {

	/** Proxy type name */
	static public final String PROXY_TYPE = "Meter";

	/** Get the proxy type name */
	public String getProxyType() {
		return PROXY_TYPE;
	}

	/** RampMeter represented by the proxy */
	public final RampMeter meter;

	/** ID of the validation camera */
	protected String camera_id = "";

	/** Get the ID of the validation camera */
	public String getCameraId() {
		return camera_id;
	}

	/** Ramp meter control mode */
	protected int control_mode;

	/** Is the meter currently metering? */
	protected boolean metering;

	/** Current release rate */
	protected int release_rate;

	/** Is the meter currently locked? */
	protected boolean locked = false;

	/** Ramp meter lock */
	protected RampMeterLock lock;

	/** Queue flag */
	protected boolean queue_exists;

	/** Create a new MeterProxy */
	public MeterProxy(RampMeter rampMeter) throws RemoteException {
		super(rampMeter);
		meter = rampMeter;
		updateStatusInfo();
		updateUpdateInfo();
	}

	/** Get a string representation of the ramp meter */
	public String toString() {
		String result = getShortDescription();
		if(isLocked())
			return result + " : " + lock.getUser();
		else
			return result;
	}

	/** Get a string location of the ramp meter */
	public String getLocationString() {
		return GeoLocHelper.getMeterDescription(loc);
	}

	/** Update the ramp meter status information */
	public void updateStatusInfo() throws RemoteException {
		super.updateStatusInfo();
		metering = meter.isMetering();
		release_rate = meter.getReleaseRate();
		locked = meter.isLocked();
		if(locked)
			lock = meter.getLock();
		else
			lock = null;
		queue_exists = meter.queueExists();
	}

	/** Update the ramp meter update information */
	public void updateUpdateInfo() throws RemoteException {
		super.updateUpdateInfo();
		String camera = meter.getCamera();
		if(camera == null)
			camera_id = "";
		else
			camera_id = camera;
		control_mode = meter.getControlMode();
	}

	/** Get the ramp meter control mode */
	public int getControlMode() {
		return control_mode;
	}

	/** Is the meter currently metering? */
	public boolean isMetering() {
		return metering;
	}

	/** Does a queue exist */
	public boolean queueExists() {
		return queue_exists;
	}

	/** Get the current release rate */
	public int getReleaseRate() {
		return release_rate;
	}

	/** Is the meter currently locked? */
	public boolean isLocked() {
		return locked;
	}

	/** Get a ramp meter lock */
	public RampMeterLock getLock() {
		return lock;
	}

	/** Show the properties form for the ramp meter */
	public void showPropertiesForm(TmsConnection tc) throws RemoteException
	{
		tc.getDesktop().show(new RampMeterProperties(tc, id));
	}

	/** Get a popup for this ramp meter */
	public JPopupMenu getPopup(TmsConnection tc) {
		JPopupMenu popup = makePopup(getShortDescription());
		if(isMetering()) {
			popup.add(new JMenuItem(new ShrinkQueueAction(this)));
			popup.add(new JMenuItem(new GrowQueueAction(this)));
			popup.add(new TurnOffAction(this));
		} else
			popup.add(new TurnOnAction(this));
		JCheckBoxMenuItem litem = new JCheckBoxMenuItem(
			new LockMeterAction(this, tc.getDesktop()));
		litem.setSelected(isLocked());
		popup.add(litem);
		popup.add(new JMenuItem(new LogDeviceAction(this, tc)));
		popup.addSeparator();
		popup.add(new JMenuItem(new PropertiesAction(this, tc)));
		popup.add(new JMenuItem(new MeterDataAction(this,
			tc.getDesktop(), tc.getDataFactory())));
		return popup;
	}
}
