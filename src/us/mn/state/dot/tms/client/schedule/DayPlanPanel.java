/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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
import javax.swing.JCheckBox;
import us.mn.state.dot.tms.DayPlan;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A panel for editing day plans.
 *
 * @author Douglas Lau
 */
public class DayPlanPanel extends ProxyTablePanel<DayPlan> {

	/** Holidays checkbox */
	private final JCheckBox holidays_chk = new JCheckBox(
		I18N.get("day.plan.holidays")
	);

	/** Create a new day plan panel */
	public DayPlanPanel(Session s) {
		super(new DayPlanModel(s));
	}

	/** Add create/delete widgets to the button panel */
	@Override
	protected void addCreateDeleteWidgets(GroupLayout.SequentialGroup hg,
		GroupLayout.ParallelGroup vg)
	{
		hg.addComponent(holidays_chk);
		vg.addComponent(holidays_chk);
		hg.addGap(UI.hgap);
		super.addCreateDeleteWidgets(hg, vg);
	}

	/** Update the button panel */
	@Override
	public void updateButtonPanel() {
		boolean e = model.canAdd();
		holidays_chk.setEnabled(e);
		holidays_chk.setSelected(true);
		super.updateButtonPanel();
	}

	/** Create a new proxy object */
	@Override
	protected void createObject() {
		DayPlanModel mdl = getDayPlanModel();
		if (mdl != null) {
			String name = add_txt.getText().trim();
			add_txt.setText("");
			mdl.createObject(name, holidays_chk.isSelected());
		}
		updateButtonPanel();
	}

	/** Get the day plan model */
	private DayPlanModel getDayPlanModel() {
		ProxyTableModel<DayPlan> mdl = model;
		return (mdl instanceof DayPlanModel)
		     ? (DayPlanModel) mdl
		     : null;
	}
}
