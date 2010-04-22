/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010  Minnesota Department of Transportation
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

import javax.swing.Action;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.client.proxy.ProxyAction;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;

/**
 * Selects the specified camera.
 *
 * @author Douglas Lau
 */
public class CameraSelectAction extends ProxyAction<Camera> {

	/** Camera selection model */
	protected final ProxySelectionModel<Camera> sel_model;

	/** Create a new action to select a camera */
	public CameraSelectAction(Camera c, ProxySelectionModel<Camera> mdl) {
		super(c);
		sel_model = mdl;
		putValue(Action.NAME, c.getName());
		putValue(Action.SHORT_DESCRIPTION, "Select camera");
		putValue(Action.LONG_DESCRIPTION, "Select camera " +
			c.getName());
	}

	/** Actually perform the action */
	protected void do_perform() {
		sel_model.setSelected(proxy);
	}
}
