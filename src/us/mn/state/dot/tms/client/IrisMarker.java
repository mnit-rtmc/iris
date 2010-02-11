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

import us.mn.state.dot.map.marker.AbstractMarker;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * An iris marker uses system attributes to determine the default size and
 * maximum scale to draw on the map.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class IrisMarker extends AbstractMarker {

	/** Initial scale used in subclass constructors, pixels/map unit */
	static protected final float INIT_SCALE = 1000;

	/** Get map icon size scale */
	static private float getIconSizeScale() {
		return SystemAttrEnum.MAP_ICON_SIZE_SCALE.getFloat();
	}

	/** Get the map icon maximum size scale */
	static private float getIconSizeScaleMax() {
		return SystemAttrEnum.MAP_ICON_SIZE_SCALE_MAX.getFloat();
	}

	/** Marker size in pixels */
	protected final float size_pixels;

	/** Maximum size in user coordinates (zero for no maximum) */
	protected final float max_size;

	/** Create a new iris marker.
	 * @param c Count of nodes on marker path.
	 * @param sp Default size of marker in pixels.
	 * @param ms Maximum size of marker in user coordinates. */
	public IrisMarker(int c, int sp, int ms) {
		super(c);
		size_pixels = sp * getIconSizeScale();
		max_size = ms * getIconSizeScaleMax();
	}

	/** Get the scaled marker size.
	 * @param scale Map scale in user coordinates per pixel.
	 * @return Marker size in user coordinates. */
	protected float getMarkerSize(float scale) {
		float size = size_pixels * scale;
		if(max_size > 0)
			return Math.min(size, max_size);
		else
			return size;
	}
}
