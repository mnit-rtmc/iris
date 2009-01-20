/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.proxy.TmsMapLayer;
import us.mn.state.dot.tms.client.proxy.TmsMapProxy;

/**
 * The MeterManager class provides proxies for RampMeter objects.
 *
 * @author Douglas Lau
 */
public class MeterManager extends DeviceHandlerImpl {

	/** Get the proxy type name of the manager */
	public String getProxyType() {
		return MeterProxy.PROXY_TYPE;
	}

	/** Create a new meter manager */
	protected MeterManager(TmsConnection c, SortedList meter_list) {
		super(c, meter_list, new RampMeterTheme());
		addStatusModel(RampMeter.STATUS_AVAILABLE);
		addStatusModel(RampMeter.STATUS_LOCKED_OFF);
		addStatusModel(RampMeter.STATUS_METERING);
		addStatusModel(RampMeter.STATUS_QUEUE);
		addStatusModel(RampMeter.STATUS_QUEUE_BACKUP);
		addStatusModel(RampMeter.STATUS_CONGESTED);
		addStatusModel(RampMeter.STATUS_WARNING);
		addStatusModel(RampMeter.STATUS_LOCKED_ON);
		addStatusModel(RampMeter.STATUS_UNAVAILABLE);
		addStatusModel(RampMeter.STATUS_FAILED);
		addStatusModel(RampMeter.STATUS_INACTIVE);
		initialize();
	}

	/** Load a MeterProxy by id */
	protected TmsMapProxy loadProxy(Object id) {
		RampMeter meter = (RampMeter)r_list.getElement((String)id);
		return new MeterProxy(meter);
	}

	/** Create the ramp meter layer */
	static public TmsMapLayer createLayer(final TmsConnection c) {
		SortedList meter_list = c.getProxy().getMeterList();
		return new TmsMapLayer(new MeterManager(c, meter_list));
	}

	/** Show the properties form for the ramp meter */
	public void showPropertiesForm(TmsConnection tc) {
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
