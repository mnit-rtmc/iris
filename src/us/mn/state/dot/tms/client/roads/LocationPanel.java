/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2016  Minnesota Department of Transportation
 * Copyright (C) 2014  AHMCT, University of California
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
import javax.swing.JList;
import javax.swing.JTextField;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.LocModifier;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.client.IrisClient;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.map.PointSelector;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.IListCellRenderer;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.geo.Position;
import us.mn.state.dot.tms.geo.SphericalMercatorPosition;

/**
 * LocationPanel is a Swing panel for viewing and editing object locations.
 *
 * @author Douglas Lau
 * @author Travis Swanston
 */
public class LocationPanel extends IPanel implements ProxyView<GeoLoc> {

	/** Get the Double value of a text field */
	static private Double getTextDouble(JTextField tf) {
		String v = tf.getText();
		try {
			return Double.parseDouble(v);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/** Get a double to use for a text field */
	static private String asText(Double d) {
		return (d != null) ? d.toString() : "";
	}

	/** GeoLoc action */
	abstract private class LAction extends IAction {
		protected LAction(String text_id) {
			super(text_id);
		}
		protected final void doActionPerformed(ActionEvent e) {
			GeoLoc l = loc;
			if (l != null)
				do_perform(l);
		}
		abstract void do_perform(GeoLoc l);
		protected final void doUpdateSelected() {
			GeoLoc l = loc;
			if (l != null)
				do_update(l);
		}
		abstract void do_update(GeoLoc l);
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
	private final JComboBox<Road> roadway_cbx = new JComboBox<Road>();

	/** Roadway model */
	private final IComboBoxModel<Road> roadway_mdl;

	/** Roadway action */
	private final LAction roadway_act = new LAction("location.roadway") {
		protected void do_perform(GeoLoc l) {
			l.setRoadway(roadway_mdl.getSelectedProxy());
		}
		protected void do_update(GeoLoc l) {
			roadway_mdl.setSelectedItem(l.getRoadway());
		}
	};

	/** Roadway direction combo box */
	private final JComboBox<Direction> road_dir_cbx = new JComboBox
		<Direction>(Direction.values());

	/** Roadway direction action */
	private final LAction road_dir_act = new LAction("location.direction") {
		protected void do_perform(GeoLoc l) {
			l.setRoadDir((short) road_dir_cbx.getSelectedIndex());
		}
		protected void do_update(GeoLoc l) {
			road_dir_cbx.setSelectedIndex(l.getRoadDir());
		}
	};

	/** Cross street modifier combobox */
	private final JComboBox<LocModifier> cross_mod_cbx =
		new JComboBox<LocModifier>(LocModifier.values());

	/** Cross street modifier action */
	private final LAction cross_mod_act = new LAction("location.cross.mod"){
		protected void do_perform(GeoLoc l) {
			short m = (short) cross_mod_cbx.getSelectedIndex();
			l.setCrossMod(m);
		}
		protected void do_update(GeoLoc l) {
			cross_mod_cbx.setSelectedIndex(l.getCrossMod());
		}
	};

	/** Cross street combobox */
	private final JComboBox<Road> cross_cbx = new JComboBox<Road>();

	/** Cross street model */
	private final IComboBoxModel<Road> cross_mdl;

	/** Cross street action */
	private final LAction cross_act = new LAction("location.cross") {
		protected void do_perform(GeoLoc l) {
			l.setCrossStreet(cross_mdl.getSelectedProxy());
		}
		protected void do_update(GeoLoc l) {
			cross_mdl.setSelectedItem(l.getCrossStreet());
		}
	};

	/** Cross street direction combobox */
	private final JComboBox<Direction> cross_dir_cbx = new JComboBox
		<Direction>(Direction.values());

	/** Cross street direction action */
	private final LAction cross_dir_act = new LAction("location.cross.dir"){
		protected void do_perform(GeoLoc l) {
			short d = (short) cross_dir_cbx.getSelectedIndex();
			l.setCrossDir(d);
		}
		protected void do_update(GeoLoc l) {
			cross_dir_cbx.setSelectedIndex(l.getCrossDir());
		}
	};

	/** Latitude field */
	private final JTextField lat_txt = new JTextField();

	/** Longitude field */
	private final JTextField lon_txt = new JTextField();

	/** Landmark field */
	private final JTextField landmark_txt = new JTextField();

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
		watcher = new ProxyWatcher<GeoLoc>(cache, this, false);
		roadway_mdl = new IComboBoxModel<Road>(state.getRoadModel());
		cross_mdl = new IComboBoxModel<Road>(state.getRoadModel());
	}

	/** Initialize the location panel */
	@Override
	public void initialize() {
		super.initialize();
		roadway_cbx.setModel(roadway_mdl);
		roadway_cbx.setAction(roadway_act);
		road_dir_cbx.setAction(road_dir_act);
		cross_mod_cbx.setAction(cross_mod_act);
		cross_cbx.setModel(cross_mdl);
		cross_cbx.setAction(cross_act);
		cross_dir_cbx.setAction(cross_dir_act);
		cross_dir_cbx.setRenderer(new IListCellRenderer<Direction>() {
			@Override
			protected String valueToString(Direction value) {
				return value.abbrev;
			}
		});
		add("location.roadway");
		add(roadway_cbx);
		add(road_dir_cbx, Stretch.LAST);
		add(cross_mod_cbx, Stretch.NONE);
		add(cross_cbx);
		add(cross_dir_cbx, Stretch.LAST);
		add("location.latitude");
		add(lat_txt, Stretch.WIDE);
		add(new JButton(select_pt), Stretch.TALL);
		add("location.longitude");
		add(lon_txt, Stretch.WIDE);
		add(new JLabel(), Stretch.LEFT);
		add("location.landmark");
		add(landmark_txt, Stretch.WIDE);
		add(new JLabel(), Stretch.LEFT);
		createJobs();
		watcher.initialize();
	}

	/** Dispose of the location panel */
	@Override
	public void dispose() {
		watcher.dispose();
		super.dispose();
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
		landmark_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String lm = landmark_txt.getText();
				setLandmark(lm.trim());
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
		if (l != null)
			l.setLat(lt);
	}

	/** Set the longitude */
	private void setLon(Double ln) {
		GeoLoc l = loc;
		if (l != null)
			l.setLon(ln);
	}

	/** Set the landmark */
	private void setLandmark(String lm) {
		GeoLoc l = loc;
		if (l != null)
			l.setLandmark(("".equals(lm)) ? null : lm);
	}

	/** Update the edit mode */
	public void updateEditMode() {
		GeoLoc l = loc;
		roadway_act.setEnabled(canUpdate(l, "roadway"));
		road_dir_act.setEnabled(canUpdate(l, "roadDir"));
		cross_mod_act.setEnabled(canUpdate(l, "crossMod"));
		cross_act.setEnabled(canUpdate(l, "crossStreet"));
		cross_dir_act.setEnabled(canUpdate(l, "crossDir"));
		lat_txt.setEnabled(canUpdate(l, "lat"));
		lon_txt.setEnabled(canUpdate(l, "lon"));
		select_pt.setEnabled(canUpdate(l, "lat")
		                  && canUpdate(l, "lon"));
		landmark_txt.setEnabled(canUpdate(l, "landmark"));
	}

	/** Update one attribute (from ProxyView). */
	@Override
	public void update(GeoLoc l, String a) {
		if (a == null) {
			loc = l;
			updateEditMode();
		}
		if (a == null || a.equals("roadway"))
			roadway_act.updateSelected();
		if (a == null || a.equals("roadDir"))
			road_dir_act.updateSelected();
		if (a == null || a.equals("crossMod"))
			cross_mod_act.updateSelected();
		if (a == null || a.equals("crossStreet"))
			cross_act.updateSelected();
		if (a == null || a.equals("crossDir"))
			cross_dir_act.updateSelected();
		if (a == null || a.equals("lat"))
			lat_txt.setText(asText(l.getLat()));
		if (a == null || a.equals("lon"))
			lon_txt.setText(asText(l.getLon()));
		if (a == null || a.equals("landmark")) {
			String lm = l.getLandmark();
			landmark_txt.setText((lm != null) ? lm : "");
		}
		// NOTE: this was needed to fix a problem where a combo box
		//       displays the wrong entry after call to setSelectedItem
		repaint();
	}

	/** Test if the user can update an attribute */
	private boolean canUpdate(GeoLoc l, String a) {
		return session.canUpdate(l, a);
	}

	/** Clear all attributes (from ProxyView). */
	@Override
	public void clear() {
		loc = null;
		roadway_cbx.setEnabled(false);
		roadway_cbx.setSelectedIndex(0);
		road_dir_cbx.setEnabled(false);
		road_dir_cbx.setSelectedIndex(0);
		cross_mod_cbx.setEnabled(false);
		cross_mod_cbx.setSelectedIndex(0);
		cross_cbx.setEnabled(false);
		cross_cbx.setSelectedIndex(0);
		cross_dir_cbx.setEnabled(false);
		cross_dir_cbx.setSelectedIndex(0);
		lat_txt.setEnabled(false);
		lat_txt.setText("");
		lon_txt.setEnabled(false);
		lon_txt.setText("");
		landmark_txt.setEnabled(false);
		landmark_txt.setText("");
		select_pt.setEnabled(false);
	}
}
