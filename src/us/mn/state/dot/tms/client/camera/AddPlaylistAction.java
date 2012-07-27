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
 * This is an action to add a camera to the playlist.
 *
 * @author Douglas Lau
 */
public class AddPlaylistAction extends IAction {

	/** Camera manager */
	private final CameraManager manager;

	/** Proxy selection model */
	private final ProxySelectionModel<Camera> s_model;

	/** Create a new add playlist action */
	public AddPlaylistAction(CameraManager m, ProxySelectionModel<Camera> s)
	{
		super("camera.playlist.add");
		manager = m;
		s_model = s;
	}

	/** Add the selected cameras to the playlist */
	protected void do_perform() {
		for(Camera c: s_model.getSelected())
			manager.addPlaylist(c);
		s_model.clearSelection();
	}
}
