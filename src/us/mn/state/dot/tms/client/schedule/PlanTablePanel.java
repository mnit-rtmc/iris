/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.schedule;

import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.widget.IAction2;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A panel for displaying a table of plans.
 *
 * @author Douglas Lau
 */
public class PlanTablePanel<T extends SonarObject> extends IPanel {

	/** Table row height */
	static private final int ROW_HEIGHT = UI.scaled(22);

	/** Table model for plan actions */
	private ProxyTableModel<T> model;

	/** Table to hold plan actions */
	private final ZTable table = new ZTable();

	/** Action to delete the selected action */
	private final IAction2 del_action = new IAction2(
		"action.plan.action.delete")
	{
		protected void doActionPerformed(ActionEvent e) {
			ListSelectionModel sm = table.getSelectionModel();
			int row = sm.getMinSelectionIndex();
			if(row >= 0)
				deleteRow(row);
		}
	};

	/** Create a new plan table panel */
	public PlanTablePanel() {
		table.setAutoCreateColumnsFromModel(false);
		table.setRowHeight(ROW_HEIGHT);
		table.setVisibleRowCount(10);
		add(table, Stretch.FULL);
		add(new JButton(del_action), Stretch.RIGHT);
		del_action.setEnabled(false);
		addJobs();
	}

	/** Add jobs for plan table */
	private void addJobs() {
		final ListSelectionModel sm = table.getSelectionModel();
		sm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sm.addListSelectionListener(new IListSelectionAdapter() {
			@Override
			public void valueChanged() {
				int row = sm.getMinSelectionIndex();
				if(row >= 0)
					selectRow(row);
			}
		});
	}

	/** Change the selected row */
	private void selectRow(int row) {
		ProxyTableModel<T> mdl = model;
		if(mdl != null) {
			T proxy = mdl.getProxy(row);
			del_action.setEnabled(mdl.canRemove(proxy));
		} else
			del_action.setEnabled(false);
	}

	/** Delete the plan at the specified row */
	private void deleteRow(int row) {
		ProxyTableModel<T> mdl = model;
		if(mdl != null)
			mdl.deleteRow(row);
	}

	/** Change the selected action plan */
	public void setTableModel(ProxyTableModel<T> mdl) {
		del_action.setEnabled(false);
		ProxyTableModel<T> o_model = model;
		model = mdl;
		model.initialize();
		table.setColumnModel(model.createColumnModel());
		table.setModel(model);
		if(o_model != null)
			o_model.dispose();
	}

	/** Dispose of the form */
	@Override public void dispose() {
		super.dispose();
		if(model != null) {
			model.dispose();
			model = null;
		}
	}
}
