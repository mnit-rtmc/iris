/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toast;

import java.awt.geom.Point2D;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ChangeJob;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Direction;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.map.PointSelector;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;

/**
 * LocationPanel is a Swing panel for viewing and editing object locations.
 *
 * @author Douglas Lau
 */
public class LocationPanel extends FormPanel implements ProxyListener<GeoLoc> {

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

	/** GeoLoc type cache */
	protected final TypeCache<GeoLoc> cache;

	/** Location object */
	protected final GeoLoc loc;

	/** Sonar state object */
	protected final SonarState state;

	/** Freeway combobox */
	protected final JComboBox freeway = new JComboBox();

	/** Freeway direction combo box */
	protected final JComboBox freeDir = new JComboBox(Direction.DIR_LONG);

	/** Cross street modifier combobox */
	protected final JComboBox crossMod = new JComboBox(Direction.MODIFIER);

	/** Cross street combobox */
	protected final JComboBox cross = new JComboBox();

	/** Cross street direction combobox */
	protected final JComboBox crossDir = new JComboBox(Direction.DIRECTION);

	/** UTM Easting */
	protected final JSpinner easting = new JSpinner(
		new SpinnerNumberModel(0, 0, 1000000, 1));

	/** UTM Easting offset */
	protected final JSpinner eastOff = new JSpinner(
		new SpinnerNumberModel(0, -9999, 1000000, 1));

	/** UTM Northing */
	protected final JSpinner northing = new JSpinner(
		new SpinnerNumberModel(0, 0, 10000000, 1));

	/** UTM Northing offset */
	protected final JSpinner northOff = new JSpinner(
		new SpinnerNumberModel(0, -9999, 10000000, 1));

	/** Button to select a point from the map */
	protected final JButton select = new JButton("Select Point");

	/** Create a new location panel */
	public LocationPanel(Session s, GeoLoc l) {
		super(s.canUpdate(l));
		loc = l;
		state = s.getSonarState();
		cache = state.getGeoLocs();
	}

	/** Create a new location panel */
	public LocationPanel(Session s, String l) {
		this(s, GeoLocHelper.lookup(l));
	}

	/** Initialize the location panel */
	public void initialize() {
		cache.addProxyListener(this);
		freeway.setModel(new WrapperComboBoxModel(
			state.getRoadModel(), true));
		cross.setModel(new WrapperComboBoxModel(
			state.getRoadModel(), true));
		add("Freeway", freeway);
		setWidth(2);
		addRow(freeDir);
		add(crossMod);
		setWest();
		setWidth(2);
		add(cross);
		setWidth(1);
		addRow(crossDir);
		add("Easting", easting);
		setEast();
		addRow("East Offset", eastOff);
		add("Northing", northing);
		setEast();
		addRow("North Offset", northOff);
		if(enable)
			createJobs();
		updateAttribute(null);
	}

	/** Create the jobs */
	protected void createJobs() {
		new ActionJob(this, freeway) {
			public void perform() {
				loc.setFreeway((Road)freeway.getSelectedItem());
			}
		};
		new ActionJob(this, cross) {
			public void perform() {
				loc.setCrossStreet(
					(Road)cross.getSelectedItem());
			}
		};
		new ActionJob(this, freeDir) {
			public void perform() {
				loc.setFreeDir(
					(short)freeDir.getSelectedIndex());
			}
		};
		new ActionJob(this, crossMod) {
			public void perform() {
				loc.setCrossMod(
					(short)crossMod.getSelectedIndex());
			}
		};
		new ActionJob(this, crossDir) {
			public void perform() {
				loc.setCrossDir(
					(short)crossDir.getSelectedIndex());
			}
		};
		new ChangeJob(this, easting) {
			public void perform() {
				loc.setEasting(getSpinnerInteger(easting));
			}
		};
		new ChangeJob(this, eastOff) {
			public void perform() {
				loc.setEastOffset(getSpinnerInteger(eastOff));
			}
		};
		new ChangeJob(this, northing) {
			public void perform() {
				loc.setNorthing(getSpinnerInteger(northing));
			}
		};
		new ChangeJob(this, northOff) {
			public void perform() {
				loc.setNorthOffset(getSpinnerInteger(northOff));
			}
		};
	}

	/** Dispose of the location panel */
	public void dispose() {
		cache.removeProxyListener(this);
	}

	/** A new proxy has been added */
	public void proxyAdded(GeoLoc p) {
		// we're not interested
	}

	/** Enumeration of the proxy type has completed */
	public void enumerationComplete() {
		// we're not interested
	}

	/** A proxy has been removed */
	public void proxyRemoved(GeoLoc p) {
		if(p == loc) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					dispose();
				}
			});
		}
	}

	/** A proxy has been changed */
	public void proxyChanged(GeoLoc p, final String a) {
		if(p == loc) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateAttribute(a);
				}
			});
		}
	}

	/** Update one attribute on the form */
	protected void updateAttribute(String a) {
		if(a == null || a.equals("freeway"))
			freeway.setSelectedItem(loc.getFreeway());
		if(a == null || a.equals("freeDir"))
			freeDir.setSelectedIndex(loc.getFreeDir());
		if(a == null || a.equals("crossMod"))
			crossMod.setSelectedIndex(loc.getCrossMod());
		if(a == null || a.equals("crossStreet"))
			cross.setSelectedItem(loc.getCrossStreet());
		if(a == null || a.equals("crossDir"))
			crossDir.setSelectedIndex(loc.getCrossDir());
		if(a == null || a.equals("easting"))
			easting.setValue(asInt(loc.getEasting()));
		if(a == null || a.equals("eastOffset"))
			eastOff.setValue(asInt(loc.getEastOffset()));
		if(a == null || a.equals("northing"))
			northing.setValue(asInt(loc.getNorthing()));
		if(a == null || a.equals("northOffset"))
			northOff.setValue(asInt(loc.getNorthOffset()));
	}

	/** Add a "Select Point" button */
	public void addSelectPointButton(final MapBean map) {
		if(enable) {
			setWidth(4);
			addRow(select);
			new ActionJob(this, select) {
				public void perform() throws Exception {
					selectPressed(map);
				}
			};
		}
	}

	/** Select point button is pressed */
	protected void selectPressed(MapBean map) {
		map.addPointSelector(new PointSelector() {
			public void selectPoint(Point2D p) {
				int x = (int)p.getX();
				eastOff.setValue(x - getSpinnerInt(easting));
				int y = (int)p.getY();
				northOff.setValue(y - getSpinnerInt(northing));
			}
		});
	}
}
