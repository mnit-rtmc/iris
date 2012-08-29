/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010 AHMCT, University of California
 * Copyright (C) 2012  Minnesota Department of Transportation
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

import java.awt.Rectangle;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.JFrame;

/**
 * Persistent mutable user properties stored in a java properties file
 * on the client workstation.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class UserProperties {

	/** Property names */
	static private final String WIN_EXTSTATE = "win.extstate";
	static private final String WIN_X = "win.x";
	static private final String WIN_Y = "win.y";
	static private final String WIN_WIDTH = "win.width";
	static private final String WIN_HEIGHT = "win.height";
	static private final String TAB_SEL = "tab.selected";

	/** Get the directory to store user properties */
	static private File getDir() {
		String home = System.getProperty("user.home");
		return new File(home, "iris");
	}

	/** Get the file to store user properties */
	static private File getFile() {
		return new File(getDir(), "user.properties");
	}

	/** User properties */
	private final Properties props = new Properties();

	/** Create the user properties */
	public UserProperties() {
		try {
			read();
		}
		catch(FileNotFoundException e) {
			System.err.println("User properties: " +e.getMessage());
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	/** Read user properties */
	private void read() throws IOException {
		FileInputStream in = new FileInputStream(getFile());
		try {
			props.load(in);
		}
		finally {
			in.close();
		}
	}

	/** Write properties */
	public void write() throws IOException {
		File f = getFile();
		if(!f.canWrite())
			getDir().mkdirs();
		FileOutputStream fos = new FileOutputStream(f);
		try {
			props.store(fos, "IRIS Client user properties");
		}
		finally {
			fos.close();
		}
	}

	/** Set a property */
	private void setProp(String name, int i) {
		props.setProperty(name, new Integer(i).toString());
	}

	/** Get an integer property */
	private Integer getPropInt(String name) {
		String p = props.getProperty(name);
		if(p != null)
			return Integer.parseInt(p.trim());
		else
			return null;
	}

	/** Get window position from properties.
	 * @return Null on error else a rectangle for the window position. */
	public Rectangle getWindowPosition() {
		Integer x = getPropInt(WIN_X);
		Integer y = getPropInt(WIN_Y);
		Integer w = getPropInt(WIN_WIDTH);
		Integer h = getPropInt(WIN_HEIGHT);
		if(x != null && y != null && w != null && h != null)
			return new Rectangle(x, y, w, h);
		else
			return null;
	}

	/** Get window state from properties.
	 * @return Window extended state. */
	public Integer getWindowState() {
		Integer st = getPropInt(WIN_EXTSTATE);
		if(st != null && st == JFrame.MAXIMIZED_BOTH)
			return JFrame.MAXIMIZED_BOTH;
		else
			return null;
	}

	/** Return the name of a selected tab prop name */
	private String getTabPropName(int i) {
		return TAB_SEL + "." + String.valueOf(i);
	}

	/** Get array of currently selected tabs (as Integers) in each pane */
	public Object[] getSelectedTabs() {
		ArrayList<Integer> sti = new ArrayList<Integer>();
		for(int i = 0; ; i++) {
			String pn = getTabPropName(i);
			Integer t = getPropInt(pn);
			if(t != null)
				sti.add(t);
			else
				break;
		}
		return sti.toArray();
	}

	/** Update user properties associated with JFrame */
	public void setWindowProperties(IrisClient frame) {
		int es = frame.getExtendedState();
		setProp(WIN_EXTSTATE, es);
		Rectangle r = frame.getBounds();
		setProp(WIN_X, r.x);
		setProp(WIN_Y, r.y);
		setProp(WIN_WIDTH, r.width);
		setProp(WIN_HEIGHT, r.height);
		int[] sti = frame.getSelectedTabIndex();
		for(int i = 0; i < sti.length; ++i)
			setProp(getTabPropName(i), sti[i]);
	}
}
