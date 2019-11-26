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
package us.mn.state.dot.tms.client.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.TableCellEditor;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.CameraAction;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.CameraPresetHelper;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;

/**
 * Table model for camera actions assigned to action plans
 *
 * @author Douglas Lau
 */
public class CameraActionModel extends ProxyTableModel<CameraAction> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<CameraAction> descriptor(Session s) {
		return new ProxyDescriptor<CameraAction>(
			s.getSonarState().getCamCache().getCameraActions(),
			false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<CameraAction>> createColumns() {
		ArrayList<ProxyColumn<CameraAction>> cols =
			new ArrayList<ProxyColumn<CameraAction>>(2);
		cols.add(new ProxyColumn<CameraAction>("camera.preset", 160) {
			public Object getValueAt(CameraAction ca) {
				return ca.getPreset();
			}
		});
		cols.add(new ProxyColumn<CameraAction>("action.plan.phase",100){
			public Object getValueAt(CameraAction ca) {
				return ca.getPhase();
			}
			public boolean isEditable(CameraAction ca) {
				return canWrite(ca);
			}
			public void setValueAt(CameraAction ca, Object value) {
				if (value instanceof PlanPhase)
					ca.setPhase((PlanPhase) value);
			}
			protected TableCellEditor createCellEditor() {
				JComboBox<PlanPhase> cbx = new JComboBox
					<PlanPhase>();
				cbx.setModel(new IComboBoxModel<PlanPhase>(
					phase_mdl));
				return new DefaultCellEditor(cbx);
			}
		});
		return cols;
	}

	/** Action plan */
	private final ActionPlan action_plan;

	/** Plan phase model */
	private final ProxyListModel<PlanPhase> phase_mdl;

	/** Create a new camera action table model */
	public CameraActionModel(Session s, ActionPlan ap) {
		super(s, descriptor(s), 16);
		action_plan = ap;
		phase_mdl = s.getSonarState().getPhaseModel();
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(CameraAction proxy) {
		return proxy.getActionPlan() == action_plan;
	}

	/** Check if the user can add a proxy */
	@Override
	public boolean canAdd() {
		return action_plan != null && super.canAdd();
	}

	/** Create an object with the camera name */
	@Override
	public void createObject(String name) {
		CameraPreset cp = CameraPresetHelper.lookup(name.trim());
		if (cp != null && action_plan != null)
			create(cp);
	}

	/** Create a new camera action */
	private void create(CameraPreset cp) {
		String name = createUniqueName();
		if (name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("action_plan", action_plan);
			attrs.put("preset", cp);
			attrs.put("phase", action_plan.getDefaultPhase());
			descriptor.cache.createObject(name, attrs);
		}
	}

	/** Create a unique camera action name */
	private String createUniqueName() {
		for (int uid = 1; uid <= 999; uid++) {
			String n = action_plan.getName() + "_" + uid;
			if (descriptor.cache.lookupObject(n) == null)
				return n;
		}
		return null;
	}
}
