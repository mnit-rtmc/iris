/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incidents;

import java.rmi.RemoteException;
import java.util.Properties;
import java.util.logging.Logger;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.tdxml.DdsException;
import us.mn.state.dot.trafmap.IncidentLayer;
import us.mn.state.dot.tms.client.TmsConnection;

/**
 * A map layer for displaying incidents as arrows
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class TmsIncidentLayer extends IncidentLayer {

	/** Theme to draw the layer */
	protected final DirectionalIncidentTheme theme;

	/** Create a new TMS incdent layer */
	public TmsIncidentLayer(Properties props, Logger logger,
		TmsConnection tc) throws DdsException, RemoteException
	{
		super(props, logger);
		theme = new DirectionalIncidentTheme(tc);
	}

	/** Create a new layer state */
	public LayerState createState() {
		return new LayerState(this, theme);
	}
}
