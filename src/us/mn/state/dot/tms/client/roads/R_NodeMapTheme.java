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
import us.mn.state.dot.map.VectorSymbol;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.client.sonar.ProxyTheme;

/**
 * Theme for roadway nodes on map
 *
 * @author Douglas Lau
 */
public class R_NodeMapTheme extends ProxyTheme<R_Node> {

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

	/** Create a new roadway node map theme */
	public R_NodeMapTheme(R_NodeManager m) {
		super(m, "R_Nodes", new StationMarker());
	}

	/** Get the shape to use for the given map object */
	protected Shape getShape(MapObject o) {
		R_Node n = manager.findProxy(o);
		if(n != null)
			return getShape(n);
		else
			return STATION_MARKER;
	}

	/** Get the shape to use for the given r_node */
	protected Shape getShape(R_Node n) {
		R_NodeType nt = R_NodeType.fromOrdinal(n.getNodeType());
		switch(nt) {
		case ENTRANCE:
			return ENTRANCE_MARKER;
		case EXIT:
			return EXIT_MARKER;
		case INTERSECTION:
			return INTERSECTION_MARKER;
		case ACCESS:
			return ACCESS_MARKER;
		default:
			return STATION_MARKER;
		}
	}

	/** Get the symbol for the given map object */
	public Symbol getSymbol(MapObject o) {
		VectorSymbol sym = (VectorSymbol)super.getSymbol(o);
		sym.setShape(getShape(o));
		return sym;
	}
}
