/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2024  Minnesota Department of Transportation
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

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.BufferOverflowException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.net.ssl.SSLException;
import static us.mn.state.dot.sched.TimeSteward.currentTimeMillis;
import us.mn.state.dot.sonar.Conduit;
import us.mn.state.dot.sonar.Connection;
import us.mn.state.dot.sonar.Message;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.NamespaceError;
import us.mn.state.dot.sonar.ProtocolError;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.SSLState;
import us.mn.state.dot.tms.AccessLevel;
import us.mn.state.dot.tms.User;
import us.mn.state.dot.tms.server.UserImpl;

/**
 * A connection encapsulates the state of one client connection on the server.
 *
 * @author Douglas Lau
 */
public class ConnectionImpl extends Conduit implements Connection {

	/** Check if a name is watch positive. This means that the name can be
	 * used as a positive entry in the watching set. */
	static protected boolean isWatchPositive(Name name) {
		return name.isType() || name.isObject();
	}

	/** Check if a name is watch negative. This means that the name can
	 * be used as a negative entry in the watching set. */
	static protected boolean isWatchNegative(Name name) {
		return name.isAttribute() && name.getObjectPart().equals("");
	}

	/** Define the set of valid messages from a client connection */
	static protected final EnumSet<Message> MESSAGES = EnumSet.of(
		Message.LOGIN, Message.PASSWORD, Message.QUIT,
		Message.ENUMERATE, Message.IGNORE, Message.OBJECT,
		Message.REMOVE, Message.ATTRIBUTE);

	/** Lookup a message from the specified message code */
	static protected Message lookupMessage(char code) throws ProtocolError {
		for (Message m: MESSAGES)
			if (code == m.code)
				return m;
		throw ProtocolError.invalidMessageCode();
	}

	/** Random number generator for session IDs */
	static protected final Random RAND = new Random();

	/** Create a new session ID */
	static protected long createSessionId() {
		return RAND.nextLong();
	}

	/** Client host name and port */
	protected final String hostport;

	/** Get the SONAR object name */
	@Override
	public String getName() {
		return hostport;
	}

	/** Get notes (including hashtags) */
	@Override
	public String getNotes() {
		return null;
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return hostport;
	}

	/** User logged in on the connection.
	 * May be null (before a successful login). */
	protected UserImpl user;

	/** Get the user logged in on the connection.
	 * May be null (before a successful login). */
	@Override
	public User getUser() {
		return user;
	}

	/** Get the name of the user logged in on the connection */
	public String getUserName() {
		UserImpl u = user;
		return u != null ? u.getName() : "Unauthenticated";
	}

	/** Session ID (random cookie) */
	protected final long sessionId;

	/** Get the SONAR session ID */
	@Override
	public long getSessionId() {
		return sessionId;
	}

	/** Task processor */
	private final TaskProcessor processor;

	/** SONAR namepsace */
	protected final ServerNamespace namespace;

	/** Selection key for the socket channel */
	protected final SelectionKey skey;

	/** Channel to client */
	protected final SocketChannel channel;

	/** Inet address of client */
	private final InetAddress address;

	/** SSL state for encrypting network data */
	protected final SSLState state;

	/** Set of names the connection is watching */
	protected final Set<String> watching = new HashSet<String>();

	/** Phantom object for setting attributes before storing a new object
	 * in the database. */
	protected SonarObject phantom;

	/** Create a new connection */
	public ConnectionImpl(TaskProcessor p, SelectionKey k, SocketChannel c)
		throws SSLException, IOException
	{
		processor = p;
		namespace = processor.getNamespace();
		skey = k;
		channel = c;
		state = processor.createSSLState(this);
		address = c.socket().getInetAddress();
		StringBuilder h = new StringBuilder();
		h.append(address.getHostAddress());
		h.append(':');
		h.append(c.socket().getPort());
		hostport = h.toString();
		user = null;
		connected = true;
		sessionId = createSessionId();
	}

	/** Get the inet address */
	public InetAddress getAddress() {
		return address;
	}

	/** Start watching the specified name */
	protected void startWatching(Name name) {
		synchronized (watching) {
			watching.remove(name.toString());
			if (isWatchPositive(name))
				watching.add(name.toString());
		}
	}

	/** Stop watching the specified name */
	protected void stopWatching(Name name) {
		synchronized (watching) {
			watching.remove(name.toString());
			if (isWatchNegative(name))
				watching.add(name.toString());
		}
	}

