/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.cux50;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import static java.net.StandardSocketOptions.SO_KEEPALIVE;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import us.mn.state.dot.sched.DebugLog;

/**
 * PrServer is a generic network protocol server.
 *
 * @author Douglas Lau
 */
public final class PrServer {

	/** Name of thread/group */
	static private final String NAME = "prot_srv";

	/** Debug logger */
	static private final DebugLog LOG = new DebugLog(NAME);

	/** Thread group for select thread */
	static private final ThreadGroup GROUP = new ThreadGroup(NAME);

	/** Create a TCP server socket channel */
	static private ServerSocketChannel createServerChannel(int port)
		throws IOException
	{
		ServerSocketChannel c = ServerSocketChannel.open();
		c.configureBlocking(false);
		InetAddress host = InetAddress.getByAddress(new byte[4]);
		InetSocketAddress address = new InetSocketAddress(host, port);
		c.socket().bind(address);
		return c;
	}

	/** Thread pool for handling protocol data */
	static private final ForkJoinPool pool = new ForkJoinPool();

	/** Socket selector */
	private final Selector selector;

	/** Selector thread */
	private final Thread thread;

	/** Create a new protocol server */
	public PrServer() throws IOException {
		selector = Selector.open();
 		thread = new Thread(GROUP, NAME) {
			@Override public void run() {
				doRun();
			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	/** Run the select thread */
	private void doRun() {
		try {
			selectLoop();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Loop for the select thread */
	private void selectLoop() throws IOException {
		try {
			while (true) {
				handleSelect();
			}
		}
		finally {
			selector.close();
		}
	}

	/** Handle one select iteration */
	private void handleSelect() throws IOException {
		selector.select();
		Iterator<SelectionKey> it = selector.selectedKeys().iterator();
		while (it.hasNext()) {
			handleReady(it.next());
			it.remove();
		}
		synchronized (this) {
			// NOTE: this prevents deadlock in register
		}
	}

	/** Handle one ready selection key */
	private void handleReady(SelectionKey skey) {
		try {
			if (skey.isAcceptable())
				handleAccept(skey);
			if (skey.isWritable())
				handleWrite(skey);
			if (skey.isReadable())
				handleRead(skey);
		}
		catch (Exception e) {
			handleDisconnect(skey);
		}
	}

	/** Handle "accept" for one selection key */
	private void handleAccept(SelectionKey skey) throws IOException {
		SelectableChannel c = skey.channel();
		Object a = skey.attachment();
		if (c instanceof ServerSocketChannel &&
		    a instanceof ProtocolHandler)
		{
			ProtocolHandler ph = (ProtocolHandler) a;
			ServerSocketChannel chn = (ServerSocketChannel) c;
			SocketChannel sc = chn.accept();
			sc.configureBlocking(false);
			sc.setOption(SO_KEEPALIVE, Boolean.TRUE);
			LOG.log("connect: " + sc);
			Connection cx = new Connection(ph);
			register(sc, cx.getInterest(), cx);
		} else
			throw new EOFException("accept FAILED");
	}

	/** Handle "write" for one selection key */
	private void handleWrite(SelectionKey skey) throws IOException {
		SelectableChannel c = skey.channel();
		Object a = skey.attachment();
		if (c instanceof WritableByteChannel && a instanceof Connection)
			handleWrite(skey, (WritableByteChannel)c,(Connection)a);
		else
			throw new EOFException("write FAILED");
	}

	/** Handle "write" for one selection key */
	private void handleWrite(SelectionKey skey, WritableByteChannel ch,
		Connection cx) throws IOException
	{
		ByteBuffer tx_buf = cx.getTxBuffer();
		synchronized (tx_buf) {
			tx_buf.flip();
			ch.write(tx_buf);
			tx_buf.compact();
			skey.interestOps(cx.getInterest());
		}
	}

	/** Handle "read" for one selection key */
	private void handleRead(SelectionKey skey) throws IOException {
		SelectableChannel c = skey.channel();
		Object a = skey.attachment();
		if (c instanceof ReadableByteChannel && a instanceof Connection)
			handleRead(skey, (ReadableByteChannel)c, (Connection)a);
		else
			throw new EOFException("read FAILED");
	}

	/** Handle "read" for one selection key */
	private void handleRead(SelectionKey skey, ReadableByteChannel ch,
		Connection cx) throws IOException
	{
		int n_bytes;
		ByteBuffer rx_buf = cx.getRxBuffer();
		synchronized (rx_buf) {
			n_bytes = ch.read(rx_buf);
		}
		if (n_bytes > 0)
			executeRecvTask(skey, cx);
		else if (n_bytes < 0)
			throw new EOFException();
	}

	/** Execute receive task on the thread pool */
	private void executeRecvTask(final SelectionKey skey,
		final Connection cx)
	{
		pool.execute(new Runnable() {
			public void run() {
				handleReceive(skey, cx);
			}
		});
	}

	/** Handle receive data for one selection key */
	private void handleReceive(SelectionKey skey, Connection cx) {
		SelectableChannel ch = skey.channel();
		if (ch instanceof SocketChannel) {
			try {
				SocketChannel sc = (SocketChannel) ch;
				cx.handleReceive(sc.getRemoteAddress());
				skey.interestOps(cx.getInterest());
				selector.wakeup();
				return;
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		handleDisconnect(skey);
	}

	/** Handle a disconnect */
	private void handleDisconnect(SelectionKey skey) {
		LOG.log("disconnect: " + skey.channel());
		try {
			skey.channel().close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			skey.cancel();
		}
	}

	/** Register a selectable channel on the selector */
	private synchronized SelectionKey register(AbstractSelectableChannel ch,
		int ops, Object a) throws IOException
	{
		// NOTE: must wake up the selector because locking
		//       on this is really screwy
		selector.wakeup();
		return ch.register(selector, ops, a);
	}

	/** Listen on a port for connections */
	public void listen(int port, ProtocolHandler ph) throws IOException {
		ServerSocketChannel ch = createServerChannel(port);
		register(ch, SelectionKey.OP_ACCEPT, ph);
	}
}
