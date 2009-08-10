/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
import java.util.LinkedList;
import java.util.List;
import us.mn.state.dot.map.Layer;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.map.MapSearcher;
import us.mn.state.dot.map.Outline;
import us.mn.state.dot.map.Style;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeTransition;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.proxy.MapGeoLoc;

/**
 * SegmentLayer is a class for drawing roadway segments.
 *
 * @author Douglas Lau
  */
public class SegmentLayer extends Layer {

	/** List of segments in the layer */
	protected final List<Segment> segments = new LinkedList<Segment>();;

	/** R_Node manager */
	protected final R_NodeManager manager;

	/** Create a new segment layer */
	public SegmentLayer(R_NodeManager m) {
		super("Segments");
		manager = m;
	}

	/** Add a corridor to the segment layer */
	public void addCorridor(CorridorBase c) {
		Segment seg = new Segment();
		MapGeoLoc ploc = null;
		for(R_Node n: c.getNodes()) {
			MapGeoLoc loc = manager.findGeoLoc(n);
			if(isWithinThreshold(ploc, loc) &&
			   !isSegmentDisjointed(n))
				seg.addNode(loc);
			if(isSegmentBreak(n)) {
				segments.add(seg);
				seg = new Segment(n);
				if(loc != null)
					seg.addNode(loc);
			}
			ploc = loc;
		}
		segments.add(seg);
	}

	/** Check if two locations are within distance threshold */
	protected boolean isWithinThreshold(MapGeoLoc l0, MapGeoLoc l1) {
		if(l0 == null)
			return l1 != null;
		else {
			if(l1 == null)
				return false;
			GeoLoc g0 = l0.getGeoLoc();
			GeoLoc g1 = l1.getGeoLoc();
			return GeoLocHelper.metersTo(g0, g1) <
			       SystemAttrEnum.MAP_SEGMENT_MAX_METERS.getInt();
		}
	}

	/** Check if a node is at a segment break */
	protected boolean isSegmentBreak(R_Node n) {
		return n.getNodeType() == R_NodeType.STATION.ordinal() ||
		       isSegmentDisjointed(n);
	}

	/** Check if a node should be disjointed from a segment */
	protected boolean isSegmentDisjointed(R_Node n) {
		return n.getTransition() == R_NodeTransition.COMMON.ordinal() &&
		       n.getNodeType() == R_NodeType.ENTRANCE.ordinal();
	}

	/** Get the style to draw the segment layer */
	protected Style getStyle() {
		return new Style("Segment", null, Color.BLACK);
	}

	/** Iterate through the segments in the layer */
	public MapObject forEach(MapSearcher s) {
		for(Segment seg: segments) {
			if(s.next(seg))
				return seg;
		}
		return null;
	}

	/** Create a new layer state */
	public LayerState createState() {
		return new LayerState(this, new SegmentTheme(getStyle()));
	}
}
