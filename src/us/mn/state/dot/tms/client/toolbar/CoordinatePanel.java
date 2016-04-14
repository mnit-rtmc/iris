/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  AHMCT, University of California, Davis
 * Copyright (C) 2010-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toolbar;

import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import javax.swing.JLabel;
import java.text.DecimalFormat;
import us.mn.state.dot.geokit.Position;
import us.mn.state.dot.geokit.SphericalMercatorPosition;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.client.map.MapBean;
import us.mn.state.dot.tms.client.widget.IPanel;

/**
 * A tool panel that contains the map coordinates of the mouse pointer.
 *
 * @author Stephen Donecker
 * @author Michael Darter
 * @author Douglas Lau
 */
public class CoordinatePanel extends ToolPanel implements MouseMotionListener {

	/** Lat/lon decimal format */
	static private final String LAT_LON_FORMAT = "0.00000";

	/** Unicode degrees character */
	static private final char DEGREES = '\u00B0';

	/** Latitude label */
	static private final String LAT = I18N.get("location.lat");

	/** Longitude label */
	static private final String LON = I18N.get("location.lon");

	/** Empty coordinate label value */
	static private final String COORD = LAT + DEGREES + " " + LON + DEGREES;

	/** Is this panel IRIS enabled? */
	static public boolean getIEnabled() {
		return true;
	}

	/** Map bean */
	private final MapBean map;

	/** Label used for cursor coordinates */
	private final JLabel coord_lbl = IPanel.createValueLabel();

	/** Create a new coordinate panel */
	public CoordinatePanel(MapBean m) {
		assert m != null;
		map = m;
		add(coord_lbl);
		map.addMouseMotionListener(this);
		coord_lbl.setText(COORD);
	}

	/** Process the mouse moved event */
	@Override public void mouseMoved(MouseEvent e) {
		Point2D p = map.transformPoint(e.getPoint());
		SphericalMercatorPosition smp =
			new SphericalMercatorPosition(p.getX(), p.getY());
		coord_lbl.setText(formatCoordinates(smp));
	}

	/** Format a coordinate label */
	private String formatCoordinates(SphericalMercatorPosition smp) {
		try {
			Position pos = smp.getPosition();
			DecimalFormat df = new DecimalFormat(LAT_LON_FORMAT);
			String lat = df.format(pos.getLatitude());
			String lon = df.format(pos.getLongitude());
			return LAT + " " + lat + DEGREES + " " +
			       LON + " " + lon + DEGREES;
		}
		catch(IllegalArgumentException e) {
			return COORD;
		}
	}

	/** Process the mouse dragged event */
	@Override public void mouseDragged(MouseEvent e) {
		// dragging on the map is for panning,
		// so coordinate bar should not be updated
	}

	/** Cleanup */
	@Override public void dispose() {
		map.removeMouseMotionListener(this);
	}
}
