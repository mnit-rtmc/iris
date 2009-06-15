/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import javax.swing.Icon;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.client.proxy.StyleListModel;

/**
 * A model for ramp meter styles.  This is needed to listen for changes to
 * controller attributes and update the associated ramp meter style.
 *
 * @author Douglas lau
 */
public class MeterStyleModel extends StyleListModel<RampMeter> {

	/** Controller type cache */
	protected final TypeCache<Controller> controllers;

	/** Listener for controller updates */
	protected final ProxyListener<Controller> c_listener;

	/** Create a new ramp meter style model */
	public MeterStyleModel(MeterManager m, String n, Icon l,
		TypeCache<Controller> cont)
	{
		super(m, n, l);
		controllers = cont;
		c_listener = new ProxyListener<Controller>() {
			public void proxyAdded(Controller proxy) { }
			public void enumerationComplete() { }
			public void proxyChanged(final Controller proxy,
				final String attrib)
			{
				new AbstractJob() {
					public void perform() {
						controllerChanged(proxy,attrib);
					}
				}.addToScheduler();
			}
			public void proxyRemoved(Controller proxy) { }
		};
	}

	/** Initialize the model */
	public void initialize() {
		super.initialize();
		controllers.addProxyListener(c_listener);
	}

	/** Dispose of the proxy model */
	public void dispose() {
		controllers.removeProxyListener(c_listener);
		super.dispose();
	}

	/** Respond to a controller changed event */
	protected void controllerChanged(Controller c, String attrib) {
		if(attrib.equals("status") || attrib.equals("active"))
			controllerChanged(c);
	}

	/** Respond to a controller changed event */
	protected void controllerChanged(final Controller c) {
		// FIXME: a controller can have more than one ramp meter
		RampMeter proxy = find(new ProxyFinder<RampMeter>() {
			public boolean check(RampMeter proxy) {
				return proxy.getController() == c;
			}
		});
		if(proxy != null)
			proxyChangedSlow(proxy, "controller");
	}
}
