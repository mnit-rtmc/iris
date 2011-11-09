/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.schedule;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DmsActionHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.LaneAction;
import us.mn.state.dot.tms.LaneActionHelper;
import us.mn.state.dot.tms.MeterAction;
import us.mn.state.dot.tms.MeterActionHelper;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.TimeActionHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.dms.DmsMarker;
import us.mn.state.dot.tms.client.meter.MeterMarker;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;
import us.mn.state.dot.tms.client.proxy.StyleSummary;

/**
 * A plan manager is a container for SONAR action plan objects.
 *
 * @author Douglas Lau
 */
public class PlanManager extends ProxyManager<ActionPlan> {

	/** Name of time style */
	static public final String STYLE_TIME = "Time";

	/** Name of DMS style */
	static public final String STYLE_DMS = "DMS";

	/** Name of lane style */
	static public final String STYLE_LANE = "Lane";

	/** Name of meter style */
	static public final String STYLE_METER = "Meter";

	/** Name of active style */
	static public final String STYLE_ACTIVE = "Active";

	/** Name of all style */
	static public final String STYLE_ALL = "All";

	/** Get the action plan cache */
	static protected TypeCache<ActionPlan> getCache(Session s) {
		return s.getSonarState().getActionPlans();
	}

	/** User session */
	protected final Session session;

	/** Create a new action plan manager */
	public PlanManager(Session s, GeoLocManager lm) {
		super(getCache(s), lm);
		session = s;
		cache.addProxyListener(this);
	}

	/** Get the proxy type name */
	public String getProxyType() {
		return "Action Plan";
	}

	/** Find the map geo location for a proxy */
	protected GeoLoc getGeoLoc(ActionPlan proxy) {
		return null;
	}

	/** Get a transformed marker shape */
	protected Shape getShape(AffineTransform at) {
		return null;
	}

	/** Create a styled theme for action plans */
	protected StyledTheme createTheme() {
		PlanTheme theme = new PlanTheme(this);
		theme.addStyle(STYLE_TIME, new TimeMarker());
		theme.addStyle(STYLE_DMS, new DmsMarker());
		theme.addStyle(STYLE_LANE);
		theme.addStyle(STYLE_METER, new MeterMarker());
		theme.addStyle(STYLE_ACTIVE);
		theme.addStyle(STYLE_ALL);
		return theme;
	}

	/** Show the properties form for the selected proxy */
	public void showPropertiesForm() {
		// There is no action plan properties form
	}

	/** Create a popup menu for the selected proxy object(s) */
	protected JPopupMenu createPopup() {
		return null;
	}

	/** Check the style of the specified proxy */
	public boolean checkStyle(String s, ActionPlan proxy) {
		if(STYLE_TIME.equals(s))
			return hasTimeAction(proxy);
		else if(STYLE_DMS.equals(s))
			return hasDmsAction(proxy);
		else if(STYLE_LANE.equals(s))
			return hasLaneAction(proxy);
		else if(STYLE_METER.equals(s))
			return hasMeterAction(proxy);
		else if(STYLE_ACTIVE.equals(s))
			return proxy.getActive();
		else
			return STYLE_ALL.equals(s);
	}

	/** Test if an action plan has time actions */
	private boolean hasTimeAction(final ActionPlan p) {
		return null != TimeActionHelper.find(new Checker<TimeAction>() {
			public boolean check(TimeAction ta) {
				return ta.getActionPlan() == p;
			}
		});
	}

	/** Test if an action plan has DMS actions */
	private boolean hasDmsAction(final ActionPlan p) {
		return null != DmsActionHelper.find(new Checker<DmsAction>() {
			public boolean check(DmsAction da) {
				return da.getActionPlan() == p;
			}
		});
	}

	/** Test if an action plan has lane actions */
	private boolean hasLaneAction(final ActionPlan p) {
		return null != LaneActionHelper.find(new Checker<LaneAction>() {
			public boolean check(LaneAction la) {
				return la.getActionPlan() == p;
			}
		});
	}

	/** Test if an action plan has meter actions */
	private boolean hasMeterAction(final ActionPlan p) {
		return null !=MeterActionHelper.find(new Checker<MeterAction>(){
			public boolean check(MeterAction ma) {
				return ma.getActionPlan() == p;
			}
		});
	}

	/** Create a new style summary for this proxy type */
	public StyleSummary<ActionPlan> createStyleSummary() {
		StyleSummary<ActionPlan> summary = super.createStyleSummary();
		summary.setStyle(STYLE_ALL);
		return summary;
	}

	/** Get the layer zoom visibility threshold */
	protected int getZoomThreshold() {
		return 10;
	}
}
