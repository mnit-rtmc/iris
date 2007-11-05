/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006  Minnesota Department of Transportation
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

/**
 * An interface to handle operations in the poll queue.
 *
 * @author Douglas Lau
 */
public interface OperationHandler {

	/** Do something with one of the operations */
	public void handle(int priority, Operation o);
}
