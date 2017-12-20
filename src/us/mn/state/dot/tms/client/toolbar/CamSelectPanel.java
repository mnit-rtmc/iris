/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2017  Minnesota Department of Transportation
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
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.PlayList;
import us.mn.state.dot.tms.PlayListHelper;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.VideoMonitorHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.CameraManager;
import us.mn.state.dot.tms.client.camera.CameraTheme;
import us.mn.state.dot.tms.client.camera.MonitorMarker;
import us.mn.state.dot.tms.client.camera.PlayListMarker;
import us.mn.state.dot.tms.client.map.Style;
import us.mn.state.dot.tms.client.map.VectorSymbol;

/**
 * A tool panel for quick camera selection.
 *
 * @author Douglas Lau
 */
public class CamSelectPanel extends ToolPanel {

	/** Selection mode */
	private interface Mode {
		void selectDevice();
		Icon getIcon();
		String getID();
	}

	/** Is this panel IRIS enabled? */
	static public boolean getIEnabled() {
		return true;
	}

	/** Video monitor symbol */
	static private final VectorSymbol MONITOR = new VectorSymbol(
		new MonitorMarker());

	/** Play list symbol */
	static private final VectorSymbol PLAYLIST = new VectorSymbol(
		new PlayListMarker());

	/** Camera manager */
	private final CameraManager manager;

	/** Key dispatcher */
	private final KeyDispatcher dispatcher = new KeyDispatcher();

	/** Icon label */
	private final JLabel lbl = new JLabel();

	/** ID text field */
	private final JTextField txt = new JTextField(8);

	/** Current selection mode */
	private Mode mode = cameraMode();

	/** Create camera selection mode */
	private Mode cameraMode() {
		return new Mode() {
			public void selectDevice() {
				Camera c = lookupCamera();
				if (c != null)
					manager.selectCamera(c);
			}
			public Icon getIcon() {
				return manager.getIcon(lookupCamera());
			}
			public String getID() {
				Camera c = lookupCamera();
				return (c != null) ? c.getName() : "";
			}
		};
	}

	/** Lookup a camera by ID */
	private Camera lookupCamera() {
		String t = getText();
		Camera c = CameraHelper.lookup(t);
		return (c != null) ? c : CameraHelper.findUID(t);
	}

	/** Create monitor selection mode */
	private Mode monitorMode() {
		return new Mode() {
			public void selectDevice() {
				VideoMonitor m = lookupMonitor();
				if (m != null)
					manager.selectMonitor(m);
			}
			public Icon getIcon() {
				return MONITOR.getLegend(getMonitorStyle());
			}
			public String getID() {
				VideoMonitor m = lookupMonitor();
				return (m != null) ? m.getName() : "";
			}
		};
	}

	/** Lookup a monitor by ID */
	private VideoMonitor lookupMonitor() {
		String t = getText();
		VideoMonitor m = VideoMonitorHelper.lookup(t);
		return (m != null) ? m : VideoMonitorHelper.findUID(t);
	}

	/** Get icon style for a monitor */
	private Style getMonitorStyle() {
		VideoMonitor m = lookupMonitor();
		return (m != null) ? CameraTheme.ACTIVE : CameraTheme.ALL;
	}

	/** Create playlist selection mode */
	private Mode playListMode() {
		return new Mode() {
			public void selectDevice() {
				manager.selectMonitorPlayList(lookupPlayList());
			}
			public Icon getIcon() {
				return PLAYLIST.getLegend(getPlayListStyle());
			}
			public String getID() {
				PlayList pl = lookupPlayList();
				return (pl != null) ? pl.getName() : "";
			}
		};
	}

	/** Lookup a play list by ID / num */
	private PlayList lookupPlayList() {
		String t = getText();
		PlayList pl = PlayListHelper.lookup(t);
		return (pl != null) ? pl : PlayListHelper.findNum(t);
	}

	/** Get icon style for a play list */
	private Style getPlayListStyle() {
		PlayList pl = lookupPlayList();
		return (pl != null) ? CameraTheme.ACTIVE : CameraTheme.ALL;
	}

	/** Create a new camera select panel */
	public CamSelectPanel(Session s) {
		manager = s.getCameraManager();
		txt.setMaximumSize(txt.getPreferredSize());
		addGap();
		add(txt);
		addGap();
		add(lbl);
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
			.addKeyEventDispatcher(dispatcher);
		txt.addKeyListener(new KeyAdapter() {
			@Override public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER)
					selectDevice();
			}
			@Override public void keyReleased(KeyEvent ke) {
				updateLabel();
			}
		});
		updateLabel();
	}

	/** Dispose of the panel */
	@Override
	public void dispose() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
			.removeKeyEventDispatcher(dispatcher);
		removeAll();
	}

	/** Select the entered device */
	private void selectDevice() {
		mode.selectDevice();
		mode = cameraMode();
		updateText("");
	}

	/** Get the current entered text */
	private String getText() {
		return txt.getText();
	}

	/** Update label from entered ID */
	private void updateLabel() {
		String t = getText();
		if (t.length() > 10) {
			mode = cameraMode();
			txt.setText("");
		}
		lbl.setIcon(mode.getIcon());
		lbl.setText(mode.getID());
	}

	/** Update the text widget */
	private void updateText(String t) {
		txt.setText(t);
		updateLabel();
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
			updateText(getText() + c);
			break;
		case '\n':
			selectDevice();
			break;
		case '.':
			String t = getText();
			if (t.length() > 0)
				updateText(t.substring(0, t.length() - 1));
			break;
		case '*':
			mode = monitorMode();
			updateText("");
			break;
		case '/':
			mode = playListMode();
			updateText("");
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
		case KeyEvent.VK_KP_DOWN:	/* xorg */
		case KeyEvent.VK_DOWN:		/* Windows */
			return '2';
		case KeyEvent.VK_PAGE_DOWN:
			return '3';
		case KeyEvent.VK_KP_LEFT:	/* xorg */
		case KeyEvent.VK_LEFT:		/* Windows */
			return '4';
		case KeyEvent.VK_BEGIN:		/* xorg */
		case KeyEvent.VK_CLEAR:		/* Windows */
			return '5';
		case KeyEvent.VK_KP_RIGHT:	/* xorg */
		case KeyEvent.VK_RIGHT:		/* Windows */
			return '6';
		case KeyEvent.VK_HOME:
			return '7';
		case KeyEvent.VK_KP_UP:		/* xorg */
		case KeyEvent.VK_UP:		/* Windows */
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
