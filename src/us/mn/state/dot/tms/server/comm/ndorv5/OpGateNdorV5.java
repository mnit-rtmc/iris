/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2021  SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm.ndorv5;

import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.server.GateArmImpl;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

import static us.mn.state.dot.tms.server.comm.ndorv5.GateNdorV5Poller.GATENDORv5_LOG;

import us.mn.state.dot.sonar.User;

/**
 * Operation for NDOR Gate v5 device
 * Note:  Code updated in August 2016 to include
 * multi-arm gate protocol referred to as v5.
 *
 * @author John L. Stanley - SRF Consulting
 */
abstract public class OpGateNdorV5<T extends ControllerProperty>
  extends OpDevice<T> {

	/** User who initiated control */
	protected User user = null;

	/** Log an error msg */
	protected void logError(String msg) {
		if (GATENDORv5_LOG.isOpen())
			GATENDORv5_LOG.log(controller.getName() + "! " + msg);
	}

	/** Gate arm device */
	protected final GateArmImpl gate_arm;

	/** Status property */
	protected GateNdorV5Property prop;

	/** Create a new NDOR Gate v5 operation */
	protected OpGateNdorV5(PriorityLevel p, GateArmImpl ga, boolean ex) {
		super(p, ga, ex);
		gate_arm = ga;
		initGateOpVars();
	}

	// String representation of the controller's gate-arm number
	// using NDOR gate-protocol v5 (with multi-gate extension)
	// (IRIS controller pin number) == (NDORv5 controller gate arm number)
	//   pin 1 --> ""
	//   pin 2-8 --> "2"-"8"
	//   all other gate numbers --> null
	protected String sGateArm;
	
	protected String sArmNumber;
	
	/** Create a new NDOR Gate v5 operation */
	protected OpGateNdorV5(PriorityLevel p, GateArmImpl ga) {
		super(p, ga);
		gate_arm = ga;
		initGateOpVars();
	}

	protected void initGateOpVars() {
		int pin = gate_arm.getPin();
		sArmNumber = ""+pin;
		if ((pin < 1) || (pin > 8)) {
			setErrorStatus("Invalid pin");
			sGateArm = null;
		} else
			sGateArm = (pin == 1) ? "" : (""+pin);
	}

	/** Update controller status */
	@SuppressWarnings("incomplete-switch")
	protected void updateStatus() {
		GateArmState cur_state = prop.getState();
		if (gate_arm.getBeaconOn()) {
			switch (cur_state) {
			case OPEN:
			case CLOSING:
				cur_state = GateArmState.BEACON_ON;
			}
		}
		// prevent polling from clearing manual-override error
		switch (gate_arm.getArmStateEnum()) {
			case CLOSING_FAIL:
				if (cur_state == GateArmState.OPEN)
					return;
				break;
			case OPENING_FAIL:
				if (cur_state == GateArmState.CLOSED)
					return;
				break;
		}
		if (gate_arm.getArmStateEnum() != cur_state) {
			setNdorV5ArmStateNotify(cur_state, user);
		}

		if (cur_state.isMoving() == false) {
			setMaintStatus(prop.getMaintStatus());
			updateMaintStatus();
		}
	}

	@Override
	// OpController.toString() uses just the controller's
	// name.  NDOR gates can have more than one arm per
	// controller so we need this override version.
	public String toString() {
		return "("+this.getClass().getSimpleName()+", "+gate_arm.getName()+")";
	}

	//-----------------------------------------------------

	/** Set the gate arm state with a temporary
	 *  override for the NDORv5Gate BEACON_ON state.
	 * @param gas Gate arm state.
	 * @param o User who requested new state, or null. */
	public void setNdorV5ArmStateNotify(GateArmState gas, User o) {
		// Temporarily substitute BEACON_ON for OPEN
		// at start of NDOR-gate CLOSE cycle
		if (gate_arm.getBeaconOn()
		 && (gas == GateArmState.OPEN))
			gas = GateArmState.BEACON_ON;
		gate_arm.setArmStateNotify(gas, o);
	}
}
