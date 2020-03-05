/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  Minnesota Department of Transportation
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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.DevicePurpose;
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

	/** External beacon combo box model */
	private final IComboBoxModel<Beacon> beacon_mdl;

	/** External beacon action */
	private final IAction beacon_act = new IAction("dms.beacon.ext") {
		protected void doActionPerformed(ActionEvent e) {
			dms.setBeacon(beacon_mdl.getSelectedProxy());
		}
		@Override
		protected void doUpdateSelected() {
			beacon_mdl.setSelectedItem(dms.getBeacon());
		}
	};

	/** External beacon combo box */
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

	/** Device purpose model */
	private final DefaultComboBoxModel<DevicePurpose> purpose_mdl =
		new DefaultComboBoxModel<DevicePurpose>(DevicePurpose.values());

	/** Device purpose action */
	private final IAction purpose_act = new IAction("dms.purpose") {
		protected void doActionPerformed(ActionEvent e) {
			DevicePurpose dp = (DevicePurpose) purpose_mdl
				.getSelectedItem();
			dms.setPurpose((dp != null)
					? dp.ordinal()
					: DevicePurpose.GENERAL.ordinal());
		}
		@Override
		protected void doUpdateSelected() {
			DevicePurpose dp = DevicePurpose.fromOrdinal(
				dms.getPurpose());
			purpose_mdl.setSelectedItem(dp);
		}
	};

	/** Device purpose combo box */
	private final JComboBox<DevicePurpose> purpose_cbx =
		new JComboBox<DevicePurpose>();

	/** Checkbox for hidden flag */
	private final JCheckBox hidden_chk = new JCheckBox(new IAction(null) {
		protected void doActionPerformed(ActionEvent e) {
			dms.setHidden(hidden_chk.isSelected());
		}
	});

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
		purpose_cbx.setModel(purpose_mdl);
		purpose_cbx.setAction(purpose_act);
		add("dms.beacon.ext");
		add(beacon_cbx, Stretch.LAST);
		add("dms.static.graphic");
		add(graphic_cbx, Stretch.LAST);
		add("dms.purpose");
		add(purpose_cbx, Stretch.LAST);
		add("dms.hidden");
		add(hidden_chk, Stretch.LAST);
	}

	/** Dispose of the panel */
	@Override
	public void dispose() {
		num_graph_mdl.dispose();
	}

	/** Update the edit mode */
	public void updateEditMode() {
		graphic_act.setEnabled(canWrite("staticGraphic"));
		beacon_act.setEnabled(canWrite("beacon"));
		purpose_act.setEnabled(canWrite("purpose"));
		hidden_chk.setEnabled(canWrite("hidden"));
	}

	/** Update one attribute on the form tab */
	public void updateAttribute(String a) {
		if (null == a || a.equals("staticGraphic"))
			graphic_act.updateSelected();
		if (null == a || a.equals("beacon"))
			beacon_act.updateSelected();
		if (null == a || a.equals("purpose"))
			purpose_act.updateSelected();
		if (null == a || a.equals("hidden"))
			hidden_chk.setSelected(dms.getHidden());
	}

	/** Check if the user can write an attribute */
	private boolean canWrite(String aname) {
		return session.canWrite(dms, aname);
	}
}
