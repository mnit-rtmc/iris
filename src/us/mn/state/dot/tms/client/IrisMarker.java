/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  AHMCT, University of California
 * Copyright (C) 2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import us.mn.state.dot.map.marker.AbstractMarker;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * An iris marker uses system attributes to determine the default size and
 * maximum scale to draw on the map.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
abstract public class IrisMarker extends AbstractMarker {

	/** Get the map icon maximum size scale */
	static private float getIconSizeScaleMax() {
		return SystemAttrEnum.MAP_ICON_SIZE_SCALE_MAX.getFloat();
	}

	/** Limit the map scale based on system attributes.
	 * @param scale Map scale in user coordinates per pixel.
	 * @return Adjusted map scale in user coordinates per pixel. */
	static public float adjustScale(final float scale) {
		float sc_min = scale / 4.0f;
		float sc_max = getIconSizeScaleMax();
		return (sc_max > 0) ?
			Math.max(Math.min(scale, sc_max), sc_min) : scale;
	}

	/** Create a new iris marker.
	 * @param c Count of nodes on marker path. */
	public IrisMarker(int c) {
		super(c);
	}

	/** Get the default marker size in pixels */
	abstract protected float getSizePixels();

	/** Get the scaled marker size.
	 * @param scale Map scale in user coordinates per pixel.
	 * @return Marker size in user coordinates. */
	protected float getMarkerSize(float scale) {
		return getSizePixels() * adjustScale(scale);
	}

	/** Create a transformed marker with the specified transform */
	public Shape createTransformedMarker(AffineTransform at) {
		return path.createTransformedShape(at);
	}
}
