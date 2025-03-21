/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2025  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.lcs.LcsCellRenderer;

/**
 * Proposed LCS array cell renderer.
 *
 * @author Douglas Lau
 */
public class ProposedLcsCellRenderer extends LcsCellRenderer {

	/** User Session */
	private final Session session;

	/** Model for deployment list */
	private final DeviceDeployModel model;

	/** Create a new proposed LCS array cell renderere */
	public ProposedLcsCellRenderer(Session s, DeviceDeployModel m) {
		super(s.getLcsManager());
		session = s;
		model = m;
	}

	/** Get the user name */
	@Override
	protected String getUser(Lcs lcs) {
		return session.getUser().getName();
	}

	/** Get the indications */
	@Override
	protected int[] getIndications(Lcs lcs) {
		return model.getIndications(lcs.getName());
	}
}
