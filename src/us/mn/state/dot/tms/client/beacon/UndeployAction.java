/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.beacon;

import java.awt.event.ActionEvent;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.widget.IAction;

/**
 * Action to undeploy a beacon.
 *
 * @author Douglas Lau
 */
public class UndeployAction extends IAction {

	/** Proxy selection model */
	private final ProxySelectionModel<Beacon> sel_mdl;

	/** Create a new undeploy action */
	public UndeployAction(ProxySelectionModel<Beacon> s) {
		super("beacon.undeploy");
		sel_mdl = s;
	}

	/** Actually perform the action */
	@Override
	protected void doActionPerformed(ActionEvent e) {
		for (Beacon b: sel_mdl.getSelected())
			b.setFlashing(false);
		sel_mdl.clearSelection();
	}
}
