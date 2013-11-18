/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.geom.Point2D;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import us.mn.state.dot.geokit.Position;
import us.mn.state.dot.geokit.SphericalMercatorPosition;
import us.mn.state.dot.map.PointSelector;
import static us.mn.state.dot.sched.SwingRunner.runSwing;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.LocModifier;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.client.IrisClient;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.WrapperComboBoxModel;

/**
 * LocationPanel is a Swing panel for viewing and editing object locations.
 *
 * @author Douglas Lau
 */
public class LocationPanel extends IPanel implements ProxyView<GeoLoc> {

	/** Get the Double value of a text field */
	static private Double getTextDouble(JTextField tf) {
		String v = tf.getText();
		try {
			return Double.parseDouble(v);
		}
		catch(NumberFormatException e) {
			return null;
		}
	}

	/** Get a double to use for a text field */
	static private String asText(Double d) {
		return d != null ? d.toString() : "";
	}

	/** GeoLoc action */
	abstract private class LAction extends IAction {
		protected LAction(String text_id) {
			super(text_id);
		}
		protected final void doActionPerformed(ActionEvent e) {
			GeoLoc l = loc;
			if(l != null)
				do_perform(l);
		}
		abstract void do_perform(GeoLoc l);
	}

	/** User session */
	protected final Session session;

	/** Iris client */
	private final IrisClient client;

	/** Sonar state object */
	protected final SonarState state;

	/** Proxy watcher */
	private final ProxyWatcher<GeoLoc> watcher;

	/** Location object */
	private GeoLoc loc;

	/** Set the location */
	public void setGeoLoc(GeoLoc l) {
		watcher.setProxy(l);
	}

	/** Roadway combobox */
	private final JComboBox roadway_cbx = new JComboBox();

	/** Roadway action */
	private final LAction roadway_act = new LAction("location.roadway") {
		protected void do_perform(GeoLoc l) {
			l.setRoadway((Road)roadway_cbx.getSelectedItem());
		}
	};

	/** Roadway direction combo box */
	private final JComboBox road_dir_cbx = new JComboBox(
		Direction.getDescriptions());

	/** Roadway direction action */
	private final LAction road_dir_act = new LAction("location.direction") {
		protected void do_perform(GeoLoc l) {
			l.setRoadDir((short)road_dir_cbx.getSelectedIndex());
		}
	};

	/** Cross street modifier combobox */
	private final JComboBox cross_mod_cbx = new JComboBox(
		LocModifier.values());

	/** Cross street modifier action */
	private final LAction cross_mod_act = new LAction("location.cross.mod"){
		protected void do_perform(GeoLoc l) {
			short m = (short)cross_mod_cbx.getSelectedIndex();
			l.setCrossMod(m);
		}
	};

	/** Cross street combobox */
	private final JComboBox cross_cbx = new JComboBox();

	/** Cross street action */
	private final LAction cross_act = new LAction("location.cross") {
		protected void do_perform(GeoLoc l) {
			l.setCrossStreet((Road)cross_cbx.getSelectedItem());
		}
	};

	/** Cross street direction combobox */
	private final JComboBox cross_dir_cbx = new JComboBox(
		Direction.getAbbreviations());

	/** Cross street direction action */
	private final LAction cross_dir_act = new LAction("location.cross.dir"){
		protected void do_perform(GeoLoc l) {
			short d = (short)cross_dir_cbx.getSelectedIndex();
			l.setCrossDir(d);
		}
	};

	/** Latitude field */
	private final JTextField lat_txt = new JTextField();

	/** Longitude field */
	private final JTextField lon_txt = new JTextField();

	/** Point selector */
	private final PointSelector point_sel = new PointSelector() {
		public boolean selectPoint(Point2D p) {
			Position pos = getPosition(p);
			setLat(pos.getLatitude());
			setLon(pos.getLongitude());
			return true;
		}
		public void finish() { }
	};

	/** Action to select a point from the map */
	private final IAction select_pt = new IAction("location.select") {
		protected void doActionPerformed(ActionEvent e) {
			client.setPointSelector(point_sel);
		}
	};

	/** Create a new location panel */
	public LocationPanel(Session s) {
		session = s;
		client = s.getDesktop().client;
		state = s.getSonarState();
		TypeCache<GeoLoc> cache = state.getGeoLocs();
		watcher = new ProxyWatcher<GeoLoc>(s, this, cache, false);
	}

	/** Initialize the location panel */
	public void initialize() {
		roadway_cbx.setModel(new WrapperComboBoxModel(
			state.getRoadModel(), true));
		cross_cbx.setModel(new WrapperComboBoxModel(
			state.getRoadModel(), true));
		add("location.roadway");
		add(roadway_cbx);
		add(road_dir_cbx, Stretch.LAST);
		add(cross_mod_cbx, Stretch.NONE);
		add(cross_cbx);
		add(cross_dir_cbx, Stretch.LAST);
		add("location.lat");
		add(lat_txt, Stretch.WIDE);
		add(new JButton(select_pt), Stretch.TALL);
		add("location.lon");
		add(lon_txt, Stretch.WIDE);
		add(new JLabel(), Stretch.LEFT);
		createJobs();
		watcher.initialize();
	}

