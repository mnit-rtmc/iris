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

import java.awt.geom.Point2D;
import java.util.List;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.map.MapObject;
import us.mn.state.dot.map.MapSearcher;

/**
 * SegmentLayerState is a class for drawing roadway segments.
 *
 * @author Douglas Lau
 */
public class SegmentLayerState extends LayerState {

	/** List of segments in the layer */
	protected final List<Segment> segments;

	/** Map for scaling */
	protected MapBean map;

	/** Create a new segment layer */
	public SegmentLayerState(SegmentLayer sl) {
		super(sl, new DensityTheme());
		addTheme(new DensityTheme());
		addTheme(new SpeedTheme());
		addTheme(new FlowTheme());
		addTheme(new FreewayTheme());
		segments = sl.getSegments();
	}

	/** Set the map for scaling */
	public void setMap(MapBean m) {
		map = m;
	}

	/** Iterate through the segments in the layer */
	public MapObject forEach(MapSearcher s) {
		float scale = (map == null) ? 150f : (float)map.getPixelWorld();
		for(Segment seg: segments) {
			MapSegment ms = new MapSegment(seg, null, scale);
			if(s.next(ms))
				return ms;
		}
		return null;
	}

	/** Search a layer for a map object containing the given point */
	public MapObject search(final Point2D p) {
		return forEach(new MapSearcher() {
			public boolean next(MapObject mo) {
				return mo.getShape().contains(p);
			}
		});
	}
}
