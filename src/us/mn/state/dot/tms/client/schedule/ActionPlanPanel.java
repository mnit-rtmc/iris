/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  Minnesota Department of Transportation
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

import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.widget.ILabel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.client.widget.WrapperComboBoxModel;

/**
 * A panel for displaying a table of action plans.
 *
 * @author Douglas Lau
 */
public class ActionPlanPanel extends ProxyTablePanel<ActionPlan> {

	/** Plan phase model */
	private final ProxyListModel<PlanPhase> phase_model;

	/** Plan phase label */
	private final ILabel phase_lbl =new ILabel("action.plan.default.phase");

	/** Plan phase combo box */
	private final JComboBox phase_cbx = new JComboBox();

	/** Create a new action plan panel */
	public ActionPlanPanel(Session s) {
		super(new ActionPlanModel(s));
		phase_model = s.getSonarState().getPhaseModel();
	}

	/** Initialise the panel */
	@Override
	public void initialize() {
		super.initialize();
		phase_cbx.setModel(new WrapperComboBoxModel(phase_model));
		phase_lbl.setEnabled(model.canAdd());
		phase_cbx.setEnabled(model.canAdd());
	}

	/** Add extra widgets to the button panel */
	@Override
	protected void addExtraWidgets(GroupLayout.SequentialGroup hg,
		GroupLayout.ParallelGroup vg)
	{
		hg.addComponent(phase_lbl);
		vg.addComponent(phase_lbl);
		hg.addGap(UI.hgap);
		hg.addComponent(phase_cbx);
		vg.addComponent(phase_cbx);
		hg.addGap(UI.hgap);
		super.addExtraWidgets(hg, vg);
	}

	/** Create a new proxy object */
	@Override
	protected void createObject() {
		ActionPlanModel mdl = getActionPlanModel();
		if (mdl != null) {
			PlanPhase p = getSelectedPhase();
			if (p != null) {
				String name = add_txt.getText().trim();
				mdl.create(name, p);
			}
		}
		add_txt.setText("");
		phase_cbx.setSelectedItem(null);
	}

	/** Get the action plan model */
	private ActionPlanModel getActionPlanModel() {
		ProxyTableModel<ActionPlan> mdl = model;
		return (mdl instanceof ActionPlanModel)
		     ? (ActionPlanModel)mdl
		     : null;
	}

	/** Get the selected phase */
	private PlanPhase getSelectedPhase() {
		Object o = phase_cbx.getSelectedItem();
		return (o instanceof PlanPhase) ? (PlanPhase)o : null;
	}
}
