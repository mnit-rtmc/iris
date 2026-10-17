/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2026  SRF Consulting Group
 * Copyright (C) 2021-2025  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ndotgate;

import java.io.IOException;
import java.util.HashMap;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.server.GateArmImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.FutureOp;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to see if gate-arm has finished
 * moving to the correct position.
 *
 * Note:  Updated in January 2026 to include the
 *  shorter v6 gate-fault message strings.
 * Note2:  Updated in October 2026 for NDOTv7.  A monitor
 *  runs during any arm motion or gate override.  Each command
 *  starts a new monitor; an older monitor for the same gate
 *  arm stops quietly at its next check.
 *
 * @author John L. Stanley - SRF Consulting
 * @author Douglas Lau
 */
@SuppressWarnings("rawtypes")
public class OpCheckMoveStatus extends OpNdotGate {

	/** Seconds between status checks */
	static private final int RECHECK_SECS = 2;

	/** Current (newest) monitor for each gate arm.  An older monitor
	 * for the same gate arm stops at its next check. */
	static private final HashMap<GateArmImpl, OpCheckMoveStatus> CURRENT =
		new HashMap<GateArmImpl, OpCheckMoveStatus>();

	/** Start monitoring a gate arm.
	 * @param ga Gate arm.
	 * @param target Requested state (CLOSED, OPEN), or null to monitor
	 *               motion or a gate override not started by IRIS (only
	 *               if no monitor is running).
	 * @param delaySec Delay before the arm starts moving (seconds). */
	@SuppressWarnings("unchecked")
	static public void monitor(GateArmImpl ga, GateArmState target,
		int delaySec)
	{
		OpCheckMoveStatus op;
		synchronized (CURRENT) {
			if (target == null && CURRENT.containsKey(ga))
				return;
			op = new OpCheckMoveStatus(ga, target, delaySec);
			if (op.prop == null)
				return;
			CURRENT.put(ga, op);
		}
		int secs = (target == GateArmState.CLOSED)
		          ? delaySec
		          : RECHECK_SECS;
		FutureOp.queueOp(ga, secs, op);
	}

	/** Extra time (ms) allowed after the warning delay for the
	 * controller to start moving the arm */
	static private final long DELAY_MARGIN_MS = 5000;

	/** Check if a close was commanded for a gate arm and its warning
	 * delay is still running (arm not expected to move yet) */
	static public boolean isClosePending(GateArmImpl ga) {
		synchronized (CURRENT) {
			OpCheckMoveStatus op = CURRENT.get(ga);
			return (op != null) && op.isCloseDelay(
				TimeSteward.currentTimeMillis());
		}
	}

	/** Requested gate arm state (null if not requested by IRIS) */
	private final GateArmState target_state;

	/** Opposite gate arm state
	 * (Used to detect manual reversal of gate motion) */
	private final GateArmState antitarget_state;

	/** Estimated maximum time needed to move gate arm
	 * (from sending command until motion is complete).
	 * Due to possible failure conditions, this is a
	 * bit longer than twice the normal motion time. */
	private long maxEndTime;

	/** Time when the warning delay (plus margin) ends */
	private long delayEndTime;

	/** Flag indicating a gate override was seen while monitoring.
	 * A gate override has no time limit, and can end with the arm in
	 * either position. */
	private boolean gate_override_seen = false;

	/** Flag indicating this op was requeued for another check */
	private boolean requeued = false;

	/** Create a new gate-arm motion-monitor operation */
	@SuppressWarnings("unchecked")
	private OpCheckMoveStatus(GateArmImpl d, GateArmState gas,
		int delaySec)
	{
		super(PriorityLevel.POLL_LOW, d, false); // non-exclusive
		target_state = gas;
		if (gas == GateArmState.CLOSED)
			antitarget_state = GateArmState.OPEN;
		else if (gas == GateArmState.OPEN)
			antitarget_state = GateArmState.CLOSED;
		else
			antitarget_state = null;

		if (sGateArm != null) {
			prop = createStatusProperty();

			long started = TimeSteward.currentTimeMillis();
			maxEndTime = started + ((delaySec + 220) * 1000L);
			delayEndTime = started + (delaySec * 1000L) +
				DELAY_MARGIN_MS;
		}
	}

	/** Check if a close warning delay is running */
	private boolean isCloseDelay(long now) {
		return (target_state == GateArmState.CLOSED)
		    && !gate_override_seen
		    && (delayEndTime - now) > 0;
	}

	/** Check if this is the current monitor for the gate arm */
	private boolean isCurrent() {
		synchronized (CURRENT) {
			return CURRENT.get(gate_arm) == this;
		}
	}

	/** Create the second phase of the operation */
	protected Phase phaseTwo() {
		return (prop == null)
		     ? null
		     : (new CheckMoveCompletion());
	}

	/** Phase to check for move completion */
	protected class CheckMoveCompletion extends Phase {

		/** Query the status */
		@SuppressWarnings({ "unchecked", "synthetic-access" })
		protected Phase poll(CommMessage mess)
			throws IOException
		{
			requeued = false;
			// Superseded by a newer monitor?  Stop quietly.
			if (!isCurrent()) {
				prop = null;  // no status update
				return null;
			}
			// New property for each check, so a failed check
			// never reuses a previous response
			prop = createStatusProperty();
			mess.add(prop);
			mess.queryProps();
			if (!prop.gotValidResponse())
				throw new ParsingException("NO RESPONSE");

			// Controller reported a timeout / config error
			if (prop.isMotionTimeout())
				return null;

			// Arm moving or gate override in progress?
			if (prop.isMoving()) {
				if (prop.isGateOverride())
					gate_override_seen = true;
				long now = TimeSteward.currentTimeMillis();
				if (gate_override_seen ||
				    (maxEndTime - now) > 0)
					requeueOp();  // keep monitoring...
				else
					setTimeoutFailed();
				return null;
			}

			// Arm at rest
			GateArmState rest = prop.getRestPosition();
			if (rest == null)
				return null;  // unknown status, or lights/sign error
			if (rest == target_state || target_state == null)
				return null;  // motion complete!!
			if (gate_override_seen)
				return null;  // gate override completed
			// Close warning delay still running?
			if (isCloseDelay(TimeSteward.currentTimeMillis())) {
				requeueOp();  // arm not expected to move yet
				return null;
			}
			// Detect manual reversal of gate motion.
			if (rest == antitarget_state)
				setTimeoutFailed();
			return null;
		}
	}

	/** Report that the arm did not reach the requested state */
	private void setTimeoutFailed() {
		if (target_state == GateArmState.CLOSED)
			prop.statusOfGate = StatusOfGate.TIMEOUT_CLOSE_FAILED;
		else if (target_state == GateArmState.OPEN)
			prop.statusOfGate = StatusOfGate.TIMEOUT_OPEN_FAILED;
	}

	/** Queue this op for another check */
	private void requeueOp() {
		updateStatus();
		requeued = true;
		FutureOp.queueOp(device, RECHECK_SECS, this);
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		super.cleanup();
		if (!isSuccess())
			gate_arm.checkTimeout();
		if (!requeued) {
			synchronized (CURRENT) {
				CURRENT.remove(gate_arm, this);
			}
		}
	}
}
