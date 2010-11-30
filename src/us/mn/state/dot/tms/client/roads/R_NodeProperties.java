/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2010  Minnesota Department of Transportation
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

import java.awt.Color;
import javax.swing.JTabbedPane;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;

/**
 * R_NodeProperties is a form for viewing and editing roadway node parameters.
 *
 * @author Douglas Lau
 */
public class R_NodeProperties extends SonarObjectForm<R_Node> {

	/** Frame title */
	static protected final String TITLE = "R_Node: ";

	/** Location panel */
	protected final R_NodeLocationPanel loc_pnl;

	/** Setup panel */
	protected final R_NodeSetupPanel setup_pnl;

	/** Detector panel */
	protected final R_NodeDetectorPanel det_pnl;

	/** Create a new roadway node properties form */
	public R_NodeProperties(Session s, R_Node n) {
		super(TITLE, s, n);
		loc_pnl = new R_NodeLocationPanel(s, n);
		setup_pnl = new R_NodeSetupPanel(n);
		det_pnl = new R_NodeDetectorPanel(s, n);
	}

	/** Get the SONAR type cache */
	protected TypeCache<R_Node> getTypeCache() {
		return state.getDetCache().getR_Nodes();
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		super.initialize();
		loc_pnl.initialize();
		setup_pnl.initialize();
		det_pnl.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add("Location", loc_pnl);
		tab.add("Setup", setup_pnl);
		tab.add("Detectors", det_pnl);
		add(tab);
		updateAttribute(null);
		setBackground(Color.LIGHT_GRAY);
	}

	/** Dispose of the form */
	protected void dispose() {
		det_pnl.dispose();
		setup_pnl.dispose();
		loc_pnl.dispose();
		super.dispose();
	}

	/** Update one attribute on the form */
	protected void doUpdateAttribute(String a) {
		loc_pnl.doUpdateAttribute(a);
		setup_pnl.doUpdateAttribute(a);
	}
}
