/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2020  Minnesota Department of Transportation
 * Copyright (C) 2010-2014  AHMCT, University of California
 * Copyright (C) 2017       Iteris Inc.
 * Copyright (C) 2023       SRF Consulting Group
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

import java.awt.Frame;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import javax.swing.JFrame;
import us.mn.state.dot.tms.VideoMonitor;

/**
 * A user property is workstation-specific, such as window placement, selected
 * tabs, etc.
 *
 * @author Douglas Lau
 * @author Michael Darter
 * @author Travis Swanston
 * @author John L. Stanley
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
	VIDEO_MONITOR	("video.monitor"),
	STREAM_LNAME	("stream.layoutname"),
	STREAM_CCTV	("stream.cctv"),
	STREAM_WIDTH	("stream.width"),
	STREAM_HEIGHT	("stream.height"),
	STREAM_X	("stream.x"),
	STREAM_Y	("stream.y"),
	STREAM_SRC	("stream.src"),
	NUM_STREAM	("num.stream"),
	NUM_LAYOUT	("num.layout");

	/** Property name */
	public final String name;

	/** Create a new user property */
	private UserProperty(String n) {
		name = n;
	}

	/** Default values for use with unspecified properties */
	static private final String TAB_LIST_DEF
		= "incident, dms, camera, lcs, ramp.meter, gate.arm, "
		+ "parking.area, r_node, beacon, tag.reader, action.plan, "
		+ "comm, weather_sensor, alert";

	/** Get the directory to store user properties */
	static public File getDir() {
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

	/** Set a string property with one integer increment */
	static private void setProp(Properties p, UserProperty up, String v,
		int i)
	{
		p.setProperty(up.name + "." + i, v);
	}

	/** Set an integer property with one integer increment */
	static private void setProp(Properties p, UserProperty up, int v,
		int i)
	{
		setProp(p, up, Integer.toString(v), i);
	}

	/** Set a string property with two integer increments */
	static private void setProp(Properties p, UserProperty up, String v,
		int i, int j)
	{
		p.setProperty(up.name + "." + i + "." + j, v);
	}

	/** Set an integer property with two integer increments */
	static private void setProp(Properties p, UserProperty up, int v, int i,
		int j)
	{
		setProp(p, up, Integer.toString(v), i, j);
	}

	/** Get a property value as a string */
	static private String getProp(Properties p, UserProperty up) {
		return p.getProperty(up.name, "").trim();
	}

	/** Get a property (with one integer increment) value as a string */
	static private String getProp(Properties p, UserProperty up, int i) {
		return p.getProperty(up.name + "." + i, "").trim();
	}

	/** Get a property (with two integer increments) value as a string */
	static private String getProp(Properties p, UserProperty up, int i,
		int j)
	{
		return p.getProperty(up.name + "." + i + "." + j, "").trim();
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

	/** Get an integer property with an integer increment */
	static private Integer getPropI(Properties p, UserProperty up, int i) {
		try {
			return Integer.parseInt(getProp(p, up, i));
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

	/** Remove the specified property with one integer increment */
	static private void removeProp(Properties p, UserProperty up, int i) {
		p.remove(up.name + "." + i);
	}

	/** Remove the specified property with two integer increments */
	static private void removeProp(Properties p, UserProperty up, int i,
		int j)
	{
		p.remove(up.name + "." + i + "." + j);
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

	/** Get HashMap of camera/stream frames */
	static public HashMap<String, String> getCameraFrames(Properties p,
		String layoutName)
	{
		HashMap<String, String> hmap = new HashMap<String, String>();
		Integer lnum = getLayoutNumber(p, layoutName);
		if (lnum == null)
			return hmap;
		Integer num_streams = getPropI(p, NUM_STREAM, lnum);
		if (num_streams == null)
			return hmap;
		hmap.put(NUM_STREAM.name, Integer.toString(num_streams));
		for (int i = 0; i < num_streams; i++) {
			hmap.put(STREAM_CCTV.name + "." + i,
				getProp(p, STREAM_CCTV, lnum, i));
			hmap.put(STREAM_WIDTH.name + "." + i,
				getProp(p, STREAM_WIDTH, lnum, i));
			hmap.put(STREAM_HEIGHT.name + "." + i,
				getProp(p, STREAM_HEIGHT, lnum, i));
			hmap.put(STREAM_X.name + "." + i,
				getProp(p, STREAM_X, lnum, i));
			hmap.put(STREAM_Y.name + "." + i,
				getProp(p, STREAM_Y, lnum, i));
			hmap.put(STREAM_SRC.name + "." + i,
				getProp(p, STREAM_SRC, lnum, i));
		}
		return hmap;
	}

	/** Save a video stream window layout with the name layoutName. */
	static public void saveStreamLayout(Properties p, String layoutName) {
		// try to find this layout in the file - if we can't, start a new one
		Integer lnum = getLayoutNumber(p, layoutName);
		if (lnum == null) {
			lnum = getPropI(p, NUM_LAYOUT);
			if (lnum == null)
				lnum = 0;
		}

		Frame[] frames = IrisClient.getFrames();
		int j = 0;
		setProp(p, STREAM_LNAME, layoutName, lnum);
		for (Frame f : frames) {
			String frame_title = f.getTitle();
			if (frame_title.contains("Stream Panel") && f.isVisible()) {
				String cam_name = frame_title.split("Stream Panel: ")[1];
				setProp(p, STREAM_CCTV, cam_name, lnum, j);
				setProp(p, STREAM_WIDTH, f.getComponent(0).getWidth(), lnum, j);
				setProp(p, STREAM_HEIGHT, f.getComponent(0).getHeight(), lnum, j);
				setProp(p, STREAM_X, String.valueOf(f.getX()), lnum, j);
				setProp(p, STREAM_Y, String.valueOf(f.getY()), lnum, j);
				setProp(p, STREAM_SRC, String.valueOf(0), lnum, j); // TODO
				j += 1;
			}
		}
		setProp(p, NUM_STREAM, j, lnum);
		setProp(p, NUM_LAYOUT, lnum + 1);
	}

	/** Delete a video stream window layout with the name layoutName. */
	static public void deleteStreamLayout(Properties p, String layoutName) {
		Integer lnum = getLayoutNumber(p, layoutName);
		if (lnum == null)
			return;
		Integer num_streams = getPropI(p, NUM_STREAM, lnum);
		if (num_streams == null)
			num_streams = 0;

		for (int i = 0; i < num_streams; i++) {
			removeProp(p, STREAM_CCTV, lnum, i);
			removeProp(p, STREAM_WIDTH, lnum, i);
			removeProp(p, STREAM_HEIGHT, lnum, i);
			removeProp(p, STREAM_X, lnum, i);
			removeProp(p, STREAM_Y, lnum, i);
			removeProp(p, STREAM_SRC, lnum, i);
		}

		// delete the layout-level properties
		removeProp(p, STREAM_LNAME, lnum);
		removeProp(p, NUM_STREAM, lnum);

		// decrement the layout counter (it's 0-indexed so lnum is correct)
		setProp(p, NUM_LAYOUT, lnum);
	}

	/** Get a list of layout names (in case-insensitive alphabetical order). */
	static public ArrayList<String> getStreamLayoutNames(Properties p) {
		ArrayList<String> layoutNames = new ArrayList<String>();

		// look through all the entries for STREAM_LNAME entries
		Set<Entry<Object, Object>> entries = p.entrySet();
		for (Entry<Object, Object> e: entries) {
			String k = (String) e.getKey();
			if (k.startsWith(STREAM_LNAME.name))
				layoutNames.add((String) e.getValue());
		}
		layoutNames.sort(String::compareToIgnoreCase);
		return layoutNames;
	}

	/** Get the next available stream layout name */
	static public String getNextStreamLayoutName(Properties p) {
		HashSet<String> hSet = new HashSet<String>(
			getStreamLayoutNames(p));
		for (int i = 1; i < 999; ++i) {
			String name = "layout_" + i;
			if (!hSet.contains(name))
				return name;
		}
		assert false;
		return null;
	}

	/** Lookup the layout number for the specified layout name. If not found,
	 *  null is returned.
	 */
	static public Integer getLayoutNumber(Properties p, String layoutName) {
		// look through all the entries for STREAM_LNAME entries
		Set<Entry<Object, Object>> entries = p.entrySet();
		for (Entry<Object, Object> e: entries) {
			String k = (String) e.getKey();
			if (k.startsWith(STREAM_LNAME.name)) {
				String v = (String) e.getValue();
				if (v.equals(layoutName)) {
					// if we get one with a value equal to layoutName, get
					// the number
					String nStr = k.replace(STREAM_LNAME.name + ".", "");
					try {
						return Integer.valueOf(nStr);
					} catch (NumberFormatException ex) {
						// TODO this is probably a problem we should deal with
						ex.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	/** Update user properties */
	static public void updateProperties(Properties p, IrisClient frame) {
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
		Session s = frame.getSession();
		VideoMonitor vm = (s != null)
		                ? s.getCameraManager().getSelectedMonitor()
		                : null;
		setProp(p, VIDEO_MONITOR, (vm != null) ? vm.toString() : "");
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

	/** Get the video monitor string */
	static public String getVideoMonitor(Properties p) {
		String vm = getProp(p, VIDEO_MONITOR);
		return (!vm.isEmpty()) ? vm : null;
	}
}
