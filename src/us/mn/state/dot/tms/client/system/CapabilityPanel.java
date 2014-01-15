/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.system;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sonar.Capability;
import us.mn.state.dot.sonar.Privilege;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A panel for editing capabilities and privileges.
 *
 * @author Douglas Lau
 */
public class CapabilityPanel extends JPanel {

	/** Table model for capabilities */
	private final CapabilityModel cap_model;

	/** Table model for privileges */
	private PrivilegeModel p_model;

	/** Table to hold the capability list */
	private final ZTable cap_table = new ZTable();

	/** Table to hold the privilege list */
	private final ZTable p_table = new ZTable();

	/** Action to delete the selected capability */
	private final IAction del_cap = new IAction("capability.delete") {
		protected void doActionPerformed(ActionEvent e) {
			ListSelectionModel s = cap_table.getSelectionModel();
			int row = s.getMinSelectionIndex();
			if(row >= 0)
				cap_model.deleteRow(row);
		}
	};

	/** Aciton to delete the selected privilege */
	private final IAction del_priv = new IAction("privilege.delete") {
		protected void doActionPerformed(ActionEvent e) {
			ListSelectionModel sp = p_table.getSelectionModel();
			int row = sp.getMinSelectionIndex();
			if(row >= 0)
				p_model.deleteRow(row);
		}
	};

	/** User session */
	private final Session session;

	/** Create a new capability panel */
	public CapabilityPanel(Session s) {
		super(new GridBagLayout());
		session = s;
		cap_model = new CapabilityModel(s);
		p_model = new PrivilegeModel(session, null);
		setBorder(UI.border);
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.left = 4;
		bag.insets.right = 4;
		bag.insets.top = 4;
		bag.insets.bottom = 4;
		cap_table.setModel(cap_model);
		cap_table.setAutoCreateColumnsFromModel(false);
		cap_table.setColumnModel(cap_model.createColumnModel());
		cap_table.setVisibleRowCount(16);
		JScrollPane pane = new JScrollPane(cap_table);
		add(pane, bag);
		p_table.setModel(p_model);
		p_table.setAutoCreateColumnsFromModel(false);
		p_table.setColumnModel(p_model.createColumnModel());
		p_table.setVisibleRowCount(16);
		pane = new JScrollPane(p_table);
		add(pane, bag);
		del_cap.setEnabled(false);
		bag.gridx = 0;
		bag.gridy = 1;
		add(new JButton(del_cap), bag);
		del_priv.setEnabled(false);
		bag.gridx = 1;
		add(new JButton(del_priv), bag);
	}

	/** Initializze the panel */
	protected void initialize() {
		cap_model.initialize();
		p_model.initialize();
		ListSelectionModel s = cap_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		s.addListSelectionListener(new IListSelectionAdapter() {
			@Override
			public void valueChanged() {
				selectCapability();
			}
		});
		ListSelectionModel sp = p_table.getSelectionModel();
		sp.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sp.addListSelectionListener(new IListSelectionAdapter() {
			@Override
			public void valueChanged() {
				selectPrivilege();
			}
		});
	}

	/** Dispose of the panel */
	protected void dispose() {
		cap_model.dispose();
		p_model.dispose();
	}

	/** Change the selected capability */
	private void selectCapability() {
		ListSelectionModel s = cap_table.getSelectionModel();
		Capability c = cap_model.getProxy(s.getMinSelectionIndex());
		del_cap.setEnabled(cap_model.canRemove(c));
		final PrivilegeModel pm = p_model;
		p_model = new PrivilegeModel(session, c);
		p_model.initialize();
		p_table.clearSelection();
		p_table.setModel(p_model);
		pm.dispose();
	}

	/** Select a privilege */
	private void selectPrivilege() {
		Privilege p = getSelectedPrivilege();
		del_priv.setEnabled(p_model.canRemove(p));
	}

	/** Get the selected privilege */
	private Privilege getSelectedPrivilege() {
		final PrivilegeModel pm = p_model;	// Avoid race
		ListSelectionModel s = p_table.getSelectionModel();
		return pm.getProxy(s.getMinSelectionIndex());
	}
}
