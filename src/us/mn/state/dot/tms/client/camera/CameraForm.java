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
package us.mn.state.dot.tms.client.camera;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.toast.AbstractForm;
import us.mn.state.dot.tms.client.toast.FormPanel;
import us.mn.state.dot.tms.client.toast.SmartDesktop;
import us.mn.state.dot.tms.client.widget.ZTable;

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
	protected final ZTable c_table = new ZTable();

	/** Button to display the camera properties */
	protected final JButton properties = new JButton("Properties");

	/** Button to delete the selected camera */
	protected final JButton del_camera = new JButton("Delete");

	/** User session */
	protected final Session session;

	/** Camera type cache */
	protected final TypeCache<Camera> cache;

	/** Create a new camera form */
	public CameraForm(Session s, TypeCache<Camera> c) {
		super(TITLE);
		session = s;
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
		c_table.setVisibleRowCount(12);
		new ActionJob(this, properties) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0) {
					Camera cam = c_model.getProxy(row);
					if(cam != null)
						showPropertiesForm(cam);
				}
			}
		};
		c_table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2)
					properties.doClick();
			}
		});
		new ActionJob(this, del_camera) {
			public void perform() throws Exception {
				int row = s.getMinSelectionIndex();
				if(row >= 0)
					c_model.deleteRow(row);
			}
		};
		FormPanel panel = new FormPanel(true);
		panel.addRow(c_table);
		panel.add(properties);
		panel.addRow(del_camera);
		properties.setEnabled(false);
		del_camera.setEnabled(false);
		return panel;
	}

	/** Change the selected camera */
	protected void selectCamera() {
		int row = c_table.getSelectedRow();
		properties.setEnabled(row >= 0 && !c_model.isLastRow(row));
		del_camera.setEnabled(row >= 0 && !c_model.isLastRow(row));
	}

	/** Show the properties form for a camera */
	protected void showPropertiesForm(Camera cam) throws Exception {
		SmartDesktop desktop = session.getDesktop();
		desktop.show(new CameraProperties(session, cam));
	}
}
