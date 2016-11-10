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
 * This is an action to remove cameras from the playlist.
 *
 * @author Douglas Lau
 */
public class RemovePlaylistAction extends IAction {

	/** Camera manager */
	private final CameraManager manager;

	/** Proxy selection model */
	private final ProxySelectionModel<Camera> sel_mdl;

	/** Create a new remove playlist action */
	public RemovePlaylistAction(CameraManager m,
		ProxySelectionModel<Camera> s)
	{
		super("camera.playlist.remove");
		manager = m;
		sel_mdl = s;
	}

	/** Remove the selected cameras from the playlist */
	@Override
	protected void doActionPerformed(ActionEvent e) {
		for (Camera c: sel_mdl.getSelected())
			manager.removePlaylist(c);
		sel_mdl.clearSelection();
	}
}
