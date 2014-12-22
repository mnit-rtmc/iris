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
package us.mn.state.dot.tms.client.comm;

import java.awt.event.ActionEvent;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import us.mn.state.dot.tms.CommLink;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.CtrlCondition;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.ILabel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A panel for displaying a table of controllers.
 *
 * @author Douglas Lau
 */
public class ControllerPanel extends ProxyTablePanel<Controller> {

	/** Condition filter label */
	private final ILabel cond_lbl = new ILabel(
		"controller.condition.filter");

	/** Condition filter action */
	private final IAction cond_act = new IAction(
		"controller.condition")
	{
		protected void doActionPerformed(ActionEvent e) {
			Object v = cond_cbx.getSelectedItem();
			if (v instanceof CtrlCondition)
				setCondition((CtrlCondition)v);
			else
				setCondition(null);
		}
	};

	/** Condition combobox */
	private final JComboBox cond_cbx;

	/** Comm filter label */
	private final ILabel comm_lbl = new ILabel("controller.comm.filter");

	/** Comm filter action */
	private final IAction comm_act = new IAction("controller.comm") {
		protected void doActionPerformed(ActionEvent e) {
			Object v = comm_cbx.getSelectedItem();
			if (v instanceof CommState)
				setCommState((CommState)v);
			else
				setCommState(null);
		}
	};

	/** Comm state combo box */
	private final JComboBox comm_cbx;

	/** Create a new controller panel */
	public ControllerPanel(Session s) {
		super(new ControllerTableModel(s));
		cond_cbx = new JComboBox(CtrlCondition.values_with_null());
		comm_cbx = new JComboBox(CommState.values_with_null());
	}

	/** Initialize the panel */
	@Override
	public void initialize() {
		super.initialize();
		cond_cbx.setAction(cond_act);
		comm_cbx.setRenderer(new CommListRenderer());
		comm_cbx.setAction(comm_act);
	}

	/** Add create/delete widgets to the button panel */
	@Override
	protected void addCreateDeleteWidgets(GroupLayout.SequentialGroup hg,
		GroupLayout.ParallelGroup vg)
	{
		hg.addComponent(cond_lbl);
		vg.addComponent(cond_lbl);
		hg.addGap(UI.hgap);
		hg.addComponent(cond_cbx);
		vg.addComponent(cond_cbx);
		hg.addGap(UI.hgap);
		hg.addComponent(comm_lbl);
		vg.addComponent(comm_lbl);
		hg.addGap(UI.hgap);
		hg.addComponent(comm_cbx);
		vg.addComponent(comm_cbx);
		hg.addGap(UI.hgap);
		super.addCreateDeleteWidgets(hg, vg);
	}

	/** Set comm link filter */
	public void setCommLink(CommLink cl) {
		if (model instanceof ControllerTableModel) {
			ControllerTableModel mdl = (ControllerTableModel)model;
			mdl.setCommLink(cl);
			updateSortFilter();
		}
	}

	/** Set condition filter */
	private void setCondition(CtrlCondition c) {
		if (model instanceof ControllerTableModel) {
			ControllerTableModel mdl = (ControllerTableModel)model;
			mdl.setCondition(c);
			updateSortFilter();
		}
	}

	/** Set comm state filter */
	private void setCommState(CommState cs) {
		if (model instanceof ControllerTableModel) {
			ControllerTableModel mdl = (ControllerTableModel)model;
			mdl.setCommState(cs);
			updateSortFilter();
		}
	}
}
