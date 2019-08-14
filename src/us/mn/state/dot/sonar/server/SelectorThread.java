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
package us.mn.state.dot.sonar.server;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * The selector thread processes all data transfers with client connections.
 *
 * @author Douglas Lau
 */
public final class SelectorThread {

	/** Thread group for selector thread */
	static private final ThreadGroup GROUP = new ThreadGroup("SONAR");

	/** Create and configure a server socket channel */
	static private ServerSocketChannel createChannel(int port)
		throws IOException
	{
		ServerSocketChannel c = ServerSocketChannel.open();
		c.configureBlocking(false);
		InetAddress host = InetAddress.getByAddress(new byte[4]);
		InetSocketAddress address = new InetSocketAddress(host, port);
		c.socket().bind(address);
		return c;
	}

	/** Task processor */
	private final TaskProcessor processor;

	/** Selector for non-blocking I/O */
	private final Selector selector;

	/** Socket channel to listen for new client connections */
	private final ServerSocketChannel channel;

	/** Thread to run select loop */
	private final Thread thread;

	/** Create a new selector thread */
	public SelectorThread(TaskProcessor tp, int port) throws IOException {
		processor = tp;
		selector = Selector.open();
		channel = createChannel(port);
		channel.register(selector, SelectionKey.OP_ACCEPT);
 		thread = new Thread(GROUP, "selector") {
			@Override public void run() {
				doRun();
			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	/** Join the thread */
	public void join() throws InterruptedException {
		thread.join();
	}

	/** Selector loop to perfrom socket I/O */
	private void doRun() {
		while (true)
			doSelect();
	}

	/** Select and perform I/O on ready channels */
	private void doSelect() {
		try {
			_doSelect();
		}
		catch (Exception e) {
			System.err.println("SONAR: selector error " +
				e.getMessage());
			e.printStackTrace();
		}
	}

	/** Select and perform I/O on ready channels */
	private void _doSelect() throws IOException {
		selector.select();
		Set<SelectionKey> readySet = selector.selectedKeys();
		for (SelectionKey skey: readySet) {
			if (checkAccept(skey))
				continue;
			serviceClient(skey);
		}
		readySet.clear();
	}

	/** Check if a new client is connecting */
	private boolean checkAccept(SelectionKey skey) {
		try {
			if (skey.isAcceptable())
				doAccept();
			else
				return false;
		}
		catch (CancelledKeyException e) {
			handleDisconnect(skey, null, e.getMessage());
		}
		catch (IOException e) {
			System.err.println("SONAR: selector I/O error " +
				e.getMessage());
			e.printStackTrace();
		}
		return true;
	}

	/** Accept a new client connection */
	private void doAccept() throws IOException {
		SocketChannel sc = channel.accept();
		sc.configureBlocking(false);
		SelectionKey skey = sc.register(selector, 0);
		processor.scheduleConnect(skey, sc);
	}

	/** Do any pending read/write on a client connection */
	private void serviceClient(SelectionKey skey) {
		ConnectionImpl c = processor.lookupClient(skey);
		if (null == c) {
			handleDisconnect(skey, null, "No connection");
			return;
		}
		try {
			if (skey.isWritable())
				c.doWrite();
			if (skey.isReadable())
				c.doRead();
		}
		catch (CancelledKeyException e) {
			handleDisconnect(skey, c, "Key cancelled");
		}
		catch (EOFException e) {
			handleDisconnect(skey, c, null);
			/* Let the task processor perform the disconnect */
			Thread.yield();
		}
		catch (IOException e) {
			handleDisconnect(skey, c, "I/O error " +e.getMessage());
		}
	}

	/** Handle a disconnect */
	private void handleDisconnect(SelectionKey skey, ConnectionImpl c,
		String msg)
	{
		try {
			skey.channel().close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			skey.cancel();
		}
		if (c != null)
			processor.scheduleDisconnect(c, msg);
		else
			processor.scheduleDisconnect(skey);
	}
}
