/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
 * Copyright (C) 2010  AHMCT, University of California
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
 * Map marker.
 *
 * @author Michael Darter
 */
public class IrisMarker extends AbstractMarker {

	/** Initial scale used in subclass constructors, pixels/map unit */
	protected static final float INIT_SCALE = 1000;

	/** Icon size scale */
	private static float m_iconScale = getIconSizeScale();

	/** Maximum icon size scale */
	private static float m_iconScaleMax = getIconSizeScaleMax();

	/** Create a new ramp meter marker.
	 * @param FIXME */
	public IrisMarker(int c, int sizePixels, float iconMaxSize) {
		super(c, sizePixels, iconMaxSize * m_iconScaleMax, 
			m_iconScale);
	}

	/** Get map icon size scale. */
	static private float getIconSizeScale() {
		return SystemAttrEnum.MAP_ICON_SIZE_SCALE.getFloat();
	}

	/** Get the map icon maximum size scale. */
	static private float getIconSizeScaleMax() {
		return SystemAttrEnum.MAP_ICON_SIZE_SCALE_MAX.getFloat();
	}
}
