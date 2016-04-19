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
	private final JTextField txt = new JTextField(8);

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
		manager.selectCamera(c);
		txt.setText("");
	}

	/** Update icon from entered camera ID */
	private void updateIcon() {
		String t = txt.getText();
		if (t.length() > 10) {
			t = "";
			txt.setText(t);
		}
		lbl.setIcon(manager.getIcon(lookupCamera(t)));
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
			boolean numpad = e.getKeyLocation() ==
				KeyEvent.KEY_LOCATION_NUMPAD;
			if (e.getID() == KeyEvent.KEY_PRESSED && numpad)
				pressed = true;
			if (e.getID() == KeyEvent.KEY_RELEASED) {
				pressed = false;
				if (numpad)
					dispatchNumpadEvent(e);
			}
			return pressed;
		}
	}

	/** Dispatch a numpad key event */
	private void dispatchNumpadEvent(KeyEvent e) {
		char c = numpadChar(e.getKeyCode());
		switch (c) {
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			txt.setText(txt.getText() + c);
			updateIcon();
			break;
		case '\n':
			selectCamera();
			break;
		case '.':
			String t = txt.getText();
			if (t.length() > 0) {
				txt.setText(t.substring(0, t.length() - 1));
				updateIcon();
			}
			break;
		case '-':
			manager.selectPreviousCamera();
			break;
		case '+':
			manager.selectNextCamera();
			break;
		}
	}

	/** Get a character for a numpad keycode */
	static private char numpadChar(int kc) {
		switch (kc) {
		case KeyEvent.VK_INSERT:
			return '0';
		case KeyEvent.VK_END:
			return '1';
		case KeyEvent.VK_KP_DOWN:
			return '2';
		case KeyEvent.VK_PAGE_DOWN:
			return '3';
		case KeyEvent.VK_KP_LEFT:
			return '4';
		case KeyEvent.VK_BEGIN: /* xorg */
		case KeyEvent.VK_CLEAR:	/* Windows */
			return '5';
		case KeyEvent.VK_KP_RIGHT:
			return '6';
		case KeyEvent.VK_HOME:
			return '7';
		case KeyEvent.VK_KP_UP:
			return '8';
		case KeyEvent.VK_PAGE_UP:
			return '9';
		case KeyEvent.VK_DIVIDE:
			return '/';
		case KeyEvent.VK_MULTIPLY:
			return '*';
		case KeyEvent.VK_SUBTRACT:
			return '-';
		case KeyEvent.VK_ADD:
			return '+';
		case KeyEvent.VK_DELETE:
			return '.';
		case KeyEvent.VK_ENTER:
			return '\n';
		default:
			return 0;
		}
	}
}
