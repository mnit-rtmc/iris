/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

import java.awt.FlowLayout;
import java.awt.geom.Point2D;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.map.PointSelector;
import us.mn.state.dot.map.StyledTheme;
import us.mn.state.dot.map.Symbol;
import us.mn.state.dot.sched.ChangeJob;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.EventType;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;

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

	/** Incident selection model */
	protected final ProxySelectionModel<Incident> selectionModel;

	/** Map bean */
	protected MapBean map;

	/** Create a new incident creator */
	public IncidentCreator(StyledTheme theme,
		ProxySelectionModel<Incident> sel_model)
	{
		super(new FlowLayout());
		selectionModel = sel_model;
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
		setEnabled(false);
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
		add(btn);
		return btn;
	}

	/** Handler for button changed events */
	protected void buttonChanged(JToggleButton btn, EventType et) {
		if(btn.isSelected()) {
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
		// FIXME: look up nearest r_node and assign road, dir, easting
		//        and northing based on that.
		ClientIncident ci = new ClientIncident(et.id, null,
			(short)1, easting, northing, "....");
		selectionModel.setSelected(ci);
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
		setEnabled(m != null);
	}
}
