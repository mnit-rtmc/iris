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

import java.io.IOException;
import java.util.List;

/**
 * A conduit represents either a server or client connection.
 *
 * @author Douglas Lau
 */
abstract public class Conduit {

	/** Get the name of the conduit */
	abstract public String getName();

	/** Flag to indicate that the conduit is connected */
	protected boolean connected = false;

	/** Test if the conduit is connected */
	public boolean isConnected() {
		return connected;
	}

	/** Disconnect the conduit */
	protected void disconnect() {
		connected = false;
	}

	/** Flush out all outgoing data in the conduit */
	abstract public void flush() throws IOException;

	/** Enable writing data to the conduit */
	abstract protected void enableWrite();

	/** Disable writing data to the conduit */
	abstract protected void disableWrite();

	/** Handle a LOGIN message */
	public void doLogin(List<String> p) throws SonarException {
		throw ProtocolError.invalidMessageCode();
	}

	/** Handle a PASSWORD message */
	public void doPassword(List<String> p) throws SonarException {
		throw ProtocolError.invalidMessageCode();
	}

	/** Handle a QUIT message */
	abstract public void doQuit(List<String> p) throws SonarException;

	/** Handle an ENUMERATE message */
	public void doEnumerate(List<String> p) throws SonarException {
		throw ProtocolError.invalidMessageCode();
	}

	/** Handle an IGNORE message */
	public void doIgnore(List<String> p) throws SonarException {
		throw ProtocolError.invalidMessageCode();
	}

	/** Handle an OBJECT message */
	abstract public void doObject(List<String> p) throws SonarException;

	/** Handle an ATTRIBUTE message */
	abstract public void doAttribute(List<String> p) throws SonarException;

	/** Handle a REMOVE message */
	abstract public void doRemove(List<String> p) throws SonarException;

	/** Handle a TYPE message */
	public void doType(List<String> p) throws SonarException {
		throw ProtocolError.invalidMessageCode();
	}

	/** Handle a SHOW message */
	public void doShow(List<String> p) throws SonarException {
		throw ProtocolError.invalidMessageCode();
	}
}
