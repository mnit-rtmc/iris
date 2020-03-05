/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2019  Minnesota Department of Transportation
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
package us.mn.state.dot.sonar.server;

import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.SonarException;

/**
 * This exception indicates an attempt to access a name without permission
 *
 * @author Douglas Lau
 */
public class PermissionDenied extends SonarException {

	/** Create a new permission denied exception */
	private PermissionDenied(String m) {
		super("Permission denied: " + m);
	}

	/** Create an "authentication failed" error */
	static public PermissionDenied authenticationFailed() {
		return new PermissionDenied("Authentication failed");
	}

	/** Create a "cannot add" error */
	static public PermissionDenied cannotAdd() {
		return new PermissionDenied("Unable to add object");
	}

	/** Create a "cannot remove" error */
	static public PermissionDenied cannotRemove() {
		return new PermissionDenied("Unable to remove object");
	}

	/** Create a "cannot read" error */
	static public PermissionDenied cannotRead(String attr) {
		return new PermissionDenied("Unable to read attribute: " +attr);
	}

	/** Create a "cannot write" error */
	static public PermissionDenied cannotWrite(String attr) {
		return new PermissionDenied("Unable to write attribute: "+attr);
	}

	/** Create a new "insufficient privileges" exception */
	static public PermissionDenied create(Name n) {
		return new PermissionDenied("Insufficient privileges: " +
			n.toString());
	}
}
