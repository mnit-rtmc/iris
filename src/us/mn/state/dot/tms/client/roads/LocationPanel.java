/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2012  Minnesota Department of Transportation
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

import java.awt.geom.Point2D;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.geokit.GeodeticDatum;
import us.mn.state.dot.geokit.Position;
import us.mn.state.dot.geokit.SphericalMercatorPosition;
import us.mn.state.dot.geokit.UTMPosition;
import us.mn.state.dot.map.PointSelector;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sched.ChangeJob;
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
import us.mn.state.dot.tms.client.widget.FormPanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.WrapperComboBoxModel;

/**
 * LocationPanel is a Swing panel for viewing and editing object locations.
 *
 * @author Douglas Lau
 */
public class LocationPanel extends FormPanel implements ProxyView<GeoLoc> {

	/** Get the Integer value of a spinner */
	static private int getSpinnerInt(JSpinner s) {
		return (Integer)s.getValue();
	}

	/** Get the Integer value of a spinner */
	static private Integer getSpinnerInteger(JSpinner s) {
		int i = getSpinnerInt(s);
		if(i != 0)
			return i;
		else
			return null;
	}

	/** Get an int to use for a spinner model */
	static private int asInt(Integer i) {
		if(i != null)
			return i;
		else
			return 0;
	}

