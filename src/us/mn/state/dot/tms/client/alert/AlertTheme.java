/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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
package us.mn.state.dot.tms.client.alert;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.IpawsAlert;
import us.mn.state.dot.tms.IpawsAlertDeployer;
import us.mn.state.dot.tms.IpawsAlertDeployerHelper;
import us.mn.state.dot.tms.IpawsAlertHelper;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.dms.DMSManager;
import us.mn.state.dot.tms.client.dms.DmsTheme;
import us.mn.state.dot.tms.client.map.MapObject;
import us.mn.state.dot.tms.client.map.Style;
import us.mn.state.dot.tms.client.map.VectorSymbol;
import us.mn.state.dot.tms.client.proxy.GeoLocManager;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;

/**
 * Theme for alert objects on the map.
 *
 * @author Gordon Parikh
 */
public class AlertTheme extends ProxyTheme<IpawsAlertDeployer> {
	
	/** Current session */
	private final Session session;
	
	/** Handle to the alert manager (just manager cast to AlertManager type)*/
	private final AlertManager aManager;

	/** Handle to GeoLoc Manager */
	private final GeoLocManager glManager;
	
	/** DMS Manager object */
	private final DMSManager dManager;
	
	/** Handle to DMS theme object */
	private DmsTheme dmsTheme;
	
	/** Handle to DMS symbol object */
	private VectorSymbol dmsSymbol;
	
	/** Styles for deployed, available, and all DMS */
	private final Style dmsDeployedStyle;
	private final Style dmsAvailableStyle;
	private final Style dmsAllStyle;

