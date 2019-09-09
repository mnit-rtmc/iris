/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.sonar;

/**
 * This exception indicates problems with a SONAR request or server.
 *
 * @author Douglas Lau
 */
public class SonarException extends Exception {

	/** Create a new SONAR exception */
	public SonarException(String m) {
		super(m);
	}

	/** Create a new SONAR exception caused by another exception */
	public SonarException(Exception cause) {
		super(cause);
	}

	/** Get the message for the root cause of the exception */
	private String getRootCause() {
		Throwable c = getCause();
		while (c.getCause() != null)
			c = c.getCause();
		String m = c.getMessage();
		if (m != null)
			return m;
		else
			return c.getClass().getName();
	}

	/** Get a (non-null) message for the specified exception */
	@Override
	public String getMessage() {
		if (getCause() == null)
			return super.getMessage();
		else
			return getRootCause();
	}
}
