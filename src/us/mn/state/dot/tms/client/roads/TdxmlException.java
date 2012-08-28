/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

/**
 * Exception to encapsulate other errors.
 *
 * @author Erik Engstrom
 */
public class TdxmlException extends Exception {

	/** Create a new TDXML exception. */
	public TdxmlException() {
		super();
	}

	/** Create a new TDXML exception.
	 * @param message */
	public TdxmlException(String message) {
		super(message);
	}

	/** Create a new TDXML exception.
	 * @param message
	 * @param cause */
	public TdxmlException(String message, Throwable cause) {
		super(message, cause);
	}

	/** Create a new TDXML exception.
	 * @param cause */
	public TdxmlException(Throwable cause) {
		super(cause);
	}
}