	/** Check if the connection is watching a name */
	protected boolean isWatching(Name name) {
		synchronized (watching) {
			// Object watch is highest priority (positive)
			if (watching.contains(name.getObjectName()))
				return true;
			// Attribute watch is middle priority (negative)
			if (watching.contains(name.getAttributeName()))
				return false;
			// Type watch is lowest priority (positive)
			return watching.contains(name.getTypePart());
		}
	}

	/** Destroy the connection */
	public void destroy() {
		if (isConnected())
			disconnect("Connection destroyed");
	}

	/** Disconnect the client connection.
	 * This may only be called on the Task Processor thread. */
	protected void disconnect() {
		super.disconnect();
		synchronized (watching) {
			watching.clear();
		}
		processor.disconnect(skey);
		try {
			channel.close();
		}
		catch (IOException e) {
			TaskProcessor.DEBUG.log("Close error: " +
				e.getMessage() + " on " + getName());
		}
	}

	/** Disconnect the client connection.
	 * This may only be called on the Task Processor thread. */
	protected void disconnect(String msg) {
		TaskProcessor.DEBUG.log(msg + " on " + getName() + ", " +
			getUserName());
		disconnect();
	}

	/** Read messages from the socket channel.
	 * This may only be called on the Server thread. */
	void doRead() throws IOException {
		int nbytes;
		ByteBuffer net_in = state.getNetInBuffer();
		synchronized (net_in) {
			nbytes = channel.read(net_in);
		}
		if (nbytes > 0)
			processor.processMessages(this);
		else if (nbytes < 0)
			throw new EOFException();
	}

	/** Write pending data to the socket channel.
	 * This may only be called on the Server thread. */
	void doWrite() throws IOException {
		ByteBuffer net_out = state.getNetOutBuffer();
		synchronized (net_out) {
			net_out.flip();
			channel.write(net_out);
			if (!net_out.hasRemaining())
				disableWrite();
			net_out.compact();
		}
		processor.flush(this);
	}

	/** Enable writing data back to the client */
	@Override
	public void enableWrite() {
		skey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		skey.selector().wakeup();
	}

	/** Disable writing data back to the client */
	@Override
	public void disableWrite() {
		skey.interestOps(SelectionKey.OP_READ);
		skey.selector().wakeup();
	}

	/** Notify the client of a new object being added.
	 * This may only be called on the Task Processor thread. */
	protected void notifyObject(SonarObject o) {
		try {
			namespace.enumerateObject(state.encoder, o);
			flush();
		}
		catch (SonarException e) {
			disconnect("Notify error: " + e.getMessage());
		}
		catch (IOException e) {
			disconnect("Notify error: " + e.getMessage());
		}
	}

	/** Notify the client of a new object being added.
	 * This may only be called on the Task Processor thread. */
	void notifyObject(Name name, SonarObject o) {
		if (isWatching(name))
			notifyObject(o);
	}

	/** Notify the client of an attribute change.
	 * This may only be called on the Task Processor thread. */
	void notifyAttribute(Name name, String[] params) {
		User u = user;
		if (u != null &&
		    isWatching(name) &&
		    namespace.accessLevel(name, u) >= AccessLevel.VIEW.ordinal())
		{
			notifyAttribute(name.toString(), params);
		}
	}

	/** Notify the client of an attribute change.
	 * This may only be called on the Task Processor thread. */
	private void notifyAttribute(String name, String[] params) {
		try {
			state.encoder.encode(Message.ATTRIBUTE, name, params);
			flush();
		}
		catch (IOException e) {
			disconnect("I/O error: notifyAttribute " + name);
		}
	}

	/** Notify the client of a name being removed.
	 * This may only be called on the Task Processor thread. */
	void notifyRemove(Name name) {
		if (isWatching(name)) {
			notifyRemove(name.toString());
			stopWatching(name);
		}
	}

	/** Notify the client of a name being removed.
	 * This may only be called on the Task Processor thread. */
	protected void notifyRemove(String name) {
		try {
			state.encoder.encode(Message.REMOVE, name);
			flush();
		}
		catch (IOException e) {
			disconnect("I/O error: notifyRemove " + name);
		}
	}

	/** Check that the client is logged in */
	protected void checkLoggedIn() throws SonarException {
		if (user == null)
			throw ProtocolError.authenticationRequired();
	}

	/** Process any incoming messages.
	 * This may only be called on the Task Processor thread. */
	void processMessages() {
		if (!isConnected())
			return;
		try {
			_processMessages();
		}
		catch (SSLException e) {
			disconnect("SSL error " + e.getMessage());
		}
		catch (IOException e) {
			disconnect("I/O error: processMessages");
		}
	}

