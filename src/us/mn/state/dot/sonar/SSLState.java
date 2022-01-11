/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2022  Minnesota Department of Transportation
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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Properties;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import us.mn.state.dot.sched.DebugLog;

/**
 * The SSL state manages buffers and handshaking for one SSL connection.
 *
 * @author Douglas Lau
 */
public class SSLState {

	/** SONAR TLS debug log */
	static private final DebugLog DEBUG_TLS = new DebugLog("sonar_tls");

	/** Size (in bytes) of network buffers */
	static private final int NETWORK_SIZE = 1 << 16;

	/** Default cipher suites */
	static private final String DEFAULT_SUITES = "TLS_.*AES_256.*";

	/** Create a SSL engine */
	static private SSLEngine createSSLEngine(SSLContext context,
		Properties props)
	{
		SSLEngine engine = context.createSSLEngine();
		String regex = props.getProperty("sonar.cipher.suites",
			DEFAULT_SUITES);
		engine.setEnabledProtocols(getProtocols(engine));
		engine.setEnabledCipherSuites(getCipherSuites(engine, regex));
		return engine;
	}

	/** Get an array of protocol versions to enable */
	static private String[] getProtocols(SSLEngine engine) {
		ArrayList<String> protocols = new ArrayList<String>();
		for (String sp: engine.getSupportedProtocols()) {
			if (sp.equals("TLSv1.2") || sp.equals("TLSv1.3")) {
				protocols.add(sp);
				DEBUG_TLS.log("protocol enabled: " + sp);
			}
		}
		return protocols.toArray(new String[0]);
	}

	/** Get an array of cipher suites which match a regex */
	static private String[] getCipherSuites(SSLEngine engine,
		String regex)
	{
		ArrayList<String> suites = new ArrayList<String>();
		for (String cs: engine.getSupportedCipherSuites()) {
			if (cs.matches(regex)) {
				DEBUG_TLS.log("suite enabled: " + cs);
				suites.add(cs);
			}
		}
		return suites.toArray(new String[0]);
	}

	/** Conduit */
	private final Conduit conduit;

	/** SSL engine */
	private final SSLEngine engine;

	/** Byte buffer to store outgoing encrypted network data */
	private final ByteBuffer net_out;

	/** Byte buffer to store incoming encrypted network data */
	private final ByteBuffer net_in;

	/** Byte buffer to store incoming SONAR data */
	private final ByteBuffer app_in;

	/** Byte buffer to wrap outgoing SSL data */
	private final ByteBuffer ssl_out;

	/** Byte buffer to unwrap incoming SSL data */
	private final ByteBuffer ssl_in;

	/** Decoder for messages received */
	public final MessageDecoder decoder;

	/** Encoder for messages to send */
	public final MessageEncoder encoder;

	/** Create a new SONAR SSL state */
	public SSLState(Conduit c, SSLContext context, Properties props,
		boolean client) throws SSLException, IOException
	{
		conduit = c;
		engine = createSSLEngine(context, props);
		engine.setUseClientMode(client);
		SSLSession session = engine.getSession();
		int p_size = session.getPacketBufferSize();
		int a_size = session.getApplicationBufferSize();
		net_in = ByteBuffer.allocate(NETWORK_SIZE);
		net_out = ByteBuffer.allocate(NETWORK_SIZE);
		app_in = ByteBuffer.allocate(a_size);
		ssl_out = ByteBuffer.allocate(p_size);
		ssl_in = ByteBuffer.allocate(a_size);
		decoder = new MessageDecoder(app_in);
		encoder = new MessageEncoder(a_size);
		engine.beginHandshake();
	}

	/** Get the network out buffer */
	public ByteBuffer getNetOutBuffer() {
		return net_out;
	}

	/** Ge the network in buffer */
	public ByteBuffer getNetInBuffer() {
		return net_in;
	}

	/** Read available data from network input buffer.
	 * This may only be called on the Task Processor thread. */
	public boolean doRead() throws SSLException {
		doUnwrap();
		while (doHandshake());
		return app_in.position() > 0;
	}

	/** Do something to progress handshaking */
	private boolean doHandshake() throws SSLException {
		SSLEngineResult.HandshakeStatus hs = engine.getHandshakeStatus();
		debugHandshake(hs);
		switch (hs) {
		case NEED_TASK:
			doTask();
			return true;
		case NEED_WRAP:
			doWrap();
			return true;
		case NEED_UNWRAP:
			doUnwrap();
			return true;
		default:
			return false;
		}
	}

	/** Debug a TLS handshake */
	private void debugHandshake(SSLEngineResult.HandshakeStatus hs) {
		if (hs != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
			if (DEBUG_TLS.isOpen()) {
				DEBUG_TLS.log("TLS handshake " + hs + " for " +
					conduit.getName());
			}
		}
	}

	/** Write data to the network output buffer.
	 * This may only be called on the Task Processor thread. */
	public void doWrite() throws SSLException {
		if (canWrite())
			doWrap();
		else
			conduit.enableWrite();
	}

	/** Check if data should be written.
	 * This may only be called on the Task Processor thread. */
	public boolean shouldWrite() {
		return encoder.hasData() && canWrite();
	}

	/** Check if data can be written to network buffer */
	public boolean canWrite() {
		synchronized (net_out) {
			return net_out.remaining() > ssl_out.capacity();
		}
	}

	/** Perform a delegated SSL engine task */
	private void doTask() {
		Runnable task = engine.getDelegatedTask();
		if (task != null)
			task.run();
	}

	/** Wrap application data into SSL buffer */
	private void doWrap() throws SSLException {
		ssl_out.clear();
		ByteBuffer app_out = encoder.getBuffer();
		app_out.flip();
		try {
			engine.wrap(app_out, ssl_out);
		}
		finally {
			encoder.compact();
		}
		ssl_out.flip();
		int n_bytes;
		synchronized (net_out) {
			net_out.put(ssl_out);
			n_bytes = net_out.position();
		}
		if (n_bytes > 0)
			conduit.enableWrite();
	}

	/** Unwrap SSL data into appcliation buffer */
	private boolean doUnwrap() throws SSLException {
		synchronized (net_in) {
			net_in.flip();
			try {
				int n_rem = net_in.remaining();
				if (n_rem > 0) {
					ssl_in.clear();
					engine.unwrap(net_in, ssl_in);
					ssl_in.flip();
					app_in.put(ssl_in);
				}
				return net_in.remaining() < n_rem;
			}
			finally {
				net_in.compact();
			}
		}
	}
}