	/** GeoLoc action */
	abstract private class LAction extends IAction {
		protected LAction(String text_id) {
			super(text_id);
		}
		protected final void do_perform() {
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
	private final SonarState state;

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

	/** Roadway direction combo box */
	private final JComboBox road_dir_cbx = new JComboBox(
		Direction.getDescriptions());

	/** Cross street modifier combobox */
	private final JComboBox cross_mod_cbx = new JComboBox(
		LocModifier.values());

	/** Cross street combobox */
	private final JComboBox cross_cbx = new JComboBox();

	/** Cross street direction combobox */
	private final JComboBox cross_dir_cbx = new JComboBox(
		Direction.getAbbreviations());

	/** UTM Easting */
	private final JSpinner easting = new JSpinner(
		new SpinnerNumberModel(0, 0, 1000000, 1));

	/** UTM Northing */
	private final JSpinner northing = new JSpinner(
		new SpinnerNumberModel(0, 0, 10000000, 1));

	/** Point selector */
	private final PointSelector point_sel = new PointSelector() {
		public void selectPoint(Point2D p) {
			client.setPointSelector(null);
			UTMPosition utm = getPosition(p);
			easting.setValue((int)Math.round(utm.getEasting()));
			northing.setValue((int)Math.round(utm.getNorthing()));
		}
	};

	/** Action to select a point from the map */
	private final IAction select_pt = new IAction("location.select") {
		protected void do_perform() {
			client.setPointSelector(point_sel);
		}
	};

	/** Create a new location panel */
	public LocationPanel(Session s) {
		super(true);
		session = s;
		client = s.getDesktop().client;
		state = s.getSonarState();
		TypeCache<GeoLoc> cache = state.getGeoLocs();
		watcher = new ProxyWatcher<GeoLoc>(s, this, cache, false);
	}

	/** Initialize the location panel */
	public void initialize() {
		roadway_cbx.setAction(new LAction("location.roadway") {
			protected void do_perform(GeoLoc l) {
				l.setRoadway(
					(Road)roadway_cbx.getSelectedItem());
			}
		});
		roadway_cbx.setModel(new WrapperComboBoxModel(
			state.getRoadModel(), true));
		road_dir_cbx.setAction(new LAction("location.direction") {
			protected void do_perform(GeoLoc l) {
				l.setRoadDir(
					(short)road_dir_cbx.getSelectedIndex());
			}
		});
		cross_mod_cbx.setAction(new LAction("location.cross.mod") {
			protected void do_perform(GeoLoc l) {
				short m=(short)cross_mod_cbx.getSelectedIndex();
				l.setCrossMod(m);
			}
		});
		cross_cbx.setAction(new LAction("location.cross") {
			protected void do_perform(GeoLoc l) {
				l.setCrossStreet(
					(Road)cross_cbx.getSelectedItem());
			}
		});
		cross_cbx.setModel(new WrapperComboBoxModel(
			state.getRoadModel(), true));
		cross_dir_cbx.setAction(new LAction("location.cross.dir") {
			protected void do_perform(GeoLoc l) {
				short d=(short)cross_dir_cbx.getSelectedIndex();
				l.setCrossDir(d);
			}
		});
		add(new ILabel("location.roadway"), roadway_cbx);
		setWidth(2);
		addRow(road_dir_cbx);
		add(cross_mod_cbx);
		setWest();
		setWidth(2);
		add(cross_cbx);
		setWidth(1);
		addRow(cross_dir_cbx);
		add(new ILabel("location.easting"), easting);
		finishRow();
		add(new ILabel("location.northing"), northing);
		finishRow();
		setWidth(4);
		updateSelectBag();
		addRow(new JButton(select_pt));
		createJobs();
		watcher.initialize();
	}

	/** Select position grid bag constraints */
	protected void updateSelectBag() {
		bag.gridx = 2;
		bag.gridy = 2;
		bag.gridwidth = 1;
		bag.gridheight = 2;
	}

	/** Create the jobs */
	protected void createJobs() {
		new ChangeJob(this, easting) {
			public void perform() {
				setEasting(getSpinnerInteger(easting));
			}
		};
		new ChangeJob(this, northing) {
			public void perform() {
				setNorthing(getSpinnerInteger(northing));
			}
		};
	}

	/** Get a UTM position */
	protected UTMPosition getPosition(Point2D p) {
		SphericalMercatorPosition smp = new SphericalMercatorPosition(
			p.getX(), p.getY());
		Position pos = smp.getPosition();
		return UTMPosition.convert(GeodeticDatum.WGS_84, pos);
	}

	/** Set the easting */
	private void setEasting(Integer e) {
		GeoLoc l = loc;
		if(l != null)
			l.setEasting(e);
	}

	/** Set the northing */
	private void setNorthing(Integer n) {
		GeoLoc l = loc;
		if(l != null)
			l.setNorthing(n);
	}

	/** Dispose of the location panel */
	public void dispose() {
		watcher.dispose();
		super.dispose();
	}

	/** Update one attribute */
	public final void update(final GeoLoc l, final String a) {
		// Serialize on WORKER thread
		new AbstractJob() {
			public void perform() {
				doUpdate(l, a);
				// NOTE: this is needed to fix a problem where
				//       a combo box displays the wrong entry
				//       after a call to setSelectedItem
				repaint();
			}
		}.addToScheduler();
	}

	/** Update one attribute */
	protected void doUpdate(GeoLoc l, String a) {
		if(a == null)
			loc = l;
		if(a == null || a.equals("roadway")) {
			roadway_cbx.setEnabled(canUpdate(l, "roadway"));
			roadway_cbx.setSelectedItem(l.getRoadway());
		}
		if(a == null || a.equals("roadDir")) {
			road_dir_cbx.setEnabled(canUpdate(l, "roadDir"));
			road_dir_cbx.setSelectedIndex(l.getRoadDir());
		}
		if(a == null || a.equals("crossMod")) {
			cross_mod_cbx.setEnabled(canUpdate(l, "crossMod"));
			cross_mod_cbx.setSelectedIndex(l.getCrossMod());
		}
		if(a == null || a.equals("crossStreet")) {
			cross_cbx.setEnabled(canUpdate(l, "crossStreet"));
			cross_cbx.setSelectedItem(l.getCrossStreet());
		}
		if(a == null || a.equals("crossDir")) {
			cross_dir_cbx.setEnabled(canUpdate(l, "crossDir"));
			cross_dir_cbx.setSelectedIndex(l.getCrossDir());
		}
		if(a == null || a.equals("easting")) {
			boolean p = canUpdate(l, "easting");
			easting.setEnabled(p);
			easting.setValue(asInt(l.getEasting()));
			select_pt.setEnabled(p);
		}
		if(a == null || a.equals("northing")) {
			boolean p = canUpdate(l, "northing");
			northing.setEnabled(p);
			northing.setValue(asInt(l.getNorthing()));
			select_pt.setEnabled(p);
		}
	}

	/** Test if the user can update an attribute */
	protected boolean canUpdate(GeoLoc l, String a) {
		return session.canUpdate(l, a);
	}

	/** Clear all attributes */
	public final void clear() {
		// Serialize on WORKER thread
		new AbstractJob() {
			public void perform() {
				doClear();
			}
		}.addToScheduler();
	}

	/** Clear all attributes */
	protected void doClear() {
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
		easting.setEnabled(false);
		easting.setValue(0);
		northing.setEnabled(false);
		northing.setValue(0);
		select_pt.setEnabled(false);
	}
}
