/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2008  Minnesota Department of Transportation
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

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import us.mn.state.dot.tms.ControllerImpl;

/**
 * An operation which is performed on a field controller.
 *
 * @author Douglas Lau
 */
abstract public class ControllerOperation extends Operation {

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
		String s = e.getMessage();
		controller.logException(id, s);
		// FIXME: this is a bit fragile
		boolean r = (e instanceof ParsingException) ||
			(e instanceof SocketTimeoutException) ||
			(e instanceof EOFException);
		if(r && controller.retry(id))
			return;
		else
			super.handleException(e);
	}

	/** Cleanup the operation */
	public void cleanup() {
		controller.completeOperation(id, success);
	}
}
