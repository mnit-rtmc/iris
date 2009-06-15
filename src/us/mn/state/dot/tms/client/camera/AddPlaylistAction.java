/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
import javax.swing.AbstractAction;
import javax.swing.Action;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;

/**
 * This is an action to add a camera to the playlist.
 *
 * @author Douglas Lau
 */
public class AddPlaylistAction extends AbstractAction {

	/** Camera manager */
	protected final CameraManager manager;

	/** Proxy selection model */
	protected final ProxySelectionModel<Camera> s_model;

	/** Create a new add playlist action */
	public AddPlaylistAction(CameraManager m, ProxySelectionModel<Camera> s)
	{
		manager = m;
		s_model = s;
		putValue(Action.NAME, "Add to playlist");
		putValue(Action.SHORT_DESCRIPTION, "Add cameras to playlist");
		putValue(Action.LONG_DESCRIPTION, "Add the selected " +
			"cameras to the playlist");
	}

	/** Schedule the action to be performed */
	public void actionPerformed(ActionEvent e) {
		new AbstractJob() {
			public void perform() {
				do_perform();
			}
		}.addToScheduler();
	}

	/** Add the selected cameras to the playlist */
	protected void do_perform() {
		for(Camera c: s_model.getSelected())
			manager.addPlaylist(c);
		s_model.clearSelection();
	}
}