	/** Create the jobs */
	protected void createJobs() {
		lat_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				setLat(getTextDouble(lat_txt));
			}
		});
		lon_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				setLon(getTextDouble(lon_txt));
			}
		});
	}

	/** Get a position */
	private Position getPosition(Point2D p) {
		SphericalMercatorPosition smp = new SphericalMercatorPosition(
			p.getX(), p.getY());
		return smp.getPosition();
	}

	/** Set the latitude */
	private void setLat(Double lt) {
		GeoLoc l = loc;
		if(l != null)
			l.setLat(lt);
	}

	/** Set the longitude */
	private void setLon(Double ln) {
		GeoLoc l = loc;
		if(l != null)
			l.setLon(ln);
	}

	/** Dispose of the location panel */
	@Override
	public void dispose() {
		watcher.dispose();
		super.dispose();
	}

	/** Update one attribute */
	@Override
	public final void update(final GeoLoc l, final String a) {
		runSwing(new Runnable() {
			public void run() {
				doUpdate(l, a);
				// NOTE: this is needed to fix a problem where
				//       a combo box displays the wrong entry
				//       after a call to setSelectedItem
				repaint();
			}
		});
	}

	/** Update one attribute */
	private void doUpdate(GeoLoc l, String a) {
		if(a == null)
			loc = l;
		if(a == null || a.equals("roadway"))
			updateRoadway(l);
		if(a == null || a.equals("roadDir"))
			updateRoadDir(l);
		if(a == null || a.equals("crossMod"))
			updateCrossMod(l);
		if(a == null || a.equals("crossStreet"))
			updateCrossStreet(l);
		if(a == null || a.equals("crossDir"))
			updateCrossDir(l);
		if(a == null || a.equals("lat")) {
			boolean p = canUpdate(l, "lat");
			lat_txt.setEnabled(p);
			lat_txt.setText(asText(l.getLat()));
			select_pt.setEnabled(p);
		}
		if(a == null || a.equals("lon")) {
			boolean p = canUpdate(l, "lon");
			lon_txt.setEnabled(p);
			lon_txt.setText(asText(l.getLon()));
			select_pt.setEnabled(p);
		}
	}

	/** Update roadway attribute */
	private void updateRoadway(GeoLoc l) {
		boolean e = canUpdate(l, "roadway");
		roadway_cbx.setAction(null);
		roadway_cbx.setEnabled(e);
		roadway_cbx.setSelectedItem(l.getRoadway());
		if(e)
			roadway_cbx.setAction(roadway_act);
	}

	/** Update roadway direction attribute */
	private void updateRoadDir(GeoLoc l) {
		boolean e = canUpdate(l, "roadDir");
		road_dir_cbx.setAction(null);
		road_dir_cbx.setEnabled(e);
		road_dir_cbx.setSelectedIndex(l.getRoadDir());
		if(e)
			road_dir_cbx.setAction(road_dir_act);
	}

	/** Update cross street modifier attribute */
	private void updateCrossMod(GeoLoc l) {
		boolean e = canUpdate(l, "crossMod");
		cross_mod_cbx.setAction(null);
		cross_mod_cbx.setEnabled(e);
		cross_mod_cbx.setSelectedIndex(l.getCrossMod());
		if(e)
			cross_mod_cbx.setAction(cross_mod_act);
	}

	/** Update cross street attribute */
	private void updateCrossStreet(GeoLoc l) {
		boolean e = canUpdate(l, "crossStreet");
		cross_cbx.setAction(null);
		cross_cbx.setEnabled(e);
		cross_cbx.setSelectedItem(l.getCrossStreet());
		if(e)
			cross_cbx.setAction(cross_act);
	}

	/** Update cross street direction attribute */
	private void updateCrossDir(GeoLoc l) {
		boolean e = canUpdate(l, "crossDir");
		cross_dir_cbx.setAction(null);
		cross_dir_cbx.setEnabled(e);
		cross_dir_cbx.setSelectedIndex(l.getCrossDir());
		if(e)
			cross_dir_cbx.setAction(cross_dir_act);
	}

	/** Test if the user can update an attribute */
	private boolean canUpdate(GeoLoc l, String a) {
		return session.canUpdate(l, a);
	}

	/** Clear all attributes */
	@Override
	public final void clear() {
		runSwing(new Runnable() {
			public void run() {
				doClear();
			}
		});
	}

	/** Clear all attributes */
	protected void doClear() {
		loc = null;
		roadway_cbx.setAction(null);
		roadway_cbx.setEnabled(false);
		roadway_cbx.setSelectedIndex(0);
		road_dir_cbx.setAction(null);
		road_dir_cbx.setEnabled(false);
		road_dir_cbx.setSelectedIndex(0);
		cross_mod_cbx.setAction(null);
		cross_mod_cbx.setEnabled(false);
		cross_mod_cbx.setSelectedIndex(0);
		cross_cbx.setAction(null);
		cross_cbx.setEnabled(false);
		cross_cbx.setSelectedIndex(0);
		cross_dir_cbx.setAction(null);
		cross_dir_cbx.setEnabled(false);
		cross_dir_cbx.setSelectedIndex(0);
		lat_txt.setEnabled(false);
		lat_txt.setText("");
		lon_txt.setEnabled(false);
		lon_txt.setText("");
		select_pt.setEnabled(false);
	}
}
