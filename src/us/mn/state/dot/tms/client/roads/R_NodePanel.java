/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2017  Minnesota Department of Transportation
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

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A panel for viewing and editing roadway node parameters.
 *
 * @author Douglas Lau
 */
public class R_NodePanel extends JPanel {

	/** Proxy watcher */
	private final ProxyWatcher<R_Node> watcher;

	/** Tabbed pane */
	private final JTabbedPane tab = new JTabbedPane();

	/** Location panel */
	private final R_NodeLocationPanel loc_pnl;

	/** Setup panel */
	private final R_NodeSetupPanel setup_pnl;

	/** Detector panel */
	private final R_NodeDetectorPanel det_pnl;

	/** Proxy view */
	private final ProxyView<R_Node> view = new ProxyView<R_Node>() {
		public void enumerationComplete() { }
		public void update(R_Node n, String a) {
			loc_pnl.update(n, a);
			setup_pnl.update(n, a);
			det_pnl.setR_Node(n);
		}
		public void clear() {
			loc_pnl.clear();
			setup_pnl.clear();
			det_pnl.setR_Node(null);
		}
	};

	/** Set a new r_node */
	public void setR_Node(R_Node n) {
		if (n != null)
			loc_pnl.setGeoLoc(n.getGeoLoc());
		else
			loc_pnl.setGeoLoc(null);
		watcher.setProxy(n);
	}

	/** Create a new roadway node panel */
	public R_NodePanel(Session s) {
		super(new BorderLayout());
		loc_pnl = new R_NodeLocationPanel(s);
		setup_pnl = new R_NodeSetupPanel(s);
		det_pnl = new R_NodeDetectorPanel(s);
		TypeCache<R_Node> cache =
			s.getSonarState().getDetCache().getR_Nodes();
		watcher = new ProxyWatcher<R_Node>(cache, view, false);
		setBorder(BorderFactory.createTitledBorder(I18N.get(
			"r_node.selected")));
	}

	/** Initialize the widgets on the panel */
	public void initialize() {
		watcher.initialize();
		loc_pnl.initialize();
		setup_pnl.initialize();
		det_pnl.initialize();
		tab.add(I18N.get("location"), loc_pnl);
		tab.add(I18N.get("device.setup"), setup_pnl);
		tab.add(I18N.get("detector.plural"), det_pnl);
		add(tab, BorderLayout.CENTER);
	}

	/** Update the edit mode */
	public void updateEditMode() {
		loc_pnl.updateEditMode();
		setup_pnl.updateEditMode();
	}

	/** Dispose of the panel */
	public void dispose() {
		det_pnl.dispose();
		setup_pnl.dispose();
		loc_pnl.dispose();
		watcher.dispose();
	}
}
