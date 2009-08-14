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

import java.awt.Color;
import java.awt.Shape;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterLock;
import us.mn.state.dot.tms.RampMeterQueue;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.PropertiesAction;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.proxy.StyleListModel;
import us.mn.state.dot.tms.client.proxy.TeslaAction;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/**
 * The MeterManager class provides proxies for RampMeter objects.
 *
 * @author Douglas Lau
 */
public class MeterManager extends ProxyManager<RampMeter> {

	/** Ramp meter map object shape */
	static protected final Shape SHAPE = new MeterMarker();

	/** Name of available style */
	static public final String STYLE_AVAILABLE = "Available";

	/** Name of queue full style */
	static public final String STYLE_QUEUE_FULL = "Queue full";

	/** Name of queue exists style */
	static public final String STYLE_QUEUE_EXISTS = "Queue exists";

	/** Name of metering style */
	static public final String STYLE_METERING = "Metering";

	/** Name of locked style */
	static public final String STYLE_LOCKED = "Locked";

	/** Name of maintenance style */
	static public final String STYLE_MAINTENANCE = "Maintenance";

	/** Name of failed style */
	static public final String STYLE_FAILED = "Failed";

	/** Name of "no controller" style */
	static public final String STYLE_NO_CONTROLLER = "No controller";

	/** Name of inactive style */
	static public final String STYLE_INACTIVE = "Inactive";

	/** Name of all style */
	static public final String STYLE_ALL = "All";

	/** Test if a meter is active */
	static protected boolean isActive(RampMeter proxy) {
		Controller ctr = proxy.getController();
		return ctr != null && ctr.getActive();
	}

	/** Test if a meter is available */
	static protected boolean isAvailable(RampMeter proxy) {
		return isActive(proxy) &&
		       !isFailed(proxy) &&
		       !isMetering(proxy) &&
		       !needsMaintenance(proxy);
	}

	/** Test if a meter needs maintenance */
	static protected boolean needsMaintenance(RampMeter proxy) {
		RampMeterLock lck = RampMeterLock.fromOrdinal(proxy.getMLock());
		return lck == RampMeterLock.POLICE_PANEL ||
		       lck == RampMeterLock.KNOCK_DOWN;
	}

	/** Test if a meter is metering */
	static protected boolean isMetering(RampMeter proxy) {
		return proxy.getRate() != null;
	}

	/** Test if a meter has a queue */
	static protected boolean queueExists(RampMeter proxy) {
		return isActive(proxy) &&
		       !isFailed(proxy) &&
		       proxy.getQueue() == RampMeterQueue.EXISTS.ordinal();
	}

	/** Test if a meter has a full queue */
	static protected boolean queueFull(RampMeter proxy) {
		return isActive(proxy) &&
		       !isFailed(proxy) &&
		       proxy.getQueue() == RampMeterQueue.FULL.ordinal();
	}

	/** Test if a ramp meter is failed */
	static protected boolean isFailed(RampMeter proxy) {
		Controller ctr = proxy.getController();
		return ctr != null && (!"".equals(ctr.getStatus()));
	}

	/** User session */
	protected final Session session;

	/** Create a new meter manager */
	public MeterManager(Session s, TypeCache<RampMeter> c,
		GeoLocManager lm)
	{
		super(c, lm);
		session = s;
		initialize();
	}

	/** Create a style list model for the given symbol */
	protected StyleListModel<RampMeter> createStyleListModel(Symbol s) {
		return new MeterStyleModel(this, s.getLabel(), s.getLegend(),
			session.getSonarState().getConCache().getControllers());
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return "Ramp Meter";
	}

	/** Get the shape for a given proxy */
	protected Shape getShape(RampMeter proxy) {
		return SHAPE;
	}

