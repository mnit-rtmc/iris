/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.comm;

import java.io.IOException;
import us.mn.state.dot.tms.ControllerImpl;
import us.mn.state.dot.tms.utils.SString;

/**
 * An operation which is performed on a field controller.
 *
 * @author Douglas Lau
 */
abstract public class ControllerOperation extends Operation {

	/** Get a message describing an IO exception */
	static protected String exceptionMessage(IOException e) {
		String m = e.getMessage();
		if(m != null && m.length() > 0)
			return m;
		else
			return e.getClass().getSimpleName();
	}

	/** Filter a message */
	static protected String filterMessage(String m) {
		final int MAXLEN = 64;
		return SString.truncate(m, MAXLEN);
	}

	/** Controller to be polled */
	protected final ControllerImpl controller;

	/** Get the controller being polled */
	public ControllerImpl getController() {
		return controller;
	}

	/** Drop address of the controller to be polled */
	protected final int drop;

	/** Device ID */
	protected final String id;

	/** Error status message */
	protected String errorStatus = null;

	/** Create a new controller operation */
	protected ControllerOperation(int p, ControllerImpl c, String i) {
		super(p);
		controller = c;
		drop = controller.getDrop();
		id = i;
	}

	/** Create a new controller operation */
	protected ControllerOperation(int p, ControllerImpl c) {
		this(p, c, c.toString());
	}

	/** Start an operation on the device */
	public void start() {
		if(controller != null)
			controller.addOperation(this);
	}

	/** Get a string description of the operation */
	public String toString() {
		return super.toString() + " (" + id + ")";
	}

	/** Handle an exception */
	public void handleException(IOException e) {
		controller.logException(id, filterMessage(exceptionMessage(e)));
		if(!controller.retry(id))
			super.handleException(e);
	}

	/** Cleanup the operation */
	public void cleanup() {
		if(errorStatus != null)
			controller.setError(filterMessage(errorStatus));
		controller.completeOperation(id, success);
	}
}
