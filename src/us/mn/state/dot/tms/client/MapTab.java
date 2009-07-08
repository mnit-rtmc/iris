/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2009  Minnesota Department of Transportation
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
import javax.swing.Box;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import us.mn.state.dot.trafmap.ViewLayer;
import us.mn.state.dot.map.LayerState;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.map.MapToolBar;
import us.mn.state.dot.tms.client.toolbar.IrisToolBar;

/**
 * Base class for all Iris tabs which contain maps
 *
 * @author Douglas Lau
 */
abstract public class MapTab extends IrisTab {

	/** toolbar */
	protected IrisToolBar m_tb;

	/** Map to be displayed on the tab */
	protected final MapBean map;

	/** session */
	final Session m_tc;

	/** Create a new map tab */
	public MapTab(final Session tc, String n, String t) {
		super(n, t);
		m_tc = tc;
		map = new MapBean(true);
		map.setBackground(new Color(208, 216, 208));
	}

	/** Create a map tool bar with appropriate view buttons */
	protected MapToolBar createToolBar(ViewLayer vlayer) {
		MapToolBar b = new MapToolBar(map);
		for(LayerState s: map.getLayers())
			b.addThemeLegend(s);
		b.add(Box.createGlue());
		b.add(Box.createGlue());
		if(vlayer == null)
			b.addButton(b.createHomeButton());
		else {
			JButton[] views = vlayer.createViewButtons(map);
			for(int i = 0; i < views.length; i++)
				b.addButton(views[i]);
		}
		b.setFloatable(false);
		return b;
	}

	/** Create a map status bar */
	protected IrisToolBar createIrisToolBar() {
		IrisToolBar b = new IrisToolBar(map, m_tc);
		b.setFloatable(false);
		return b;
	}

	/** Create the map panel */
	protected JPanel createMapPanel(ViewLayer vlayer) {
		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createBevelBorder(
			BevelBorder.LOWERED));
		p.add(map, BorderLayout.CENTER);
		JPanel mapPanel = new JPanel(new BorderLayout());
		MapToolBar toolBar = createToolBar(vlayer);
		mapPanel.add(toolBar, BorderLayout.NORTH);
		mapPanel.add(p, BorderLayout.CENTER);
		m_tb = createIrisToolBar();
		mapPanel.add(m_tb, BorderLayout.SOUTH);
		return mapPanel;
	}

	/** Perform any clean up necessary */
	public void dispose() {
		map.dispose();
	}
}
