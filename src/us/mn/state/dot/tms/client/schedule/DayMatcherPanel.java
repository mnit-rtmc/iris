/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.DayMatcher;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A panel for displaying a table of day matchers.
 *
 * @author Douglas Lau
 */
public class DayMatcherPanel extends ProxyTablePanel<DayMatcher> {

	/** Month selector */
	private final JComboBox<String> month_cbx;

	/** Day selector */
	private final JComboBox<String> day_cbx;

	/** Weekday selector */
	private final JComboBox<String> weekday_cbx;

	/** Week selector */
	private final JComboBox<String> week_cbx;

	/** Shift selector */
	private final JComboBox<String> shift_cbx;

	/** Create a new day matcher panel */
	public DayMatcherPanel(Session s) {
		super(new DayMatcherModel(s, null));
		month_cbx = DayMatcherModel.monthSelector();
		day_cbx = DayMatcherModel.daySelector();
		weekday_cbx = DayMatcherModel.weekdaySelector();
		week_cbx = DayMatcherModel.weekSelector();
		shift_cbx = DayMatcherModel.shiftSelector();
		shift_cbx.setSelectedIndex(2);
	}

	/** Initialise the panel */
	@Override
	public void initialize() {
		super.initialize();
		updateButtonPanel();
	}

	/** Add create/delete widgets to the button panel */
	@Override
	protected void addCreateDeleteWidgets(GroupLayout.SequentialGroup hg,
		GroupLayout.ParallelGroup vg)
	{
		hg.addComponent(month_cbx);
		vg.addComponent(month_cbx);
		hg.addGap(UI.hgap);
		hg.addComponent(day_cbx);
		vg.addComponent(day_cbx);
		hg.addGap(UI.hgap);
		hg.addComponent(weekday_cbx);
		vg.addComponent(weekday_cbx);
		hg.addGap(UI.hgap);
		hg.addComponent(week_cbx);
		vg.addComponent(week_cbx);
		hg.addGap(UI.hgap);
		hg.addComponent(shift_cbx);
		vg.addComponent(shift_cbx);
		hg.addGap(UI.hgap);
		super.addCreateDeleteWidgets(hg, vg);
	}

	/** Update the button panel */
	@Override
	public void updateButtonPanel() {
		boolean e = model.canAdd();
		month_cbx.setEnabled(e);
		month_cbx.setSelectedIndex(0);
		day_cbx.setEnabled(e);
		day_cbx.setSelectedIndex(0);
		weekday_cbx.setEnabled(e);
		weekday_cbx.setSelectedIndex(0);
		week_cbx.setEnabled(e);
		week_cbx.setSelectedIndex(0);
		shift_cbx.setEnabled(e);
		shift_cbx.setSelectedIndex(2);
		super.updateButtonPanel();
	}

	/** Create a new proxy object */
	@Override
	protected void createObject() {
		DayMatcherModel mdl = getDayMatcherModel();
		if (mdl != null) {
			mdl.createObject(
				(String) month_cbx.getSelectedItem(),
				(String) day_cbx.getSelectedItem(),
				(String) weekday_cbx.getSelectedItem(),
				(String) week_cbx.getSelectedItem(),
				(String) shift_cbx.getSelectedItem()
			);
		}
		updateButtonPanel();
	}

	/** Get the day matcher model */
	private DayMatcherModel getDayMatcherModel() {
		ProxyTableModel<DayMatcher> mdl = model;
		return (mdl instanceof DayMatcherModel)
		     ? (DayMatcherModel) mdl
		     : null;
	}
}
