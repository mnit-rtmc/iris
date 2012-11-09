/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dinrelay;

import java.util.Arrays;
import java.util.Iterator;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.LCSIndication;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.MessagePoller;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to send new indicaitons to a Lane Control Signal array.
 *
 * @author Douglas Lau
 */
public class OpSendLCSIndications extends OpLCS {

	/** Indications to send */
	private final Integer[] indications;

	/** User who sent the indications */
	private final User user;

	/** Flag to indicate all helper ops succeeded */
	private boolean op_success = true;

	/** Count of remaining operations */
	private int n_ops = 0;

	/** Create a new operation to send LCS indications */
	public OpSendLCSIndications(LCSArrayImpl l, Integer[] ind, User u) {
		super(PriorityLevel.COMMAND, l);
		indications = ind;
		user = u;
	}

	/** Operation equality test */
	public boolean equals(Object o) {
		if(o instanceof OpSendLCSIndications) {
			OpSendLCSIndications op = (OpSendLCSIndications)o;
			return lcs_array == op.lcs_array &&
			       Arrays.equals(indications, op.indications);
		} else
			return false;
	}

	/** Create the second phase of the operation */
	protected Phase<DinRelayProperty> phaseTwo() {
		return new CreateOutletCommands();
	}

	/** Phase to create operations to command outlet status */
	private class CreateOutletCommands extends Phase<DinRelayProperty> {

		/** Create the outlet query operations */
		protected Phase<DinRelayProperty> poll(
			CommMessage<DinRelayProperty> mess)
		{
			Iterator<ControllerImpl> it = controllers.iterator();
			synchronized(this) {
				while(it.hasNext())
					createOutletOp(it.next());
			}
			return null;
		}
	}

	/** Create one outlet command operation */
	private void createOutletOp(final ControllerImpl c) {
		MessagePoller mp = c.getPoller();
		if(mp instanceof DinRelayPoller) {
			DinRelayPoller drp = (DinRelayPoller)mp;
			drp.commandOutlets(c, getOutlets(c), new OutletProperty(
				new OutletProperty.OutletCallback()
			{
				public void updateOutlets(boolean[] outlets) {
					// not needed for command
				}
				public void complete(boolean success) {
					opComplete(success);
				}
			}));
			n_ops++;
		} else
			op_success = false;
	}

	/** Get the outlet command state for one controller.
	 * @param c Controller being updated.
	 * @return outlets Outlet state to command for controller. */
	private boolean[] getOutlets(final ControllerImpl c) {
		final boolean[] outlets = new boolean[8];
		lcs_array.findIndications(new Checker<LCSIndication>() {
			public boolean check(LCSIndication li) {
				if(li.getController() == c)
					updateIndication(li, outlets);
				return false;
			}
		});
		return outlets;
	}

	/** Update one indication value (if set).
	 * @param li LCS indication pin association.
	 * @param outlets Array of outlet states, indexed by pin */
	private void updateIndication(LCSIndication li, boolean[] outlets) {
		int i = li.getLcs().getLane() - 1;
		// We must check bounds here in case the LCSIndication
		// was added after the "indications" array was created
		if(i >= 0 && i < indications.length) {
			if(indications[i] == li.getIndication()) {
				int p = li.getPin() - 1;
				if(p >= 0 && p < outlets.length)
					outlets[p] = true;
			}
		}
	}

	/** Cleanup the operation */
	private synchronized void opComplete(boolean success) {
		if(!success)
			op_success = false;
		n_ops--;
		if(n_ops == 0 && op_success)
			lcs_array.setIndicationsCurrent(indications, user);
	}
}
