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
package us.mn.state.dot.tms.client.warning;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/**
 * A form for displaying and editing warning signs
 *
 * @author Douglas Lau
 */
public class WarningSignForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "Warning Signs";

	/** Table model for warning signs */
	protected WarningSignModel w_model;

	/** Table to hold the warning signs */
	protected final JTable w_table = new JTable();

	/** Button to display the warning sign properties */
	protected final JButton properties = new JButton("Properties");

	/** Button to delete the selected warning sign */
	protected final JButton del_sign = new JButton("Delete Sign");

	/** TMS connection */
	protected final TmsConnection connection;

	/** Warning sign type cache */
	protected final TypeCache<WarningSign> cache;

	/** Create a new warning sign form */
	public WarningSignForm(TmsConnection tc, TypeCache<WarningSign> c) {
		super(TITLE);
		connection = tc;
		cache = c;
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		w_model = new WarningSignModel(cache);
		add(createWarningSignPanel());
	}

	/** Dispose of the form */
	protected void dispose() {
		w_model.dispose();
	}

	/** Create warning sign panel */
	protected JPanel createWarningSignPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.left = HGAP;
		bag.insets.right = HGAP;
		bag.insets.top = VGAP;
		bag.insets.bottom = VGAP;
		bag.gridheight = 2;
		final ListSelectionModel s = w_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectWarningSign();
			}
		};
		w_table.setModel(w_model);
		w_table.setAutoCreateColumnsFromModel(false);
		w_table.setColumnModel(w_model.createColumnModel());
		JScrollPane pane = new JScrollPane(w_table);
		panel.add(pane, bag);
		bag.gridheight = 1;
		bag.anchor = GridBagConstraints.CENTER;
		properties.setEnabled(false);
		panel.add(properties, bag);
		new ActionJob(this, properties) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0) {
					WarningSign ws = w_model.getProxy(row);
					if(ws != null)
						showPropertiesForm(ws);
				}
			}
		};
		bag.gridx = 1;
		bag.gridy = 1;
		del_sign.setEnabled(false);
		panel.add(del_sign, bag);
		new ActionJob(this, del_sign) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					w_model.deleteRow(row);
			}
		};
		return panel;
	}

	/** Change the selected warning sign */
	protected void selectWarningSign() {
		int row = w_table.getSelectedRow();
		properties.setEnabled(row >= 0 && !w_model.isLastRow(row));
		del_sign.setEnabled(row >= 0 && !w_model.isLastRow(row));
	}

	/** Show the properties form for a warning sign */
	protected void showPropertiesForm(WarningSign ws) throws Exception {
		SmartDesktop desktop = connection.getDesktop();
		desktop.show(new WarningSignProperties(connection, ws));
	}
}