	/** Process any incoming messages.
	 * This may only be called on the Task Processor thread. */
	protected void _processMessages() throws SSLException, IOException {
		while (state.doRead()) {
			List<String> params = state.decoder.decode();
			while (params != null) {
				processMessage(params);
				params = state.decoder.decode();
			}
		}
		flush();
	}

	/** Process one message from the client.
	 * This may only be called on the Task Processor thread. */
	protected void processMessage(List<String> params)
		throws IOException
	{
		try {
			if (params.size() > 0)
				_processMessage(params);
		}
		catch (SonarException e) {
			state.encoder.encode(Message.SHOW, e.getMessage());
			TaskProcessor.DEBUG.log("Message error: " +
				e.getMessage());
		}
	}

	/** Process one message from the client.
	 * This may only be called on the Task Processor thread. */
	protected void _processMessage(List<String> params)
		throws SonarException
	{
		String c = params.get(0);
		if (c.length() != 1)
			throw ProtocolError.invalidMessageCode();
		_processMessage(lookupMessage(c.charAt(0)), params);
	}

	/** Process one message from the client.
	 * This may only be called on the Task Processor thread. */
	private void _processMessage(Message m, List<String> params)
		throws SonarException
	{
		final boolean op = TaskProcessor.DEBUG_TIME.isOpen();
		final long st = (op) ? currentTimeMillis() : 0;
		try {
			m.handle(this, params);
		}
		finally {
			if (op) {
				long el = currentTimeMillis() - st;
				TaskProcessor.debugElapsed(m.toString(), el);
			}
		}
	}

	/** Start writing data to client.
	 * This may only be called on the Task Processor thread. */
	protected void startWrite() throws IOException {
		if (state.shouldWrite())
			state.doWrite();
	}

	/** Tell the I/O thread to flush the output buffer.
	 * This may only be called on the Task Processor thread. */
	@Override
	public void flush() {
		try {
			state.encoder.flush();
			if (isConnected())
				startWrite();
		}
		catch (BufferOverflowException e) {
			disconnect("Buffer overflow error");
		}
		catch (IOException e) {
			disconnect("I/O error: " + e.getMessage());
		}
	}

	/** Respond to a LOGIN message.
	 * This may only be called on the Task Processor thread. */
	@Override
	public void doLogin(List<String> params) throws SonarException {
		if (user != null)
			throw ProtocolError.alreadyLoggedIn();
		if (params.size() != 3)
			throw ProtocolError.wrongParameterCount();
		String name = params.get(1);
		String password = params.get(2);
		doLogin(name, password.toCharArray());
	}

	/** Login a user */
	private void doLogin(String name, char[] password) {
		processor.authenticate(this, name, password);
	}

	/** Finish a LOGIN after user has been authenticated.
	 * This may only be called on the Task Processor thread. */
	public void finishLogin(UserImpl u) {
		try {
			user = u;
			// The first TYPE message indicates a successful login
			state.encoder.encode(Message.TYPE);
			// Send the connection name to the client first
			state.encoder.encode(Message.SHOW, hostport);
			flush();
		}
		catch (IOException e) {
			disconnect("I/O error: finishLogin " + e.getMessage());
		}
	}

	/** Fail a LOGIN attempt.
	 * This may only be called on the Task Processor thread. */
	public void failLogin() {
		try {
			state.encoder.encode(Message.SHOW, PermissionDenied.
				authenticationFailed().getMessage());
			flush();
		}
		catch (IOException e) {
			disconnect("I/O error: failLogin " + e.getMessage());
		}
	}

	/** Respond to a PASSWORD message */
	@Override
	public void doPassword(List<String> params) throws SonarException {
		checkLoggedIn();
		if (params.size() != 3)
			throw ProtocolError.wrongParameterCount();
		char[] pwd_current = params.get(1).toCharArray();
		char[] pwd_new = params.get(2).toCharArray();
		processor.changePassword(this, user, pwd_current, pwd_new);
	}

	/** Fail a PASSWORD change attempt */
	public void failPassword(String msg) {
		try {
			state.encoder.encode(Message.SHOW, msg);
			flush();
		}
		catch (IOException e) {
			disconnect("I/O error: failPassword " + e.getMessage());
		}
	}

	/** Respond to a QUIT message.
	 * This may only be called on the Task Processor thread. */
	@Override
	public void doQuit(List<String> params) {
		disconnect();
	}

