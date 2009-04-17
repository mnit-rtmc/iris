/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.rwis;

import java.net.URL;
import java.util.logging.Logger;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.tdxml.TdxmlException;
import us.mn.state.dot.trafmap.IncidentLayer;
import us.mn.state.dot.trafmap.RwisLayer;
import us.mn.state.dot.tms.client.incidents.DirectionalIncidentTheme;
import us.mn.state.dot.tms.client.rwis.RwisTab;

/**
 * A map layer for displaying incidents as arrows
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author Michael Darter
 */
public class TmsRwisLayer extends RwisLayer {

	/** Theme to draw the layer */
	protected final DirectionalIncidentTheme theme;

	/** Create a new TMS incdent layer */
	public TmsRwisLayer(URL u, Logger logger) throws TdxmlException {
		super(u, logger);
		theme = new DirectionalIncidentTheme();
	}

	/** Create a new layer state */
	public LayerState createState() {
		return new LayerState(this, theme);
	}
}
