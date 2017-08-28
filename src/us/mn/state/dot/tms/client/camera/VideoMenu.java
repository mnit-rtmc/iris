/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IMenu;
import us.mn.state.dot.tms.client.widget.SmartDesktop;

/**
 * VideoMenu contains a menu of camera/video monitor items.
 *
 * @author Douglas Lau
 */
public class VideoMenu extends IMenu {

	/** User Session */
	private final Session session;

	/** Desktop */
	private final SmartDesktop desktop;

	/** Create a new video menu */
	public VideoMenu(final Session s) {
		super("video");
		session = s;
		desktop = s.getDesktop();
		addItem(createEncoderTypeItem());
		addItem(session.createTableAction(Camera.SONAR_TYPE));
		addItem(createMonitorStyleItem());
		addItem(createVideoMonitorItem());
	}

	/** Create an encoder type menu item action */
	private IAction createEncoderTypeItem() {
		return EncoderTypeForm.isPermitted(session) ?
		    new IAction("camera.encoder.type.plural") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new EncoderTypeForm(session));
			}
		    } : null;
	}

	/** Create a monitor style menu item action */
	private IAction createMonitorStyleItem() {
		return MonitorStyleForm.isPermitted(session) ?
		    new IAction("monitor.style.plural") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new MonitorStyleForm(session));
			}
		} : null;
	}

	/** Create a video monitor menu item action */
	private IAction createVideoMonitorItem() {
		return VideoMonitorForm.isPermitted(session) ?
		    new IAction("video.monitor") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new VideoMonitorForm(session));
			}
		    } : null;
	}
}
