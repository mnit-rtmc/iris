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

import java.util.Map;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.lcs.LCSArrayCellRenderer;

/**
 * Proposed device cell renderer.
 *
 * @author Douglas Lau
 */
public class ProposedLcsCellRenderer extends LCSArrayCellRenderer {

	/** User Session */
	private final Session session;

	/** Mapping of LCS array names to proposed indications */
	private final Map<String, Integer []> indications;

	/** Create a new proposed LCS array cell renderere */
	public ProposedLcsCellRenderer(Session s, Map<String, Integer []> ind) {
		super(s.getLCSArrayManager());
		session = s;
		indications = ind;
	}

	/** Get the user name */
	@Override
	protected User getUser(LCSArray lcs_array) {
		return session.getUser();
	}

	/** Get the indications */
	@Override
	protected Integer[] getIndications(LCSArray lcs_array) {
		return indications.get(lcs_array.getName());
	}
}
