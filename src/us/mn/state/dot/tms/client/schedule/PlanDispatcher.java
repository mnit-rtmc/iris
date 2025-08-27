/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2025  Minnesota Department of Transportation
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

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.ActionPlanHelper;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.DeviceAction;
import us.mn.state.dot.tms.DeviceActionHelper;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A plan dispatcher is a GUI panel for dispatching action plans
 *
 * @author Douglas Lau
 */
public class PlanDispatcher extends IPanel implements ProxyView<ActionPlan> {

	/** Name component */
	private final JLabel name_lbl = createValueLabel();

	/** Notes component */
	private final JLabel notes_lbl = createValueLabel();

	/** DMS count component */
	private final JLabel dms_lbl = createValueLabel();

	/** Beacon count component */
	private final JLabel beacon_lbl = createValueLabel();

	/** Camera count component */
	private final JLabel camera_lbl = createValueLabel();

	/** Meter count component */
	private final JLabel meter_lbl = createValueLabel();

	/** Plan phase combo box */
	private final JComboBox<PlanPhase> phase_cbx =
		new JComboBox<PlanPhase>();

	/** Current session */
	private final Session session;

	/** Action plan manager */
	private final PlanManager manager;

	/** Selection model */
	private final ProxySelectionModel<ActionPlan> sel_mdl;

	/** Selection listener */
	private final ProxySelectionListener sel_listener =
		new ProxySelectionListener()
	{
		public void selectionChanged() {
			setSelected(sel_mdl.getSingleSelection());
		}
	};

	/** Proxy watcher */
	private final ProxyWatcher<ActionPlan> watcher;

	/** Create a new plan dispatcher */
	public PlanDispatcher(Session s, PlanManager m) {
		session = s;
		manager = m;
		sel_mdl = manager.getSelectionModel();
		TypeCache<ActionPlan> cache =s.getSonarState().getActionPlans();
		watcher = new ProxyWatcher<ActionPlan>(cache, this, true);
	}

	/** Initialize the widgets on the panel */
	@Override
	public void initialize() {
		super.initialize();
		setTitle(I18N.get("action.plan.selected"));
		add("action.plan.name");
		add(name_lbl, Stretch.LAST);
		add("device.notes");
		add(notes_lbl, Stretch.LAST);
		add("dms");
		add(dms_lbl, Stretch.LAST);
		add("beacon.title");
		add(beacon_lbl, Stretch.LAST);
		add("camera.title");
		add(camera_lbl, Stretch.LAST);
		add("ramp_meter.title");
		add(meter_lbl, Stretch.LAST);
		add("action.plan.phase");
		add(phase_cbx, Stretch.LAST);
		watcher.initialize();
		clear();
		sel_mdl.addProxySelectionListener(sel_listener);
	}

	/** Dispose of the panel */
	@Override
	public void dispose() {
		watcher.dispose();
		sel_mdl.removeProxySelectionListener(sel_listener);
		clear();
		super.dispose();
	}

	/** Set the selected action plan */
	public void setSelected(ActionPlan ap) {
		watcher.setProxy(ap);
	}

	/** Called when all proxies have been enumerated (from ProxyView). */
	@Override
	public void enumerationComplete() { }

