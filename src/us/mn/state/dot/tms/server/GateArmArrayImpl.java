/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.GateArmArray;
import us.mn.state.dot.tms.GateArmArrayHelper;
import us.mn.state.dot.tms.GateArmInterlock;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.PlanPhase;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.User;
import static us.mn.state.dot.tms.server.GateArmSystem.checkEnabled;
import static us.mn.state.dot.tms.server.GateArmSystem.sendEmailAlert;
import static us.mn.state.dot.tms.server.MainServer.TIMER;
import us.mn.state.dot.tms.utils.CidrBlock;

/**
 * A Gate Arm array is a group of gate arms at a single ramp location.
 * All gate arms in an array are always controlled as a group.
 *
 * @author Douglas Lau
 */
public class GateArmArrayImpl extends DeviceImpl implements GateArmArray {

	/** Allow list of CIDR blocks */
	static private final List<CidrBlock> ALLOWLIST =
		new ArrayList<CidrBlock>();

	/** Initialize the gate arm allow list */
	static public void initAllowList(Properties props)
		throws IllegalArgumentException
	{
		List<CidrBlock> allow = CidrBlock.parseList(props.getProperty(
			"gate.arm.allowlist"));
		ALLOWLIST.addAll(allow);
	}

	/** Check if IP address is in allow list */
	static private boolean checkList(InetAddress a)
		throws ChangeVetoException
	{
		for (CidrBlock block: ALLOWLIST) {
			if (block.matches(a))
				return true;
		}
		throw new ChangeVetoException("IP ADDRESS NOT ALLOWED: " + a);
	}

	/** Exception thrown for interlock conflicts */
	static private final ChangeVetoException INTERLOCK_CONFLICT =
		new ChangeVetoException("INTERLOCK CONFLICT");

