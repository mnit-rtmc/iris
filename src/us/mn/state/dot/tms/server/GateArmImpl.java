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

import java.io.File;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DeviceRequest;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.GateArmState;
import us.mn.state.dot.tms.GeoLoc;
import us.mn.state.dot.tms.ItemStyle;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.TMSException;
import us.mn.state.dot.tms.server.comm.GateArmPoller;
import us.mn.state.dot.tms.server.comm.MessagePoller;

/**
 * Gate Arm Device.
 *
 * @author Douglas Lau
 */
public class GateArmImpl extends DeviceImpl implements GateArm {

	/** Path to configuration enable file.  This must not be a system
	 * attribute for security reasons. */
	static private final File CONFIG_ENABLE = new File(
		"/var/lib/iris/gate_arm_enable");

	/** Config override flag */
	static private boolean CONFIG_FLAG = true;

	/** Test whether gate arm configuration is enabled */
	static private boolean isConfigEnabled() {
		return CONFIG_FLAG &&
		       CONFIG_ENABLE.isFile() &&
		       CONFIG_ENABLE.canRead() &&
		       CONFIG_ENABLE.canWrite();
	}

	/** Disable gate arm configuration */
	static public void disableConfig() {
		if(isConfigEnabled())
			CONFIG_FLAG = CONFIG_ENABLE.delete();
		System.err.println(new Date().toString() +
			": " + CONFIG_ENABLE.toString() + ", " + CONFIG_FLAG);
	}

	/** Load all the gate arms */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, GateArmImpl.class);
		store.query("SELECT name, geo_loc, controller, pin, notes, " +
			"camera, approach, dms, open_msg, closed_msg " +
			"FROM iris." + SONAR_TYPE  + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new GateArmImpl(namespace,
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

	/** Create a new gate arm with a string name */
	public GateArmImpl(String n) throws TMSException, SonarException {
		super(n);
		GeoLocImpl g = new GeoLocImpl(name);
		g.notifyCreate();
		geo_loc = g;
	}

	/** Create a gate arm */
	private GateArmImpl(String n, GeoLocImpl loc, ControllerImpl c,
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

	/** Create a gate arm */
	private GateArmImpl(Namespace ns, String n, String loc, String c,
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

	/** Software version */
	private transient String version;

	/** Set the version */
	public void setVersion(String v) {
		if(!v.equals(version)) {
			version = v;
			notifyAttribute("version");
			ControllerImpl c = (ControllerImpl)getController();
			if(c != null)
				c.setVersion(version);
		}
	}

	/** Get the version */
	@Override public String getVersion() {
		return version;
	}

	/** Request a device operation */
	@Override public void setDeviceRequest(int r) {
		DeviceRequest req = checkRequest(r);
		if(isConfigEnabled() && req != null) {
			GateArmPoller p = getGateArmPoller();
			if(p != null)
				p.sendRequest(this, req);
		}
	}

	/** Check a device request for valid gate arm requests */
	private DeviceRequest checkRequest(int r) {
		DeviceRequest req = DeviceRequest.fromOrdinal(r);
		switch(req) {
		case QUERY_STATUS:
		case RESET_DEVICE:
			return req;
		}
		return null;
	}

	/** The owner of the next state to be requested.  This is a write-only
	 * SONAR attribute. */
	private transient User ownerNext;

	/** Set the next state owner.  When a user sends a new state to the
	 * gate arm, two attributes must be set: ownerNext and armState.  There
	 * can be a race between two clients setting these attributes.  If
	 * ownerNext is non-null when being set, then a race has been detected,
	 * meaning two clients are trying to set the state at the same time. */
	@Override public synchronized void setOwnerNext(User o) {
		if(ownerNext != null && o != null) {
			System.err.println("GateArmImpl.setOwnerNext: " +
				getName() + ", " + ownerNext.getName() +
				" vs. " + o.getName());
			ownerNext = null;
		} else
			ownerNext = o;
	}

	/** Gate arm state */
	private transient GateArmState arm_state = GateArmState.UNKNOWN;

	/** Set the arm state */
	@Override public void setArmState(int gas) {
		// FIXME: log changes to gate arm state
		arm_state = GateArmState.fromOrdinal(gas);
	}

	/** Set the arm state */
	public void doSetArmState(int gas) throws TMSException {
		User o_next = ownerNext;	// Avoid race
		// ownerNext is only valid for one message, clear it
		ownerNext = null;
		if(o_next == null)
			throw new ChangeVetoException("MUST SET OWNER FIRST");
		doSetArmState(gas, o_next);
	}

	/** Set the arm state */
	private synchronized void doSetArmState(int gas, User o)
		throws TMSException
	{
		GateArmState s = GateArmState.fromOrdinal(gas);
		if(validateStateReq(s) && isConfigEnabled()) {
			// FIXME: check for conflicts
			GateArmPoller p = getGateArmPoller();
			if(p != null)
				doSetArmState(s, o, p);
		}
	}

	/** Validate a new gate arm state */
	private boolean validateStateReq(GateArmState gas) throws TMSException {
		if(gas == arm_state)
			return false;
		switch(arm_state) {
		case CLOSED:
			if(gas == GateArmState.OPENING)
				return true;
			break;
		case OPEN:
			if(gas == GateArmState.WARN_CLOSE)
				return true;
			break;
		case WARN_CLOSE:
			if(gas == GateArmState.CLOSING)
				return true;
			break;
		}
		throw new ChangeVetoException("INVALID STATE CHANGE: " +
			arm_state + " to " + gas);
	}

	/** Set the arm state.
	 * @param gas Requested gate arm state.
	 * @param o User requesting new state.
	 * @param p Gate arm poller. */
	private void doSetArmState(GateArmState gas, User o, GateArmPoller p) {
		setArmState(gas.ordinal());
		switch(gas) {
		case OPENING:
			p.openGate(this, o);
			break;
		case CLOSING:
			p.closeGate(this, o);
			break;
		default:
			break;
		}
	}

	/** Get the arm state */
	@Override public int getArmState() {
		return arm_state.ordinal();
	}

	/** Item style bits */
	private transient long styles = calculateStyles();

	/** Calculate item styles */
	private long calculateStyles() {
		long s = ItemStyle.ALL.bit();
		if(getController() == null)
			s |= ItemStyle.NO_CONTROLLER.bit();
		if(isClosed())
			s |= ItemStyle.CLOSED.bit();
		if(isOpen())
			s |= ItemStyle.OPEN.bit();
		if(isMoving())
			s |= ItemStyle.MOVING.bit();
		if(needsMaintenance())
			s |= ItemStyle.MAINTENANCE.bit();
		if(isFailed())
			s |= ItemStyle.FAILED.bit();
		if(!isActive())
			s |= ItemStyle.INACTIVE.bit();
		return s;
	}

	/** Update the item styles */
	public void updateStyles() {
		setStyles(calculateStyles());
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
	private boolean isOpen() {
		return arm_state != GateArmState.CLOSED;
	}

	/** Test if gate arm is moving */
	private boolean isMoving() {
		return arm_state == GateArmState.OPENING ||
		       arm_state == GateArmState.CLOSING;
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

	/** Get the gate arm poller */
	private GateArmPoller getGateArmPoller() {
		MessagePoller mp = getPoller();
		if(mp instanceof GateArmPoller)
			return (GateArmPoller)mp;
		else
			return null;
	}
}
