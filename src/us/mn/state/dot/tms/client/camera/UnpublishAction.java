/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2012  Minnesota Department of Transportation
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
	private final ProxySelectionModel<Camera> s_model;

	/** Create a new unpublish action */
	public UnpublishAction(ProxySelectionModel<Camera> s) {
		super("camera.unpublish");
		s_model = s;
	}

	/** Publish the selected cameras */
	protected void do_perform() {
		for(Camera c: s_model.getSelected())
			c.setPublish(false);
		s_model.clearSelection();
	}
}
