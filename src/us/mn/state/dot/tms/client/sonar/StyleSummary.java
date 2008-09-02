/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.sonar;

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
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import us.mn.state.dot.sonar.SonarObject;

/**
 * Panel to display a summary of styled objects and select status to list
 *
 * @author Douglas Lau
 */
public class StyleSummary<T extends SonarObject> extends JPanel {

	/** Proxy manager */
	protected final ProxyManager<T> manager;

	/** List to display devices of selected status */
	protected final ProxyJList<T> list;

	/** Radio button group */
	protected final ButtonGroup r_buttons = new ButtonGroup();

	/** Status label */
	protected final JLabel s_label = new JLabel();

	/** Create a new style summary panel */
	public StyleSummary(final ProxyManager<T> man) {
		super(new GridBagLayout());
		manager = man;
		ProxyCellRenderer<T> renderer =
			new ProxyCellRenderer<T>(manager);
		setBorder(BorderFactory.createTitledBorder(
			manager.getProxyType() + " Summary"));
		list = new ProxyJList<T>(manager);
		list.setCellRenderer(renderer);
		GridBagConstraints bag = new GridBagConstraints();
		String[] styles = manager.getStyles();
		int half = styles.length / 2;
		for(int i = 0; i < styles.length; i++) {
			final StyleListModel<T> m =
				manager.getStyleModel(styles[i]);
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
				public void contentsChanged(ListDataEvent e) {
//					c.setText(Integer.toString(
//						m.getSize()));
				}
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
		bag.gridy = half;
		bag.gridwidth = 4;
		bag.insets.top = 8;
		add(s_label, bag);
		bag.gridy = half + 1;
		bag.gridwidth = 9;
		bag.weightx = 0;
		bag.weighty = 1;
		bag.fill = GridBagConstraints.BOTH;
		add(new JScrollPane(list), bag);
	}

	/** Set the selected style */
	public void setStyle(String style) {
		s_label.setText(manager.getProxyType() + " status: " + style);
		StyleListModel<T> m = manager.getStyleModel(style);
		list.setModel(m);
		list.setSelectionModel(m.getSelectionModel());
	}

	/** Dispose of the widget */
	public void dispose() {
		removeAll();
	}
}
