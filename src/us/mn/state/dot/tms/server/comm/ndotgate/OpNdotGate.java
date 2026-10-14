/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2026  SRF Consulting Group
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
 *
 * Derived in part from MNDOT's IRIS code for controlling their
 * HySecurity STC gates.
 */
package us.mn.state.dot.tms.server.comm.ndotgate;

import static us.mn.state.dot.tms.server.comm.ndotgate.NdotGatePoller.NDOTGATE_LOG;

import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.User;
import us.mn.state.dot.tms.server.GateArmImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation for NDORv5/NDOTv6 Gate controller
 * Note:  Code updated in August 2016 to include
 * multi-arm gate protocol referred to as v5.
 *
 * @author John L. Stanley - SRF Consulting
 */
abstract public class OpNdotGate<T extends ControllerProperty>
	extends OpDevice<T>
{
	/** Log an error msg */
	protected void logError(String msg) {
		if (NDOTGATE_LOG.isOpen())
			NDOTGATE_LOG.log(controller.getName() + "! " + msg);
	}

	/** Gate arm device */
	protected final GateArmImpl gate_arm;

	// String representation of the controller's gate-arm number
	// using NDORv5/NDOTv6 gate-protocol (with multi-gate extension)
	// (IRIS controller pin number) == (controller gate arm number)
	//   pin 1 --> ""
	//   pin 2-8 --> "2"-"8"
	//   all other gate numbers --> null
	protected final String sGateArm;

	/** Status property */
	protected NdotGateProperty prop;

	/** Create a new NDOT Gate operation */
	protected OpNdotGate(PriorityLevel p, GateArmImpl ga, boolean ex) {
		super(p, ga, ex);
		gate_arm = ga;
		int pin = gate_arm.getPin();
		if ((pin < 1) || (pin > 8)) {
			putCtrlFaults("other", "Invalid pin");
			sGateArm = null;
		} else
			sGateArm = (pin == 1) ? "" : ("" + pin);
	}

	/** Create a new "Retrieve Gate Status" property */
	protected NdotGateProperty createStatusProperty() {
		// NDORv5/NDOTv6/NDOTv7 "Retrieve Gate Status" command
		return new NdotGateProperty("*S"+sGateArm+"#\r\n");
	}

	/** Update gate arm / controller status */
	protected void updateStatus() {
		if (prop == null)
			return;
		// During a close warning delay, report an open arm as closing,
		// so IRIS does not send the close again (which would restart
		// the controller's delay)
		// (a close sent during a gate override is ignored by the
		// controller)
		prop.closeDelay = !prop.isGateOverride() &&
			OpCheckMoveStatus.isClosePending(gate_arm);
		GateArmState new_state = prop.getState();
		String fault = prop.getFault();
		gate_arm.setFaultNotify(fault);
		gate_arm.setErrorFlagsNotify(prop.getGateArmErrors());
		gate_arm.setStatusFlagsNotify(prop.getGateArmFlags());
		if (fault != null) {
			putCtrlFaults("other", fault);
			updateCtrlStatus();
			controller.incrementControllerErr();
		}
		// Always set: even with no state change, this recalculates the
		// interlock (clearing SYSTEM_DISABLE once the system is enabled)
		gate_arm.setArmStateNotify(new_state);
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		super.cleanup();
		updateStatus();
	}
}
