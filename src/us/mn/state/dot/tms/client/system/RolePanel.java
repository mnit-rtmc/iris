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
package us.mn.state.dot.tms.client.system;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sonar.Role;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.TmsForm;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A panel for editing roles.
 *
 * @author Douglas Lau
 */
public class RolePanel extends JPanel {

	/** Table model for roles */
	protected final RoleModel r_model;

	/** Table to hold the role list */
	protected final ZTable r_table = new ZTable();

	/** Table model for role capabilities */
	protected final RoleCapabilityModel rc_model;

	/** Table to hold the role capability list */
	protected final ZTable rc_table = new ZTable();

	/** Button to delete the selected role */
	protected final JButton del_role = new JButton("Delete Role");

	/** Create a new role panel */
	public RolePanel(Session s) {
		super(new GridBagLayout());
		rc_model = new RoleCapabilityModel(s);
		r_model = new RoleModel(s, rc_model);
		setBorder(TmsForm.BORDER);
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
		del_role.setToolTipText("Delete the selected role");
		Box box = Box.createHorizontalBox();
		box.add(Box.createHorizontalGlue());
		box.add(del_role);
		box.add(Box.createHorizontalGlue());
		bag.gridx = 1;
		bag.gridy = 1;
		add(box, bag);
	}

	/** Initializze the panel */
	protected void initialize() {
		rc_model.initialize();
		r_model.initialize();
		final ListSelectionModel s = r_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		s.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if(!e.getValueIsAdjusting())
					selectRole();
			}
		});
		new ActionJob(this, del_role) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					r_model.deleteRow(row);
			}
		};
	}

	/** Dispose of the panel */
	protected void dispose() {
		r_model.dispose();
		rc_model.dispose();
	}

	/** Change the selected role */
	protected void selectRole() {
		ListSelectionModel s = r_table.getSelectionModel();
		Role r = r_model.getProxy(s.getMinSelectionIndex());
		del_role.setEnabled(r_model.canRemove(r));
		rc_model.setSelectedRole(r);
	}
}
