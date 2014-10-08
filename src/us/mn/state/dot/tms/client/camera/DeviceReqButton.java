/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.Icons;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A button to send device requests.
 *
 * @author Douglas Lau
 */
public class DeviceReqButton extends JButton {

	/** Camera PTZ */
	private final CameraPTZ cam_ptz;

	/** Device request for pressed state */
	private final DeviceRequest pressed_req;

	/** Device request for released state */
	private final DeviceRequest released_req;

	/** Most recent pressed state */
	private boolean pressed = false;

	/** Create a device request button.
	 * @param text_id
	 * @param cptz Camera PTZ.
	 * @param pdr Device request for pressed state.
	 * @param rdr Device request for released state. */
	public DeviceReqButton(String text_id, CameraPTZ cptz,
		DeviceRequest pdr, DeviceRequest sdr)
	{
		cam_ptz = cptz;
		pressed_req = pdr;
		released_req = sdr;
		setAction(new IAction(text_id) {
			protected void doActionPerformed(ActionEvent ev) {
				doReleased();
			}
		});
		setMargin(UI.buttonInsets());
		ImageIcon icon = Icons.getIconByPropName(text_id);
		if (icon != null) {
			setIcon(icon);
			setHideActionText(true);
		}
		addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent ce) {
				boolean p = getModel().isPressed();
				if (p && !pressed)
					doPressed();
				else if (pressed && !p)
					doReleased();
			}
		});
	}

	/** Create a device request button.
	 * @param text_id
	 * @param cptz Camera PTZ.
	 * @param pdr Device request for pressed state. */
	public DeviceReqButton(String text_id, CameraPTZ cptz,
		DeviceRequest pdr)
	{
		this(text_id, cptz, pdr, null);
	}

	/** Do button pressed */
	private void doPressed() {
		if (pressed_req != null)
			cam_ptz.sendRequest(pressed_req);
		pressed = true;
	}

	/** Do button released */
	private void doReleased() {
		if (released_req != null)
			cam_ptz.sendRequest(released_req);
		pressed = false;
	}

	/** Set enabled status */
	@Override
	public void setEnabled(boolean e) {
		super.setEnabled(e && cam_ptz.canRequestDevice());
	}
}
