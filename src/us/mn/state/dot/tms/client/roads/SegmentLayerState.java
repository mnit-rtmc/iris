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

	/** Segment layer */
	protected final SegmentLayer seg_layer;

	/** Create a new segment layer */
	public SegmentLayerState(SegmentLayer sl, MapBean mb) {
		super(sl, mb, new DensityTheme());
		addTheme(new DensityTheme());
		addTheme(new SpeedTheme());
		addTheme(new FlowTheme());
		addTheme(new FreewayTheme());
		seg_layer = sl;
	}

	/** Iterate through the segments in the layer */
	public MapObject forEach(MapSearcher s) {
		float scale = getScale();
		if(scale > 20)
			return forEachStation(s, scale);
		else
			return forEachLane(s, scale);
	}

	/** Iterate through the stations in the layer */
	protected MapObject forEachStation(MapSearcher s, float scale) {
		float inner = scale / 2;		// inner scale
		float outer = 6 * scale;		// outer scale
		for(Segment seg: seg_layer) {
			MapSegment ms = new MapSegment(seg, inner, outer);
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
		final float lane_width = 3 * scale + 5 * (20 - scale) / 20;
		for(Segment seg: seg_layer) {
			R_NodeModel mdl = seg.getModel();
			int left = seg.getLeftLine();
			int right = seg.getRightLine();
			int n_lanes = Math.max(right - left, 1);
			float inner = scale / 2;
			float outer = lane_width * n_lanes;
			float width = (outer - inner) / n_lanes;
			for(int i = right; i > left; i--) {
				int ln = 1 + seg.getLaneShift() - i;
				float in_a = inner + width * mdl.getUpstream(i);
				float in_b = inner + width*mdl.getDownstream(i);
				MapSegment ms = new MapSegment(seg, ln, in_a,
					in_a + width, in_b, in_b + width);
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
