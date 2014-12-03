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

import java.util.Date;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.TimeAction;
import us.mn.state.dot.tms.TimeActionHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel2;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.widget.ILabel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.client.widget.WrapperComboBoxModel;

/**
 * A panel for displaying a table of time actions.
 *
 * @author Douglas Lau
 */
public class TimeActionPanel extends ProxyTablePanel<TimeAction> {

	/** Day plan model */
	private final ProxyListModel<DayPlan> day_plan_mdl;

	/** Day plan label */
	private final ILabel day_plan_lbl = new ILabel("action.plan.day");

	/** Day plan combo box */
	private final JComboBox day_plan_cbx = new JComboBox();

	/** Date label */
	private final ILabel date_lbl = new ILabel("action.plan.date");

	/** Date text field */
	private final JTextField date_txt = new JTextField(10);

	/** Time-of-day label */
	private final ILabel time_lbl = new ILabel("action.plan.time");

	/** Time-of-day text field */
	private final JTextField time_txt = new JTextField(12);

	/** Create a new time action panel */
	public TimeActionPanel(Session s) {
		super(new TimeActionModel(s, null));
		day_plan_mdl = s.getSonarState().getDayModel();
	}

	/** Initialise the panel */
	@Override
	public void initialize() {
		day_plan_lbl.setEnabled(false);
		day_plan_cbx.setEnabled(false);
		date_lbl.setEnabled(false);
		time_lbl.setEnabled(false);
		super.initialize();
		day_plan_cbx.setModel(new WrapperComboBoxModel(day_plan_mdl));
		date_txt.setAction(add_proxy);
		time_txt.setAction(add_proxy);
	}

	/** Set the model */
	@Override
	public void setModel(ProxyTableModel2<TimeAction> m) {
		super.setModel(m);
		boolean e = m.canAdd();
		day_plan_lbl.setEnabled(e);
		day_plan_cbx.setEnabled(e);
		date_lbl.setEnabled(e);
		time_lbl.setEnabled(e);
	}

	/** Add extra widgets to the button panel */
	@Override
	protected void addExtraWidgets(GroupLayout.SequentialGroup hg,
		GroupLayout.ParallelGroup vg)
	{
		hg.addComponent(day_plan_lbl);
		vg.addComponent(day_plan_lbl);
		hg.addGap(UI.hgap);
		hg.addComponent(day_plan_cbx);
		vg.addComponent(day_plan_cbx);
		hg.addGap(UI.hgap);
		hg.addComponent(date_lbl);
		vg.addComponent(date_lbl);
		hg.addGap(UI.hgap);
		hg.addComponent(date_txt);
		vg.addComponent(date_txt);
		hg.addGap(UI.hgap);
		hg.addComponent(time_lbl);
		vg.addComponent(time_lbl);
		hg.addGap(UI.hgap);
		hg.addComponent(time_txt);
		vg.addComponent(time_txt);
		hg.addGap(UI.hgap);
	}

	/** Create a new proxy object */
	@Override
	protected void createObject() {
		TimeActionModel mdl = getTimeActionModel();
		if (mdl != null) {
			String st = getSelectedTime();
			if (st != null) {
				DayPlan dp = getSelectedDayPlan();
				String sd = getSelectedDate();
				mdl.createObject(dp, sd, st);
			}
		}
		day_plan_cbx.setSelectedItem(null);
		date_txt.setText("");
		time_txt.setText("");
	}

	/** Get the time action model */
	private TimeActionModel getTimeActionModel() {
		ProxyTableModel2<TimeAction> mdl = model;
		return (mdl instanceof TimeActionModel)
		     ? (TimeActionModel)mdl
		     : null;
	}

	/** Get the selected time formatted as a string */
	private String getSelectedTime() {
		Date td = TimeActionHelper.parseTime(time_txt.getText());
		return TimeActionHelper.formatTime(td);
	}

	/** Get the selected day plan */
	private DayPlan getSelectedDayPlan() {
		Object o = day_plan_cbx.getSelectedItem();
		return (o instanceof DayPlan) ? (DayPlan)o : null;
	}

	/** Get the selected date formatted as a string */
	private String getSelectedDate() {
		Date sd = TimeActionHelper.parseDate(date_txt.getText());
		return TimeActionHelper.formatDate(sd);
	}
}
