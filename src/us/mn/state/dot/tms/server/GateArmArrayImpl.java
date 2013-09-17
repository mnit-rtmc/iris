/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.GateArmArray;
import us.mn.state.dot.tms.GateArmInterlock;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.Road;
import us.mn.state.dot.tms.TMSException;
import static us.mn.state.dot.tms.server.GateArmSystem.checkEnabled;
import static us.mn.state.dot.tms.server.GateArmSystem.sendEmailAlert;

/**
 * A Gate Arm array is a group of gate arms at a single ramp location.
 * All gate arms in an array are always controlled as a group.
 *
 * @author Douglas Lau
 */
public class GateArmArrayImpl extends DeviceImpl implements GateArmArray {

	/** Exception thrown for interlock conflicts */
	static private final ChangeVetoException INTERLOCK_CONFLICT =
		new ChangeVetoException("INTERLOCK CONFLICT");

	/** Load all the gate arm arrays */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, GateArmArrayImpl.class);
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"camera, approach, dms, open_msg, closed_msg " +
			"FROM iris." + SONAR_TYPE  + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new GateArmArrayImpl(
					namespace,
					row.getString(1),	// name
					row.getString(2),	// geo_loc
					row.getString(3),	// controller
					row.getInt(4),		// pin
					row.getString(5),	// notes
					row.getString(6),	// camera
					row.getString(7),	// approach
					row.getString(8),	// dms
					row.getString(9),	// open_msg
					row.getString(10)	// closed_msg
				));
			}
		});
	}

	/** Get a mapping of the columns */
	@Override public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("geo_loc", geo_loc);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("notes", notes);
		map.put("camera", camera);
		map.put("approach", approach);
		map.put("dms", dms);
		map.put("open_msg", open_msg);
		map.put("closed_msg", closed_msg);
		return map;
	}

	/** Get the database table name */
	@Override public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	@Override public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new gate arm array with a string name */
	public GateArmArrayImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name);
		g.notifyCreate();
		geo_loc = g;
	}

	/** Create a gate arm array */
	private GateArmArrayImpl(String n, GeoLocImpl loc, ControllerImpl c,
		int p, String nt, Camera cam, Camera ap, DMS d, QuickMessage om,
		QuickMessage cm)
	{
		super(n, c, p, nt);
		geo_loc = loc;
		camera = cam;
		approach = ap;
		dms = d;
		open_msg = om;
		closed_msg = cm;
		initTransients();
	}

	/** Create a gate arm array */
	private GateArmArrayImpl(Namespace ns, String n, String loc, String c,
		int p, String nt, String cam, String ap, String d,
		String om, String cm)
	{
		this(n, (GeoLocImpl)ns.lookupObject(GeoLoc.SONAR_TYPE, loc),
		     (ControllerImpl)ns.lookupObject(Controller.SONAR_TYPE, c),
		     p, nt, (Camera)ns.lookupObject(Camera.SONAR_TYPE, cam),
		     (Camera)ns.lookupObject(Camera.SONAR_TYPE, ap),
		     (DMS)ns.lookupObject(DMS.SONAR_TYPE, d),
		     (QuickMessage)ns.lookupObject(QuickMessage.SONAR_TYPE, om),
		     (QuickMessage)ns.lookupObject(QuickMessage.SONAR_TYPE,cm));
	}

	/** Destroy an object */
	@Override public void doDestroy() throws TMSException {
		super.doDestroy();
		geo_loc.notifyRemove();
	}

	/** Set the controller of the device */
	@Override public void doSetController(Controller c) throws TMSException{
		throw new ChangeVetoException("Cannot assign controller");
	}

	/** Set the controller I/O pin number */
	@Override public void doSetPin(int p) throws TMSException {
		throw new ChangeVetoException("Cannot assign pin");
	}

	/** Device location */
	private GeoLocImpl geo_loc;

	/** Get the device location */
	@Override public GeoLoc getGeoLoc() {
		return geo_loc;
	}

	/** Camera from which this can be seen */
	private Camera camera;

	/** Set the verification camera */
	@Override public void setCamera(Camera c) {
		camera = c;
	}

	/** Set the verification camera */
	public void doSetCamera(Camera c) throws TMSException {
		if(c != camera) {
			store.update(this, "camera", c);
			setCamera(c);
		}
	}

	/** Get verification camera */
	@Override public Camera getCamera() {
		return camera;
	}

	/** Camera to view approach */
	private Camera approach;

	/** Set the approach camera */
	@Override public void setApproach(Camera c) {
		approach = c;
	}

	/** Set the approach camera */
	public void doSetApproach(Camera c) throws TMSException {
		if(c != approach) {
			store.update(this, "approach", c);
			setApproach(c);
		}
	}

	/** Get approach camera */
	@Override public Camera getApproach() {
		return approach;
	}

	/** DMS for warning */
	private DMS dms;

	/** Set the DMS for warning */
	@Override public void setDms(DMS d) {
		dms = d;
	}

	/** Set the DMS for warning */
	public void doSetDms(DMS d) throws TMSException {
		if(d != dms) {
			store.update(this, "dms", d);
			setDms(d);
		}
	}

	/** Get the DMS for warning */
	@Override public DMS getDms() {
		return dms;
	}

	/** Quick message to send for OPEN state */
	private QuickMessage open_msg;

	/** Set the OPEN quick message */
	@Override public void setOpenMsg(QuickMessage om) {
		open_msg = om;
	}

	/** Set the OPEN quick message */
	public void doSetOpenMsg(QuickMessage om) throws TMSException {
		if(om != open_msg) {
			store.update(this, "open_msg", om);
			setOpenMsg(om);
		}
	}

	/** Get the OPEN quick message */
	@Override public QuickMessage getOpenMsg() {
		return open_msg;
	}

	/** Quick message to send for CLOSED state */
	private QuickMessage closed_msg;

	/** Set the CLOSED quick message */
	@Override public void setClosedMsg(QuickMessage cm) {
		closed_msg = cm;
	}

	/** Set the CLOSED quick message */
	public void doSetClosedMsg(QuickMessage cm) throws TMSException {
		if(cm != closed_msg) {
			store.update(this, "closed_msg", cm);
			setClosedMsg(cm);
		}
	}

	/** Get the CLOSED quick message */
	@Override public QuickMessage getClosedMsg() {
		return closed_msg;
	}

	/** Array of all gate arms */
	private transient final GateArmImpl[] arms = new GateArmImpl[MAX_ARMS];

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
		if(idx < 0 || idx > MAX_ARMS)
			throw new ChangeVetoException("Invalid index");
		if(ga != null && arms[idx] != null)
			throw new ChangeVetoException("Already assigned");
		arms[idx] = ga;
	}

	/** Request a device operation */
	@Override public void setDeviceRequest(int r) {
		for(int i = 0; i < MAX_ARMS; i++) {
			GateArmImpl ga = arms[i];
			if(ga != null)
				ga.setDeviceRequest(r);
		}
	}

	/** The owner of the next state to be requested.  This is a write-only
	 * SONAR attribute. */
	private transient User ownerNext;

	/** Set the next state owner.  When a user sends a new state to the
	 * gate arm array, two attributes must be set: ownerNext and armState.
	 * There can be a race between two clients setting these attributes.  If
	 * ownerNext is non-null when being set, then a race has been detected,
	 * meaning two clients are trying to set the state at the same time. */
	@Override public synchronized void setOwnerNext(User o) {
		if(ownerNext != null && o != null) {
			System.err.println("GateArmArrayImpl.setOwnerNext: " +
				getName() + ", " + ownerNext.getName() +
				" vs. " + o.getName());
			ownerNext = null;
		} else
			ownerNext = o;
	}

	/** Gate arm state */
	private transient GateArmState arm_state = GateArmState.UNKNOWN;

	/** Set the next arm state (request change) */
	@Override public void setArmStateNext(int gas) {
		// Do nothing; required by iface
	}

	/** Set the arm state (request change) */
	public void doSetArmStateNext(int gas) throws TMSException {
		User o_next = ownerNext;	// Avoid race
		// ownerNext is only valid for one message, clear it
		ownerNext = null;
		if(o_next == null)
			throw new ChangeVetoException("MUST SET OWNER FIRST");
		final GateArmState cs = arm_state;
		GateArmState rs = validateStateReq(
			GateArmState.fromOrdinal(gas), cs);
		if((rs != cs) && checkEnabled())
			requestArmState(rs, o_next);
	}

	/** Validate a new requested gate arm state.
	 * @param rs Requested gate arm state.
	 * @param cs Current arm state.
	 * @return Validated state: OPENING, WARN_CLOSE or CLOSING.
	 * @throws TMSException for invalid state change or interlock. */
	private GateArmState validateStateReq(GateArmState rs, GateArmState cs)
		throws TMSException
	{
		if(rs == GateArmState.OPENING) {
			if(deny_open)
				throw INTERLOCK_CONFLICT;
			if(cs == GateArmState.CLOSED ||
			   cs == GateArmState.WARN_CLOSE)
				return rs;
		}
		if(rs == GateArmState.WARN_CLOSE) {
			if(deny_close)
				throw INTERLOCK_CONFLICT;
			if(cs == GateArmState.OPEN || cs == GateArmState.FAULT)
				return rs;
		}
		if(rs == GateArmState.CLOSING) {
			if(deny_close)
				throw INTERLOCK_CONFLICT;
			if(cs == GateArmState.WARN_CLOSE)
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
		setArmState(rs);
		for(int i = 0; i < MAX_ARMS; i++) {
			GateArmImpl ga = arms[i];
			if(ga != null)
				ga.requestArmState(rs, o);
		}
		// FIXME: update DMS for WARN_CLOSE state
	}

	/** Set the arm state */
	private void setArmState(GateArmState gas) {
		arm_state = gas;
		notifyAttribute("armState");
		updateStyles();
	}

	/** Update the arm state */
	public void updateArmState() {
		GateArmState cs = arm_state;
		GateArmState gas = aggregateArmState();
		if(gas != cs)
			setArmState(gas);
	}

	/** Get the aggregate arm state for all arms in the array */
	private GateArmState aggregateArmState() {
		boolean opening = false;
		boolean open = false;
		boolean closing = false;
		boolean closed = false;
		for(int i = 0; i < MAX_ARMS; i++) {
			GateArmImpl ga = arms[i];
			if(ga != null) {
				GateArmState gas = ga.getArmStateEnum();
				switch(gas) {
				case UNKNOWN:
				case FAULT:
					return gas;
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
		if(opening && !(closing || closed))
			return GateArmState.OPENING;
		if(closing && !(opening || open))
			return GateArmState.CLOSING;
		if(open && !(closed || opening || closing))
			return GateArmState.OPEN;
		if(closed && !(open || opening || closing))
			return GateArmState.OPEN;
		if(opening || open || closing || closed)
			return GateArmState.FAULT;
		else
			return GateArmState.UNKNOWN;
	}

	/** Get the arm state */
	@Override public int getArmState() {
		return arm_state.ordinal();
	}

	/** Flag to deny gate arm open (interlock) */
	private transient boolean deny_open = true;

	/** Set interlock flag to deny gate open */
	private void setDenyOpen(boolean d) {
		int gai = getInterlock();
		deny_open = d;
		if(gai != getInterlock())
			setInterlockNotify();
	}

	/** Flag to deny gate arm close (interlock) */
	private transient boolean deny_close = false;

	/** Flag to enable gate arm system */
	private transient boolean system_enable = false;

	/** Set flag to enable gate arm system */
	public void setSystemEnable(boolean e) {
		int gai = getInterlock();
		system_enable = e && isActive();
		if(gai != getInterlock())
			setInterlockNotify();
	}

	/** Set the interlock flag */
	private void setInterlockNotify() {
		notifyAttribute("interlock");
		setDeviceRequest(DeviceRequest.SEND_SETTINGS.ordinal());
	}

	/** Get the interlock enum */
	@Override public int getInterlock() {
		if(!system_enable)
			return GateArmInterlock.SYSTEM_DISABLE.ordinal();
		else if(deny_open && deny_close)
			return GateArmInterlock.DENY_ALL.ordinal();
		else if(deny_open)
			return GateArmInterlock.DENY_OPEN.ordinal();
		else if(deny_close)
			return GateArmInterlock.DENY_CLOSE.ordinal();
		else
			return GateArmInterlock.NONE.ordinal();
	}

	/** Check if arm open interlock in effect */
	public boolean isOpenInterlock() {
		return deny_open || !system_enable;
	}

	/** Item style bits */
	private transient long styles = calculateStyles();

	/** Calculate item styles */
	private long calculateStyles() {
		long s = ItemStyle.ALL.bit();
		if(isClosed())
			s |= ItemStyle.CLOSED.bit();
		if(isOpen())
			s |= ItemStyle.OPEN.bit();
		if(isMoving())
			s |= ItemStyle.MOVING.bit();
		if(needsMaintenance())
			s |= ItemStyle.MAINTENANCE.bit();
		if(isActive() && isFailed())
			s |= ItemStyle.FAILED.bit();
		if(!isActive())
			s |= ItemStyle.INACTIVE.bit();
		return s;
	}

	/** Update the item styles */
	@Override public void updateStyles() {
		setStyles(calculateStyles());
		GateArmSystem.checkInterlocks(getRoad());
		setSystemEnable(checkEnabled());
		setConflict(deny_open && isOpen());
	}

	/** Conflict detected flag.  This is initially set to true because
	 * devices start in failed state after a server restart. */
	private transient boolean conflict = true;

	/** Set open conflict state */
	private void setConflict(boolean c) {
		if(c != conflict) {
			conflict = c;
			if(conflict)
				sendEmailAlert("OPEN CONFLICT: " + name);
		}
	}

	/** Get gate arm road */
	public Road getRoad() {
		GeoLoc gl = getGeoLoc();
		return gl != null ? gl.getRoadway() : null;
	}

	/** Get gate arm road direction.
	 * @return Index of road direction, or 0 for unknown */
	public int getRoadDir() {
		GeoLoc gl = getGeoLoc();
		return gl != null ? gl.getRoadDir() : 0;
	}

	/** Set the valid open direction for road.
	 * @param d Valid open direction; 0 for any, -1 for none */
	public void setOpenDirection(int d) {
		int gd = getRoadDir();
		setDenyOpen(d != 0 && d != gd);
	}

	/** Get the active status.  Tests that at least one gate arm is
	 * assigned to the array and all are active. */
	@Override public boolean isActive() {
		boolean any = false;
		boolean all = true;
		for(int i = 0; i < MAX_ARMS; i++) {
			GateArmImpl ga = arms[i];
			if(ga != null) {
				boolean a = ga.isActive();
				any = (any || a);
				all = (all && a);
			}
		}
		return any && all;
	}

	/** Get the failure status */
	@Override public boolean isFailed() {
		for(int i = 0; i < MAX_ARMS; i++) {
			GateArmImpl ga = arms[i];
			if(ga != null && ga.isFailed())
				return true;
		}
		return false;
	}

	/** Test if gate arm is closed */
	private boolean isClosed() {
		return isOnline() && arm_state == GateArmState.CLOSED;
	}

	/** Test if gate arm is online (active and not failed) */
	private boolean isOnline() {
		return isActive() && !isFailed();
	}

	/** Test if gate arm is (or may be) open */
	public boolean isOpen() {
		return isActive() && arm_state != GateArmState.CLOSED;
	}

	/** Test if gate arm is moving */
	private boolean isMoving() {
		return isOnline() &&
		      (arm_state == GateArmState.OPENING ||
		       arm_state == GateArmState.CLOSING);
	}

	/** Test if gate arm needs maintenance */
	private boolean needsMaintenance() {
		return isOnline() && arm_state == GateArmState.FAULT;
	}

	/** Set the item style bits (and notify clients) */
	private void setStyles(long s) {
		if(s != styles) {
			styles = s;
			notifyAttribute("styles");
		}
	}

	/** Get item style bits */
	@Override public long getStyles() {
		return styles;
	}
}
