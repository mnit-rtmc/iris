/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.map.MapSearcher;
import us.mn.state.dot.tms.client.proxy.TmsMapLayer;
import us.mn.state.dot.tms.client.proxy.TmsMapProxy;

/**
 * Layer for drawing roadway node objects on the map.
 *
 * @author Douglas Lau
 */
public class R_NodeLayer extends TmsMapLayer {

	/** Currently selected corridor */
	protected String corridor = null;

	/** Select a new freeway corridor */
	public void setCorridor(String c) {
		selectionModel.setSelected(null);
		corridor = c;
		notifyRendererChanged();
	}

	/** Create a new roadway node layer */
	public R_NodeLayer(R_NodeHandler h) {
		super(h);
	}

	/** Check if a roadway node is on the selected corridor */
	protected boolean checkNode(MapObject o) {
		if(o instanceof R_NodeProxy) {
			R_NodeProxy n = (R_NodeProxy)o;
			return n.getCorridor().equals(corridor);
		}
		return false;
	}

	/** Iterate through all shapes in the layer */
	public MapObject forEach(MapSearcher s) {
		Map<Object, TmsMapProxy> proxies = handler.getProxies();
		synchronized(proxies) {
			for(TmsMapProxy o: proxies.values()) {
				if(checkNode(o) && s.next(o))
					return o;
			}
		}
		return null;
	}

	/** Get the extent for the currently selected corridor */
	public Rectangle2D getExtent() {
		Rectangle2D extent = null;
		Map<Object, TmsMapProxy> proxies = handler.getProxies();
		synchronized(proxies) {
			for(TmsMapProxy o: proxies.values()) {
				if(checkNode(o)) {
					R_NodeProxy p = (R_NodeProxy)o;
					extent = p.getUnion(extent);
				}
			}
		}
		if(extent != null)
			return extent;
		else
			return super.getExtent();
	}
}
