/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2016  Minnesota Department of Transportation
 * Copyright (C) 2010-2014 AHMCT, University of California
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
 * A user property is workstation-specific, such as window placement, selected
 * tabs, etc.
 *
 * @author Douglas Lau
 * @author Michael Darter
 * @author Travis Swanston
 */
public enum UserProperty {
	WIN_EXT_STATE	("win.extstate"),
	WIN_X		("win.x"),
	WIN_Y		("win.y"),
	WIN_WIDTH	("win.width"),
	WIN_HEIGHT	("win.height"),
	TAB_SEL_0	("tab.selected.0"),
	TAB_SEL_1	("tab.selected.1"),
	TAB_SEL_2	("tab.selected.2"),
	TAB_SEL_3	("tab.selected.3"),
	TAB_LIST	("tab.list"),
	SCALE		("scale"),
	VIDEO_EXTVIEWER	("video.extviewer");

	/** Property name */
	public final String name;

	/** Create a new user property */
	private UserProperty(String n) {
		name = n;
	}

	/** Default values for use with unspecified properties */
	static private final String TAB_LIST_DEF
		= "incident, dms, camera, lcs, ramp.meter, gate.arm, r_node, "
		+ "beacon, tag.reader, action.plan, comm";

	/** Get the directory to store user properties */
	static private File getDir() {
		String home = System.getProperty("user.home");
		return new File(home, "iris");
	}

	/** Get the file to store user properties */
	static private File getFile() {
		return new File(getDir(), "user.properties");
	}

	/** Load user properties */
	static public Properties load(Properties defs) throws IOException {
		Properties p = new Properties(defs);
		try {
			loadFile(p);
		}
		catch (FileNotFoundException e) {
			System.err.println("User properties: " +e.getMessage());
		}
		return p;
	}

	/** Load user properties from file */
	static private void loadFile(Properties p) throws IOException {
		FileInputStream in = new FileInputStream(getFile());
		try {
			p.load(in);
		}
		finally {
			in.close();
		}
	}

	/** Store properties */
	static public void store(Properties p) throws IOException {
		File f = getFile();
		if (!f.canWrite())
			getDir().mkdirs();
		FileOutputStream fos = new FileOutputStream(f);
		try {
			p.store(fos, "IRIS Client user properties");
		}
		finally {
			fos.close();
		}
	}

	/** Set a string property */
	static private void setProp(Properties p, UserProperty up, String v) {
		p.setProperty(up.name, v);
	}

	/** Set an integer property */
	static private void setProp(Properties p, UserProperty up, int i) {
		setProp(p, up, Integer.toString(i));
	}

	/** Get a property value as a string */
	static private String getProp(Properties p, UserProperty up) {
		return p.getProperty(up.name, "").trim();
	}

	/** Get an integer property */
	static private Integer getPropI(Properties p, UserProperty up) {
		try {
			return Integer.parseInt(getProp(p, up));
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/** Get a float property */
	static private Float getPropF(Properties p, UserProperty up) {
		try {
			return Float.parseFloat(getProp(p, up));
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/** Get window position from properties.
	 * @return Null on error else a rectangle for the window position. */
	static public Rectangle getWindowPosition(Properties p) {
		Integer x = getPropI(p, WIN_X);
		Integer y = getPropI(p, WIN_Y);
		Integer w = getPropI(p, WIN_WIDTH);
		Integer h = getPropI(p, WIN_HEIGHT);
		if (x != null && y != null && w != null && h != null)
			return new Rectangle(x, y, w, h);
		else
			return null;
	}

	/** Get window state from properties.
	 * @return Window extended state. */
	static public Integer getWindowState(Properties p) {
		Integer st = getPropI(p, WIN_EXT_STATE);
		if (st != null && st == JFrame.MAXIMIZED_BOTH)
			return JFrame.MAXIMIZED_BOTH;
		else
			return null;
	}

	/** Get array of currently selected tabs in each pane */
	static public String[] getSelectedTabs(Properties p) {
		ArrayList<String> st = new ArrayList<String>();
		String t = getProp(p, TAB_SEL_0);
		if (t.length() > 0)
			st.add(t);
		t = getProp(p, TAB_SEL_1);
		if (t.length() > 0)
			st.add(t);
		t = getProp(p, TAB_SEL_2);
		if (t.length() > 0)
			st.add(t);
		t = getProp(p, TAB_SEL_3);
		if (t.length() > 0)
			st.add(t);
		return st.toArray(new String[0]);
	}

	/** Update user properties associated with JFrame */
	static public void setWindowProperties(Properties p, IrisClient frame) {
		int es = frame.getExtendedState();
		setProp(p, WIN_EXT_STATE, es);
		Rectangle r = frame.getBounds();
		setProp(p, WIN_X, r.x);
		setProp(p, WIN_Y, r.y);
		setProp(p, WIN_WIDTH, r.width);
		setProp(p, WIN_HEIGHT, r.height);
		String[] st = frame.getSelectedTabs();
		if (st.length > 0)
			setProp(p, TAB_SEL_0, st[0]);
		if (st.length > 1)
			setProp(p, TAB_SEL_1, st[1]);
		if (st.length > 2)
			setProp(p, TAB_SEL_2, st[2]);
		if (st.length > 3)
			setProp(p, TAB_SEL_3, st[3]);
	}

	/** Get the user interface scale factor */
	static public float getScale(Properties p) {
		Float s = getPropF(p, SCALE);
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
	static public String[] getTabList(Properties p) {
		String val = getProp(p, TAB_LIST);
		if ("".equals(val))
			val = TAB_LIST_DEF;
		String[] fields = val.split(",");
		ArrayList<String> tl = new ArrayList<String>(fields.length);
		for (String f : fields)
			tl.add(f.trim());
		return tl.toArray(new String[tl.size()]);
	}

	/**
	 * Return the external video viewer executable string.
	 * @return the external video viewer executable string,
	 *         or null if not found.
	 */
	static public String getExternalVideoViewer(Properties p) {
		String ev = getProp(p, VIDEO_EXTVIEWER);
		if ("".equals(ev))
			return null;
		return ev;
	}
}
