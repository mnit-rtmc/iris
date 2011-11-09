/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2011  Minnesota Department of Transportation
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

import javax.swing.JButton;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.TmsForm;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A panel for displaying a table of plans.
 *
 * @author Douglas Lau
 */
public class PlanTablePanel<T extends SonarObject> extends FormPanel {

	/** Table row height */
	static protected final int ROW_HEIGHT = 22;

	/** Table model for plan actions */
	protected ProxyTableModel<T> model;

	/** Table to hold plan actions */
	protected final ZTable table = new ZTable();

	/** Button to delete the selected action */
	protected final JButton del_btn = new JButton("Delete");

	/** Create a new plan table panel */
	public PlanTablePanel() {
		super(true);
		setBorder(TmsForm.BORDER);
		table.setAutoCreateColumnsFromModel(false);
		table.setRowHeight(ROW_HEIGHT);
		table.setVisibleRowCount(10);
		addRow(table);
		addRow(del_btn);
		del_btn.setEnabled(false);
		addActionJobs();
	}

	/** Add jobs for plan table */
	private void addActionJobs() {
		final ListSelectionModel sm = table.getSelectionModel();
		sm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, sm) {
			public void perform() {
				if(!event.getValueIsAdjusting()) {
					int row = sm.getMinSelectionIndex();
					if(row >= 0)
						selectRow(row);
				}
			}
		};
		new ActionJob(this, del_btn) {
			public void perform() throws Exception {
				int row = sm.getMinSelectionIndex();
				if(row >= 0)
					deleteRow(row);
			}
		};
	}

	/** Change the selected row */
	private void selectRow(int row) {
		ProxyTableModel<T> mdl = model;
		if(mdl != null) {
			T proxy = mdl.getProxy(row);
			del_btn.setEnabled(mdl.canRemove(proxy));
		} else
			del_btn.setEnabled(false);
	}

	/** Delete the plan at the specified row */
	private void deleteRow(int row) {
		ProxyTableModel<T> mdl = model;
		if(mdl != null)
			mdl.deleteRow(row);
	}

	/** Change the selected action plan */
	public void setTableModel(ProxyTableModel<T> mdl) {
		del_btn.setEnabled(false);
		ProxyTableModel<T> o_model = model;
		model = mdl;
		model.initialize();
		table.setColumnModel(model.createColumnModel());
		table.setModel(model);
		if(o_model != null)
			o_model.dispose();
	}

	/** Dispose of the form */
	public void dispose() {
		super.dispose();
		if(model != null) {
			model.dispose();
			model = null;
		}
	}
}
