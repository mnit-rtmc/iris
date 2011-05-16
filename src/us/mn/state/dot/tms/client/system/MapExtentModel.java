/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.system;

import java.awt.geom.Point2D;
import java.util.HashMap;
import us.mn.state.dot.geokit.Position;
import us.mn.state.dot.geokit.SphericalMercatorPosition;
import us.mn.state.dot.geokit.ZoomLevel;
import us.mn.state.dot.tms.MapExtent;
import us.mn.state.dot.tms.client.IrisClient;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for map extents.
 *
 * @author Douglas Lau
 */
public class MapExtentModel extends ProxyTableModel<MapExtent> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<MapExtent>("Map Extent", 160) {
			public Object getValueAt(MapExtent me) {
				return me.getName();
			}
			public boolean isEditable(MapExtent me) {
				return (me == null) && canAdd();
			}
			public void setValueAt(MapExtent me, Object value) {
				String v = value.toString().trim();
				if(v.length() > 0)
					createExtent(v);
			}
		},
		new ProxyColumn<MapExtent>("Lon", 80, Float.class) {
			public Object getValueAt(MapExtent me) {
				return me.getLon();
			}
			public boolean isEditable(MapExtent me) {
				return canUpdate(me);
			}
			public void setValueAt(MapExtent me, Object value) {
				if(value instanceof Float)
					me.setLon((Float)value);
			}
		},
		new ProxyColumn<MapExtent>("Lat", 80, Float.class) {
			public Object getValueAt(MapExtent me) {
				return me.getLat();
			}
			public boolean isEditable(MapExtent me) {
				return canUpdate(me);
			}
			public void setValueAt(MapExtent me, Object value) {
				if(value instanceof Float)
					me.setLat((Float)value);
			}
		},
		new ProxyColumn<MapExtent>("Zoom", 80, Integer.class) {
			public Object getValueAt(MapExtent me) {
				return me.getZoom();
			}
			public boolean isEditable(MapExtent me) {
				return canUpdate(me);
			}
			public void setValueAt(MapExtent me, Object value) {
				if(value instanceof Integer)
					me.setZoom((Integer)value);
			}
		},
	    };
	}

	/** Iris client */
	protected final IrisClient client;

	/** Create a new map extent table model */
	public MapExtentModel(Session s, IrisClient ic) {
		super(s, s.getSonarState().getMapExtents());
		client = ic;
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return MapExtent.SONAR_TYPE;
	}

	/** Create a new map extent */
	protected void createExtent(String name) {
		Point2D c = client.getMapModel().getCenter();
		SphericalMercatorPosition smp = new SphericalMercatorPosition(
			c.getX(), c.getY());
		Position pos = smp.getPosition();
		ZoomLevel zoom = client.getMapModel().getZoomLevel();
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("lat", pos.getLatitude());
		attrs.put("lon", pos.getLongitude());
		attrs.put("zoom", zoom.ordinal());
		cache.createObject(name, attrs);
	}
}
