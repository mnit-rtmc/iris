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
package us.mn.state.dot.tms.client.dms;

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
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.utils.I18NMessages;

/**
 * A form for displaying a table of dynamic message signs.
 *
 * @author Douglas Lau
 */
public class DMSForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = I18NMessages.get("dms.title");

	/** Table model for DMSs */
	protected DMSModel d_model;

	/** Table to hold the DMS list */
	protected final JTable d_table = new JTable();

	/** Button to display the properties */
	protected final JButton properties = new JButton("Properties");

	/** Button to delete the selected sign */
	protected final JButton del_sign = new JButton("Delete");

	/** TMS connection */
	protected final TmsConnection connection;

	/** DMS type cache */
	protected final TypeCache<DMS> cache;

	/** Create a new DMS form */
	public DMSForm(TmsConnection tc, TypeCache<DMS> c) {
		super(TITLE);
		connection = tc;
		cache = c;
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		d_model = new DMSModel(cache);
		add(createDMSPanel());
	}

	/** Dispose of the form */
	protected void dispose() {
		d_model.dispose();
	}

	/** Create DMS panel */
	protected JPanel createDMSPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.left = HGAP;
		bag.insets.right = HGAP;
		bag.insets.top = VGAP;
		bag.insets.bottom = VGAP;
		bag.gridheight = 2;
		final ListSelectionModel s = d_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectSign();
			}
		};
		d_table.setModel(d_model);
		d_table.setAutoCreateColumnsFromModel(false);
		d_table.setColumnModel(d_model.createColumnModel());
		JScrollPane pane = new JScrollPane(d_table);
		panel.add(pane, bag);
		bag.gridheight = 1;
		bag.anchor = GridBagConstraints.CENTER;
		properties.setEnabled(false);
		panel.add(properties, bag);
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
		bag.gridx = 1;
		bag.gridy = 1;
		del_camera.setEnabled(false);
		panel.add(del_camera, bag);
		new ActionJob(this, del_camera) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					d_model.deleteRow(row);
			}
		};
		return panel;
	}

	/** Change the selected sign */
	protected void selectSign() {
		int row = d_table.getSelectedRow();
		properties.setEnabled(row >= 0 && !d_model.isLastRow(row));
		del_camera.setEnabled(row >= 0 && !d_model.isLastRow(row));
	}

	/** Show the properties form */
	protected void showPropertiesForm(DMS dms) throws Exception {
		SmartDesktop desktop = connection.getDesktop();
		desktop.show(new DMSProperties(connection, dms));
	}
}
