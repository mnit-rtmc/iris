/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.map.MapToolBar;
import us.mn.state.dot.trafmap.ViewLayer;
import us.mn.state.dot.tms.client.toolbar.IrisToolBar;

/**
 * A screen pane is a pane which contains all components for one screen on
 * the IRIS client.
 *
 * @author Douglas Lau
 */
public class ScreenPane extends JPanel {

	/** Tabbed side pane */
	protected final JTabbedPane tab_pane;

	/** Map to be displayed on the screen pane */
	protected final MapBean map;

	/** Get the map */
	public MapBean getMap() {
		return map;
	}

	/** Map panel */
	protected final JPanel map_panel;

	/** Create a new screen pane */
	public ScreenPane(ViewLayer vlayer) {
		setLayout(new BorderLayout());
		tab_pane = new JTabbedPane(SwingConstants.TOP);
		add(tab_pane, BorderLayout.WEST);
		map = new MapBean(true);
		map.setBackground(new Color(208, 216, 208));
		map_panel = createMapPanel(vlayer);
		add(map_panel, BorderLayout.CENTER);
		tab_pane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				Component tab = tab_pane.getSelectedComponent();
				if(tab instanceof MapTab) {
					MapTab mt = (MapTab)tab;
					map.setModel(mt.getMapModel());
				}
			}
		});
	}

	/** Add a tab to the screen pane */
	public void addTab(IrisTab tab) {
		tab_pane.addTab(tab.getName(), null, tab, tab.getTip());
	}

	/** Remove all the tabs */
	public void removeTabs() {
		tab_pane.removeAll();
	}

	/** Create the map panel */
	protected JPanel createMapPanel(ViewLayer vlayer) {
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createBevelBorder(
			BevelBorder.LOWERED));
		p.add(map, BorderLayout.CENTER);
		JPanel mp = new JPanel(new BorderLayout());
		MapToolBar toolBar = createToolBar(vlayer);
		mp.add(toolBar, BorderLayout.NORTH);
		mp.add(p, BorderLayout.CENTER);
//		mp.add(createIrisToolBar(), BorderLayout.SOUTH);
		return mp;
	}

	/** Create a map tool bar with appropriate view buttons */
	protected MapToolBar createToolBar(ViewLayer vlayer) {
		MapToolBar b = new MapToolBar(map);
		b.setFloatable(false);
		if(vlayer != null) {
			for(JButton v: vlayer.createViewButtons(map))
				b.addButton(v);
		} else
			b.addHomeButton();
		return b;
	}

	/** Create a map status bar */
/*	protected IrisToolBar createIrisToolBar() {
		IrisToolBar b = new IrisToolBar(map, session);
		b.setFloatable(false);
		return b;
	} */
}
