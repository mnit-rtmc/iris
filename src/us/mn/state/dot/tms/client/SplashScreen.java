/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client;

import java.awt.Color;
import java.awt.Cursor;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import us.mn.state.dot.tms.utils.Screen;

/**
 * The splash screen displayed during the initialization of the SignClient
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class SplashScreen extends JWindow {

	/** Create a new splash screen */
	public SplashScreen() {
		setAlwaysOnTop(false);
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		URL url = getClass().getResource("/images/SplashLogo.jpg");
		ImageIcon icon = new ImageIcon(url);
		JLabel label = new JLabel(icon);
		label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		getContentPane().add(label);
		pack();
		Screen s = Screen.getAllScreens()[0];
		s.centerWindow(this);
	}

	/** Dispose of the splash screen */
	public void dispose() {
		removeAll();
		super.dispose();
	}
}
