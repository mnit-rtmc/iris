/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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

import java.awt.Dimension;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.SystemAttribute;
import us.mn.state.dot.tms.client.toast.FormPanel;

/**
 * This is a tab for viewing and editing system attributes.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SystemAttributeTab extends FormPanel {

	/** Tab name */
	static protected final String TAB_NAME = "All";

	/** Table row height */
	static protected final int ROW_HEIGHT = 20;

	/** table model */
	protected final SystemAttributeTableModel m_tableModel;

	/** traffic device attribute table */
	protected final JTable m_table = new JTable();

	/** Button to delete the selected attribute */
	protected final JButton del_attrib_btn = new JButton("Delete");

	/** form this tab is displayed on */
	protected final SystemAttributeForm m_form;

	/** Create the attribute editor tab.
	 * @param sa Type cache for system attributres.
	 * @param form Form this tab is placed onto.
	 */
	public SystemAttributeTab(TypeCache<SystemAttribute> sa,
		SystemAttributeForm form)
	{
		super(true);
		m_form = form;
		m_tableModel = new SystemAttributeTableModel(sa, form);
		createControls();
	}

	/** return the text name of the tab */
	public String getName() {
		return TAB_NAME;
	}

	/** create the attribute editor tab */
	protected void createControls() {
		initTable();
		addRow(m_table);
		setCenter();
		addRow(del_attrib_btn);
		new ActionJob(this, del_attrib_btn) {
			public void perform() throws Exception {
				final ListSelectionModel s = 
					m_table.getSelectionModel();
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					m_tableModel.deleteRow(row);
			}
		};
		del_attrib_btn.setEnabled(false);
	}

	/** Initialize the table */
	protected void initTable() {
		ListSelectionModel s = m_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				selectAttribute();
			}
		};
		m_table.setAutoCreateColumnsFromModel(false);
		m_table.setColumnModel(
			SystemAttributeTableModel.createColumnModel());
		m_table.setModel(m_tableModel);
		m_table.setRowHeight(ROW_HEIGHT);
		m_table.setPreferredScrollableViewportSize(new Dimension(
			m_table.getPreferredSize().width, ROW_HEIGHT * 12));
	}

	/** Select an attribute */
	protected void selectAttribute() {
		SystemAttribute proxy = m_tableModel.getProxy(
			m_table.getSelectedRow());
		if(proxy != null) {
			String a = proxy.getName();
			del_attrib_btn.setEnabled(m_form.canRemoveAttribute(a));
		} else
			del_attrib_btn.setEnabled(false);
	}

	/** cleanup */
	public void dispose() {
		m_tableModel.dispose();
	}
}
