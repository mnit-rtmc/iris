/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.device;

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
import us.mn.state.dot.tms.TMSObject;
import us.mn.state.dot.tms.client.TmsSelectionModel;

/**
 * Panel to display a summary of device status and select status to list
 *
 * @author Douglas Lau
 */
public class StatusSummary extends JPanel {

	/** Device handler */
	protected final DeviceHandlerImpl handler;

	/** List to display devices of selected status */
	protected final TmsJList list;

	/** Radio button group */
	protected final ButtonGroup r_buttons = new ButtonGroup();

	/** Status label */
	protected final JLabel s_label = new JLabel();

	/** Create a new status summary panel */
	public StatusSummary(final DeviceHandlerImpl h) {
		super(new GridBagLayout());
		handler = h;
		setBorder(BorderFactory.createTitledBorder(
			handler.getProxyType() + " Status Summary"));
		list = new TmsJList(handler);
		GridBagConstraints bag = new GridBagConstraints();
		NamedListModel[] models = handler.getListModels();
		int half = models.length / 2;
		for(int i = 0; i < models.length; i++) {
			final NamedListModel m = models[i];
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
					setStatus(m.getStatus());
				}
			});
			m.addListDataListener(new ListDataListener() {
				public void contentsChanged(ListDataEvent e) {
					c.setText(Integer.toString(
						m.getSize()));
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

	/** Set the selected status */
	public void setStatus(int status) {
		NamedListModel m = handler.getStatusModel(status);
		TmsSelectionModel s_model = handler.getSelectionModel();
		s_label.setText(handler.getProxyType() + " status: " +
			m.getName());
		list.setModel(m);
		TMSObject o = s_model.getSelected();
		if(o != null)
			list.setSelectedValue(o, true);
	}

	public void dispose() {
		removeAll();
	}
}