	/** Update one attribute on the form */
	@Override
	public void update(ActionPlan ap, String a) {
		if (a == null) {
			phase_cbx.setAction(null);
			phase_cbx.setModel(createPhaseModel(ap));
			phase_cbx.setEnabled(isWritePermitted(ap));
		}
		if (a == null || a.equals("name"))
			name_lbl.setText(ap.getName());
		if (a == null || a.equals("notes")) {
			String n = ap.getNotes();
			notes_lbl.setText((n != null) ? n : "");
		}
		if (a == null || a.equals("active")) {
			dms_lbl.setText(countDMS(ap));
			beacon_lbl.setText(countBeacons(ap));
			camera_lbl.setText(countCameras(ap));
			meter_lbl.setText(countRampMeters(ap));
		}
		if (a == null || a.equals("phase")) {
			phase_cbx.setAction(null);
			ComboBoxModel mdl = phase_cbx.getModel();
			// We must call setSelectedItem on the model, because
			// it might not contain the phase.  In that case,
			// calling JComboBox.setSelectedItem will fail.
			if (mdl instanceof DefaultComboBoxModel) {
				DefaultComboBoxModel dcm =
					(DefaultComboBoxModel) mdl;
				dcm.setSelectedItem(ap.getPhase());
			}
			phase_cbx.setAction(new ChangePhaseAction(ap,
				phase_cbx));
		}
	}

	/** Create a combo box model for plan phases */
	private ComboBoxModel<PlanPhase> createPhaseModel(final ActionPlan ap) {
		TreeSet<PlanPhase> phases = createPhaseSet(ap);
		removeNextPhases(phases);
		DefaultComboBoxModel<PlanPhase> mdl = new DefaultComboBoxModel
			<PlanPhase>();
		mdl.addElement(ap.getDefaultPhase());
		phases.remove(ap.getDefaultPhase());
		for (PlanPhase p: phases)
			mdl.addElement(p);
		mdl.setSelectedItem(ap.getPhase());
		return mdl;
	}

	/** Create a set of phases for an action plan */
	private TreeSet<PlanPhase> createPhaseSet(final ActionPlan ap) {
		final TreeSet<PlanPhase> phases =
			new TreeSet<PlanPhase>(comparator);
		Iterator<DeviceAction> dit = DeviceActionHelper.iterator(ap);
		while (dit.hasNext()) {
			DeviceAction da = dit.next();
			phases.add(da.getPhase());
		}
		return phases;
	}

	/** Comparator for plan phases */
	private final Comparator<PlanPhase> comparator =
		new Comparator<PlanPhase>()
	{
		public int compare(PlanPhase a, PlanPhase b) {
			String aa = a.getName();
			String bb = b.getName();
			return aa.compareTo(bb);
		}
	};

	/** Remove phases which are "next" phases */
	private void removeNextPhases(TreeSet<PlanPhase> phases) {
		TreeSet<PlanPhase> n_phases =
			new TreeSet<PlanPhase>(comparator);
		for (PlanPhase p: phases) {
			PlanPhase np = p.getNextPhase();
			if (np != null)
				n_phases.add(np);
		}
		phases.removeAll(n_phases);
	}

	/** Get a count a beacons controlled by an action plan */
	private String countBeacons(ActionPlan ap) {
		return Integer.toString(ActionPlanHelper.countBeacons(ap));
	}

	/** Get a count of cameras controlled by an action plan */
	private String countCameras(ActionPlan ap) {
		return Integer.toString(ActionPlanHelper.countCameras(ap));
	}

	/** Get a count of DMS controlled by an action plan */
	private String countDMS(ActionPlan ap) {
		return Integer.toString(ActionPlanHelper.countDms(ap));
	}

	/** Get a count a ramp meters controlled by an action plan */
	private String countRampMeters(ActionPlan ap) {
		return Integer.toString(ActionPlanHelper.countRampMeters(ap));
	}

	/** Check if the user is permitted to update the given action plan */
	private boolean isWritePermitted(ActionPlan ap) {
		return session.isWritePermitted(ap, "phase") && ap.getActive();
	}

	/** Clear the proxy view */
	@Override
	public void clear() {
		name_lbl.setText("");
		notes_lbl.setText("");
		dms_lbl.setText("");
		beacon_lbl.setText("");
		camera_lbl.setText("");
		meter_lbl.setText("");
		phase_cbx.setAction(null);
		phase_cbx.setModel(new DefaultComboBoxModel<PlanPhase>());
		phase_cbx.setSelectedItem(null);
		phase_cbx.setEnabled(false);
	}
}