	/** Respond to an ENUMERATE message.
	 * This may only be called on the Task Processor thread. */
	@Override
	public void doEnumerate(List<String> params) throws SonarException {
		checkLoggedIn();
		if (params.size() > 2)
			throw ProtocolError.wrongParameterCount();
		Name name = createName(params);
		int lvl = namespace.accessLevel(name, user);
		if (lvl < AccessLevel.VIEW.ordinal())
			throw PermissionDenied.create(name);
		startWatching(name);
		try {
			namespace.enumerate(state.encoder, name);
		}
		catch (IOException e) {
			throw new SonarException(e.getMessage());
		}
	}

	/** Create a name */
	private Name createName(List<String> params) {
		return (params.size() > 1)
		      ? new Name(params.get(1))
		      : new Name("");
	}

	/** Respond to an IGNORE message.
	 * This may only be called on the Task Processor thread. */
	@Override
	public void doIgnore(List<String> params) throws SonarException {
		checkLoggedIn();
		if (params.size() != 2)
			throw ProtocolError.wrongParameterCount();
		Name name = new Name(params.get(1));
		stopWatching(name);
	}

	/** Respond to an OBJECT message.
	 * This may only be called on the Task Processor thread. */
	@Override
	public void doObject(List<String> params) throws SonarException {
		checkLoggedIn();
		if (params.size() != 2)
			throw ProtocolError.wrongParameterCount();
		Name name = new Name(params.get(1));
		if (name.isObject()) {
			int lvl = namespace.accessLevel(name, user);
			if (lvl < name.accessWrite())
				throw PermissionDenied.create(name);
			createObject(name);
		} else
			throw NamespaceError.nameInvalid(name);
	}

	/** Create a new object in the server namespace.
	 * This may only be called on the Task Processor thread. */
	private void createObject(Name name) throws SonarException {
		SonarObject o = getObject(name);
		try {
			processor.doStoreObject(o);
		}
		finally {
			phantom = null;
		}
	}

	/** Get the specified object (either phantom or new object).
	 * This may only be called on the Task Processor thread. */
	private SonarObject getObject(Name name) throws SonarException {
		if (isPhantom(name))
			return phantom;
		else
			return namespace.createObject(name);
	}

	/** Check if the specified name refers to the phantom object.
	 * This may only be called on the Task Processor thread. */
	protected boolean isPhantom(Name name) {
		return phantom != null &&
		       phantom.getTypeName().equals(name.getTypePart()) &&
		       phantom.getName().equals(name.getObjectPart());
	}

	/** Respond to a REMOVE message.
	 * This may only be called on the Task Processor thread. */
	@Override
	public void doRemove(List<String> params) throws SonarException {
		checkLoggedIn();
		if (params.size() != 2)
			throw ProtocolError.wrongParameterCount();
		Name name = new Name(params.get(1));
		int lvl = namespace.accessLevel(name, user);
		if (lvl < name.accessWrite())
			throw PermissionDenied.create(name);
		SonarObject obj = namespace.lookupObject(name);
		if (obj != null) {
			namespace.removeObject(obj);
			processor.notifyRemove(name);
		} else
			throw NamespaceError.nameInvalid(name);
	}

	/** Respond to an ATTRIBUTE message.
	 * This may only be called on the Task Processor thread. */
	@Override
	public void doAttribute(List<String> params) throws SonarException {
		checkLoggedIn();
		if (params.size() < 2)
			throw ProtocolError.wrongParameterCount();
		Name name = new Name(params.get(1));
		if (name.isAttribute()) {
			if (!checkWriteAttr(name))
				throw PermissionDenied.create(name);
			setAttribute(name, params);
		} else
			throw NamespaceError.nameInvalid(name);
	}

	/** Check if an attribute if writable */
	private boolean checkWriteAttr(Name name) {
		int lvl = namespace.accessLevel(name, user);
		return lvl >= name.accessWrite();
	}

	/** Set the value of an attribute.
	 * This may only be called on the Task Processor thread. */
	private void setAttribute(Name name, List<String> params)
		throws SonarException
	{
		String[] v = new String[params.size() - 2];
		for (int i = 0; i < v.length; i++)
			v[i] =  params.get(i + 2);
		if (isPhantom(name))
			namespace.setAttribute(name, v, phantom);
		else {
			phantom = namespace.setAttribute(name, v);
			if (phantom == null)
				processor.notifyAttribute(name, v);
		}
	}
}