	/** Solid stroke line */
	static private final BasicStroke LINE_SOLID = new BasicStroke(8,
		BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	
	/** Active alert area outline color */
	static private final Color ACTIVE_ALERT_COLOR = Color.RED;
	
	/** Active alert area fill color */
	static private final Color ACTIVE_ALERT_FILL = new Color(255,0,0,40);
	
	/** Pending alert area outline color */
	static private final Color PENDING_ALERT_COLOR = Color.MAGENTA;
	
	/** Pending alert area fill color */
	static private final Color PENDING_ALERT_FILL =
			new Color(205, 51, 139, 40);
	
	/** Scheduled alert area outline color */
	static private final Color SCHEDULED_ALERT_COLOR = Color.ORANGE;
	
	/** Scheduled alert area fill color */
	static private final Color SCHEDULED_ALERT_FILL =
			new Color(255, 128, 0, 40);
	
	/** Past alert area outline color */
	static private final Color PAST_ALERT_COLOR = Color.DARK_GRAY;
	
	/** Past alert area fill color */
	static private final Color PAST_ALERT_FILL = new Color(105,105,105,40);
	
	public AlertTheme(AlertManager m, Session s) {
		super(m, new AlertMarker());
		
		// get handles to things we will use when drawing DMS
		session = s;
		aManager = m;
		dManager = session.getDMSManager();
		dmsTheme = (DmsTheme) dManager.getTheme();
		dmsDeployedStyle = dmsTheme.getStyle(ItemStyle.DEPLOYED);
		dmsAvailableStyle = dmsTheme.getStyle(ItemStyle.AVAILABLE);
		dmsAllStyle = dmsTheme.getStyle(ItemStyle.ALL);
		glManager = session.getGeoLocManager();
		
		addStyle(ItemStyle.PENDING, PENDING_ALERT_COLOR);
		addStyle(ItemStyle.SCHEDULED, SCHEDULED_ALERT_COLOR);
		addStyle(ItemStyle.ACTIVE, ACTIVE_ALERT_COLOR);
		addStyle(ItemStyle.INACTIVE, ProxyTheme.COLOR_INACTIVE);
		addStyle(ItemStyle.PAST, PAST_ALERT_COLOR);
		addStyle(ItemStyle.ALL, Color.WHITE);
	}
	
	/** Draw the specified map object. Overridden to do nothing so only
	 *  selected alerts are drawn.
	 */
	@Override
	public void draw(Graphics2D g, MapObject mo) { }
	
	/** Hit-test an alert */
	@Override
	public boolean hit(Point2D p, MapObject mo) {
		// if we have a selected alert already, don't deselect if the user
		// clicked inside the alert area
		if (aManager.getSelectedAlert() != null && mo instanceof MapGeoLoc) {
			// make sure this is the same deployer
			MapGeoLoc mgl = (MapGeoLoc) mo;
			GeoLoc gl = mgl.getGeoLoc();
			IpawsAlertDeployer iad = IpawsAlertDeployerHelper.
					lookup(gl.getName());
			
			// if not do whatever
			if (!aManager.getSelectedAlert().getName().equals(iad.getName()))
				return super.hit(p, mo);
			
			// lookup the alert to get the area shapes
			IpawsAlert ia = IpawsAlertHelper.lookup(
					aManager.getSelectedAlert().getAlertId());
			
			// get shapes and transform the point
			ArrayList<Shape> shapes = IpawsAlertHelper.getShapes(ia);
			for (Shape shp: shapes)
				// return if they clicked inside an alert
				return shp.contains(p);
		}
		return super.hit(p, mo);
	}
	
	/** Draw the alert on the map. The GeoLoc is the centroid of the alert and
	 *  is not drawn (really just kind of a hack to get this method to fire).
	 *  The alert area is drawn, along with DMS selected for the alert.
	 */
	@Override
	public void drawSelected(Graphics2D g, MapObject mo) {
		if (mo instanceof MapGeoLoc) {
			// get the GeoLoc from the object and use it to lookup the deployer
			MapGeoLoc mgl = (MapGeoLoc) mo;
			GeoLoc gl = mgl.getGeoLoc();
			IpawsAlertDeployer iad = IpawsAlertDeployerHelper.
					lookup(gl.getName());
			
			// draw the deployer/alert
			drawAlert(g, iad);
		}
	}
	
	/** Draw an alert deployer. */
	private void drawAlert(Graphics2D g, IpawsAlertDeployer iad) {
		// save the current transform before drawing anything
		AffineTransform t = g.getTransform();
		
		// lookup the alert to get the area polygon
		IpawsAlert ia = IpawsAlertHelper.lookup(iad.getAlertId());
		
		// create a set of Shape objects from the MultiPolygon in the alert
		// TODO need to make sure the PostGIS JAR makes it to the client
		// (and actually works) in WebStart
		ArrayList<Shape> shapes = IpawsAlertHelper.getShapes(ia);
		
		// check the style with the deployer - past alerts will be a
		// different color and alert style will trigger different behavior
		// past and inactive alerts are gray
		Color outline = PAST_ALERT_COLOR;
		Color fill = PAST_ALERT_FILL;
		ItemStyle alertState = null;
		if (manager.checkStyle(ItemStyle.ACTIVE, iad)) {
			// active alerts are red
			outline = ACTIVE_ALERT_COLOR;
			fill = ACTIVE_ALERT_FILL;
			alertState = ItemStyle.ACTIVE;
		} else if (manager.checkStyle(ItemStyle.PENDING, iad)) {
			// pending alerts are orange
			outline = PENDING_ALERT_COLOR;
			fill = PENDING_ALERT_FILL;
			alertState = ItemStyle.PENDING;
		} else if (manager.checkStyle(ItemStyle.SCHEDULED, iad)) {
			// pending alerts are orange
			outline = SCHEDULED_ALERT_COLOR;
			fill = SCHEDULED_ALERT_FILL;
			alertState = ItemStyle.SCHEDULED;
		}
		
		// draw the polygons on the graphics context
		g.setStroke(LINE_SOLID);
		for (Shape shp: shapes) {
			// draw the outline as a solid color
			g.setColor(outline);
			g.draw(shp);
			
			// fill with semi-transparent color
			g.setColor(fill);
			g.fill(shp);
		}
		
		// if an alert is pending or active and in edit mode, we will
		// draw auto DMS as deployed, optional DMS as available, and the
		// user will be able to opt to draw all DMS as all
		
		// set the scale on the theme before getting the symbol
		float scale = (float) dManager.getLayerState().getScale();
		dmsTheme.setScale(scale);
		dmsSymbol = (VectorSymbol) dmsTheme.getSymbol();
		if (alertState == ItemStyle.PENDING || (aManager.getEditing() &&
				(alertState == ItemStyle.ACTIVE
				|| alertState == ItemStyle.SCHEDULED))) {
			// draw all DMS in group (if requested), then optional DMS,
			// then auto DMS (in that order so the styles look right)
			if (aManager.getShowAllDms() && iad.getSignGroup() != null) {
				for (DMS d: DmsSignGroupHelper.getSignsInGroup(
						iad.getSignGroup()))
					drawDms(g, d.getName(), dmsAllStyle, t);
			}
			
			for (String dmsName: iad.getOptionalDms())
				drawDms(g, dmsName, dmsAvailableStyle, t);
			for (String dmsName: iad.getAutoDms())
				drawDms(g, dmsName, dmsDeployedStyle, t);
			
			
			
		} else if (alertState == ItemStyle.ACTIVE
				|| alertState == ItemStyle.SCHEDULED) {
			// for active alerts not in edit mode, draw only deployed DMS
			for (String dmsName: iad.getDeployedDms())
				drawDms(g, dmsName, dmsDeployedStyle, t);
		} else {
			// for past and inactive alerts draw only deployed DMS but using
			// "all" style
			for (String dmsName: iad.getDeployedDms())
				drawDms(g, dmsName, dmsAllStyle, t);
		}
		
	}
	
	/** Draw the DMS with the given name on the graphics context using the
	 *  specified style, resetting the transform to t afterwards.
	 */
	private void drawDms(Graphics2D g, String dmsName,
			Style style, AffineTransform t) {
		DMS dms = DMSHelper.lookup(dmsName);
		if (dms == null || dms.getGeoLoc() == null)
			return;
		
		// get a map object for this DMS and draw (potentially as selected)
		MapGeoLoc dmgl = glManager.findMapGeoLoc(dms.getGeoLoc());
		if (dms == aManager.getSelectedDms()) {
			dmsSymbol.drawSelected(g, dmgl, style);
			
			// FIXME not sure why selected DMS don't have the right style
			g.setTransform(t); // reset transform
			dmsSymbol.draw(g, dmgl, style);
		} else
			dmsSymbol.draw(g, dmgl, style);
		
		g.setTransform(t); // reset transform
	}
}
