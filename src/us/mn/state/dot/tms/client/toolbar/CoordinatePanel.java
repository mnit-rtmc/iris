/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  AHMCT, University of California, Davis
 * Copyright (C) 2010-2011  Minnesota Department of Transportation
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
import us.mn.state.dot.map.MapBean;

/**
 * A tool panel that contains the map coordinates of the mouse pointer.
 *
 * @author Stephen Donecker
 * @author Michael Darter
 * @author Douglas Lau
 */
public class CoordinatePanel extends ToolPanel implements MouseMotionListener {

	/** Map bean */
	protected final MapBean map;

	/** Label used for cursor coordinates */
	protected final JLabel coord_lbl = new JLabel();

	/** Lat/lon decimal format */
	static protected final String LAT_LON_FORMAT = "0.000000";

	/** Constructor */
	public CoordinatePanel(MapBean m) {
		assert m !=  null;
		map = m;
		add(coord_lbl);
		map.addMouseMotionListener(this);
	}

	/** is this panel IRIS enabled? */
	public static boolean getIEnabled() {
		return true;
	}

	/** Process the mouse moved event and update the status bar */
	public void mouseMoved(MouseEvent e) {
		Point2D p = map.transformPoint(e.getPoint());
		SphericalMercatorPosition smp =
			new SphericalMercatorPosition(p.getX(), p.getY());
		Position pos = smp.getPosition();
		DecimalFormat df = new DecimalFormat(LAT_LON_FORMAT);
		String lat = df.format(pos.getLatitude());
		String lon = df.format(pos.getLongitude());
		coord_lbl.setText("lat " + lat + "\u00B0 lon " + lon +"\u00B0");
	}

	/** Process the mouse dragged event and update the status bar */
	public void mouseDragged(MouseEvent e) {
		mouseMoved(e);
	}

	/** cleanup */
	public void dispose() {
		map.removeMouseMotionListener(this);
	}
}
