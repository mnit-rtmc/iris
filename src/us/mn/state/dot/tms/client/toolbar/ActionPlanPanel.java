/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toolbar;

import javax.swing.JComboBox;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * A tool panel that deploys action plans.
 *
 * @author Douglas Lau
 */
public class ActionPlanPanel extends ToolPanel {

	/** Combo box for all action plans */
	protected final JComboBox combo_box = new JComboBox();

	/** Model for action plan combo box */
	protected final ActionPlanComboModel model;

	/** Create an action plan panel */
	public ActionPlanPanel(Session s) {
		model = new ActionPlanComboModel(s, this);
		combo_box.setModel(model);
		add(combo_box);
		add(model.getCheckBox());
	}

	/** Dispose of the panel */
	public void dispose() {
		model.dispose();
	}

	/** Is this panel IRIS enabled? */
	public static boolean getIEnabled() {
		return SystemAttrEnum.
			ACTIONPLAN_TOOLBAR_ENABLE.getBoolean();
	}

	/** Set the tool tip text */
	public void setToolTipText(String t) {
		super.setToolTipText(t);
		combo_box.setToolTipText(t);
	}
}
