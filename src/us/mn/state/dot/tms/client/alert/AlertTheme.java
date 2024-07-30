/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021-2024  Minnesota Department of Transportation
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
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import org.postgis.MultiPolygon;
import org.postgis.Point;
import org.postgis.Polygon;
import us.mn.state.dot.tms.AlertInfo;
import us.mn.state.dot.tms.AlertInfoHelper;
import us.mn.state.dot.tms.AlertState;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DmsActionHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.Hashtags;
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
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.geo.SphericalMercatorPosition;

/**
 * Theme for alert objects on the map.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class AlertTheme extends ProxyTheme<AlertInfo> {

	/** Solid stroke line */
	static private final BasicStroke LINE_SOLID = new BasicStroke(8,
		BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

	/** Pending alert area outline color */
	static private final Color PENDING_ALERT_COLOR = Color.MAGENTA;

	/** Pending alert area fill color */
	static private final Color PENDING_ALERT_FILL =
		new Color(205, 51, 139, 40);

	/** Active alert area outline color */
	static private final Color ACTIVE_ALERT_COLOR = Color.ORANGE;

	/** Active alert area fill color */
	static private final Color ACTIVE_ALERT_FILL =
		new Color(255, 128, 0, 40);

	/** Cleared alert area outline color */
	static private final Color CLEARED_ALERT_COLOR = Color.DARK_GRAY;

	/** Cleared alert area fill color */
	static private final Color CLEARED_ALERT_FILL = new Color(105, 105, 105,
		40);

	/** Build awt.Shape objects from a MultiPolygon, returning a list of
	 *  Shape objects with each representing a polygon. */
	static private ArrayList<Shape> getShapes(AlertInfo ai) {
		ArrayList<Shape> paths = new ArrayList<Shape>();
		if (ai == null)
			return paths;
		MultiPolygon mp = ai.getGeoPoly();
		for (Polygon poly: mp.getPolygons()) {
			// draw a path of each polygon
			GeneralPath path = new GeneralPath();
			Point p = poly.getFirstPoint();
			if (p != null) {
				SphericalMercatorPosition smp =
					getSphereMercatorPos(p);
				path.moveTo(smp.getX(), smp.getY());
			}
			for (int i = 1; i < poly.numPoints(); i++) {
				p = poly.getPoint(i);
				SphericalMercatorPosition smp =
					getSphereMercatorPos(p);
				path.lineTo(smp.getX(), smp.getY());
			}
			path.closePath();
			paths.add(path);
		}
		return paths;
	}

	/** Convert a PostGIS Point to a SphericalMercatorPosition object. */
	static private SphericalMercatorPosition getSphereMercatorPos(Point p) {
		Position pos = new Position(p.y, p.x);
		return SphericalMercatorPosition.convert(pos);
	}

	/** Current session */
	private final Session session;

	/** Handle to the alert manager */
	private final AlertManager aManager;

	/** Handle to GeoLoc Manager */
	private final GeoLocManager glManager;

	/** DMS Manager object */
	private final DMSManager dManager;

	/** Handle to DMS theme object */
	private final DmsTheme dmsTheme;

	/** Handle to DMS symbol object */
	private VectorSymbol dmsSymbol;

	/** Styles for deployed, available, and all DMS */
	private final Style dmsDeployedStyle;
	private final Style dmsAvailableStyle;
	private final Style dmsAllStyle;

	public AlertTheme(Session s, AlertManager m) {
		super(m, new AlertMarker());

		// get handles to things we will use when drawing DMS
		session = s;
		aManager = m;
		dManager = session.getDMSManager();
		dmsTheme = (DmsTheme) dManager.getTheme();
		dmsDeployedStyle = dmsTheme.getStyle(ItemStyle.EXTERNAL);
		dmsAvailableStyle = dmsTheme.getStyle(ItemStyle.AVAILABLE);
		dmsAllStyle = dmsTheme.getStyle(ItemStyle.ALL);
		glManager = session.getGeoLocManager();

		addStyle(ItemStyle.PENDING, PENDING_ALERT_COLOR);
		addStyle(ItemStyle.ACTIVE, ACTIVE_ALERT_COLOR);
		addStyle(ItemStyle.CLEARED, CLEARED_ALERT_COLOR);
		addStyle(ItemStyle.ALL, Color.WHITE);
	}

	/** Draw the specified map object.  Overridden to do nothing so only
	 *  selected alerts are drawn.  */
	@Override
	public void draw(Graphics2D g, MapObject mo) { }

	/** Hit-test an alert */
	@Override
	public boolean hit(Point2D p, MapObject mo) {
		AlertInfo sel = aManager.getSelectionModel()
			.getSingleSelection();
		// if we have a selected alert already, don't deselect if the
		// user clicked inside the alert area
		if (sel != null && mo instanceof MapGeoLoc) {
			// make sure this is the same alert
			MapGeoLoc mgl = (MapGeoLoc) mo;
			GeoLoc gl = mgl.getGeoLoc();
			AlertInfo ai = AlertInfoHelper.lookup(gl.getName());

			// if not do whatever
			if (!sel.getName().equals(ai.getName()))
				return super.hit(p, mo);

			// get shapes and transform the point
			ArrayList<Shape> shapes = getShapes(ai);
			for (Shape shp: shapes)
				// return if they clicked inside an alert
				return shp.contains(p);
		}
		return super.hit(p, mo);
	}

	/** Draw the alert on the map.  The GeoLoc is the centroid of the alert
	 *  and is not drawn (really just kind of a hack to get this method to
	 *  fire).
	 *
	 *  The alert area is drawn, along with DMS selected for the alert.
	 */
	@Override
	public void drawSelected(Graphics2D g, MapObject mo) {
		if (mo instanceof MapGeoLoc) {
			// get the GeoLoc from the object
			// and use it to lookup the alert
			MapGeoLoc mgl = (MapGeoLoc) mo;
			GeoLoc gl = mgl.getGeoLoc();
			AlertInfo ai = AlertInfoHelper.lookup(gl.getName());
			drawAlert(g, ai);
		}
	}

	/** Draw an alert */
	private void drawAlert(Graphics2D g, AlertInfo ai) {
		// save the current transform before drawing anything
		AffineTransform t = g.getTransform();

		// use alert state to set outline and fill color
		Color outline = CLEARED_ALERT_COLOR;
		Color fill = CLEARED_ALERT_FILL;
		AlertState st = AlertState.fromOrdinal(ai.getAlertState());
		switch (st) {
		case PENDING:
			outline = PENDING_ALERT_COLOR;
			fill = PENDING_ALERT_FILL;
			break;
		case ACTIVE:
			outline = ACTIVE_ALERT_COLOR;
			fill = ACTIVE_ALERT_FILL;
			break;
		}

		// draw the polygons on the graphics context
		g.setStroke(LINE_SOLID);
		ArrayList<Shape> shapes = getShapes(ai);
		for (Shape shp: shapes) {
			g.setColor(outline);
			g.draw(shp);
			g.setColor(fill);
			g.fill(shp);
		}

		// set the scale on the theme before getting the symbol
		float scale = dManager.getLayerState().getScale();
		dmsTheme.setScale(scale);
		dmsSymbol = (VectorSymbol) dmsTheme.getSymbol();
		String aht = ai.getAllHashtag();
		Set<DMS> active = AlertInfoHelper.findActiveSigns(ai);
		if (st == AlertState.PENDING) {
			// draw DMS with alert All hashtag, then active
			// (in that order so the styles look right)
			Iterator<DMS> it = DMSHelper.iterator();
			while (it.hasNext()) {
				DMS d = it.next();
				Hashtags tags = new Hashtags(d.getNotes());
				if (tags.contains(aht))
					drawDms(g, d, dmsAvailableStyle, t);
			}
			it = active.iterator();
			while (it.hasNext())
				drawDms(g, it.next(), dmsDeployedStyle, t);
		} else if (st == AlertState.ACTIVE) {
			// for active alerts draw only active DMS
			Iterator<DMS> it = active.iterator();
			while (it.hasNext())
				drawDms(g, it.next(), dmsDeployedStyle, t);
		} else {
			// for cleared alerts draw only active DMS
			// but using "all" style
			Iterator<DMS> it = active.iterator();
			while (it.hasNext())
				drawDms(g, it.next(), dmsAllStyle, t);
		}
	}

	/** Draw the DMS with the given name on the graphics context using the
	 *  specified style, resetting the transform to t afterwards. */
	private void drawDms(Graphics2D g, DMS dms, Style style,
		AffineTransform t)
	{
		if (dms.getGeoLoc() == null)
			return;

		// get a map object for this DMS and draw
		// (potentially as selected)
		MapGeoLoc dmgl = glManager.findMapGeoLoc(dms.getGeoLoc());
		if (dms == aManager.getSelectedDms()) {
			dmsSymbol.drawSelected(g, dmgl, style);
			g.setTransform(t); // reset transform
		}
		dmsSymbol.draw(g, dmgl, style);
		g.setTransform(t); // reset transform
	}

	/** Zoom to the selected alert area, showing the entire area on the map.*/
	public void zoomToAlertArea(AlertInfo ai) {
		ArrayList<Shape> shapes = getShapes(ai);

		// find the min and max x and y points
		ArrayList<Double> xPoints = new ArrayList<Double>();
		ArrayList<Double> yPoints = new ArrayList<Double>();
		for (Shape shp: shapes) {
			Rectangle2D r = shp.getBounds2D();
			xPoints.add(r.getMinX());
			xPoints.add(r.getMaxX());
			yPoints.add(r.getMinY());
			yPoints.add(r.getMaxY());
		}
		Collections.sort(xPoints);
		Collections.sort(yPoints);
		double minX = xPoints.get(0);
		double maxX = xPoints.get(xPoints.size() - 1);
		double minY = yPoints.get(0);
		double maxY = yPoints.get(yPoints.size() - 1);
		double w = maxX - minX;
		double h = maxY - minY;

		aManager.getScreenPane().getMap().getModel().setExtent(minX,
			minY, w, h);
	}
}
