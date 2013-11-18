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

import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.client.proxy.ProxyAction;

/**
 * This action changes the phase of an action plan.
 *
 * @author Douglas Lau
 */
public class ChangePhaseAction extends ProxyAction<ActionPlan> {

	/** Combo box component */
	private final JComboBox cmb;

	/** Create a new action to change the phase of an action plan */
	public ChangePhaseAction(ActionPlan p, JComboBox c) {
		super("action.plan.phase.change", p);
		cmb = c;
	}

	/** Actually perform the action */
	protected void doActionPerformed(ActionEvent e) {
		Object s = (Object)cmb.getSelectedItem();
		if(s instanceof PlanPhase)
			proxy.setPhase((PlanPhase)s);
	}
}
