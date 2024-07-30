/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2024  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.IPanel;

/**
 * PropSetup is a GUI panel for displaying and editing setup info on a DMS
 * properties form.
 *
 * @author Douglas Lau
 */
public class PropSetup extends IPanel {

	/** Notes text area */
	private final JTextArea notes_txt = new JTextArea(10, 32);

	/** Remote beacon combo box model */
	private final IComboBoxModel<Beacon> beacon_mdl;

	/** Remote beacon action */
	private final IAction beacon_act = new IAction("dms.beacon.rem") {
		protected void doActionPerformed(ActionEvent e) {
			dms.setBeacon(beacon_mdl.getSelectedProxy());
		}
		@Override
		protected void doUpdateSelected() {
			beacon_mdl.setSelectedItem(dms.getBeacon());
		}
	};

	/** Remote beacon combo box */
	private final JComboBox<Beacon> beacon_cbx = new JComboBox<Beacon>();

	/** Numbered graphic model */
	private final NumberedGraphicModel num_graph_mdl;

	/** Graphic combo box model */
	private final IComboBoxModel<Graphic> graphic_mdl;

	/** Graphic action */
	private final IAction graphic_act = new IAction("dms.static.graphic") {
		protected void doActionPerformed(ActionEvent e) {
			dms.setStaticGraphic(graphic_mdl.getSelectedProxy());
		}
		@Override
		protected void doUpdateSelected() {
			graphic_mdl.setSelectedItem(dms.getStaticGraphic());
		}
	};

	/** Static image graphic combo box */
	private final JComboBox<Graphic> graphic_cbx = new JComboBox<Graphic>();

	/** User session */
	private final Session session;

	/** DMS to display */
	private final DMS dms;

	/** Create a new DMS properties setup panel */
	public PropSetup(Session s, DMS sign) {
		session = s;
		SonarState state = session.getSonarState();
		dms = sign;
		beacon_mdl = new IComboBoxModel<Beacon>(state.getBeaconModel());
		num_graph_mdl = NumberedGraphicModel.create(session);
		graphic_mdl = new IComboBoxModel<Graphic>(num_graph_mdl);
	}

	/** Initialize the widgets on the form */
	@Override
	public void initialize() {
		super.initialize();
		beacon_cbx.setModel(beacon_mdl);
		beacon_cbx.setAction(beacon_act);
		graphic_cbx.setModel(graphic_mdl);
		graphic_cbx.setAction(graphic_act);
		graphic_cbx.setRenderer(new GraphicListCellRenderer());
		add("device.notes");
		add(notes_txt, Stretch.LAST);
		add("dms.beacon.rem");
		add(beacon_cbx, Stretch.LAST);
		add("dms.static.graphic");
		add(graphic_cbx, Stretch.LAST);
		createJobs();
	}

	/** Create the widget jobs */
	private void createJobs() {
		notes_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				String n = notes_txt.getText().trim();
				dms.setNotes((n.length() > 0) ? n : null);
			}
		});
	}

	/** Dispose of the panel */
	@Override
	public void dispose() {
		num_graph_mdl.dispose();
	}

	/** Update the edit mode */
	public void updateEditMode() {
		notes_txt.setEnabled(canWrite("notes"));
		graphic_act.setEnabled(canWrite("staticGraphic"));
		beacon_act.setEnabled(canWrite("beacon"));
	}

	/** Update one attribute on the form tab */
	public void updateAttribute(String a) {
		if (a == null || a.equals("notes")) {
			String n = dms.getNotes();
			notes_txt.setText((n != null) ? n : "");
		}
		if (null == a || a.equals("staticGraphic"))
			graphic_act.updateSelected();
		if (null == a || a.equals("beacon"))
			beacon_act.updateSelected();
	}

	/** Check if the user can write an attribute */
	private boolean canWrite(String aname) {
		return session.canWrite(dms, aname);
	}
}
