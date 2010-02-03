/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import java.awt.geom.Point2D;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.map.PointSelector;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.sched.ChangeJob;
import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.LaneType;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.R_NodeType;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.roads.R_NodeManager;

/**
 * GUI for creating new incidents.
 *
 * @author Douglas Lau
 */
public class IncidentCreator extends JPanel {

	/** Button to create a "crash" incident */
	protected final JToggleButton crash_btn;

	/** Button to create a "stall" incident */
	protected final JToggleButton stall_btn;

	/** Button to create a "blockage" incident */
	protected final JToggleButton block_btn;

	/** Button to create a "road work" incident */
	protected final JToggleButton work_btn;

	/** Lane type combo box */
	protected final JComboBox ltype_cbox;

	/** Incident selection model */
	protected final ProxySelectionModel<Incident> selectionModel;

	/** R_Node manager */
	protected final R_NodeManager r_node_manager;

	/** Map bean */
	protected MapBean map;

	/** Create a new incident creator */
	public IncidentCreator(StyledTheme theme,
		ProxySelectionModel<Incident> sel_model, R_NodeManager r_man)
	{
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		selectionModel = sel_model;
		r_node_manager = r_man;
		setBorder(BorderFactory.createTitledBorder(
			"Create new incident"));
		crash_btn = createButton(IncidentManager.STYLE_CRASH,
			EventType.INCIDENT_CRASH, theme);
		stall_btn = createButton(IncidentManager.STYLE_STALL,
			EventType.INCIDENT_STALL, theme);
		block_btn = createButton(IncidentManager.STYLE_DEBRIS,
			EventType.INCIDENT_DEBRIS, theme);
		work_btn = createButton(IncidentManager.STYLE_ROADWORK,
			EventType.INCIDENT_ROADWORK, theme);
		ltype_cbox = createLaneTypeCombo();
		Box box = Box.createHorizontalBox();
		box.add(crash_btn);
		box.add(Box.createHorizontalStrut(4));
		box.add(stall_btn);
		box.add(Box.createHorizontalStrut(4));
		box.add(block_btn);
		box.add(Box.createHorizontalStrut(4));
		box.add(work_btn);
		add(box);
		add(Box.createVerticalStrut(4));
		box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(new JLabel("Lane Type"));
		box.add(Box.createHorizontalStrut(4));
		box.add(ltype_cbox);
		box.add(Box.createHorizontalGlue());
		add(box);
		setEnabled(false);
	}

	/** Create the lane type combo box */
	protected JComboBox createLaneTypeCombo() {
		return new JComboBox(new LaneType[] {
			LaneType.MAINLINE,
			LaneType.EXIT,
			LaneType.MERGE,
			LaneType.CD_LANE
		});
	}

	/** Create a button for creating an incident */
	protected JToggleButton createButton(String sty, final EventType et,
		StyledTheme theme)
	{
		Symbol sym = theme.getSymbol(sty);
		final JToggleButton btn = new JToggleButton(sty,
			sym.getLegend());
		new ChangeJob(btn) {
			public void perform() {
				buttonChanged(btn, et);
			}
		};
		btn.setHorizontalTextPosition(SwingConstants.LEADING);
		return btn;
	}

	/** Handler for button changed events */
	protected void buttonChanged(JToggleButton btn, EventType et) {
		if(btn.isSelected()) {
			selectionModel.clearSelection();
			// NOTE: cannot use ButtonGroup for this because it
			// will not let the user deselect a button by clicking
			// on it once it has been selected.  Arrgh!
			if(btn != crash_btn)
				crash_btn.setSelected(false);
			if(btn != stall_btn)
				stall_btn.setSelected(false);
			if(btn != block_btn)
				block_btn.setSelected(false);
			if(btn != work_btn)
				work_btn.setSelected(false);
			createIncident(btn, et);
		} else {
			if(getSelected() == null) {
				MapBean m = map;
				if(m != null)
					m.addPointSelector(null);
			}
		}
	}

