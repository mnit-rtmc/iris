/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.widget.IAction2;

/**
 * A panel containing buttons for recalling and storing camera presets.
 *
 * @author Douglas Lau
 */
public class PresetPanel extends JPanel {

	/** Number of buttons used to go to preset location */
	static private final int NUMBER_PRESET_BUTTONS =
		SystemAttrEnum.CAMERA_NUM_PRESET_BTNS.getInt();

	/** Array of buttons used to go to preset locations */
	private final JButton[] preset_btn = new JButton[NUMBER_PRESET_BUTTONS];

	/** Selected camera */
	private Camera camera = null;

	/** Create a preset panel */
	public PresetPanel() {
		super(new GridLayout(0, 6, 6, 6));
		for(int i = 0; i < NUMBER_PRESET_BUTTONS; i++) {
			preset_btn[i] = createPresetButton(i + 1);
			add(preset_btn[i]);
		}
	}

	/** Create a preset button */
	private JButton createPresetButton(final int num) {
		JButton btn = new JButton(new IAction2("camera.preset") {
			protected void doActionPerformed(ActionEvent e) {
				Camera c = camera;
				if(c != null)
					c.setRecallPreset(num);
			}
		});
		btn.setText(Integer.toString(num));
		return btn;
	}

	/** Set the camera */
	public void setCamera(Camera c) {
		camera = c;
	}

	/** Set enabled status */
	public void setEnabled(boolean enable) {
		for(JButton b: preset_btn)
			b.setEnabled(enable);
	}
}
