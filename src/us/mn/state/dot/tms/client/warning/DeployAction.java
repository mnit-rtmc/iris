/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.warning;

import java.awt.event.ActionEvent;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.widget.IAction2;

/**
 * Action to deploy a warning sign.
 *
 * @author Douglas Lau
 */
public class DeployAction extends IAction2 {

	/** Proxy selection model */
	private final ProxySelectionModel<WarningSign> s_model;

	/** Create a new deploy action */
	public DeployAction(ProxySelectionModel<WarningSign> s) {
		super("warning.sign.deploy");
		s_model = s;
	}

	/** Actually perform the action */
	protected void doActionPerformed(ActionEvent e) {
		for(WarningSign s: s_model.getSelected())
			s.setDeployed(true);
		s_model.clearSelection();
	}
}