	/** Create a incident */
	protected void createIncident(final JToggleButton btn,
		final EventType et)
	{
		assert et == EventType.INCIDENT_CRASH ||
		       et == EventType.INCIDENT_STALL ||
		       et == EventType.INCIDENT_DEBRIS ||
		       et == EventType.INCIDENT_ROADWORK;
		MapBean m = map;
		if(m == null)
			return;
		m.addPointSelector(new PointSelector() {
			public void selectPoint(Point2D p) {
				int x = (int)p.getX();
				int y = (int)p.getY();
				createIncident(et, x, y);
				btn.setSelected(false);
				setEnabled(true);
			}
		});
	}

	/** Create an incident */
	protected void createIncident(final EventType et, int easting,
		int northing)
	{
		LaneType lt = (LaneType)ltype_cbox.getSelectedItem();
		if(lt == null)
			return;
		GeoLoc loc = r_node_manager.createGeoLoc(easting, northing,
			lt == LaneType.CD_LANE);
		if(loc == null)
			return;
		loc = snapGeoLoc(lt, loc);
		Road road = loc.getFreeway();
		short dir = loc.getFreeDir();
		int east = GeoLocHelper.getTrueEasting(loc);
		int north = GeoLocHelper.getTrueNorthing(loc);
		int n_lanes = getLaneCount(lt, loc);
		if(n_lanes > 0) {
			ClientIncident ci = new ClientIncident(et.id,
				(short)lt.ordinal(), road, dir, east, north,
				createImpact(n_lanes));
			selectionModel.setSelected(ci);
			ltype_cbox.setSelectedItem(LaneType.MAINLINE);
		}
	}

	/** Snap a location to the proper lane type */
	protected GeoLoc snapGeoLoc(LaneType lt, GeoLoc loc) {
		CorridorBase cb = r_node_manager.lookupCorridor(loc);
		if(cb == null)
			return loc;
		int east = GeoLocHelper.getTrueEasting(loc);
		int north = GeoLocHelper.getTrueNorthing(loc);
		switch(lt) {
		case EXIT:
			R_Node n = cb.findNearest(east, north, R_NodeType.EXIT);
			if(n != null)
				return n.getGeoLoc();
			else
				return loc;
		case MERGE:
			R_Node mn = cb.findNearest(east, north,
				R_NodeType.ENTRANCE);
			if(mn != null)
				return mn.getGeoLoc();
			else
				return loc;
		default:
			return loc;
		}
	}

	/** Get the lane count at the incident location */
	protected int getLaneCount(LaneType lt, GeoLoc loc) {
		CorridorBase cb = r_node_manager.lookupCorridor(loc);
		int east = GeoLocHelper.getTrueEasting(loc);
		int north = GeoLocHelper.getTrueNorthing(loc);
		switch(lt) {
		case EXIT:
			R_Node n = cb.findNearest(east, north, R_NodeType.EXIT);
			if(n != null)
				return n.getLanes();
			else
				return 0;
		case MERGE:
			R_Node mn = cb.findNearest(east, north,
				R_NodeType.ENTRANCE);
			if(mn != null)
				return mn.getLanes();
			else
				return 0;
		default:
			return cb.getLaneCount(east, north);
		}
	}

	/** Create an impact string for the given number of lanes */
	protected String createImpact(int n_lanes) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < n_lanes + 2; i++)
			sb.append('.');
		return sb.toString();
	}

	/** Get the selected toggle button */
	protected JToggleButton getSelected() {
		if(crash_btn.isSelected())
			return crash_btn;
		if(stall_btn.isSelected())
			return stall_btn;
		if(block_btn.isSelected())
			return block_btn;
		if(work_btn.isSelected())
			return work_btn;
		return null;
	}

	/** Set enabled */
	public void setEnabled(boolean e) {
		crash_btn.setEnabled(e);
		stall_btn.setEnabled(e);
		block_btn.setEnabled(e);
		work_btn.setEnabled(e);
	}

	/** Dispose of the incident creator */
	public void dispose() {
		removeAll();
	}

	/** Set the map */
	public void setMap(MapBean m) {
		map = m;
	}
}
