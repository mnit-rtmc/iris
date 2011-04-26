/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2011  Minnesota Department of Transportation
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
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import us.mn.state.dot.geokit.GeodeticDatum;
import us.mn.state.dot.geokit.Position;
import us.mn.state.dot.geokit.SphericalMercatorPosition;
import us.mn.state.dot.geokit.UTMPosition;
import us.mn.state.dot.map.PointSelector;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sched.ActionJob;
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
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;

/**
 * LocationPanel is a Swing panel for viewing and editing object locations.
 *
 * @author Douglas Lau
 */
public class LocationPanel extends FormPanel implements ProxyView<GeoLoc> {

	/** Get the Integer value of a spinner */
	static protected int getSpinnerInt(JSpinner s) {
		return (Integer)s.getValue();
	}

	/** Get the Integer value of a spinner */
	static protected Integer getSpinnerInteger(JSpinner s) {
		int i = getSpinnerInt(s);
		if(i != 0)
			return i;
		else
			return null;
	}

	/** Get an int to use for a spinner model */
	static protected int asInt(Integer i) {
		if(i != null)
			return i;
		else
			return 0;
	}

	/** User session */
	protected final Session session;

	/** Iris client */
	protected final IrisClient client;

	/** Sonar state object */
	protected final SonarState state;

	/** Proxy watcher */
	protected final ProxyWatcher<GeoLoc> watcher;

	/** Location object */
	protected GeoLoc loc;

	/** Set the location */
	public void setGeoLoc(GeoLoc l) {
		watcher.setProxy(l);
	}

	/** Roadway combobox */
	protected final JComboBox roadway = new JComboBox();

	/** Roadway direction combo box */
	protected final JComboBox roadDir = new JComboBox(
		Direction.getDescriptions());

	/** Cross street modifier combobox */
	protected final JComboBox crossMod = new JComboBox(
		LocModifier.values());

	/** Cross street combobox */
	protected final JComboBox cross = new JComboBox();

	/** Cross street direction combobox */
	protected final JComboBox crossDir = new JComboBox(
		Direction.getAbbreviations());

	/** UTM Easting */
	protected final JSpinner easting = new JSpinner(
		new SpinnerNumberModel(0, 0, 1000000, 1));

	/** UTM Northing */
	protected final JSpinner northing = new JSpinner(
		new SpinnerNumberModel(0, 0, 10000000, 1));

	/** Button to select a point from the map */
	protected final JButton select_btn = new JButton("Select Point");

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
		roadway.setModel(new WrapperComboBoxModel(
			state.getRoadModel(), true));
		cross.setModel(new WrapperComboBoxModel(
			state.getRoadModel(), true));
		add("Roadway", roadway);
		setWidth(2);
		addRow(roadDir);
		add(crossMod);
		setWest();
		setWidth(2);
		add(cross);
		setWidth(1);
		addRow(crossDir);
		add("Easting", easting);
		finishRow();
		add("Northing", northing);
		finishRow();
		setWidth(4);
		updateSelectBag();
		addRow(select_btn);
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
		new ActionJob(this, roadway) {
			public void perform() {
				setRoadway((Road)roadway.getSelectedItem());
			}
		};
		new ActionJob(this, cross) {
			public void perform() {
				setCrossStreet((Road)cross.getSelectedItem());
			}
		};
		new ActionJob(this, roadDir) {
			public void perform() {
				setRoadDir((short)roadDir.getSelectedIndex());
			}
		};
		new ActionJob(this, crossMod) {
			public void perform() {
				setCrossMod((short)crossMod.getSelectedIndex());
			}
		};
		new ActionJob(this, crossDir) {
			public void perform() {
				setCrossDir((short)crossDir.getSelectedIndex());
			}
		};
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
		final PointSelector ps = new PointSelector() {
			public void selectPoint(Point2D p) {
				UTMPosition utm = getPosition(p);
				easting.setValue(
					(int)Math.round(utm.getEasting()));
				northing.setValue(
					(int)Math.round(utm.getNorthing()));
				client.setPointSelector(null);
			}
		};
		new ActionJob(this, select_btn) {
			public void perform() {
				client.setPointSelector(ps);
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

	/** Set the roadway */
	protected void setRoadway(Road r) {
		GeoLoc l = loc;
		if(l != null)
			l.setRoadway(r);
	}

	/** Set the cross street */
	protected void setCrossStreet(Road x) {
		GeoLoc l = loc;
		if(l != null)
			l.setCrossStreet(x);
	}

	/** Set the road direction */
	protected void setRoadDir(short d) {
		GeoLoc l = loc;
		if(l != null)
			l.setRoadDir(d);
	}

	/** Set the cross street modifier */
	protected void setCrossMod(short m) {
		GeoLoc l = loc;
		if(l != null)
			l.setCrossMod(m);
	}

	/** Set the cross direction */
	protected void setCrossDir(short d) {
		GeoLoc l = loc;
		if(l != null)
			l.setCrossDir(d);
	}

	/** Set the easting */
	protected void setEasting(Integer e) {
		GeoLoc l = loc;
		if(l != null)
			l.setEasting(e);
	}

	/** Set the northing */
	protected void setNorthing(Integer n) {
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
			roadway.setEnabled(canUpdate(l, "roadway"));
			roadway.setSelectedItem(l.getRoadway());
		}
		if(a == null || a.equals("roadDir")) {
			roadDir.setEnabled(canUpdate(l, "roadDir"));
			roadDir.setSelectedIndex(l.getRoadDir());
		}
		if(a == null || a.equals("crossMod")) {
			crossMod.setEnabled(canUpdate(l, "crossMod"));
			crossMod.setSelectedIndex(l.getCrossMod());
		}
		if(a == null || a.equals("crossStreet")) {
			cross.setEnabled(canUpdate(l, "crossStreet"));
			cross.setSelectedItem(l.getCrossStreet());
		}
		if(a == null || a.equals("crossDir")) {
			crossDir.setEnabled(canUpdate(l, "crossDir"));
			crossDir.setSelectedIndex(l.getCrossDir());
		}
		if(a == null || a.equals("easting")) {
			boolean p = canUpdate(l, "easting");
			easting.setEnabled(p);
			easting.setValue(asInt(l.getEasting()));
			select_btn.setEnabled(p);
		}
		if(a == null || a.equals("northing")) {
			boolean p = canUpdate(l, "northing");
			northing.setEnabled(p);
			northing.setValue(asInt(l.getNorthing()));
			select_btn.setEnabled(p);
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
		roadway.setEnabled(false);
		roadway.setSelectedIndex(0);
		roadDir.setEnabled(false);
		roadDir.setSelectedIndex(0);
		crossMod.setEnabled(false);
		crossMod.setSelectedIndex(0);
		cross.setEnabled(false);
		cross.setSelectedIndex(0);
		crossDir.setEnabled(false);
		crossDir.setSelectedIndex(0);
		easting.setEnabled(false);
		easting.setValue(0);
		northing.setEnabled(false);
		northing.setValue(0);
		select_btn.setEnabled(false);
	}
}
