/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toolbar;

import java.awt.Color;
import java.awt.KeyboardFocusManager;
import java.awt.KeyEventDispatcher;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JTextField;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.CameraManager;
import us.mn.state.dot.tms.client.camera.CameraTheme;

/**
 * A tool panel for quick camera selection.
 *
 * @author Douglas Lau
 */
public class CamSelectPanel extends ToolPanel {

	/** Is this panel IRIS enabled? */
	static public boolean getIEnabled() {
		return true;
	}

	/** Camera manager */
	private final CameraManager manager;

	/** Key dispatcher */
	private final KeyDispatcher dispatcher = new KeyDispatcher();

	/** Icon label */
	private final JLabel lbl = new JLabel();

	/** ID text field */
	private final JTextField txt = new JTextField(10);

	/** Create a new camera select panel */
	public CamSelectPanel(Session s) {
		manager = s.getCameraManager();
		lbl.setIcon(manager.getIcon(CameraTheme.ALL));
		txt.setMaximumSize(txt.getPreferredSize());
		add(Box.createHorizontalStrut(4));
		add(lbl);
		add(Box.createHorizontalStrut(4));
		add(txt);
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
			.addKeyEventDispatcher(dispatcher);
		txt.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					selectCamera();
			}
			@Override public void keyReleased(KeyEvent ke) {
				updateIcon();
			}
		});
	}

	/** Dispose of the panel */
	@Override
	public void dispose() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
			.removeKeyEventDispatcher(dispatcher);
		removeAll();
	}

	/** Select the entered camera */
	private void selectCamera() {
		Camera c = lookupCamera(txt.getText());
		txt.setText("");
	}

	/** Update icon from entered camera ID */
	private void updateIcon() {
		lbl.setIcon(manager.getIcon(lookupCamera(txt.getText())));
	}

	/** Lookup a camera by ID */
	private Camera lookupCamera(String id) {
		Camera c = CameraHelper.lookup(id);
		return (c != null) ? c : CameraHelper.findUID(id);
	}

	/** Key dispatcher for application-wide numpad hotkeys */
	private class KeyDispatcher implements KeyEventDispatcher {
		/* We need to keep track of last pressed state so we
		 * don't propogate KEY_TYPED events from numpad */
		private boolean pressed = false;
		private final Toolkit tk = Toolkit.getDefaultToolkit();
		@Override
		public boolean dispatchKeyEvent(KeyEvent e) {
			if (tk.getLockingKeyState(KeyEvent.VK_NUM_LOCK)) {
				pressed = false;
				return false;
			}
			System.err.println(e.getKeyCode());
			if (e.getID() == KeyEvent.KEY_PRESSED &&
			    e.getKeyLocation() == KeyEvent.KEY_LOCATION_NUMPAD)
				pressed = true;
			if (e.getID() == KeyEvent.KEY_RELEASED)
				pressed = false;
			return pressed;
		}
	}
}
