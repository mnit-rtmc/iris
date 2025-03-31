/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2024  SRF Consulting Group
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
package us.mn.state.dot.tms.server.comm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.URI;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import us.mn.state.dot.tms.server.ControllerImpl;

/**
 * SshMessenger is a class used to interface between the IRIS
 * Messenger subsystem and the JSch SSH communication jar.
 * It creates an SSH connection between IRIS and an SSH
 * enabled device using a IP address:port provided by an
 * device's comm_link URI string and using un:pw credentials 
 * from the associated Controller.pw field.
 * 
 * @author John L. Stanley - SRF Consulting
 */
public class SshMessenger extends BasicMessenger {

	/** Initialize a JSch SSH control object.
	 * We only need one and it's shared by all
	 * SshMessenger(s). */
	private static final JSch jsch = new JSch();

	/** Create a SSH stream messenger.
	 * @param u URI of remote host.
	 * @param rt Receive timeout (ms).
	 * @param ct Connect timeout (ms).
	 * @param nrd No-response disconnect (sec). */
	static protected SshMessenger create(URI u, int rt, int ct, int nrd)
		throws MessengerException, IOException
	{
		// Unlike most messengers, SSH only opens the
		// connection in the getRawOutputStream(...)
		// method after we get the login credentials.
		return new SshMessenger(createSocketAddress(u), rt, ct, nrd);
	}

	/** Controller struct */
	private ControllerImpl controller;
	
	/** Address to connect */
	private final InetSocketAddress inetAddress;

	/** Receive timeout (ms) */
	private final int recv_timeout;

	/** Connect timeout (ms) */
	private final int conn_timeout;

	/** SSH Session */
    private Session session;
    
	/** SSH channel */
    private Channel channel;

	/** Raw Input stream */
	private InputStream input;

	/** Raw Output stream */
	private OutputStream output;

	/** Create a new SSH messenger.
	 * NOTE: must call setConnected to switch from conn_timeout to
	 *       recv_timeout. */
	private SshMessenger(InetSocketAddress ia, int rt, int ct, int nrd)
		throws IOException
	{
		// TODO Auto-generated constructor stub
		super(nrd);
		inetAddress = ia;
		recv_timeout = rt;
		conn_timeout = ct;
	}

	/** Finish opening the SSH session and then the shell channel. */
	public void finishOpen(ControllerImpl c) throws IOException {
		if (controller != null)
			return;
		controller = c;
		String host = inetAddress.getHostString();
		int    port = inetAddress.getPort();
		String unpw = controller.getPassword();
		if (unpw == null)
			unpw = "";
		String[] parts = unpw.split(":", 2);
		if (parts.length != 2)
			throw new ConnectException("Invalid SSH Credentials");
		String un = parts[0];
		String pw = parts[1];
		try {
			// create and init the SSH session object
			session = jsch.getSession(un, host, port);
			session.setPassword(pw);
			// Disable strict host key checking
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			/** Establish TCP-connection to device */
			session.connect(conn_timeout);
			if (session.isConnected() == false)
				throw new ConnectException("SSH Session Could Not Connect");
			/** Establish shell-channel connection */
			channel = session.openChannel("shell");
			if (channel == null)
				throw new ConnectException("SSH Shell Could Not Connect");
			input   = channel.getInputStream();
			output  = channel.getOutputStream();
			channel.connect();
		} catch (IOException | JSchException ex) {
			ex.printStackTrace(); // temporary
			String msg = ex.getMessage();
			// Improve skimpy or overly-long exception msgs
			if (msg.equals("Auth fail"))
				msg = "Wrong SSH Usernme or Password";
			else if (msg.contains("Read timed out"))
				msg = "SSH Connect Timed Out";
			else if (msg.contains("Connection refused: connect"))
				msg = "SSH Connection Refused";
			else if (msg.contains("socket is not established"))
				msg = "SSH Can't Connect";
			System.out.println("> \""+msg+"\"");
			throw new ConnectException(msg);
		}
	}
	
	/** Get the input stream.
	 * @param path Relative path name.
	 * @return An input stream for reading from the messenger. */
	@Override
	protected InputStream getRawInputStream(String path) {
		return input;
	}

	/** Get the output stream. */
	@Override
	protected OutputStream getRawOutputStream(ControllerImpl c) throws IOException {
		if (output == null)
			finishOpen(c);
		return output;
	}

	/** Close the messenger */
	@Override
	protected void close2() throws IOException {
		input = null;
		output = null;
		try {
			if (channel != null)
				channel.disconnect();
		} catch (Exception e) {
			// do nothing
		}
		channel = null;
		try {
			if (session != null)
				session.disconnect();
		} catch (Exception e) {
			// do nothing
		}
		session = null;
	}

	/** Drain any bytes from the input stream */
	@Override
	public void drain() throws IOException {
		if (input == null)
			return;
		int a = input.available();
		if (a > 0)
			input.skip(a);
	}
}
