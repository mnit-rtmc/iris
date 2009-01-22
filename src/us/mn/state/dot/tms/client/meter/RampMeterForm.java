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
package us.mn.state.dot.tms.client.meter;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.SmartDesktop;

/**
 * A form for displaying a table of ramp meters.
 *
 * @author Douglas Lau
 */
public class RampMeterForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "Ramp Meters";

	/** Table model for ramp meters */
	protected RampMeterModel model;

	/** Table to hold the ramp meter list */
	protected final JTable table = new JTable();

	/** Button to display the properties */
	protected final JButton propertiesBtn = new JButton("Properties");

	/** Button to delete the selected proxy */
	protected final JButton deleteBtn = new JButton("Delete");

	/** TMS connection */
	protected final TmsConnection connection;

	/** Type cache */
	protected final TypeCache<RampMeter> cache;

	/** Create a new ramp meter form */
	public RampMeterForm(TmsConnection tc, TypeCache<RampMeter> c) {
		super(TITLE);
		connection = tc;
		cache = c;
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		model = new RampMeterModel(cache);
		add(createRampMeterPanel());
	}

	/** Dispose of the form */
	protected void dispose() {
		model.dispose();
	}

	/** Create ramp meter panel */
	protected JPanel createRampMeterPanel() {
		final ListSelectionModel s = table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectProxy();
			}
		};
		new ActionJob(this, propertiesBtn) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0) {
					RampMeter meter = model.getProxy(row);
					if(meter != null)
						showPropertiesForm(meter);
				}
			}
		};
		new ActionJob(this, deleteBtn) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					model.deleteRow(row);
			}
		};
		propertiesBtn.setEnabled(false);
		deleteBtn.setEnabled(false);
		FormPanel panel = new FormPanel(true);
		table.setModel(model);
		table.setAutoCreateColumnsFromModel(false);
		table.setColumnModel(model.createColumnModel());
		panel.addRow(table);
		panel.addRow(propertiesBtn, deleteBtn);
		return panel;
	}

	/** Change the selected proxy */
	protected void selectProxy() {
		int row = table.getSelectedRow();
		propertiesBtn.setEnabled(row >= 0 && !model.isLastRow(row));
		deleteBtn.setEnabled(row >= 0 && !model.isLastRow(row));
	}

	/** Show the properties form */
	protected void showPropertiesForm(RampMeter meter) {
		SmartDesktop desktop = connection.getDesktop();
		desktop.show(new RampMeterProperties(connection, meter));
	}
}
