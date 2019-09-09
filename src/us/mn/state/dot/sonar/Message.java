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

import java.util.List;

/**
 * A message defined for the SONAR wire protocol
 *
 * @author Douglas Lau
 */
public enum Message {

	/** Separator between messages (ASCII record separator) */
	RECORD_SEP('\u001e'),

	/** Separator between message parameters (ASCII unit separator) */
	UNIT_SEP('\u001f'),

	/** Null reference code point (ASCII NUL) */
	NULL_REF('\u0000'),

	/** Login and authenticate a user */
	LOGIN('l') { public void handle(Conduit c, List<String> p)
		throws SonarException
	{
		c.doLogin(p);
	}},

	/** Change the user's password */
	PASSWORD('p') { public void handle(Conduit c, List<String> p)
		throws SonarException
	{
		c.doPassword(p);
	}},

	/** Quit the client connection */
	QUIT('q') { public void handle(Conduit c, List<String> p)
		throws SonarException
	{
		c.doQuit(p);
	}},

	/** Enumerate a SONAR name */
	ENUMERATE('e') { public void handle(Conduit c, List<String> p)
		throws SonarException
	{
		c.doEnumerate(p);
	}},

	/** Ignore a SONAR name */
	IGNORE('i') { public void handle(Conduit c, List<String> p)
		throws SonarException
	{
		c.doIgnore(p);
	}},

	/** Add a SONAR object */
	OBJECT('o') { public void handle(Conduit c, List<String> p)
		throws SonarException
	{
		c.doObject(p);
	}},

	/** Remove a SONAR name */
	REMOVE('r') { public void handle(Conduit c, List<String> p)
		throws SonarException
	{
		c.doRemove(p);
	}},

	/** Set a SONAR attribute */
	ATTRIBUTE('a') { public void handle(Conduit c, List<String> p)
		throws SonarException
	{
		c.doAttribute(p);
	}},

	/** Change the SONAR type */
	TYPE('t') { public void handle(Conduit c, List<String> p)
		throws SonarException
	{
		c.doType(p);
	}},

	/** Show the client a message */
	SHOW('s') { public void handle(Conduit c, List<String> p)
		throws SonarException
	{
		c.doShow(p);
	}};

	/** Message code */
	public final char code;

	/** Create a new message type */
	private Message(char c) {
		code = c;
	}

	/** Handle a received message */
	public void handle(Conduit c, List<String> p) throws SonarException {
		throw ProtocolError.invalidMessageCode();
	}
}
