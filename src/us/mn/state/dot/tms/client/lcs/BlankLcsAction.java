/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2013  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.widget.IAction;

/**
 * Action to blank all selected LCS arrays.
 *
 * @author Douglas Lau
 */
public class BlankLcsAction extends IAction {

	/** Selection model */
	private final ProxySelectionModel<LCSArray> selectionModel;

	/** User who is sending message */
	private final User owner;

	/** Create a new action to blank the selected LCS array */
	public BlankLcsAction(ProxySelectionModel<LCSArray> s, User o) {
		super("lcs.blank");
		selectionModel = s;
		owner = o;
	}

	/** Actually perform the action */
	protected void doActionPerformed(ActionEvent e) {
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
