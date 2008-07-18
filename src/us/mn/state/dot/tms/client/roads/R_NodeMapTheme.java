/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2008  Minnesota Department of Transportation
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

import java.awt.Color;
import java.awt.Shape;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.map.Outline;
import us.mn.state.dot.map.Style;
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.map.VectorSymbol;
import us.mn.state.dot.tms.R_Node;

/**
 * Theme for roadway nodes on map
 *
 * @author Douglas Lau
 */
public class R_NodeMapTheme extends StyledTheme {

	/** Marker to draw roadway entrance nodes */
	static protected final Shape ENTRANCE_MARKER = new EntranceMarker();

	/** Marker to draw roadway exit nodes */
	static protected final Shape EXIT_MARKER = new ExitMarker();

	/** Marker to draw roadway station nodes */
	static protected final Shape STATION_MARKER = new StationMarker();

	/** Marker to draw roadway intersection nodes */
	static protected final Shape INTERSECTION_MARKER =
		new IntersectionMarker();

	/** Marker to draw access nodes */
	static protected final Shape ACCESS_MARKER = new AccessMarker();

	/** Style to render the TMS object */
	static protected final Style STYLE = new Style("Normal",
		Outline.createSolid(Color.BLACK, 15), R_NodeRenderer.COLOR_GPS);

	/** Style to render the non-GPS nodes */
	static protected final Style NO_GPS = new Style("No GPS",
		Outline.createSolid(Color.BLACK, 15), R_NodeRenderer.COLOR_LOC);

	/** Style to render the mismatch nodes */
	static protected final Style MISMATCH = new Style("Mismatch",
		Outline.createSolid(Color.BLACK, 15), Color.YELLOW);

	/** Create a new roadway node map theme */
	public R_NodeMapTheme() {
		super("R_Nodes", STYLE, new StationMarker());
		addStyle(NO_GPS);
//		addStyle(MISMATCH);
	}

	/** Get an appropriate style for the given map object */
	public Style getStyle(MapObject o) {
		if(o instanceof R_NodeProxy) {
			R_NodeProxy n = (R_NodeProxy)o;
			if(!n.hasGPS())
				return NO_GPS;
//			if(n.hasMismatch())
//				return MISMATCH;
		}
		return STYLE;
	}

	/** Get the shape to use for the given map object */
	protected Shape getShape(MapObject o) {
		if(o instanceof R_NodeProxy) {
			R_NodeProxy r_node = (R_NodeProxy)o;
			switch(r_node.getNodeType()) {
				case R_Node.TYPE_STATION:
					return STATION_MARKER;
				case R_Node.TYPE_ENTRANCE:
					return ENTRANCE_MARKER;
				case R_Node.TYPE_EXIT:
					return EXIT_MARKER;
				case R_Node.TYPE_INTERSECTION:
					return INTERSECTION_MARKER;
				case R_Node.TYPE_ACCESS:
					return ACCESS_MARKER;
			}
		}
		return STATION_MARKER;
	}

	/** Get the symbol for the given map object */
	public Symbol getSymbol(MapObject o) {
		VectorSymbol sym = (VectorSymbol)super.getSymbol(o);
		sym.setShape(getShape(o));
		return sym;
	}
}
