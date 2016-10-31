/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
import java.util.HashMap;
import java.util.Map;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSIndication;
import us.mn.state.dot.tms.TMSException;

/**
 * A lane-use control sign indication is a mapping of a controller I/O pin
 * with a specific lane-use indication.
 *
 * @author Douglas Lau
 */
public class LCSIndicationImpl extends BaseObjectImpl implements LCSIndication {

	/** Load all the LCS indications */
	static protected void loadAll() throws TMSException {
		namespace.registerType(SONAR_TYPE, LCSIndicationImpl.class);
		store.query("SELECT name, controller, pin, lcs, indication " +
			"FROM iris." + SONAR_TYPE + ";", new ResultFactory()
		{
			public void create(ResultSet row) throws Exception {
				namespace.addObject(new LCSIndicationImpl(
					namespace,
					row.getString(1),	// name
					row.getString(2),	// controller
					row.getInt(3),		// pin
					row.getString(4),	// lcs
					row.getInt(5)		// indication
				));
			}
		});
	}

	/** Get a mapping of the columns */
	public Map<String, Object> getColumns() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("name", name);
		map.put("controller", controller);
		map.put("pin", pin);
		map.put("lcs", lcs);
		map.put("indication", indication);
		return map;
	}

	/** Get the database table name */
	public String getTable() {
		return "iris." + SONAR_TYPE;
	}

	/** Get the SONAR type name */
	public String getTypeName() {
		return SONAR_TYPE;
	}

	/** Create a new LCS indication */
	public LCSIndicationImpl(String n) {
		super(n);
	}

	/** Create a new LCS indication */
	public LCSIndicationImpl(Namespace ns, String n, String c, int p,
		String l, int i)
	{
		this(n,(ControllerImpl)ns.lookupObject(Controller.SONAR_TYPE,c),
		     p, (LCSImpl)ns.lookupObject(LCS.SONAR_TYPE, l), i);
	}

	/** Create a new LCS indication */
	public LCSIndicationImpl(String n, ControllerImpl c, int p, LCSImpl l,
		int i)
	{
		this(n);
		controller = c;
		pin = p;
		lcs = l;
		indication = i;
		initTransients();
	}

	/** Initialize the controller for this LCS indicaiton */
	public void initTransients() {
		updateControllerPin(null, 0, controller, pin);
	}

	/** Controller associated with this LCS indication */
	protected ControllerImpl controller;

	/** Update the controller and/or pin.
	 * @param oc Old controller.
	 * @param op Old pin.
	 * @param nc New controller.
	 * @param np New pin. */
	protected void updateControllerPin(ControllerImpl oc, int op,
		ControllerImpl nc, int np)
	{
		if(oc != null)
			oc.setIO(op, null);
		if(nc != null)
			nc.setIO(np, this);
	}

	/** Set the controller of the LCS indication */
	public void setController(Controller c) {
		controller = (ControllerImpl)c;
	}

	/** Set the controller of the LCS indication */
	public void doSetController(Controller c) throws TMSException {
		if(c == controller)
			return;
		if(pin < 1 || pin > Controller.ALL_PINS)
			throw new ChangeVetoException("Invalid pin: " + pin);
		store.update(this, "controller", c);
		updateControllerPin(controller, pin, (ControllerImpl)c, pin);
		setController(c);
	}

	/** Get the controller to which this LCS indication is assigned */
	public Controller getController() {
		return controller;
	}

	/** Controller I/O pin number */
	protected int pin;

	/** Set the controller I/O pin number */
	public void setPin(int p) {
		pin = p;
	}

	/** Set the controller I/O pin number */
	public void doSetPin(int p) throws TMSException {
		if(p == pin)
			return;
		if(p < 1 || p > Controller.ALL_PINS)
			throw new ChangeVetoException("Invalid pin: " + p);
		store.update(this, "pin", p);
		updateControllerPin(controller, pin, controller, p);
		setPin(p);
	}

	/** Get the controller I/O pin number */
	public int getPin() {
		return pin;
	}

	/** LCS associated with this indication */
	protected LCSImpl lcs;

	/** Get the LCS */
	public LCS getLcs() {
		return lcs;
	}

	/** Ordinal of LaneUseIndication */
	protected int indication;

	/** Get the indication (ordinal of LaneUseIndication) */
	public int getIndication() {
		return indication;
	}

	/** Get item style bits */
	@Override
	public long getStyles() {
		return 0;
	}

	/** Destroy an indication */
	public void doDestroy() throws TMSException {
		// Don't allow an indication to be destroyed if it is assigned
		// to a controller.  This is needed because the Controller
		// io_pins HashMap will still have a reference to it.
		if(controller != null) {
			throw new ChangeVetoException("Indication must be " +
				"removed from controller before being " +
				"destroyed: " + name);
		}
		super.doDestroy();
	}
}
