/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.comm;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.util.HashSet;
import java.util.Iterator;
import us.mn.state.dot.geokit.SphericalMercatorPosition;
import us.mn.state.dot.map.AbstractMarker;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.tms.Cabinet;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.RampMeterHelper;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.ProxyTheme;

/**
 * A theme for drawing controller markers.
 *
 * @author Douglas Lau
 */
public class ControllerTheme extends ProxyTheme<Controller> {

	/** Color to draw spider web */
	static private Color WEB_COLOR = new Color(255, 255, 255, 192);

	/** Color to draw spider web */
	static private Color WEB_COLOR2 = new Color(64, 0, 128);

	/** Stroke for drawing the web */
	static private Stroke createWebStroke(float scale) {
		return new BasicStroke(3.5f * scale,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f);
	}

	/** Stroke for drawing the web */
	static private Stroke createWebStroke2(float scale) {
		return new BasicStroke(1.5f * scale,
			BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 1f,
			new float[] { 6 * scale, 2 * scale, 4 * scale,
			2 * scale }, 0);
	}

	/** Create a new controller theme */
	public ControllerTheme(ProxyManager<Controller> m, AbstractMarker mkr) {
		super(m, mkr);
	}

	/** Map scale */
	private float scale = 1;

	/** Set the map scale */
	@Override
	public void setScale(float s) {
		scale = s;
		super.setScale(s);
	}

	/** Draw a selected map object */
	@Override
	public void drawSelected(Graphics2D g, MapObject mo) {
		Controller c = manager.findProxy(mo);
		if (c != null) {
			Cabinet cab = c.getCabinet();
			if (cab != null) {
				GeoLoc loc = cab.getGeoLoc();
				if (loc != null) {
					SphericalMercatorPosition pos =
						GeoLocHelper.getPosition(loc);
					if (pos != null)
						drawSelected(g, c, pos);
				}
			}
		}
		super.drawSelected(g, mo);
	}

	/** Draw a selected controller */
	private void drawSelected(Graphics2D g, Controller c,
		SphericalMercatorPosition pos)
	{
		HashSet<GeoLoc> locs = new HashSet<GeoLoc>();
		addRampMeterLocs(c, locs);
		addDetectorLocs(c, locs);
		g.setColor(WEB_COLOR);
		g.setStroke(createWebStroke(scale));
		drawSpider(g, pos, locs);
		g.setColor(WEB_COLOR2);
		g.setStroke(createWebStroke2(scale));
		drawSpider(g, pos, locs);
	}

	/** Add ramp meter locations for a controller */
	private void addRampMeterLocs(Controller c, HashSet<GeoLoc> locs) {
		Iterator<RampMeter> it = RampMeterHelper.iterator();
		while(it.hasNext()) {
			RampMeter rm = it.next();
			if(rm.getController() == c) {
				GeoLoc l = rm.getGeoLoc();
				if(l != null)
					locs.add(l);
			}
		}
	}

	/** Add detector locations for a controller */
	private void addDetectorLocs(Controller c, HashSet<GeoLoc> locs) {
		Iterator<Detector> it = DetectorHelper.iterator();
		while(it.hasNext()) {
			Detector det = it.next();
			if(det.getController() == c) {
				GeoLoc l = det.getR_Node().getGeoLoc();
				if(l != null)
					locs.add(l);
			}
		}
	}

	/** Draw spider webs from the controller to devices */
	private void drawSpider(Graphics2D g, SphericalMercatorPosition pos,
		HashSet<GeoLoc> locs)
	{
		for(GeoLoc l: locs) {
			SphericalMercatorPosition p =
				GeoLocHelper.getPosition(l);
			g.draw(new Line2D.Double(pos.getX(), pos.getY(),
				p.getX(), p.getY()));
		}
	}
}
