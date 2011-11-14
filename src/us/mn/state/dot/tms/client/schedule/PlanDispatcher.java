/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2011  Minnesota Department of Transportation
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

import java.awt.GridBagConstraints;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsAction;
import us.mn.state.dot.tms.DmsActionHelper;
import us.mn.state.dot.tms.LaneAction;
import us.mn.state.dot.tms.LaneActionHelper;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.MeterAction;
import us.mn.state.dot.tms.MeterActionHelper;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.toast.FormPanel;

/**
 * A plan dispatcher is a GUI panel for dispatching action plans
 *
 * @author Douglas Lau
 */
public class PlanDispatcher extends FormPanel
	implements ProxyListener<ActionPlan>, ProxySelectionListener<ActionPlan>
{
	/** Name component */
	private final JTextField nameTxt = createTextField();

	/** Description component */
	private final JTextField descriptionTxt = createTextField();

	/** DMS count component */
	private final JTextField dmsTxt = createTextField();

	/** Lane count component */
	private final JTextField laneTxt = createTextField();

	/** Meter count component */
	private final JTextField meterTxt = createTextField();

	/** Plan phase combo box */
	private final JComboBox phaseCmb = new JComboBox();

	/** Current session */
	private final Session session;

	/** Action plan manager */
	private final PlanManager manager;

	/** Selection model */
	private final ProxySelectionModel<ActionPlan> selectionModel;

	/** Action plan proxy cache */
	private final TypeCache<ActionPlan> cache;

	/** Selected action plan */
	private ActionPlan selected = null;

	/** Create a new plan dispatcher */
	public PlanDispatcher(Session s, PlanManager m) {
		super(true);
		session = s;
		manager = m;
		selectionModel = manager.getSelectionModel();
		cache = session.getSonarState().getActionPlans();
		setTitle("Selected Action Plan");
		setEnabled(false);
		add(new JLabel("Name"));
		bag.weightx = 0.4f;
		bag.weighty = 0.4f;
		setWest();
		add(nameTxt);
		bag.weightx = 0.6f;
		bag.weighty = 0.6f;
		addRow(new JLabel(""));
		add(new JLabel("Description"));
		setFill();
		setWidth(2);
		add(descriptionTxt);
		finishRow();
		add("DMS", dmsTxt);
		finishRow();
		add("Lane Markings", laneTxt);
		finishRow();
		add("Ramp Meters", meterTxt);
		finishRow();
		add("Phase", phaseCmb);
		finishRow();
		setSelected(null);
		cache.addProxyListener(this);
		selectionModel.addProxySelectionListener(this);
	}

	/** Dispose of the panel */
	public void dispose() {
		selectionModel.removeProxySelectionListener(this);
		cache.removeProxyListener(this);
		setSelected(null);
		super.dispose();
	}

	/** A new proxy has been added */
	public void proxyAdded(ActionPlan proxy) {
		// we're not interested
	}

	/** Enumeration of the proxy type has completed */
	public void enumerationComplete() {
		// we're not interested
	}

	/** A proxy has been removed */
	public void proxyRemoved(ActionPlan proxy) {
		if(proxy == selected) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					setSelected(null);
				}
			});
		}
	}

	/** A proxy has been changed */
	public void proxyChanged(final ActionPlan proxy, final String a) {
		if(proxy == selected) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateAttribute(proxy, a);
				}
			});
		}
	}

	/** Called whenever a plan is added to the selection */
	public void selectionAdded(ActionPlan s) {
		if(selectionModel.getSelectedCount() <= 1)
			setSelected(s);
	}

	/** Called whenever a plan is removed from the selection */
	public void selectionRemoved(ActionPlan s) {
		if(selectionModel.getSelectedCount() == 1) {
			for(ActionPlan p: selectionModel.getSelected())
				setSelected(p);
		} else if(s == selected)
			setSelected(null);
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
			nameTxt.setText("");
			descriptionTxt.setText("");
			dmsTxt.setText("");
			laneTxt.setText("");
			meterTxt.setText("");
			phaseCmb.setAction(null);
			phaseCmb.setModel(new DefaultComboBoxModel());
			phaseCmb.setSelectedItem(null);
		}
		setEnabled(canUpdate(proxy));
	}

	/** Create a combo box model for plan phases */
	private DefaultComboBoxModel createPhaseModel(final ActionPlan plan) {
		final TreeSet<PlanPhase> phases =
			new TreeSet<PlanPhase>(comparator);
		DmsActionHelper.find(new Checker<DmsAction>() {
			public boolean check(DmsAction da) {
				if(da.getActionPlan() == plan)
					phases.add(da.getPhase());
				return false;
			}
		});
		LaneActionHelper.find(new Checker<LaneAction>() {
			public boolean check(LaneAction la) {
				if(la.getActionPlan() == plan)
					phases.add(la.getPhase());
				return false;
			}
		});
		MeterActionHelper.find(new Checker<MeterAction>() {
			public boolean check(MeterAction ma) {
				if(ma.getActionPlan() == plan)
					phases.add(ma.getPhase());
				return false;
			}
		});
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		model.addElement(plan.getDefaultPhase());
		phases.remove(plan.getDefaultPhase());
		for(PlanPhase p: phases)
			model.addElement(p);
		model.setSelectedItem(plan.getPhase());
		return model;
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

	/** Enable or disable the panel */
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		phaseCmb.setEnabled(enabled);
		phaseCmb.setAction(null);
	}

	/** Update one attribute on the form */
	protected void updateAttribute(ActionPlan plan, String a) {
		if(a == null || a.equals("name"))
			nameTxt.setText(plan.getName());
		if(a == null || a.equals("description"))
			descriptionTxt.setText(plan.getDescription());
		if(a == null || a.equals("active")) {
			dmsTxt.setText(Integer.toString(countDMS(plan)));
			laneTxt.setText(Integer.toString(countLanes(plan)));
			meterTxt.setText(Integer.toString(countMeters(plan)));
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
	private int countDMS(final ActionPlan p) {
		final HashSet<SignGroup> plan_groups = new HashSet<SignGroup>();
		DmsActionHelper.find(new Checker<DmsAction>() {
			public boolean check(DmsAction da) {
				if(da.getActionPlan() == p)
					plan_groups.add(da.getSignGroup());
				return false;
			}
		});
		HashSet<DMS> plan_signs = new HashSet<DMS>();
		for(SignGroup sg: plan_groups)
			plan_signs.addAll(SignGroupHelper.find(sg));
		return plan_signs.size();
	}

	/** Get a count a lane markings controlled by an action plan */
	private int countLanes(final ActionPlan p) {
		final HashSet<LaneMarking> plan_lanes =
			new HashSet<LaneMarking>();
		LaneActionHelper.find(new Checker<LaneAction>() {
			public boolean check(LaneAction la) {
				if(la.getActionPlan() == p)
					plan_lanes.add(la.getLaneMarking());
				return false;
			}
		});
		return plan_lanes.size();
	}

	/** Get a count a ramp meters controlled by an action plan */
	private int countMeters(final ActionPlan p) {
		final HashSet<RampMeter> plan_meters =
			new HashSet<RampMeter>();
		MeterActionHelper.find(new Checker<MeterAction>() {
			public boolean check(MeterAction ma) {
				if(ma.getActionPlan() == p)
					plan_meters.add(ma.getRampMeter());
				return false;
			}
		});
		return plan_meters.size();
	}

	/** Check if the user can update the given action plan */
	private boolean canUpdate(ActionPlan plan) {
		return session.canUpdate(plan, "phase") && plan.getActive();
	}
}
