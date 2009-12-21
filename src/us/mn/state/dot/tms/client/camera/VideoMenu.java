/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/**
 * VideoMenu contains a menu of camera/video monitor items.
 *
 * @author Douglas Lau
 */
public class VideoMenu extends JMenu {

	/** Create a new video menu */
	public VideoMenu(final Session s) {
		super("Video");
		final SmartDesktop desktop = s.getDesktop();
		JMenuItem item = new JMenuItem("Cameras");
		item.setMnemonic('C');
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new CameraForm(s));
			}
		};
		add(item);
		item = new JMenuItem("Monitors");
		new ActionJob(item) {
			public void perform() throws Exception {
				desktop.show(new VideoMonitorForm(s));
			}
		};
		add(item);
	}
}
