/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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
import javax.swing.JInternalFrame;
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
		addItem(createPlayListItem());
		addItem(createMonitorStyleItem());
		addItem(createVideoMonitorItem());
		addItem(createCameraTemplateItem());
		addItem(createVidSrcTemplateItem());
		addItem(createFlowStreamItem());
	}

	/** Create an encoder type menu item action */
	private IAction createEncoderTypeItem() {
		return EncoderTypeForm.isPermitted(session) ?
		    new IAction("encoder.type.plural") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new EncoderTypeForm(session));
			}
		} : null;
	}

	/** Create a play list menu item action */
	private IAction createPlayListItem() {
		return PlayListForm.isPermitted(session) ?
		    new IAction("play.list.title") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new PlayListForm(session));
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

	/** Create a flow stream item action */
	private IAction createFlowStreamItem() {
		return FlowStreamForm.isPermitted(session) ?
		    new IAction("flow.stream") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new FlowStreamForm(session));
			}
		} : null;
	}
	
	/** Create a camera template menu item action */
	private IAction createCameraTemplateItem() {
		return CameraTemplateForm.isPermitted(session) ?
		    new IAction("camera.templates") {
			protected void doActionPerformed(ActionEvent e) {
				desktop.show(new CameraTemplateForm(session));
			}
		} : null;
	}
	
	/** Create a video source template menu item action */
	private IAction createVidSrcTemplateItem() {
		return VidSourceTemplateEditor.isPermitted(session) ?
				new IAction("camera.video_source.templates") {
			protected void doActionPerformed(ActionEvent e) {
				VidSourceTemplateEditor vste =
						new VidSourceTemplateEditor(session);
				JInternalFrame frame = desktop.show(vste);
				vste.setFrame(frame);
			}
		} : null;
	}
}
