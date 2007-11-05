/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
import java.awt.CardLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A screen pane is a pane which contains all components for one screen on
 * the IRIS client.
 *
 * @author Douglas Lau
 */
public class ScreenPane extends JPanel {

	/** Tabbed side pane */
	protected final JTabbedPane tab_pane;

	/** Card layout for main panel */
	protected final CardLayout cards;

	/** Main panel */
	protected final JPanel main_panel;

	/** Create a new screen pane */
	public ScreenPane() {
		setLayout(new BorderLayout());
		tab_pane = new JTabbedPane(SwingConstants.TOP);
		add(tab_pane, BorderLayout.WEST);
		cards = new CardLayout();
		main_panel = new JPanel(cards);
		add(main_panel, BorderLayout.CENTER);
		tab_pane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int i = tab_pane.getSelectedIndex();
				if(i >= 0) {
					String t = tab_pane.getTitleAt(i);
					cards.show(main_panel, t);
				}
			}
		});
	}

	/** Add a tab to the screen pane */
	public void addTab(IrisTab tab) {
		tab_pane.addTab(tab.getName(), null, tab.getTabPanel(),
			tab.getTip());
		JPanel main = tab.getMainPanel();
		if(main != null)
			main_panel.add(main, tab.getName());
	}

	/** Remove all the tabs */
	public void removeTabs() {
		tab_pane.removeAll();
		main_panel.removeAll();
	}
}
