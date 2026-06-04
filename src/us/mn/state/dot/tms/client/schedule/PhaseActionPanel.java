/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2026  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.schedule;

import us.mn.state.dot.tms.PhaseAction;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;

/**
 * A panel for displaying a table of phase actions.
 *
 * @author Douglas Lau
 */
public class PhaseActionPanel extends ProxyTablePanel<PhaseAction> {

	/** Create a new time action panel */
	public PhaseActionPanel(Session s) {
		super(new PhaseActionModel(s, null));
	}

	/** Create a new proxy object */
	@Override
	protected void createObject() {
		PhaseActionModel mdl = getPhaseActionModel();
		if (mdl != null)
			mdl.createObject();
	}

	/** Get the time action model */
	private PhaseActionModel getPhaseActionModel() {
		ProxyTableModel<PhaseAction> mdl = model;
		return (mdl instanceof PhaseActionModel)
		     ? (PhaseActionModel) mdl
		     : null;
	}
}
