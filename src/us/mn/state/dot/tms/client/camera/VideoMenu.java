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
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.utils.I18N;

/**
 * VideoMenu contains a menu of camera/video monitor items.
 *
 * @author Douglas Lau
 */
public class VideoMenu extends JMenu {

	/** User Session */
	private final Session session;

	/** Desktop */
	private final SmartDesktop desktop;

	/** Create a new video menu */
	public VideoMenu(final Session s) {
		super(I18N.get("video"));
		session = s;
		desktop = s.getDesktop();
		JMenuItem item = createEncoderTypeItem();
		if (item != null)
			add(item);
		item = createCameraItem();
		if (item != null)
			add(item);
		item = createMonitorStyleItem();
		if (item != null)
			add(item);
		item = createVideoMonitorItem();
		if (item != null)
			add(item);
	}

	/** Create the encoder type menu item */
	private JMenuItem createEncoderTypeItem() {
		if (EncoderTypeForm.isPermitted(session)) {
			return new JMenuItem(new IAction(
				"camera.encoder.type.plural")
			{
				protected void doActionPerformed(ActionEvent e){
					desktop.show(new EncoderTypeForm(
						session));
				}
			});
		} else
			return null;
	}

	/** Create the camera menu item */
	private JMenuItem createCameraItem() {
		if (!CameraForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("camera.title") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new CameraForm(session));
			}
		});
	}

	/** Create the monitor style menu item */
	private JMenuItem createMonitorStyleItem() {
		if (MonitorStyleForm.isPermitted(session)) {
			return new JMenuItem(new IAction("monitor.style.plural")
			{
				protected void doActionPerformed(ActionEvent e){
					desktop.show(new MonitorStyleForm(
						session));
				}
			});
		} else
			return null;
	}

	/** Create the video monitor menu item */
	private JMenuItem createVideoMonitorItem() {
		if (!VideoMonitorForm.isPermitted(session))
			return null;
		return new JMenuItem(new IAction("video.monitor") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new VideoMonitorForm(session));
			}
		});
	}
}
