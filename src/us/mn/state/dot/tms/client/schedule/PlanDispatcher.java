/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011-2013  Minnesota Department of Transportation
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
import static us.mn.state.dot.tms.client.widget.SwingRunner.runSwing;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DmsActionHelper;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.LaneAction;
import us.mn.state.dot.tms.LaneActionHelper;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.MeterAction;
import us.mn.state.dot.tms.MeterActionHelper;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A plan dispatcher is a GUI panel for dispatching action plans
 *
 * @author Douglas Lau
 */
public class PlanDispatcher extends IPanel implements ProxyListener<ActionPlan>{

	/** Name component */
	private final JLabel name_lbl = createValueLabel();

	/** Description component */
	private final JLabel description_lbl = createValueLabel();

	/** DMS count component */
	private final JLabel dms_lbl = createValueLabel();

	/** Lane count component */
	private final JLabel lane_lbl = createValueLabel();

	/** Meter count component */
	private final JLabel meter_lbl = createValueLabel();

	/** Plan phase combo box */
	private final JComboBox phaseCmb = new JComboBox();

	/** Current session */
	private final Session session;

	/** Action plan manager */
	private final PlanManager manager;

	/** Selection model */
	private final ProxySelectionModel<ActionPlan> sel_model;

	/** Selection listener */
	private final ProxySelectionListener<ActionPlan> sel_listener =
		new ProxySelectionListener<ActionPlan>()
	{
		public void selectionAdded(ActionPlan s) {
			setSelected(getSelected());
		}
		public void selectionRemoved(ActionPlan s) {
			setSelected(getSelected());
		}
	};

	/** Action plan proxy cache */
	private final TypeCache<ActionPlan> cache;

	/** Selected action plan */
	private ActionPlan selected = null;

	/** Create a new plan dispatcher */
	public PlanDispatcher(Session s, PlanManager m) {
		session = s;
		manager = m;
		sel_model = manager.getSelectionModel();
		cache = session.getSonarState().getActionPlans();
		setTitle(I18N.get("action.plan.selected"));
		setEnabled(false);
		add("action.plan.name");
		add(name_lbl, Stretch.LAST);
		add("device.description");
		add(description_lbl, Stretch.LAST);
		add("dms");
		add(dms_lbl, Stretch.LAST);
		add("lane.markings");
		add(lane_lbl, Stretch.LAST);
		add("ramp.meter.long.plural");
		add(meter_lbl, Stretch.LAST);
		add("action.plan.phase");
		add(phaseCmb, Stretch.LAST);
		setSelected(null);
		cache.addProxyListener(this);
		sel_model.addProxySelectionListener(sel_listener);
	}

	/** Dispose of the panel */
	@Override
	public void dispose() {
		sel_model.removeProxySelectionListener(sel_listener);
		cache.removeProxyListener(this);
		setSelected(null);
		super.dispose();
	}

	/** A new proxy has been added */
	@Override
	public void proxyAdded(ActionPlan proxy) {
		// we're not interested
	}

	/** Enumeration of the proxy type has completed */
	@Override
	public void enumerationComplete() {
		// we're not interested
	}

	/** A proxy has been removed */
	@Override
	public void proxyRemoved(ActionPlan proxy) {
		if(proxy == selected) {
			runSwing(new Runnable() {
				public void run() {
					setSelected(null);
				}
			});
		}
	}

	/** A proxy has been changed */
	@Override
	public void proxyChanged(final ActionPlan proxy, final String a) {
		if(proxy == selected) {
			runSwing(new Runnable() {
				public void run() {
					updateAttribute(proxy, a);
				}
			});
		}
	}

	/** Get the selected action plan */
	private ActionPlan getSelected() {
		if(sel_model.getSelectedCount() == 1) {
			for(ActionPlan ap: sel_model.getSelected())
				return ap;
		}
		return null;
	}

	/** Select a action plan to display */
	public void setSelected(ActionPlan proxy) {
		if(selected != null)
			cache.ignoreObject(selected);
		if(proxy != null)
			cache.watchObject(proxy);
		selected = proxy;
		if(proxy != null) {
			phaseCmb.setAction(null);
			phaseCmb.setModel(createPhaseModel(proxy));
			updateAttribute(proxy, null);
		} else {
			name_lbl.setText("");
			description_lbl.setText("");
			dms_lbl.setText("");
			lane_lbl.setText("");
			meter_lbl.setText("");
			phaseCmb.setAction(null);
			phaseCmb.setModel(new DefaultComboBoxModel());
			phaseCmb.setSelectedItem(null);
		}
		setEnabled(isUpdatePermitted(proxy));
	}

