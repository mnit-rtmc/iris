/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2018  Minnesota Department of Transportation
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
package us.mn.state.dot.sonar.client;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.EnumSet;
import java.util.List;
import java.util.Properties;
import javax.naming.AuthenticationException;
import javax.net.ssl.SSLEngine;
import us.mn.state.dot.sched.ExceptionHandler;
import us.mn.state.dot.sonar.Conduit;
import us.mn.state.dot.sonar.ConfigurationError;
import us.mn.state.dot.sonar.Message;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.Props;
import us.mn.state.dot.sonar.ProtocolError;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.SSLState;

/**
 * A client conduit represents a client connection.
 *
 * @author Douglas Lau
 */
class ClientConduit extends Conduit {

	/** Wait up to 20 seconds for login */
	static private final long LOGIN_MS = 20000;

	/** Define the set of valid messages from the server */
	static private final EnumSet<Message> MESSAGES = EnumSet.of(
		Message.QUIT, Message.OBJECT, Message.REMOVE, Message.ATTRIBUTE,
		Message.TYPE, Message.SHOW);

	/** Lookup a message from the specified message code */
	static private Message lookupMessage(char code) throws ProtocolError {
		for (Message m: MESSAGES) {
			if (code == m.code)
				return m;
		}
		throw ProtocolError.invalidMessageCode();
	}

	/** Create and configure a socket channel */
	static private SocketChannel createChannel(String host, int port)
		throws IOException
	{
		SocketChannel c = SocketChannel.open();
		c.configureBlocking(false);
		c.connect(new InetSocketAddress(host, port));
		return c;
	}

	/** Create and configure a socket channel */
	static private SocketChannel createChannel(Properties props)
		throws ConfigurationError, IOException
	{
		String h = Props.getProp(props, "sonar.host");
		int p = Props.getIntProp(props, "sonar.port");
		return createChannel(h, p);
	}

	/** Get a string host:port representation */
	private String getHostPort() {
		StringBuilder h = new StringBuilder();
		h.append(channel.socket().getInetAddress().getHostAddress());
		h.append(':');
		h.append(channel.socket().getPort());
		return h.toString();
	}

	/** Get the conduit name */
	public String getName() {
		return getHostPort();
	}

	/** Socket channel to communicate with the server */
	private final SocketChannel channel;

	/** Client thread */
	private final Client client;

	/** Key for selecting on the channel */
	private final SelectionKey key;

	/** SSL connection state information */
	private final SSLState state;

	/** Cache of all proxy objects */
	private final ClientNamespace namespace;

	/** Get the namespace */
	Namespace getNamespace() {
		return namespace;
	}

	/** Exception handler */
	private final ExceptionHandler handler;

	/** Flag to determine if login was accepted */
	private boolean loggedIn = false;

	/** Flag to indicate disposed */
	private boolean disposed = false;

	/** Name of connection */
	private String connection = null;

	/** Get the connection name */
	public String getConnection() {
		return connection;
	}

	/** Check if the user successfully logged in */
	public boolean isLoggedIn() {
		return loggedIn;
	}

	/** Create a new client conduit */
	public ClientConduit(Properties props, Client c, Selector selector,
		SSLEngine engine, ExceptionHandler h)
		throws ConfigurationError, IOException
	{
		channel = createChannel(props);
		client = c;
		key = channel.register(selector, SelectionKey.OP_CONNECT);
		engine.setUseClientMode(true);
		state = new SSLState(this, engine);
		namespace = new ClientNamespace();
		handler = h;
		connected = false;
	}

	/** Dispose of the conduit */
	public void dispose() {
		disposed = true;
		disconnect();
		// Stop waiting for login
		notifyLogin();
	}

	/** Complete the connection on the socket channel */
	void doConnect() throws IOException {
		if (channel.finishConnect()) {
			connected = true;
			disableWrite();
			flush();
		}
	}

	/** Read messages from the socket channel.
	 * @return true if data was successfully read. */
	boolean doRead() throws IOException {
		int nbytes;
		ByteBuffer net_in = state.getNetInBuffer();
		synchronized (net_in) {
			nbytes = channel.read(net_in);
		}
		if (nbytes < 0)
			throw new EOFException();
		return (nbytes > 0);
	}

	/** Write pending data to the socket channel */
	public boolean doWrite() throws IOException {
		ByteBuffer net_out = state.getNetOutBuffer();
		synchronized (net_out) {
			net_out.flip();
			channel.write(net_out);
			if (!net_out.hasRemaining())
				disableWrite();
			net_out.compact();
		}
		return true;
	}

	/** Start writing data to client */
	private void startWrite() throws IOException {
		if (state.shouldWrite())
			state.doWrite();
	}

	/** Flush out all outgoing data in the conduit */
	@Override
	public void flush() throws IOException {
		state.encoder.flush();
		if (isConnected())
			startWrite();
	}

	/** Disconnect the conduit */
	@Override
	protected void disconnect() {
		super.disconnect();
		closeChannel();
		closeSelector();
		loggedIn = false;
	}

	/** Close the channel */
	private void closeChannel() {
		try {
			channel.close();
		}
		catch (IOException e) {
			System.err.println("SONAR: Close error: " +
				e.getMessage());
		}
	}

