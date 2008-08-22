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
package us.mn.state.dot.tms.client.camera;

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
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.client.toast.AbstractForm;

/**
 * A form for displaying and editing cameras
 *
 * @author Douglas Lau
 */
public class CameraForm extends AbstractForm {

	/** Frame title */
	static protected final String TITLE = "Cameras";

	/** Table model for cameras */
	protected CameraModel c_model;

	/** Table to hold the camera list */
	protected final JTable c_table = new JTable();

	/** Button to delete the selected camera */
	protected final JButton del_camera = new JButton("Delete Camera");

	/** Camera type cache */
	protected final TypeCache<Camera> cache;

	/** Create a new camera form */
	public CameraForm(TypeCache<Camera> c) {
		super(TITLE);
		cache = c;
	}

	/** Initializze the widgets in the form */
	protected void initialize() {
		c_model = new CameraModel(cache);
		add(createCameraPanel());
	}

	/** Dispose of the form */
	protected void dispose() {
		c_model.dispose();
	}

	/** Create camera panel */
	protected JPanel createCameraPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BORDER);
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.left = HGAP;
		bag.insets.right = HGAP;
		bag.insets.top = VGAP;
		bag.insets.bottom = VGAP;
		final ListSelectionModel s = c_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectCamera();
			}
		};
		c_table.setModel(c_model);
		c_table.setAutoCreateColumnsFromModel(false);
		c_table.setColumnModel(c_model.createColumnModel());
		JScrollPane pane = new JScrollPane(c_table);
		panel.add(pane, bag);
		del_camera.setEnabled(false);
		panel.add(del_camera, bag);
		new ActionJob(this, del_camera) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					c_model.deleteRow(row);
			}
		};
		return panel;
	}

	/** Change the selected camera */
	protected void selectCamera() {
		int row = c_table.getSelectedRow();
		del_camera.setEnabled(row >= 0 && !c_model.isLastRow(row));
	}
}
