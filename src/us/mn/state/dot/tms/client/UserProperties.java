/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2014 AHMCT, University of California
 * Copyright (C) 2012-2014  Minnesota Department of Transportation
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
import us.mn.state.dot.sched.ExceptionHandler;

/**
 * Persistent mutable user properties stored in a java properties file
 * on the client workstation.
 *
 * @author Michael Darter
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class UserProperties {

	/** Property names */
	static private final String WIN_EXTSTATE = "win.extstate";
	static private final String WIN_X = "win.x";
	static private final String WIN_Y = "win.y";
	static private final String WIN_WIDTH = "win.width";
	static private final String WIN_HEIGHT = "win.height";
	static private final String TAB_LIST = "tab.list";
	static private final String TAB_SEL = "tab.selected";
	static private final String SCALE = "scale";

	/** Default values for use with unspecified properties */
	static private final String DEFVAL_TAB_LIST
		= "incident, dms, camera, lcs, ramp.meter, gate.arm, r_node, "
		+ "action.plan, comm";

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
	private final Properties user_props = new Properties();

	/** Client properties */
	private final Properties client_props;

	/** Create the user properties */
	public UserProperties(Properties cp, ExceptionHandler h) {
		client_props = cp;
		try {
			read();
		}
		catch (FileNotFoundException e) {
			System.err.println("User properties: " +e.getMessage());
		}
		catch (IOException e) {
			h.handle(e);
		}
	}

	/** Read user properties */
	private void read() throws IOException {
		FileInputStream in = new FileInputStream(getFile());
		try {
			user_props.load(in);
		}
		finally {
			in.close();
		}
	}

	/** Write properties */
	public void write() throws IOException {
		File f = getFile();
		if (!f.canWrite())
			getDir().mkdirs();
		FileOutputStream fos = new FileOutputStream(f);
		try {
			user_props.store(fos, "IRIS Client user properties");
		}
		finally {
			fos.close();
		}
	}

	/** Set a string property */
	private void setProp(String name, String v) {
		user_props.setProperty(name, v);
	}

	/** Set an integer property */
	private void setProp(String name, int i) {
		setProp(name, Integer.toString(i));
	}

	/** Set a float property */
	private void setProp(String name, float f) {
		setProp(name, Float.toString(f));
	}

	/**
	 * Get a string property, first checking user properties, then
	 * client properties.  Note: properties which are not found in user
	 * properties but are found in client properties will be returned,
	 * but will not be copied over to user properties, as such properties
	 * should only override client properties if they are explicitly
	 * entered into the user properties file.  Otherwise, subsequent
	 * modification of these client properties centrally by the admin
	 * will have no effect on users whose user property files contain
	 * implicit copies of them.
	 *
	 * @param name The property name
	 * @return The trimmed property value, from user properties if
	 *         present, else from client properties if present, else
	 *         empty-string.
	 */
	private String getPropString(String name) {
		String val = user_props.getProperty(name);
		if (val == null)
			val = client_props.getProperty(name, "");
		return val.trim();
	}

	/** Get an integer property */
	private Integer getPropInt(String name) {
		try {
			return Integer.parseInt(getPropString(name));
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/** Get a float property */
	private Float getPropFloat(String name) {
		try {
			return Float.parseFloat(getPropString(name));
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/** Get window position from properties.
	 * @return Null on error else a rectangle for the window position. */
	public Rectangle getWindowPosition() {
		Integer x = getPropInt(WIN_X);
		Integer y = getPropInt(WIN_Y);
		Integer w = getPropInt(WIN_WIDTH);
		Integer h = getPropInt(WIN_HEIGHT);
		if (x != null && y != null && w != null && h != null)
			return new Rectangle(x, y, w, h);
		else
			return null;
	}

	/** Get window state from properties.
	 * @return Window extended state. */
	public Integer getWindowState() {
		Integer st = getPropInt(WIN_EXTSTATE);
		if (st != null && st == JFrame.MAXIMIZED_BOTH)
			return JFrame.MAXIMIZED_BOTH;
		else
			return null;
	}

	/** Return the name of a selected tab prop name */
	private String getTabPropName(int i) {
		return TAB_SEL + "." + String.valueOf(i);
	}

	/** Get array of currently selected tabs in each pane */
	public String[] getSelectedTabs() {
		ArrayList<String> st = new ArrayList<String>();
		for (int i = 0; ; i++) {
			String pn = getTabPropName(i);
			String t = getPropString(pn);
			if (t.length() > 0)
				st.add(t);
			else
				break;
		}
		return st.toArray(new String[0]);
	}

	/** Get the user interface scale factor */
	public float getScale() {
		Float s = getPropFloat(SCALE);
		if (s != null && s >= 0.25f && s <= 4.0f)
			return s;
		else
			return 1f;
	}

	/**
	 * Get the TAB_LIST property, or its default, as an ordered, trimmed
	 * array.
	 *
	 * @return an ordered, trimmed array containing the entries specified
	 *         in the TAB_LIST property, or if the property is not
	 *         specified, the default value for TAB_LIST.
	 */
	public String[] getTabList() {
		String val = getPropString(TAB_LIST);
		if ("".equals(val))
			val = DEFVAL_TAB_LIST;
		String[] fields = val.split(",");
		ArrayList<String> tl = new ArrayList<String>(fields.length);
		for (String f : fields)
			tl.add(f.trim());
		return tl.toArray(new String[tl.size()]);
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
		String[] st = frame.getSelectedTabs();
		for (int i = 0; i < st.length; i++)
			setProp(getTabPropName(i), st[i]);
	}
}