	/** Load all the gate arm arrays */
	static protected void loadAll() throws TMSException {
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"opposing, prereq, camera, approach, action_plan, " +
			"arm_state, interlock FROM iris." + SONAR_TYPE  + ";",
			new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new GateArmArrayImpl(row));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("geo_loc", geo_loc);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("notes", notes);
		map.put("opposing", opposing);
		map.put("prereq", prereq);
		map.put("camera", camera);
		map.put("approach", approach);
		map.put("action_plan", action_plan);
		map.put("arm_state", arm_state.ordinal());
		map.put("interlock", interlock.ordinal());
		return map;
	}

	/** Create a new gate arm array with a string name */
	public GateArmArrayImpl(String n) throws TMSException, SonarException {
		super(n);
		opposing = true;
		arm_state = GateArmState.UNKNOWN;
		interlock = GateArmInterlock.NONE;
		GeoLocImpl g = new GeoLocImpl(name, SONAR_TYPE);
		g.notifyCreate();
		geo_loc = g;
		GateArmSystem.disable(n, "create array");
	}

	/** Create a gate arm array */
	private GateArmArrayImpl(ResultSet row) throws SQLException {
		this(row.getString(1),    // name
		     row.getString(2),    // geo_loc
		     row.getString(3),    // controller
		     row.getInt(4),       // pin
		     row.getString(5),    // notes
		     row.getBoolean(6),   // opposing
		     row.getString(7),    // prereq
		     row.getString(8),    // camera
		     row.getString(9),    // approach
		     row.getString(10),   // action_plan
		     row.getInt(11),      // arm_state
		     row.getInt(12)       // interlock
		);
	}

	/** Create a gate arm array */
	private GateArmArrayImpl(String n, String loc, String c, int p,
		String nt, boolean ot, String pr, String cam, String ap,
		String pln, int as, int lk)
	{
		this(n, lookupGeoLoc(loc), lookupController(c), p, nt, ot, pr,
		     lookupCamera(cam), lookupCamera(ap), lookupActionPlan(pln),
		     as, lk);
	}

	/** Create a gate arm array */
	private GateArmArrayImpl(String n, GeoLocImpl loc, ControllerImpl c,
		int p, String nt, boolean ot, String pr, Camera cam, Camera ap,
		ActionPlanImpl pln, int as, int lk)
	{
		super(n, c, p, nt);
		geo_loc = loc;
		opposing = ot;
		prereq = pr;
		camera = cam;
		approach = ap;
		action_plan = pln;
		arm_state = GateArmState.fromOrdinal(as);
		interlock = GateArmInterlock.fromOrdinal(lk);
		initTransients();
	}

	/** Destroy an object */
	@Override
	public void doDestroy() throws TMSException {
		super.doDestroy();
		geo_loc.notifyRemove();
		GateArmSystem.disable(name, "destroy array");
	}

	/** Set the controller of the device */
	@Override
	public void doSetController(Controller c) throws TMSException{
		throw new ChangeVetoException("Cannot assign controller");
	}

	/** Set the controller I/O pin number */
	@Override
	public void doSetPin(int p) throws TMSException {
		throw new ChangeVetoException("Cannot assign pin");
	}

	/** Device location */
	private final GeoLocImpl geo_loc;

	/** Get the device location */
	@Override
	public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Opposing traffic flag */
	private boolean opposing;

	/** Set the opposing traffic flag */
	@Override
	public void setOpposing(boolean ot) {
		GateArmSystem.disable(name, "set opposing");
		opposing = ot;
	}

	/** Set the opposing traffic flag */
	public void doSetOpposing(boolean ot) throws TMSException {
		if (ot != opposing) {
			store.update(this, "opposing", ot);
			setOpposing(ot);
		}
	}

	/** Get the opposing traffic flag */
	@Override
	public boolean getOpposing() {
		return opposing;
	}

	/** Prerequisite gate arm array */
	private String prereq;

	/** Set the prerequisite gate arm array */
	@Override
	public void setPrereq(String pr) {
		GateArmSystem.disable(name, "set prereq");
		prereq = pr;
	}

	/** Set the prerequisite gate arm array */
	public void doSetPrereq(String pr) throws TMSException {
		if (!objectEquals(pr, prereq)) {
			store.update(this, "prereq", pr);
			setPrereq(pr);
		}
	}

	/** Get prerequisite gate arm array */
	@Override
	public String getPrereq() {
		return prereq;
	}

	/** Get prerequisite gate arm array */
	private GateArmArrayImpl getPrerequisite() {
		return (GateArmArrayImpl) GateArmArrayHelper.lookup(prereq);
	}

	/** Camera from which this can be seen */
	private Camera camera;

	/** Set the verification camera */
	@Override
	public void setCamera(Camera c) {
		GateArmSystem.disable(name, "set camera");
		camera = c;
	}

	/** Set the verification camera */
	public void doSetCamera(Camera c) throws TMSException {
		if (c != camera) {
			store.update(this, "camera", c);
			setCamera(c);
		}
	}

	/** Get verification camera */
	@Override
	public Camera getCamera() {
		return camera;
	}

	/** Camera to view approach */
	private Camera approach;

	/** Set the approach camera */
	@Override
	public void setApproach(Camera c) {
		GateArmSystem.disable(name, "set approach");
		approach = c;
	}

	/** Set the approach camera */
	public void doSetApproach(Camera c) throws TMSException {
		if (c != approach) {
			store.update(this, "approach", c);
			setApproach(c);
		}
	}

	/** Get approach camera */
	@Override
	public Camera getApproach() {
		return approach;
	}

	/** Action plan */
	private ActionPlanImpl action_plan;

	/** Set the action plan */
	@Override
	public void setActionPlan(ActionPlan ap) {
		GateArmSystem.disable(name, "set actionPlan");
		if (ap instanceof ActionPlanImpl)
			action_plan = (ActionPlanImpl) ap;
	}

	/** Set the action plan */
	public void doSetActionPlan(ActionPlan ap) throws TMSException {
		if (ap != action_plan) {
			store.update(this, "action_plan", ap);
			setActionPlan(ap);
		}
	}

	/** Get the action plan */
	@Override
	public ActionPlan getActionPlan() {
		return action_plan;
	}

	/** Array of all gate arms */
	private transient final GateArmImpl[] arms = new GateArmImpl[MAX_ARMS];

	/** Get one gate arm */
	private GateArmImpl getArm(int i) {
		GateArmImpl[] a = arms;
		return (a != null) ? a[i] : null;
	}

	/** Get array of gate arms */
	public synchronized GateArmImpl[] getArms() {
		return Arrays.copyOf(arms, MAX_ARMS);
	}

	/** Set one gate arm.
	 * @param idx Array index.
	 * @param ga Gate Arm */
	public synchronized void setIndex(int idx, GateArmImpl ga)
		throws TMSException
	{
		idx--;
		if (idx < 0 || idx >= MAX_ARMS)
			throw new ChangeVetoException("Invalid index");
		if (ga != null && arms[idx] != null)
			throw new ChangeVetoException("Already assigned");
		arms[idx] = ga;
	}

	/** The owner of the next state to be requested.  This is a write-only
	 * SONAR attribute. */
	private transient User ownerNext;

	/** Set the next state owner.  When a user sends a new state to the
	 * gate arm array, two attributes must be set: ownerNext and armState.
	 * There can be a race between two clients setting these attributes.  If
	 * ownerNext is non-null when being set, then a race has been detected,
	 * meaning two clients are trying to set the state at the same time. */
	@Override
	public synchronized void setOwnerNext(User o) {
		if (ownerNext != null && o != null) {
			logError("OWNER CONFLICT: " + ownerNext.getName() +
			         " vs. " + o.getName());
			ownerNext = null;
		} else
			ownerNext = o;
	}

	/** Gate arm state */
	private GateArmState arm_state;

	/** Set the next arm state (request change) */
	@Override
	public void setArmStateNext(int gas) {
		// Do nothing; required by iface
	}

	/** Set the arm state (request change) */
	public synchronized void doSetArmStateNext(int gas) throws TMSException{
		try {
			checkList(MainServer.server.getProcAddress());
			if (ownerNext != null)
				doSetArmStateNext(gas, ownerNext);
			else
				throw new ChangeVetoException("OWNER CONFLICT");
		}
		finally {
			// ownerNext is only valid for one message, clear it
			ownerNext = null;
		}
	}

	/** Set the arm state (request change) */
	private void doSetArmStateNext(int gas, User o) throws TMSException {
		final GateArmState cs = arm_state;
		GateArmState rs = validateStateReq(
			GateArmState.fromOrdinal(gas), cs);
		if ((rs != cs) && checkEnabled())
			requestArmState(rs, ownerNext);
	}

	/** Validate a new requested gate arm state.
	 * @param rs Requested gate arm state.
	 * @param cs Current arm state.
	 * @return Validated state: OPENING, WARN_CLOSE or CLOSING.
	 * @throws TMSException for invalid state change or interlock. */
	private GateArmState validateStateReq(GateArmState rs, GateArmState cs)
		throws TMSException
	{
		GateArmInterlock gai = interlock;
		boolean has_signs = GateArmArrayHelper.hasActionPlanSigns(this);
		if (rs == GateArmState.OPENING) {
			if (!gai.isOpenAllowed())
				throw INTERLOCK_CONFLICT;
			if (cs.canRequestOpening())
				return rs;
		}
		if (rs == GateArmState.WARN_CLOSE) {
			if (!gai.isCloseAllowed())
				throw INTERLOCK_CONFLICT;
			if (cs.canRequestWarnClose(has_signs))
				return rs;
		}
		if (rs == GateArmState.CLOSING) {
			if (!gai.isCloseAllowed())
				throw INTERLOCK_CONFLICT;
			if (cs.canRequestClosing(has_signs))
				return rs;
		}
		throw new ChangeVetoException("INVALID STATE CHANGE: " + cs +
			" to " + rs);
	}

	/** Request a change to the gate arm state for all arms in array.
	 * @param rs Requested gate arm state.
	 * @param o User requesting new state. */
	private synchronized void requestArmState(GateArmState rs, User o)
		throws TMSException
	{
		if (rs == GateArmState.WARN_CLOSE) {
			setArmState(rs);
			updatePlanPhase(o);
			return;
		}
		for (int i = 0; i < MAX_ARMS; i++) {
			GateArmImpl ga = getArm(i);
			if (ga != null)
				ga.requestArmState(rs, o);
		}
	}

	/** Set the arm state */
	private void setArmState(GateArmState gas) {
		try {
			store.update(this, "arm_state", gas.ordinal());
		}
		catch (TMSException e) {
			GateArmSystem.disable(name, "DB arm_state");
		}
		arm_state = gas;
		notifyAttribute("armState");
		updateStyles();
		if (gas == GateArmState.UNKNOWN)
			sendEmailAlert("COMMUNICATION FAILED: " + name);
		if (gas == GateArmState.FAULT)
			sendEmailAlert("FAULT: " + name);
	}

	/** Update the action plan phase */
	private void updatePlanPhase(User o) {
		ActionPlanImpl ap = action_plan;
		if (ap != null) {
			try {
				updatePlanPhase(ap, o);
			}
			catch (TMSException e) {
				logError("updatePlanPhase: " + e.getMessage());
			}
		}
	}

	/** Update the action plan phase */
	private void updatePlanPhase(ActionPlanImpl ap, User o)
		throws TMSException
	{
		String uid = (o != null) ? o.getName() : null;
		if (isMsgOpen()) {
			PlanPhase op = lookupPlanPhase(
				PlanPhase.GATE_ARM_OPEN);
			if (op != null && ap.setPhaseNotify(op, uid))
				updateDmsActions(ap);
		} else {
			PlanPhase cp = lookupPlanPhase(
				PlanPhase.GATE_ARM_CLOSED);
			if (cp != null && ap.setPhaseNotify(cp, uid))
				updateDmsActions(ap);
		}
	}

	/** Test if message should be open */
	private boolean isMsgOpen() {
		return isActive() && arm_state == GateArmState.OPEN;
	}

	/** Update scheduled DMS actions for a plan */
	private void updateDmsActions(ActionPlanImpl ap) {
		TIMER.addJob(new DeviceActionJob(ap));
	}

	/** Update the arm state */
	public void updateArmState(User o) {
		GateArmState cs = arm_state;
		GateArmState gas = aggregateArmState();
		// Don't update WARN_CLOSE back to OPEN
		if (gas != cs &&
		   (gas != GateArmState.OPEN || cs != GateArmState.WARN_CLOSE))
			setArmState(gas);
		else
			checkEnabled();
		updatePlanPhase(o);
	}

	/** Get the aggregate arm state for all arms in the array */
	private GateArmState aggregateArmState() {
		boolean unknown = false;
		boolean fault = false;
		boolean opening = false;
		boolean open = false;
		boolean closing = false;
		boolean closed = false;
		for (int i = 0; i < MAX_ARMS; i++) {
			GateArmImpl ga = getArm(i);
			if (ga != null && ga.isActive()) {
				GateArmState gas = ga.getArmStateEnum();
				switch (gas) {
				case UNKNOWN:
					unknown = true;
					break;
				case FAULT:
					fault = true;
					break;
				case OPENING:
					opening = true;
					break;
				case OPEN:
					open = true;
					break;
				case CLOSING:
					closing = true;
					break;
				case CLOSED:
					closed = true;
					break;
				}
			}
		}
		if (unknown)
			return GateArmState.UNKNOWN;
		if (fault)
			return GateArmState.FAULT;
		if (opening && !closing)
			return GateArmState.OPENING;
		if (closing && !opening)
			return GateArmState.CLOSING;
		if (open && !(closed || opening || closing))
			return GateArmState.OPEN;
		if (closed && !(open || opening || closing))
			return GateArmState.CLOSED;
		return GateArmState.FAULT;
	}

	/** Get the arm state */
	@Override
	public int getArmState() {
		return arm_state.ordinal();
	}

	/** Calculate item styles */
	@Override
	protected long calculateStyles() {
		long s = super.calculateStyles();
		if (isClosed())
			s |= ItemStyle.CLOSED.bit();
		if (isOpen())
			s |= ItemStyle.OPEN.bit();
		if (isMoving())
			s |= ItemStyle.MOVING.bit();
		return s;
	}

	/** Update the item styles */
	@Override
	public void updateStyles() {
		super.updateStyles();
		GateArmSystem.checkInterlocks(getRoad());
		GateArmSystem.updateDependencies();
		setSystemEnable(checkEnabled());
		setOpenConflict(interlock.isOpenLocked() && isPossiblyOpen());
		setCloseConflict(interlock.isCloseLocked() && isClosed());
	}

	/** Gate arm interlock */
	private GateArmInterlock interlock;

	/** Get the interlock ordinal */
	@Override
	public int getInterlock() {
		return interlock.ordinal();
	}

	/** Get the interlock enum */
	public GateArmInterlock getInterlockEnum() {
		return interlock;
	}

	/** Set the interlock flag */
	private void setInterlockNotify() {
		GateArmInterlock lk = lock_state.getInterlock();
		if (lk != interlock) {
			try {
				store.update(this, "interlock", lk.ordinal());
			}
			catch (TMSException e) {
				GateArmSystem.disable(name, "DB interlock");
			}
			interlock = lk;
			notifyAttribute("interlock");
			sendInterlocks();
		}
	}

	/** Send gate arm interlock settings */
	private void sendInterlocks() {
		for (int i = 0; i < MAX_ARMS; i++) {
			GateArmImpl ga = getArm(i);
			if (ga != null)
				ga.sendInterlocks();
		}
	}

	/** Lock state for calculating interlock */
	private transient GateArmLockState lock_state = new GateArmLockState();

	/** Begin dependency transaction */
	public void beginDependencies() {
		lock_state.beginDependencies();
	}

	/** Check gate arm array dependencies */
	public void checkDependencies() {
		GateArmArrayImpl pr = getPrerequisite();
		if (pr != null) {
			if (isPossiblyOpen())
				pr.lock_state.setDependentOpen();
			lock_state.setPrereqClosed(!pr.isFullyOpen());
		}
	}

	/** Commit dependcy transaction */
	public void commitDependencies() {
		lock_state.commitDependencies();
		setInterlockNotify();
	}

	/** Set flag to enable gate arm system */
	public void setSystemEnable(boolean e) {
		lock_state.setSystemEnable(e && isActive());
		setInterlockNotify();
	}

	/** Open conflict detected flag.  This is initially set to true because
	 * devices start in failed state after a server restart. */
	private transient boolean open_conflict = true;

	/** Set open conflict state */
	private void setOpenConflict(boolean c) {
		if (c != open_conflict) {
			open_conflict = c;
			if (c)
				sendEmailAlert("OPEN CONFLICT: " + name);
		}
	}

	/** Close conflict detected flag. */
	private transient boolean close_conflict = false;

	/** Set close conflict state */
	private void setCloseConflict(boolean c) {
		if (c != close_conflict) {
			close_conflict = c;
			if (c)
				sendEmailAlert("CLOSE CONFLICT: " + name);
		}
	}

	/** Get gate arm road */
	public Road getRoad() {
		GeoLoc gl = getGeoLoc();
		return (gl != null) ? gl.getRoadway() : null;
	}

	/** Get gate arm road direction.
	 * @return Index of road direction, or 0 for unknown */
	public int getRoadDir() {
		GeoLoc gl = getGeoLoc();
		return (gl != null) ? gl.getRoadDir() : 0;
	}

	/** Set the valid open direction for road.
	 * @param dir Valid open direction; 0 for any, -1 for none */
	public void setOpenDirection(int dir) {
		int gd = getRoadDir();
		boolean open = (dir != 0) && (dir != gd);
		lock_state.setOpposingOpen(opposing && open);
		setInterlockNotify();
	}

	/** Get the active status */
	@Override
	public boolean isActive() {
		for (int i = 0; i < MAX_ARMS; i++) {
			GateArmImpl ga = getArm(i);
			if (ga != null && ga.isActive())
				return true;
		}
		return false;
	}

	/** Get the offline status */
	@Override
	public boolean isOffline() {
		for (int i = 0; i < MAX_ARMS; i++) {
			GateArmImpl ga = getArm(i);
			if (ga != null && ga.isActive() && ga.isOffline())
				return true;
		}
		return false;
	}

	/** Test if gate arm is closed */
	private boolean isClosed() {
		return isOnline() && arm_state == GateArmState.CLOSED;
	}

	/** Test if gate arm is possibly open */
	public boolean isPossiblyOpen() {
		return isActive() && arm_state != GateArmState.CLOSED;
	}

	/** Test if gate arm is open */
	private boolean isOpen() {
		return isOnline() && isPossiblyOpen();
	}

	/** Test if gate arm is fully open */
	public boolean isFullyOpen() {
		return isOnline() && arm_state == GateArmState.OPEN;
	}

	/** Test if gate arm is moving */
	private boolean isMoving() {
		return isOnline() &&
		      (arm_state == GateArmState.OPENING ||
		       arm_state == GateArmState.CLOSING);
	}

	/** Test if gate arm has faults */
	@Override
	protected boolean hasFaults() {
		return arm_state == GateArmState.FAULT;
	}

	/** Send a device request operation */
	@Override
	protected void sendDeviceRequest(DeviceRequest dr) {
		for (int i = 0; i < MAX_ARMS; i++) {
			GateArmImpl ga = getArm(i);
			if (ga != null)
				ga.sendDeviceRequest(dr);
		}
	}

	/** Perform a periodic poll */
	@Override
	public void periodicPoll(boolean is_long) {
		// handled by individual gate arms
	}
}
