/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
		if(scale > 20)
			return forEachStation(s, scale);
		else
			return forEachLane(s, scale);
	}

	/** Iterate through the stations in the layer */
	protected MapObject forEachStation(MapSearcher s, float scale) {
		float inner = scale / 2;		// inner scale
		float outer = 6 * scale;		// outer scale
		for(Segment seg: segments) {
			MapSegment ms = new MapSegment(seg, null, inner, outer);
			if(s.next(ms))
				return ms;
		}
		return null;
	}

	/** Iterate through each lane segment in the layer.
	 * @param s Map searcher callback.
	 * @param scale Number of meters per pixel.
	 * @return Map object found, if any. */
	protected MapObject forEachLane(MapSearcher s, float scale) {
		float lane_width = 3 * scale + 5 * (20 - scale) / 20;
		for(Segment seg: segments) {
			int n_lanes = Math.max(seg.getLaneCount(), 1);
			float inner = scale / 2;
			float outer = lane_width * n_lanes;
			float width = (outer - inner) / n_lanes;
			for(int i = 0; i < n_lanes; i++) {
				int l = i + 1;
				float out = outer - (i * width);
				float in = out - width;
				MapSegment ms = new MapSegment(seg, l, in, out);
				if(s.next(ms))
					return ms;
			}
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
