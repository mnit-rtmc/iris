/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2008  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.sonar.MapGeoLoc;
import us.mn.state.dot.tms.client.sonar.SonarLayer;

/**
 * Layer for drawing roadway node objects on the map.
 *
 * @author Douglas Lau
 */
public class R_NodeLayer extends SonarLayer<R_Node> {

	/** Currently selected corridor */
	protected String corridor = null;

	/** Select a new freeway corridor */
	public void setCorridor(String c) {
		corridor = c;
		notifyLayerChanged();
	}

	/** Create a new roadway node layer */
	public R_NodeLayer(R_NodeManager m) {
		super(m);
	}

	/** Get the extent for the currently selected corridor */
	public Rectangle2D getExtent() {
		if(corridor != null) {
			CorridorExtent ce = new CorridorExtent(corridor);
			ce.calculateExtent();
			if(ce.extent != null)
				return ce.extent;
		}
		return super.getExtent();
	}

	/** A class to calculate the extent of a corridor */
	protected class CorridorExtent implements Checker<R_Node> {
		protected final String cor;
		protected final R_NodeManager man;
		protected Rectangle2D extent = null;
		protected CorridorExtent(String c) {
			cor = c;
			man = (R_NodeManager)manager;
		}
		protected void calculateExtent() {
			man.findCorridor(cor, this);
		}
		public boolean check(R_Node n) {
			MapGeoLoc l = man.findGeoLoc(n);
			if(l != null)
				extent = getUnion(extent, l.getGeoLoc());
			return false;
		}
	}

	/** Get the union of the given extent with another node */
	static protected Rectangle2D getUnion(Rectangle2D e, GeoLoc loc) {
		if(GeoLocHelper.isNull(loc))
			return e;
		else if(e == null)
			return getExtent(loc);
		else {
			Rectangle2D.union(e, getExtent(loc), e);
			return e;
		}
	}

	/** Get the extent of one roadway node */
	static protected Rectangle2D getExtent(GeoLoc loc) {
		Integer x = GeoLocHelper.getCombinedEasting(loc);
		Integer y = GeoLocHelper.getCombinedNorthing(loc);
		if(x == null || y == null)
			return null;
		else
			return new Rectangle2D.Float(x-500, y-500, 1000, 1000);
	}
}