	/** Create a styled theme for ramp meters */
	protected StyledTheme createTheme() {
		ProxyTheme<RampMeter> theme = new ProxyTheme<RampMeter>(this,
			getProxyType(), SHAPE);
		theme.addStyle(STYLE_AVAILABLE, ProxyTheme.COLOR_AVAILABLE);
		theme.addStyle(STYLE_QUEUE_FULL, Color.ORANGE);
		theme.addStyle(STYLE_QUEUE_EXISTS, ProxyTheme.COLOR_DEPLOYED);
		theme.addStyle(STYLE_METERING, Color.GREEN);
		theme.addStyle(STYLE_LOCKED, null, ProxyTheme.OUTLINE_LOCKED);
		theme.addStyle(STYLE_MAINTENANCE, ProxyTheme.COLOR_UNAVAILABLE);
		theme.addStyle(STYLE_FAILED, ProxyTheme.COLOR_FAILED);
		theme.addStyle(STYLE_NO_CONTROLLER,
			ProxyTheme.COLOR_NO_CONTROLLER);
		theme.addStyle(STYLE_INACTIVE, ProxyTheme.COLOR_INACTIVE,
			ProxyTheme.OUTLINE_INACTIVE);
		theme.addStyle(STYLE_ALL);
		return theme;
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(String s, RampMeter proxy) {
		if(STYLE_AVAILABLE.equals(s))
			return isAvailable(proxy);
		else if(STYLE_QUEUE_FULL.equals(s))
			return queueFull(proxy);
		else if(STYLE_QUEUE_EXISTS.equals(s))
			return queueExists(proxy);
		else if(STYLE_METERING.equals(s))
			return isMetering(proxy);
		else if(STYLE_LOCKED.equals(s))
			return proxy.getMLock() != null;
		else if(STYLE_MAINTENANCE.equals(s))
			return needsMaintenance(proxy);
		else if(STYLE_FAILED.equals(s))
			return isFailed(proxy);
		else if(STYLE_NO_CONTROLLER.equals(s))
			return proxy.getController() == null;
		else if(STYLE_INACTIVE.equals(s))
			return !isActive(proxy);
		else
			return STYLE_ALL.equals(s);
	}

	/** Show the properties form for the selected proxy */
	public void showPropertiesForm() {
		if(s_model.getSelectedCount() == 1) {
			for(RampMeter meter: s_model.getSelected())
				showPropertiesForm(meter);
		}
	}

	/** Show the properteis form for the given proxy */
	protected void showPropertiesForm(RampMeter meter) {
		SmartDesktop desktop = session.getDesktop();
		try {
			desktop.show(new RampMeterProperties(session, meter));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	/** Create a popup menu for the selected proxy object(s) */
	protected JPopupMenu createPopup() {
		int n_selected = s_model.getSelectedCount();
		if(n_selected < 1)
			return null;
		if(n_selected == 1) {
			for(RampMeter meter: s_model.getSelected())
				return createSinglePopup(meter);
		}
		JPopupMenu p = new JPopupMenu();
		p.add(new JLabel("" + n_selected + " Meters"));
		p.addSeparator();
		// FIXME: add turn on/off all actions
		return p;
	}

	/** Create a popup menu for a single ramp meter selection */
	protected JPopupMenu createSinglePopup(final RampMeter meter) {
		JPopupMenu p = new JPopupMenu();
		p.add(makeMenuLabel(getDescription(meter)));
		p.addSeparator();
		if(isMetering(meter)) {
			p.add(new ShrinkQueueAction(meter));
			p.add(new GrowQueueAction(meter));
			p.add(new TurnOffAction(meter));
		} else
			p.add(new TurnOnAction(meter));
		if(TeslaAction.isConfigured()) {
			p.addSeparator();
			p.add(new TeslaAction<RampMeter>(meter));
		}
		p.addSeparator();
		p.add(new PropertiesAction<RampMeter>(meter) {
			protected void do_perform() {
				showPropertiesForm(meter);
			}
		});
		return p;
	}

	/** Find the map geo location for a proxy */
	protected GeoLoc getGeoLoc(RampMeter proxy) {
		return proxy.getGeoLoc();
	}
}
