/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.client.proxy.ProxyCellRenderer;

/**
 * ListCellRenderer for incident cells.
 *
 * @author Douglas Lau
 */
public class IncidentCellRenderer extends ProxyCellRenderer<Incident> {

	/** Create a new incident cell renderer */
	public IncidentCellRenderer(IncidentManager m) {
		super(m);
	}

	/** Convert value to a string */
	@Override
	protected String valueToString(Incident inc) {
		return super.valueToString(inc) + getCamera(inc);
	}

	/** Get the incident camera */
	private String getCamera(Incident inc) {
		Camera cam = inc.getCamera();
		if (cam != null)
			return " -- " + cam.getName();
		else
			return "";
	}
}
