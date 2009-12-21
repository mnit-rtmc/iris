/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.client.widget.ZTable;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * Form #2 for displaying a DMS table.
 *
 * @author Douglas Lau
 * @author Michael Darter
 * @see ViewMenu, DMSForm, DMSTable, DMSTable2
 */
public class DMSForm2 extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = I18N.get("dms.title");

	/** Table model for DMSs */
	protected final DMSModel2 d_model;

	/** Table to hold the DMS list */
	protected final ZTable d_table = new ZTable();

	/** Button to display the properties */
	protected final JButton properties = new JButton("Properties");

	/** Button to delete the selected sign */
	protected final JButton del_sign = new JButton("Delete");

	/** Session */
	protected final Session m_session;

	/** Create a new DMS form */
	public DMSForm2(Session s) {
		super(TITLE);
		m_session = s;
		d_model = new DMSModel2(s);
	}

	/** Initialize the widgets in the form */
	protected void initialize() {
		d_model.initialize();
		add(createDMSPanel());
	}

	/** Dispose of the form */
	protected void dispose() {
		d_model.dispose();
	}

	/** Create DMS panel */
	protected JPanel createDMSPanel() {
		final ListSelectionModel s = d_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectSign();
			}
		};
		new ActionJob(this, properties) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0) {
					DMS dms = d_model.getProxy(row);
					if(dms != null)
						showPropertiesForm(dms);
				}
			}
		};
		d_table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2)
					properties.doClick();
			}
		});
		new ActionJob(this, del_sign) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					d_model.deleteRow(row);
			}
		};
		d_table.setModel(d_model);
		d_table.setAutoCreateColumnsFromModel(false);
		d_table.setColumnModel(d_model.createColumnModel());
		d_table.setVisibleRowCount(16);
		FormPanel panel = new FormPanel(true);
		panel.addRow(d_table);
		panel.add(properties);
		panel.addRow(del_sign);
		properties.setEnabled(false);
		del_sign.setEnabled(false);
		return panel;
	}

	/** Change the selected sign */
	protected void selectSign() {
		int row = d_table.getSelectedRow();
		properties.setEnabled(row >= 0 && !d_model.isLastRow(row));
		del_sign.setEnabled(row >= 0 && !d_model.isLastRow(row));
	}

	/** Show the properties form */
	protected void showPropertiesForm(DMS dms) throws Exception {
		SmartDesktop desktop = m_session.getDesktop();
		desktop.show(new DMSProperties(m_session, dms));
	}
}
