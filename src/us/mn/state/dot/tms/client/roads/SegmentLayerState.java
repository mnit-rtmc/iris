/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
	private final SegmentLayer seg_layer;

	/** Create a new segment layer */
	public SegmentLayerState(SegmentLayer sl, MapBean mb) {
		super(sl, mb, new DensityTheme());
		addTheme(new DensityTheme());
		addTheme(new SpeedTheme());
		addTheme(new FlowTheme());
		addTheme(new FreewayTheme());
		seg_layer = sl;
	}

	/** Get the visibility flag */
	@Override
	public boolean isVisible() {
		Boolean v = getVisible();
		return v != null ? v : isZoomVisible();
	}

	/** Is the layer visible at the current zoom level? */
	private boolean isZoomVisible() {
		return map.getModel().getZoomLevel().ordinal() >= 10;
	}

	/** Iterate through the segments in the layer */
	@Override
	public MapObject forEach(MapSearcher s) {
		if(isPastLaneZoomThreshold())
			return forEachLane(s);
		else
			return forEachStation(s);
	}

	/** Is the zoom level past the "individual lane" threshold? */
	private boolean isPastLaneZoomThreshold() {
		return map.getModel().getZoomLevel().ordinal() >= 14;
	}

	/** Iterate through the stations in the layer */
	private MapObject forEachStation(MapSearcher s) {
		float scale = getScale();
		for(Segment seg: seg_layer) {
			MapSegment ms = new MapSegment(seg, scale);
			if(s.next(ms))
				return ms;
		}
		return null;
	}

	/** Iterate through each lane segment in the layer.
	 * @param s Map searcher callback.
	 * @return Map object found, if any. */
	private MapObject forEachLane(MapSearcher s) {
		float scale = getScale();
		for(Segment seg: seg_layer) {
			for(int sh = seg.getLeftMin(); sh < seg.getRightMax();
			    sh++)
			{
				MapSegment ms = new MapSegment(seg, sh, scale);
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