	/** Create a combo box model for plan phases */
	private DefaultComboBoxModel createPhaseModel(final ActionPlan plan) {
		TreeSet<PlanPhase> phases = createPhaseSet(plan);
		removeNextPhases(phases);
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		model.addElement(plan.getDefaultPhase());
		phases.remove(plan.getDefaultPhase());
		for(PlanPhase p: phases)
			model.addElement(p);
		model.setSelectedItem(plan.getPhase());
		return model;
	}

	/** Create a set of phases for an action plan */
	private TreeSet<PlanPhase> createPhaseSet(final ActionPlan plan) {
		final TreeSet<PlanPhase> phases =
			new TreeSet<PlanPhase>(comparator);
		Iterator<DmsAction> dit = DmsActionHelper.iterator();
		while(dit.hasNext()) {
			DmsAction da = dit.next();
			if(da.getActionPlan() == plan)
				phases.add(da.getPhase());
		}
		Iterator<LaneAction> lit = LaneActionHelper.iterator();
		while(lit.hasNext()) {
			LaneAction la = lit.next();
			if(la.getActionPlan() == plan)
				phases.add(la.getPhase());
		}
		Iterator<MeterAction> mit = MeterActionHelper.iterator();
		while(mit.hasNext()) {
			MeterAction ma = mit.next();
			if(ma.getActionPlan() == plan)
				phases.add(ma.getPhase());
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
		for(PlanPhase p: phases) {
			PlanPhase np = p.getNextPhase();
			if(np != null)
				n_phases.add(np);
		}
		phases.removeAll(n_phases);
	}

	/** Enable or disable the panel */
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		phaseCmb.setEnabled(enabled);
		phaseCmb.setAction(null);
	}

	/** Update one attribute on the form */
	protected void updateAttribute(ActionPlan plan, String a) {
		if(a == null || a.equals("name"))
			name_lbl.setText(plan.getName());
		if(a == null || a.equals("description"))
			description_lbl.setText(plan.getDescription());
		if(a == null || a.equals("active")) {
			dms_lbl.setText(Integer.toString(countDMS(plan)));
			lane_lbl.setText(Integer.toString(countLanes(plan)));
			meter_lbl.setText(Integer.toString(countMeters(plan)));
		}
		if(a == null || a.equals("phase")) {
			phaseCmb.setAction(null);
			ComboBoxModel mdl = phaseCmb.getModel();
			// We must call setSelectedItem on the model, because
			// it might not contain the phase.  In that case,
			// calling JComboBox.setSelectedItem will fail.
			if(mdl instanceof DefaultComboBoxModel) {
				DefaultComboBoxModel model =
					(DefaultComboBoxModel)mdl;
				model.setSelectedItem(plan.getPhase());
			}
			phaseCmb.setAction(new ChangePhaseAction(plan,
				phaseCmb));
		}
	}

	/** Get a count of DMS controlled by an action plan */
	private int countDMS(ActionPlan p) {
		HashSet<SignGroup> plan_groups = new HashSet<SignGroup>();
		Iterator<DmsAction> dit = DmsActionHelper.iterator();
		while(dit.hasNext()) {
			DmsAction da = dit.next();
			if(da.getActionPlan() == p)
				plan_groups.add(da.getSignGroup());
		}
		HashSet<DMS> plan_signs = new HashSet<DMS>();
		Iterator<DmsSignGroup> git = DmsSignGroupHelper.iterator();
		while(git.hasNext()) {
			DmsSignGroup dsg = git.next();
			if(plan_groups.contains(dsg.getSignGroup()))
				plan_signs.add(dsg.getDms());
		}
		return plan_signs.size();
	}

	/** Get a count a lane markings controlled by an action plan */
	private int countLanes(ActionPlan p) {
		HashSet<LaneMarking> plan_lanes = new HashSet<LaneMarking>();
		Iterator<LaneAction> lit = LaneActionHelper.iterator();
		while(lit.hasNext()) {
			LaneAction la = lit.next();
			if(la.getActionPlan() == p)
				plan_lanes.add(la.getLaneMarking());
		}
		return plan_lanes.size();
	}

	/** Get a count a ramp meters controlled by an action plan */
	private int countMeters(ActionPlan p) {
		HashSet<RampMeter> plan_meters = new HashSet<RampMeter>();
		Iterator<MeterAction> mit = MeterActionHelper.iterator();
		while(mit.hasNext()) {
			MeterAction ma = mit.next();
			if(ma.getActionPlan() == p)
				plan_meters.add(ma.getRampMeter());
		}
		return plan_meters.size();
	}

	/** Check if the user is permitted to update the given action plan */
	private boolean isUpdatePermitted(ActionPlan plan) {
		return session.isUpdatePermitted(plan, "phase") &&
		       plan.getActive();
	}
}
