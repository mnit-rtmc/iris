/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2009  Minnesota Department of Transportation
 * Copyright (C) 2010 AHMCT, University of California, Davis
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
package us.mn.state.dot.tms.client.proxy;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import us.mn.state.dot.sonar.SonarObject;

/**
 * Panel to display a summary of styled objects and select status to list
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class StyleSummary<T extends SonarObject> extends JPanel {

	/** Proxy manager */
	protected final ProxyManager<T> manager;

	/** Radio button group */
	protected final ButtonGroup r_buttons = new ButtonGroup();

	/** Card layout for style lists */
	protected final CardLayout cards = new CardLayout();

	/** List panel for card layout */
	protected final JPanel list_panel;

	/** Titled border */
	TitledBorder m_border;

	/** Create a new style summary panel */
	public StyleSummary(final ProxyManager<T> man) {
		super(new GridBagLayout());
		manager = man;
		ListCellRenderer renderer = manager.createCellRenderer();
		m_border = BorderFactory.createTitledBorder(
			manager.getProxyType() + " Summary");
		setBorder(m_border);
		list_panel = new JPanel(cards);
		GridBagConstraints bag = new GridBagConstraints();
		String[] styles = manager.getStyles();
		int half = styles.length / 2;
		for(int i = 0; i < styles.length; i++) {
			final StyleListModel<T> m =
				manager.getStyleModel(styles[i]);
			ProxyJList<T> list = manager.createList(styles[i]);
			list.setCellRenderer(renderer);
			JScrollPane scroll = new JScrollPane(list);
			list_panel.add(scroll, m.getName());
			final JRadioButton b = new JRadioButton(m.getName());
			r_buttons.add(b);
			bag.gridx = 1;
			bag.gridy = i;
			if(i >= half) {
				bag.gridx = 5;
				bag.gridy -= half;
			}
			bag.insets.right = 2;
			bag.anchor = GridBagConstraints.WEST;
			add(b, bag);
			bag.gridx = GridBagConstraints.RELATIVE;
			bag.anchor = GridBagConstraints.EAST;
			final JLabel c = new JLabel(Integer.toString(
				m.getSize()));
			add(c, bag);
			add(new JLabel(m.getLegend()), bag);
			b.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setStyle(m.getName());
				}
			});
			m.addListDataListener(new ListDataListener() {
				public void contentsChanged(ListDataEvent e) { }
				public void intervalAdded(ListDataEvent e) {
					c.setText(Integer.toString(
						m.getSize()));
				}
				public void intervalRemoved(ListDataEvent e) {
					c.setText(Integer.toString(
						m.getSize()));
				}
			});
		}
		bag.gridx = 0;
		bag.gridy = 0;
		bag.weightx = 1;
		bag.fill = GridBagConstraints.HORIZONTAL;
		add(new JPanel(), bag);
		bag.gridx = 4;
		add(new JPanel(), bag);
		bag.gridx = 8;
		add(new JPanel(), bag);
		bag.gridx = 0;
		bag.gridy = half + 1;
		bag.gridwidth = 9;
		bag.insets.top = 8;
		bag.gridy = half + 2;
		bag.weightx = 1;
		bag.weighty = 1;
		bag.fill = GridBagConstraints.BOTH;
		add(list_panel, bag);

		// These sizes force the SignPixelPanel in the 
		// dispatcher to switch sizes.
		setMinimumSize(new Dimension(500, 200));
		setPreferredSize(new Dimension(500, 275));
	}

	/** Set the selected style */
	public void setStyle(String style) {
		String t = manager.getProxyType() + " status: " + style;
		m_border = BorderFactory.createTitledBorder(t);
		m_border.setTitle(t);
		setBorder(m_border);
		cards.show(list_panel, style);
	}

	/** Dispose of the widget */
	public void dispose() {
		removeAll();
	}
}
