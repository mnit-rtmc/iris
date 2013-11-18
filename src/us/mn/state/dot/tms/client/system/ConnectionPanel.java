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

import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A panel for viewing connections.
 *
 * @author Douglas Lau
 */
public class ConnectionPanel extends IPanel {

	/** Table model for connections */
	private final ConnectionModel c_model;

	/** Table to hold the connection list */
	private final ZTable c_table = new ZTable();

	/** Action to delete the selected connection */
	private final IAction del_conn = new IAction("connection.disconnect") {
		protected void doActionPerformed(ActionEvent e) {
			ListSelectionModel s = c_table.getSelectionModel();
			int row = s.getMinSelectionIndex();
			if(row >= 0)
				c_model.deleteRow(row);
		}
	};

	/** Create a new connection panel */
	public ConnectionPanel(Session s) {
		c_model = new ConnectionModel(s);
		c_table.setModel(c_model);
		c_table.setAutoCreateColumnsFromModel(false);
		c_table.setColumnModel(c_model.createColumnModel());
		c_table.setVisibleRowCount(16);
		add(c_table, Stretch.FULL);
		if(false) {
			// FIXME: this disconnects all clients
			add(new JButton(del_conn), Stretch.RIGHT);
		}
	}

	/** Initializze the panel */
	protected void initialize() {
		c_model.initialize();
		ListSelectionModel s = c_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		s.addListSelectionListener(new IListSelectionAdapter() {
			@Override
			public void valueChanged() {
				selectConnection();
			}
		});
	}

	/** Dispose of the panel */
	@Override public void dispose() {
		c_model.dispose();
		super.dispose();
	}

	/** Change the selected connection */
	private void selectConnection() {
		ListSelectionModel s = c_table.getSelectionModel();
		Connection c = c_model.getProxy(s.getMinSelectionIndex());
		del_conn.setEnabled(c_model.canRemove(c));
	}
}
