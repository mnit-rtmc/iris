/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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

import java.io.PrintStream;

/**
 * A prioritized queue which sorts Operation objects by their priority
 * class. Operations with the same priority are sorted FIFO.
 *
 * @author Douglas Lau
 */
public final class PollQueue {

	/** The clog threshold determines how many polls can be in the queue
	 * before it is considered "clogged". */
	static protected final int CLOG_THRESHOLD = 50;

	/** Front node in the queue */
	protected Node front = null;

	/** Flag to tell when the poller is closing */
	protected boolean closing = false;

	/** Close the queue for new operations */
	public synchronized void close() {
		closing = true;
	}

	/** Check if an operation should be added to the queue */
	protected boolean shouldAdd(Operation o) {
		if(closing)
			return false;
		return o.getPriority().ordinal() <
		       PriorityLevel.DEVICE_DATA.ordinal() || !isClogged();
	}

	/** Check if the queue is clogged. This is true if the queue contains
	 * at least CLOG_THRESHOLD polls. */
	protected boolean isClogged() {
		int i = 0;
		Node node = front;
		while(node != null) {
			node = node.next;
			if(++i > CLOG_THRESHOLD)
				return true;
		}
		return false;
	}

	/** Add an operation to the queue */
	public synchronized boolean add(Operation o) {
		if(!shouldAdd(o))
			return false;
		PriorityLevel priority = o.getPriority();
		Node prev = null;
		Node node = front;
		while(node != null) {
			if(priority.ordinal() < node.priority.ordinal())
				break;
			prev = node;
			node = node.next;
		}
		node = new Node(o, node);
		if(prev == null)
			front = node;
		else
			prev.next = node;
		notify();
		return true;
	}

	/** Remove an operation from the queue */
	public synchronized Operation remove(Operation o) {
		Node prev = null;
		Node node = front;
		while(node != null) {
			if(node.operation == o) {
				if(prev == null)
					front = node;
				else
					prev.next = node;
				return o;
			}
			prev = node;
			node = node.next;
		}
		return null;
	}

	/** Does the queue have any elements? */
	public synchronized boolean hasNext() { return front != null; }

	/** Get the next operation from the queue (and remove it) */
	public synchronized Operation next() {
		while(!hasNext()) {
			try { wait(); }
			catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		Operation o = front.operation;
		front = front.next;
		return o;
	}

	/** Inner class for nodes in the queue */
	static protected final class Node {
		final Operation operation;
		final PriorityLevel priority;
		Node next;
		Node(Operation o, Node n) {
			operation = o;
			priority = o.getPriority();
			next = n;
		}
	}

	/** Do something to each operation in the queue */
	public void forEach(OperationHandler handler) {
		Node node = front;
		while(node != null) {
			handler.handle(node.priority, node.operation);
			node = node.next;
		}
	}

	/** Print the contents of the queue to the given stream */
	public void print(final PrintStream ps) {
		forEach(new OperationHandler() {
			public void handle(PriorityLevel prio, Operation o) {
				ps.println("\t" + prio + "\t" + o);
			}
		});
	}
}
