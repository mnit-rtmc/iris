/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2014  Minnesota Department of Transportation
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
 * Action to deploy a beacon.
 *
 * @author Douglas Lau
 */
public class DeployAction extends IAction {

	/** Proxy selection model */
	private final ProxySelectionModel<Beacon> sel_model;

	/** Create a new deploy action */
	public DeployAction(ProxySelectionModel<Beacon> s) {
		super("beacon.deploy");
		sel_model = s;
	}

	/** Actually perform the action */
	protected void doActionPerformed(ActionEvent e) {
		for(Beacon b: sel_model.getSelected())
			b.setFlashing(true);
		sel_model.clearSelection();
	}
}
