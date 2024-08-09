/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.gps;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Gps;
import us.mn.state.dot.tms.client.EditModeListener;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IPanel;

/**
 * Panel for configuring a GPS
 *
 * @author Douglas Lau
 */
public class GpsPanel extends IPanel {

	/** User session */
	private final Session session;

	/** Notes text area */
	private final JTextArea notes_txt = new JTextArea(8, 32);

	/** Geoloc name */
	private final JTextField geoloc_txt = new JTextField(20);

	/** Proxy watcher */
	private final ProxyWatcher<Gps> watcher;

	/** Proxy view */
	private final ProxyView<Gps> view = new ProxyView<Gps>() {
		public void enumerationComplete() { }
		public void update(Gps g, String a) {
			updateGps(g, a);
		}
		public void clear() {
			clearGps();
		}
	};

	/** Edit mode listener */
	private final EditModeListener edit_lsnr = new EditModeListener() {
		public void editModeChanged() {
			updateEditMode();
		}
	};

	/** Associated GPS object */
	private Gps gps;

	/** Create the panel */
	public GpsPanel(Session s) {
		session = s;
		TypeCache<Gps> cache = s.getSonarState().getGpses();
		watcher = new ProxyWatcher<Gps>(cache, view, false);
	}

	/** Initialize the panel */
	@Override
	public void initialize() {
		super.initialize();
		add("device.notes");
		add(notes_txt, Stretch.FULL);
		add("gps.loc");
		add(geoloc_txt, Stretch.FULL);
		createJobs();
		watcher.initialize();
		clearGps();
		session.addEditModeListener(edit_lsnr);
	}

	/** Create the jobs */
	private void createJobs() {
		notes_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				setNotes(notes_txt.getText().trim());
			}
		});
		geoloc_txt.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent e) {
				setGeoLoc(geoloc_txt.getText().trim());
			}
		});
	}

	/** Set the GPS object */
	public void setGps(Gps g) {
		watcher.setProxy(g);
	}

	/** Set the notes */
	private void setNotes(String n) {
		Gps g = gps;
		if (g != null)
			g.setNotes((n.length() > 0) ? n : null);
	}

	/** Set the geo loc */
	private void setGeoLoc(String l) {
		Gps g = gps;
		if (g != null)
			g.setGeoLoc(GeoLocHelper.lookup(l));
	}

	/** Check if an attribute can be written */
	private boolean canWrite(String a) {
		Gps g = gps;
		return (g != null) && session.canWrite(g, a);
	}

	/** Update the GPS */
	private void updateGps(Gps g, String a) {
		if (a == null) {
			gps = g;
			updateEditMode();
		}
		if (a == null || a.equals("notes")) {
			String n = g.getNotes();
			notes_txt.setText((n != null) ? n : "");
		}
		if (a == null || a.equals("geoLoc")) {
			GeoLoc loc = g.getGeoLoc();
			geoloc_txt.setText((loc != null) ? loc.getName() : "");
		}
	}

	/** Update the edit mode */
	private void updateEditMode() {
		notes_txt.setEnabled(canWrite("notes"));
		geoloc_txt.setEnabled(canWrite("geoLoc"));
	}

	/** Clear the selected GPS */
	private void clearGps() {
		gps = null;
		notes_txt.setEnabled(false);
		notes_txt.setText("");
		geoloc_txt.setEnabled(false);
		geoloc_txt.setText("");
	}

	/** Dispose of the GPS panel */
	@Override
	public void dispose() {
		clearGps();
		session.removeEditModeListener(edit_lsnr);
		watcher.dispose();
		super.dispose();
	}
}
