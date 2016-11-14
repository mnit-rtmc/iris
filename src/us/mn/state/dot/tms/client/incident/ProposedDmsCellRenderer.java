/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.dms.DmsCellRenderer;
import us.mn.state.dot.tms.client.proxy.CellRendererSize;

/**
 * Proposed DMS cell renderer.
 *
 * @author Douglas Lau
 */
public class ProposedDmsCellRenderer extends DmsCellRenderer {

	/** User Session */
	private final Session session;

	/** Model for deployment list */
	private final DeviceDeployModel model;

	/** Create a new proposed DMS cell renderere */
	public ProposedDmsCellRenderer(Session s, DeviceDeployModel m) {
		super(CellRendererSize.LARGE);
		session = s;
		model = m;
	}

	/** Get the owner user name */
	@Override
	protected String getOwner(DMS dms) {
		return session.getUser().getName();
	}

	/** Get the raster graphic for page one */
	@Override
	protected RasterGraphic getPageOne(DMS dms) {
		return model.getGraphic(dms.getName());
	}
}
