/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm;

/**
 * Operation to kill the message poller thread
 *
 * @author Douglas Lau
 */
public class KillThread<T extends ControllerProperty> extends Operation<T> {

	/** Create a new operation to kill the message poller thread */
	public KillThread() {
		super(PriorityLevel.URGENT);
	}

	/** Create the first phase of the operation */
	protected Phase<T> phaseOne() {
		return null;
	}
}
