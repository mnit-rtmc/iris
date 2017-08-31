/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2017  Minnesota Department of Transportation
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

import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.DeviceManager;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.utils.I18N;

/**
 * The MeterManager class provides proxies for RampMeter objects.
 *
 * @author Douglas Lau
 */
public class MeterManager extends DeviceManager<RampMeter> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<RampMeter> descriptor(final Session s) {
		return new ProxyDescriptor<RampMeter>(
			s.getSonarState().getRampMeters(), true
		) {
			@Override
			public RampMeterProperties createPropertiesForm(
				RampMeter meter)
			{
				return new RampMeterProperties(s, meter);
			}
			@Override
			public RampMeterForm makeTableForm() {
				return new RampMeterForm(s);
			}
		};
	}

	/** Create a new meter manager */
	public MeterManager(Session s, GeoLocManager lm) {
		super(s, lm, descriptor(s), 15);
	}

	/** Create a ramp meter map tab */
	@Override
	public RampMeterTab createTab() {
		return new RampMeterTab(session, this);
	}

	/** Create a theme for ramp meters */
	@Override
	protected ProxyTheme<RampMeter> createTheme() {
		return new MeterTheme(this);
	}

	/** Fill single selection popup */
	@Override
	protected void fillPopupSingle(JPopupMenu p, RampMeter meter) {
		if (session.isWritePermitted(meter)) {
			if (meter.getRate() != null) {
				p.add(new ShrinkQueueAction(meter, true));
				p.add(new GrowQueueAction(meter, true));
				p.add(new TurnOffAction(meter, true));
			} else
				p.add(new TurnOnAction(meter, true));
			p.addSeparator();
		}
	}

	/** Create a popup menu for multiple objects */
	@Override
	protected JPopupMenu createPopupMulti(int n_selected) {
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel(I18N.get("ramp_meter.title") + ": " +
			n_selected));
		p.addSeparator();
		// FIXME: add turn on/off all actions
		return p;
	}

	/** Find the map geo location for a proxy */
	@Override
	protected GeoLoc getGeoLoc(RampMeter proxy) {
		return proxy.getGeoLoc();
	}

	/** Get the description of a proxy */
	@Override
	public String getDescription(RampMeter proxy) {
		return proxy.getName() + " - " +
			GeoLocHelper.getOnRampDescription(getGeoLoc(proxy));
	}
}
