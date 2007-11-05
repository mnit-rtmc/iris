/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2004  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.client.dms;

import us.mn.state.dot.map.marker.AbstractMarker;

/**
 * Marker used to paint DMS.
 *
 * @author <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @author Douglas Lau
 */
public class DmsMarker extends AbstractMarker {

	/** Size (in user coordinates) to render DMS marker */
	static protected final int MARKER_SIZE = 1000;

	/** Create a new DMS marker */
	public DmsMarker() {
		this(MARKER_SIZE);
	}

	/** Create a new DMS marker */
	public DmsMarker(float size) {
		super(13);
		float height = 3 * size / 5;
		float half_width = size / 2;
		float third_width = size / 3;
		float fifth_width = size / 5;
		float half_height = height / 2;
		float x = 0;
		float y = 0;
		path.moveTo(x -= half_width, y -= half_height);
		path.lineTo(x += size, y);
		path.lineTo(x, y += fifth_width);
		path.lineTo(x -= fifth_width, y);
		path.lineTo(x, y += third_width);
		path.lineTo(x -= fifth_width, y);
		path.lineTo(x, y -= third_width);
		path.lineTo(x -= fifth_width, y);
		path.lineTo(x, y += third_width);
		path.lineTo(x -= fifth_width, y);
		path.lineTo(x, y -= third_width);
		path.lineTo(x -= fifth_width, y);
		path.closePath();
	}
}
