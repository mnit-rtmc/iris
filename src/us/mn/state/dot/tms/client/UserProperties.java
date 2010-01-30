/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010 AHMCT, University of California
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import javax.swing.JFrame;
import us.mn.state.dot.tms.utils.PropertyLoader;

/**
 * Persistent mutable user properties stored in a java 
 * properties file on the client workstation.
 *
 * @author Michael Darter
 */
public class UserProperties {

	/** Name of property in the client property file, which specifies
	 *  the name of the user property file on the client workstation. */
	public static final String FNAME_PROP_NAME = 
		"client.userpropsfile.name";

	/** Property names */
	final String WIN_EXTSTATE = "win.extstate";
	final String WIN_X = "win.x";
	final String WIN_Y = "win.y";
	final String WIN_WIDTH = "win.width";
	final String WIN_HEIGHT = "win.height";

	/** Props file name */
	private String m_fname = "";

	/** Properties, null indicates not used */
	private Properties m_props;

	/** Flag if properties have changed */
	private boolean m_changed = false;

	/** Main window size and position */
	public Rectangle m_wpos;

	/** Constructor
	 * @param fname Properties file name. If empty or null, 
	 *	  client props not used. */
	public UserProperties(String fname) {
		m_fname = fname;
		m_props = null;
	}

	/** Properties file used? */
	public boolean used() {
		return m_props != null;
	}

	/** Read properties. The properties file name must already 
	 *  have been specified. After a read, the m_props field is
	 *  updated, else it is set to null. */
	public void read() {
		FileInputStream in = getFileInputStream(m_fname);
		if(in == null)
			return;
		try {
			m_props = new Properties();
			m_props.load(in);
			System.err.println("User properties: read " + m_fname);
			m_changed = false;
		} catch(IOException ex) {
			System.err.println("UserProperties: " + ex);
			m_props = null;
			return;
		} finally {
			try {
				if(in != null)
					in.close();
			} catch(IOException ex) {
				m_props = null;
			}
		}
	}

	/** Get properties file input stream. */
	public static FileInputStream getFileInputStream(String fname) {
		if(fname == null || fname.isEmpty()) {
			System.err.println("User properties: file " + 
				"not specifed.");
			return null;
		}
		FileInputStream in = null;
		try {
			in = new FileInputStream(fname);
		} catch(FileNotFoundException ex) {
			System.err.println("User properties: specified " +
				"file not found (" + fname + "): " + ex);
		}
		return in;
	}

	/** Write properties */
	public void write() {
		final String comment = "IRIS Client properties file";
		if(used() && m_changed) {
			if(!write(m_fname, m_props, comment))
				m_changed = false;
			System.err.println("Wrote properties file: " + m_fname);
		}
	}

	/** Property exists? */
	private boolean propExists(String name) {
		if(used())
			return m_props.getProperty(name) != null;
		else
			return false;
	}

	/** Set a property */
	private void setProp(String name, int i) {
		if(used())
			m_props.setProperty(name, new Integer(i).toString());
	}

	/** Get a property */
	private int getPropInt(String name) {
		if(propExists(name)) {
			String p = m_props.getProperty(name).trim();
			return Integer.parseInt(p);
		} else
			return 0;
	}

	/** Is the window position avaiable? */
	public boolean haveWindowPosition() {
		return used() && propExists(WIN_X) && propExists(WIN_Y) &&
			propExists(WIN_WIDTH) && propExists(WIN_HEIGHT);
	}

	/** Get window position from properties.
	 * @return Null on error else a rectangle for the window position. */
	public Rectangle getWindowPosition() {
		if(!haveWindowPosition())
			return null;
		return new Rectangle(getPropInt(WIN_X), getPropInt(WIN_Y), 
			getPropInt(WIN_WIDTH), getPropInt(WIN_HEIGHT));
	}

	/** Get window state from properties.
	 * @return Window extended state. */
	public int getWindowState() {
		if(used()) {
			int s = getPropInt(WIN_EXTSTATE);
			if( s == JFrame.NORMAL || s == JFrame.MAXIMIZED_BOTH )
				return s;
		}
		return JFrame.NORMAL;
	}

	/** Update user properties associated with JFrame. */
	public void setWindowProperties(JFrame frame) {
		if(!used() || frame == null)
			return;

		m_changed = true;

		// window extended state
		int es = frame.getExtendedState();
		setProp(WIN_EXTSTATE, es);

		// update window position and size only if the window
		// is in the normal state.
		if(es == JFrame.NORMAL) {
			Rectangle r = frame.getBounds();
			setProp(WIN_X, r.x);
			setProp(WIN_Y, r.y);
			setProp(WIN_WIDTH, r.width);
			setProp(WIN_HEIGHT, r.height);
		}
	}

	/** Write properties file. 
	 * @param fname File name to write properties to.
	 * @param props Properties to write.
	 * @param comment Comment inside file. 
	 * @return False if error else true. */
	static public boolean write(String fname, Properties props, 
		String comment) 
	{
		FileOutputStream o = null;
		try {
			o = new FileOutputStream(fname);
			props.store(o, comment);
		} catch(IOException ex) {
			System.err.println("PropertyLoader: " + ex);
			return false;
		} finally {
			try {
				if(o != null)
					o.close();
				return true;
			} catch(IOException ex) {
				System.err.println("PropertyLoader: " + ex);
				return false;
			}
		}
	}
}
