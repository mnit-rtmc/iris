/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.client.sonar.ProxySelectionModel;

/**
 * Action to clear all selected LCS arrays.
 *
 * @author Douglas Lau
 */
public class ClearLcsAction extends AbstractAction {

	/** Selection model */
	protected final ProxySelectionModel<LCSArray> selectionModel;

	/** User who is sending message */
	protected final User owner;

	/** Create a new action to clear the selected LCS array */
	public ClearLcsAction(ProxySelectionModel<LCSArray> s, User o) {
		selectionModel = s;
		owner = o;
		putValue(Action.NAME, "Clear");
		putValue(Action.SHORT_DESCRIPTION, "Clear the selected LCS");
	}

	/** Schedule the action to be performed */
	public void actionPerformed(ActionEvent e) {
		new AbstractJob() {
			public void perform() {
				do_perform();
			}
		}.addToScheduler();
	}

	/** Actually perform the action */
	protected void do_perform() {
		for(LCSArray lcs_array: selectionModel.getSelected()) {
			Integer[] ind = lcs_array.getIndicationsCurrent();
			ind = new Integer[ind.length];
			for(int i = 0; i < ind.length; i++)
				ind[i] = LaneUseIndication.DARK.ordinal();
			lcs_array.setOwnerNext(owner);
			lcs_array.setIndicationsNext(ind);
		}
	}
}
