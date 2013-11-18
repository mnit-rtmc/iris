/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2013  Minnesota Department of Transportation
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
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A panel for editing roles.
 *
 * @author Douglas Lau
 */
public class RolePanel extends JPanel {

	/** Table model for roles */
	private final RoleModel r_model;

	/** Table to hold the role list */
	private final ZTable r_table = new ZTable();

	/** Table model for role capabilities */
	private final RoleCapabilityModel rc_model;

	/** Table to hold the role capability list */
	private final ZTable rc_table = new ZTable();

	/** Action to delete the selected role */
	private final IAction del_role = new IAction("role.delete") {
		protected void doActionPerformed(ActionEvent e) {
			ListSelectionModel s = r_table.getSelectionModel();
			int row = s.getMinSelectionIndex();
			if(row >= 0)
				r_model.deleteRow(row);
		}
	};

	/** Create a new role panel */
	public RolePanel(Session s) {
		super(new GridBagLayout());
		rc_model = new RoleCapabilityModel(s);
		r_model = new RoleModel(s, rc_model);
		setBorder(UI.border);
		GridBagConstraints bag = new GridBagConstraints();
		r_table.setModel(r_model);
		r_table.setAutoCreateColumnsFromModel(false);
		r_table.setColumnModel(r_model.createColumnModel());
		r_table.setVisibleRowCount(16);
		JScrollPane rpane = new JScrollPane(r_table);
		bag.gridheight = 2;
		add(rpane, bag);
		rc_table.setModel(rc_model);
		rc_table.setAutoCreateColumnsFromModel(false);
		rc_table.setColumnModel(rc_model.createColumnModel());
		rc_table.setRowSelectionAllowed(false);
		rc_table.setVisibleRowCount(12);
		bag.gridheight = 1;
		bag.insets.left = 6;
		JScrollPane spane = new JScrollPane(rc_table);
		add(spane, bag);
		del_role.setEnabled(false);
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(new JButton(del_role));
		box.add(Box.createHorizontalGlue());
		bag.gridx = 1;
		bag.gridy = 1;
		add(box, bag);
	}

	/** Initializze the panel */
	protected void initialize() {
		rc_model.initialize();
		r_model.initialize();
		ListSelectionModel s = r_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		s.addListSelectionListener(new IListSelectionAdapter() {
			@Override
			public void valueChanged() {
				selectRole();
			}
		});
	}

	/** Dispose of the panel */
	protected void dispose() {
		r_model.dispose();
		rc_model.dispose();
	}

	/** Change the selected role */
	private void selectRole() {
		ListSelectionModel s = r_table.getSelectionModel();
		Role r = r_model.getProxy(s.getMinSelectionIndex());
		del_role.setEnabled(r_model.canRemove(r));
		rc_model.setSelectedRole(r);
	}
}
