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
 * This exception indicates that a client has made a protocol error
 *
 * @author Douglas Lau
 */
public class ProtocolError extends SonarException {

	/** Create a new protocol error */
	private ProtocolError(String m) {
		super("Protocol error: " + m);
	}

	/** Create authentication required exception */
	static public ProtocolError authenticationRequired() {
		return new ProtocolError("Authentication required");
	}

	/** Create "already logged in" exception */
	static public ProtocolError alreadyLoggedIn() {
		return new ProtocolError("Already logged in");
	}

	/** Create "invalid message code" exception */
	static public ProtocolError invalidMessageCode() {
		return new ProtocolError("Invalid message code");
	}

	/** Create "wrong parameter count" exception */
	static public ProtocolError wrongParameterCount() {
		return new ProtocolError("Wrong number of parameters");
	}

	/** Create "invalid parameter" exception */
	static public ProtocolError invalidParameter() {
		return new ProtocolError("Invalid parameter");
	}
}
