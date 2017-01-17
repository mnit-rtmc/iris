/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2017  Minnesota Department of Transportation
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

import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.Iterator;

/**
 * A comm selector performs non-blocking I/O on a set of channels.
 *
 * @author Douglas Lau
 */
public class CommSelector implements Closeable {

	/** Selector for non-blocking I/O */
	private final Selector selector;

	/** Create a new comm selector */
	public CommSelector() throws IOException {
		selector = Selector.open();
	}

	/** Close the selector */
	@Override
	public void close() throws IOException {
		selector.close();
	}

	/** Selector loop to perfrom I/O */
	public void selectLoop() throws IOException {
		while (true)
			doSelect();
	}

	/** Select and perform ready I/O */
	private void doSelect() throws IOException {
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

	/** Handle ready events on a selection key */
	private void handleReady(SelectionKey key) {
		BasePoller bp = attachedPoller(key);
		if (bp != null)
			handleReady(key, bp);
		else
			handleDisconnect(key, null);
	}

	/** Handle ready events on a selection key with poller */
	private void handleReady(SelectionKey key, BasePoller bp) {
		try {
			if (key.isConnectable())
				handleConnect(key, bp);
			if (key.isWritable())
				handleWrite(key, bp);
			if (key.isReadable())
				handleRead(key, bp);
		}
		catch (Exception e) {
			// 1. java.io.IOException is most common here
			// 2. java.nio.channels.NotYetConnectedException has
			//    been observed (from ReadableByteChannel.read) when
			//    the VM is running out of memory
			bp.handleException(e);
			handleDisconnect(key, bp);
		}
	}

	/** Get the attached poller */
	private BasePoller attachedPoller(SelectionKey key) {
		Object a = key.attachment();
		return (a instanceof BasePoller) ? (BasePoller) a : null;
	}

	/** Handle connect event on a selection key */
	private void handleConnect(SelectionKey key, BasePoller bp)
		throws IOException
	{
		SelectableChannel chan = key.channel();
		if (chan instanceof SocketChannel) {
			SocketChannel sc = (SocketChannel) chan;
			if (sc.finishConnect()) {
				key.interestOps(SelectionKey.OP_READ);
				return;
			}
		}
		throw new EOFException("connect FAILED");
	}

	/** Handle write event on a selection key */
	private void handleWrite(SelectionKey key, BasePoller bp)
		throws IOException
	{
		SelectableChannel c = key.channel();
		if (c instanceof WritableByteChannel)
			handleWrite(key, (WritableByteChannel) c, bp);
		else
			throw new EOFException("write FAILED");
	}

	/** Handle write event on a selection key */
	private void handleWrite(SelectionKey key, WritableByteChannel chan,
		BasePoller bp) throws IOException
	{
		ByteBuffer tx_buf = bp.getTxBuffer();
		synchronized (tx_buf) {
			tx_buf.flip();
			chan.write(tx_buf);
			if (!tx_buf.hasRemaining())
				key.interestOps(SelectionKey.OP_READ);
			tx_buf.compact();
		}
	}

	/** Handle read event on a selection key */
	private void handleRead(SelectionKey key, BasePoller bp)
		throws IOException
	{
		SelectableChannel c = key.channel();
		if (c instanceof ReadableByteChannel)
			handleRead((ReadableByteChannel) c, bp);
		else
			throw new EOFException("read FAILED");
	}

	/** Handle read event on a channel */
	private void handleRead(ReadableByteChannel chan, BasePoller bp)
		throws IOException
	{
		int n_bytes;
		ByteBuffer rx_buf = bp.getRxBuffer();
		synchronized (rx_buf) {
			n_bytes = chan.read(rx_buf);
		}
		if (n_bytes > 0)
			bp.checkReceive();
		else if (n_bytes < 0)
			throw new EOFException();
	}

	/** Handle disconnect on a selectin key */
	private void handleDisconnect(SelectionKey key, BasePoller bp) {
		try {
			key.channel().close();
		}
		catch (IOException e) {
			if (bp != null)
				bp.handleException(e);
			else
				e.printStackTrace();
		}
		finally {
			key.cancel();
		}
	}

	/** Create a channel */
	public SelectionKey createChannel(BasePoller bp, URI uri)
		throws IOException
	{
		InetSocketAddress remote = createSocketAddress(uri);
		if ("udp".equals(uri.getScheme()))
			return createDatagramChannel(bp, remote);
		else
			return createSocketChannel(bp, remote);
	}

	/** Create an inet socket address */
	private InetSocketAddress createSocketAddress(URI uri)
		throws IOException
	{
		String host = uri.getHost();
		int p = uri.getPort();
		if (host != null && p >= 0 && p <= 65535)
			return new InetSocketAddress(host, p);
		else
			throw new MalformedURLException();
	}

	/** Create a datagram channel */
	private SelectionKey createDatagramChannel(BasePoller bp,
		SocketAddress remote) throws IOException
	{
		DatagramChannel dc = DatagramChannel.open();
		dc.configureBlocking(false);
		dc.connect(remote);
		return register(dc, 0, bp);
	}

	/** Create a socket channel */
	private SelectionKey createSocketChannel(BasePoller bp,
		SocketAddress remote) throws IOException
	{
		SocketChannel sc = SocketChannel.open();
		sc.configureBlocking(false);
		sc.connect(remote);
		return register(sc, SelectionKey.OP_CONNECT, bp);
	}

	/** Register a channel with the selector */
	private synchronized SelectionKey register(AbstractSelectableChannel ch,
		int ops, BasePoller bp) throws IOException
	{
		// NOTE: must wake up the selector because locking
		//       on this is really screwy
		selector.wakeup();
		return ch.register(selector, ops, bp);
	}
}