	/** Close the selector */
	private void closeSelector() {
		try {
			key.selector().close();
		}
		catch (IOException e) {
			System.err.println("SONAR: Close error: " +
				e.getMessage());
		}
	}

	/** Process any incoming messages */
	public void processMessages() throws IOException, SonarException {
		if (isConnected())
			doProcessMessages();
	}

	/** Process any incoming messages */
	private void doProcessMessages() throws IOException, SonarException {
		while (state.doRead()) {
			List<String> params = state.decoder.decode();
			while (params != null) {
				if (params.size() > 0)
					processMessage(params);
				params = state.decoder.decode();
			}
		}
		flush();
	}

	/** Process one message from the client */
	private void processMessage(List<String> params) throws SonarException {
		String c = params.get(0);
		if (c.length() != 1)
			throw ProtocolError.invalidMessageCode();
		Message m = lookupMessage(c.charAt(0));
		m.handle(this, params);
	}

	/** Enable writing data back to the client */
	@Override
	protected void enableWrite() {
		key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
		key.selector().wakeup();
	}

	/** Disable writing data back to the client */
	@Override
	protected void disableWrite() {
		key.interestOps(SelectionKey.OP_READ);
	}

	/** Process a QUIT message from the server */
	@Override
	public void doQuit(List<String> p) throws SonarException {
		if (p.size() != 1)
			throw ProtocolError.wrongParameterCount();
		disconnect();
	}

	/** Process an OBJECT message from the server */
	@Override
	public void doObject(List<String> p) throws SonarException {
		if (p.size() != 2)
			throw ProtocolError.wrongParameterCount();
		namespace.putObject(p.get(1));
	}

	/** Process a REMOVE message from the server */
	@Override
	public void doRemove(List<String> p) throws SonarException {
		if (p.size() != 2)
			throw ProtocolError.wrongParameterCount();
		namespace.removeObject(p.get(1));
	}

	/** Process an ATTRIBUTE message from the server */
	@Override
	public void doAttribute(List<String> p) throws SonarException {
		if (p.size() < 2)
			throw ProtocolError.wrongParameterCount();
		p.remove(0);
		String name = p.remove(0);
		namespace.updateAttribute(name, p.toArray(new String[0]));
	}

	/** Process a TYPE message from the server */
	@Override
	public void doType(List<String> p) throws SonarException {
		if (p.size() > 2)
			throw ProtocolError.wrongParameterCount();
		if (p.size() > 1)
			namespace.setCurrentType(p.get(1));
		else {
			namespace.setCurrentType("");
			loggedIn = true;
			notifyLogin();
		}
	}

	/** Notify login success or failure */
	private synchronized void notifyLogin() {
		notify();
	}

	/** Wait for login.
	 * @return true if timed out. */
	public synchronized boolean waitLogin() {
		if (disposed)
			return false;
		try {
			wait(LOGIN_MS);
			return !loggedIn;
		}
		catch (InterruptedException e) {
			return false;
		}
	}

	/** Process a SHOW message from the server */
	@Override
	public void doShow(List<String> p) throws SonarException {
		if (p.size() != 2)
			throw ProtocolError.wrongParameterCount();
		if (!loggedIn)
			notifyLogin();
		String m = p.get(1);
		// First SHOW message after login is the connection name
		if (loggedIn && connection == null)
			connection = m;
		// NOTE: this is a bit fragile
		else if (m.contains("Authentication failed"))
			handler.handle(new AuthenticationException(m));
		else if (m.startsWith("Permission denied"))
			handler.handle(new PermissionException(m));
		else
			handler.handle(new SonarShowException(m));
	}

	/** Attempt to log in to the SONAR server */
	void login(String name, String pwd) throws IOException {
		state.encoder.encode(Message.LOGIN, name, new String[] {pwd});
		flush();
	}

	/** Send a change password request */
	void changePassword(String pwd_current, String pwd_new)
		throws IOException
	{
		state.encoder.encode(Message.PASSWORD, pwd_current,
			new String[] {pwd_new});
		flush();
	}

	/** Quit the SONAR session */
	void quit() throws IOException {
		state.encoder.encode(Message.QUIT);
		flush();
	}

	/** Query all SONAR objects of the given type */
	void queryAll(TypeCache tcache) throws IOException {
		namespace.addType(tcache);
		enumerateName(new Name(tcache.tname));
	}

	/** Create the specified object name */
	void createObject(Name name) throws IOException {
		state.encoder.encode(Message.OBJECT, name.toString());
		flush();
	}

	/** Request an attribute change */
	void setAttribute(Name name, String[] params) throws IOException {
		state.encoder.encode(Message.ATTRIBUTE, name.toString(),
			params);
		flush();
	}

	/** Remove the specified object name */
	void removeObject(Name name) throws IOException {
		state.encoder.encode(Message.REMOVE, name.toString());
		flush();
	}

	/** Enumerate the specified name */
	void enumerateName(Name name) throws IOException {
		state.encoder.encode(Message.ENUMERATE, name.toString());
		flush();
	}

	/** Ignore the specified name */
	void ignoreName(Name name) throws IOException {
		state.encoder.encode(Message.IGNORE, name.toString());
		flush();
	}
}
