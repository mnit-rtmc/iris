/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera;

import java.awt.event.ActionEvent;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.widget.IAction;

/**
 * This is an action to unpublish a set of cameras.
 *
 * @author Douglas Lau
 */
public class UnpublishAction extends IAction {

	/** Proxy selection model */
	private final ProxySelectionModel<Camera> sel_mdl;

	/** Create a new unpublish action */
	public UnpublishAction(ProxySelectionModel<Camera> s) {
		super("camera.unpublish");
		sel_mdl = s;
	}

	/** Publish the selected cameras */
	@Override
	protected void doActionPerformed(ActionEvent e) {
		for (Camera c: sel_mdl.getSelected())
			c.setPublish(false);
		sel_mdl.clearSelection();
	}
}
