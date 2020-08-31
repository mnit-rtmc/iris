/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.TMSException;

/**
 * ControllerIoImpl is an abstract class for controller I/O.
 *
 * @author Douglas Lau
 */
abstract public class ControllerIoImpl extends BaseObjectImpl
	implements ControllerIO
{
	/** Create a new controller I/O */
	protected ControllerIoImpl(String n, ControllerImpl c, int p) {
		super(n);
		controller = c;
		pin = p;
	}

	/** Initialize the controller */
	@Override
	public void initTransients() {
		updateControllerPin(null, 0, controller, pin);
	}

	/** Controller associated with this IO */
	protected ControllerImpl controller;

	/** Update the controller and/or pin.
	 * @param oc Old controller.
	 * @param op Old pin.
	 * @param nc New controller.
	 * @param np New pin. */
	protected void updateControllerPin(ControllerImpl oc, int op,
		ControllerImpl nc, int np)
	{
		if (oc != null)
			oc.setIO(op, null);
		if (nc != null)
			nc.setIO(np, this);
		updateStyles();
	}

	/** Set the controller for the I/O */
	@Override
	public void setController(Controller c) {
		controller = (c instanceof ControllerImpl)
			? (ControllerImpl) c
			: null;
	}

	/** Set the controller for the I/O */
	public void doSetController(Controller c) throws TMSException {
		ControllerImpl ci = (c instanceof ControllerImpl)
			? (ControllerImpl) c
			: null;
		if (ci != controller)
			doSetControllerImpl(ci);
	}

	/** Set the controller for the I/O */
	protected void doSetControllerImpl(ControllerImpl c)
		throws TMSException
	{
		checkControllerPin(c, pin);
		ControllerImpl oc = controller;
		store.update(this, "controller", c);
		setController(c);
		// Do this last so updateStyles sees updates
		updateControllerPin(oc, pin, c, pin);
	}

	/** Check controller and pin */
	protected void checkControllerPin(ControllerImpl c, Integer p)
		throws ChangeVetoException
	{
		if (c != null && controller != null && c != controller)
			throw new ChangeVetoException("I/O has controller");
		if (p != null) {
			if (p < 1 || p > Controller.ALL_PINS) {
				throw new ChangeVetoException(
					"Invalid pin: " + p);
			}
			if (c != null && c.getIO(p) != null) {
				throw new ChangeVetoException(
					"Unavailable pin: " + p);
			}
		}
	}

	/** Get the controller for the I/O */
	@Override
	public Controller getController() {
		return controller;
	}

	/** Controller I/O pin number */
	protected int pin;

	/** Set the controller I/O pin number */
	@Override
	public void setPin(int p) {
		pin = p;
	}

	/** Set the controller I/O pin number */
	public void doSetPin(int p) throws TMSException {
		if (p != pin) {
			checkControllerPin(controller, p);
			int op = pin;
			store.update(this, "pin", p);
			setPin(p);
			// Do this last so updateStyles sees updates
			updateControllerPin(controller, op, controller, p);
		}
	}

	/** Get the controller I/O pin number */
	@Override
	public int getPin() {
		return pin;
	}

	/** Update item styles */
	public void updateStyles() {
		// subclasses may override
	}

	/** Get item style bits */
	@Override
	public long getStyles() {
		return 0; // subclasses may override
	}

	/** Perform a periodic poll */
	public void periodicPoll(boolean is_long) {
		// subclasses may override
	}

	/** Destroy a controller I/O */
	@Override
	public void doDestroy() throws TMSException {
		// Don't allow a controller I/O to be destroyed if it is
		// assigned to a controller.  This is needed because the
		// Controller io_pins HashMap will still have a reference.
		if (controller != null) {
			throw new ChangeVetoException("Must be removed from " +
				"controller before being destroyed: " + name);
		}
		super.doDestroy();
	}
}
