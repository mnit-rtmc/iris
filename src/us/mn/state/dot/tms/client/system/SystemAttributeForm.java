/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * The system attribute allows administrators to change system-wide policy
 * attributes.
 *
 * @author Douglas Lau
 */
public class SystemAttributeForm extends AbstractForm {

	/** Frame title */
	static private final String TITLE = "System Attributes";

	/** Table row height */
	static protected final int ROW_HEIGHT = 20;

	/** Table model */
	protected final SystemAttributeTableModel model;

	/** System attribute table. */
	protected final ZTable m_table = new ZTable() {
		public String getToolTipText(int row, int column) {
			Object value = model.getValueAt(row,
				SystemAttributeTableModel.COL_NAME);
			if(value instanceof String)
				return SystemAttrEnum.getDesc((String)value);
			else
				return null;
		}
	};

	/** Button to delete the selected attribute */
	protected final JButton del_btn = new JButton("Delete");

	/** Create a new system attribute form */
	public SystemAttributeForm(Session s) {
		super(TITLE);
		setHelpPageName("Help.SystemAttributeForm");
		model = new SystemAttributeTableModel(s);
	}

	/** Initialise the widgets on the form */
	protected void initialize() {
		model.initialize();
		createJobs();
		add(createPanel());
	}

	/** Dispose of the form */
	protected void dispose() {
		model.dispose();
	}

	/** Create Gui jobs */
	protected void createJobs() {
		ListSelectionModel s = m_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				selectAttribute();
			}
		};
		new ActionJob(this, del_btn) {
			public void perform() throws Exception {
				final ListSelectionModel s = 
					m_table.getSelectionModel();
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					model.deleteRow(row);
			}
		};
	}

	/** Create the panel for the form */
	protected JPanel createPanel() {
		m_table.setAutoCreateColumnsFromModel(false);
		m_table.setColumnModel(model.createColumnModel());
		m_table.setModel(model);
		m_table.setRowHeight(ROW_HEIGHT);
		m_table.setVisibleRowCount(12);
		FormPanel panel = new FormPanel(true);
		panel.addRow(m_table);
		panel.addRow(del_btn);
		del_btn.setEnabled(false);
		return panel;
	}

	/** Select an attribute */
	protected void selectAttribute() {
		SystemAttribute sa = model.getProxy(m_table.getSelectedRow());
		del_btn.setEnabled(model.canRemove(sa));
	}
}
