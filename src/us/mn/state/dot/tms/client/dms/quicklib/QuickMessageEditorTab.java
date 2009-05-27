/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms.quicklib;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.widget.ZTable;

/**
 * A JPanel for displaying and editing quick messages.
 * @see QuickMessage, QuickMessageImpl
 * @author Michael Darter
 * @author Doug Lau
 */
public class QuickMessageEditorTab extends FormPanel {

	/** Tab name */
	static protected final String TAB_NAME = "All";

	/** Table row height */
	static protected final int ROW_HEIGHT = 20;

	/** Table model */
	protected final QuickMessageTableModel m_tableModel;

	/** table */
	protected final ZTable m_table = new ZTable();

	/** Button to delete the selected row */
	protected final JButton del_btn = new JButton("Delete");

	/** form this tab is displayed on */
	protected final AbstractForm m_form;

	/** SONAR User for permission checks */
	protected final User m_user;

	/** Constructor
	 * @param sa Type cache for sign messages.
	 * @param form Form this tab is placed onto. 
	 * @param user SONAR user. */
	public QuickMessageEditorTab(TypeCache<QuickMessage> tc,
		AbstractForm form, User user)
	{
		super(true);
		m_form = form;
		m_user = user;
		m_tableModel = new QuickMessageTableModel(tc, user);
		createControls();
	}

	/** return tab text */
	public String getTabText() {
		return "Quick Library";
	}

	/** return the text name of the tab */
	public String getName() {
		return TAB_NAME;
	}

	/** create controls on the tab */
	protected void createControls() {
		initTable();
		addRow(m_table);
		addRow(del_btn);
		del_btn.setEnabled(false);
	}

	/** Initialize the table */
	protected void initTable() {
		ListSelectionModel s = m_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				selectRow();
			}
		};

		m_table.setAutoCreateColumnsFromModel(false);
		m_table.setColumnModel(
			QuickMessageTableModel.createColumnModel());
		m_table.setModel(m_tableModel);
		m_table.setRowHeight(ROW_HEIGHT);
		m_table.setVisibleRowCount(12);
		new ActionJob(this, del_btn) {
			public void perform() throws Exception {
				final ListSelectionModel s = 
					m_table.getSelectionModel();
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					m_tableModel.deleteRow(row);
			}
		};
	}

	/** cleanup */
	public void dispose() {
		m_tableModel.dispose();
	}

	/** Handle row selection change */
	protected void selectRow() {
		QuickMessage proxy = m_tableModel.getProxy(
			m_table.getSelectedRow());
		del_btn.setEnabled(proxy == null ? false : 
			m_tableModel.canRemoveProxy(proxy.getName()));
	}
}

