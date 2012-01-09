/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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
public final class OperationQueue {

	/** Front node in the queue */
	private Node front = null;

	/** Current working operation.  This is needed so that an "equal"
	 * operation cannot be added while work is in progress. */
	private Operation work = null;

	/** Flag to tell when the poller is closing */
	private boolean closing = false;

	/** Close the queue for new operations */
	public synchronized void close() {
		closing = true;
	}

	/** Enqueue a new operation */
	public synchronized boolean enqueue(Operation op) {
		if(shouldAdd(op) && op.begin()) {
			add(op);
			return true;
		} else
			return false;
	}

	/** Check if an operation should be added to the queue */
	private boolean shouldAdd(Operation op) {
		return !closing && !contains(op);
	}

	/** Check if the queue contains a given operation */
	private boolean contains(Operation op) {
		if(op.equals(work))
			return true;
		Node node = front;
		while(node != null) {
			node = node.next;
			Operation nop = node.operation;
			if(op.equals(nop) && !nop.isDone())
				return true;
		}
		return false;
	}

	/** Requeue an in-progress operation */
	public synchronized boolean requeue(Operation op) {
		if(!closing) {
			add(op);
			return true;
		} else
			return false;
	}

	/** Add an operation to the queue */
	private void add(Operation op) {
		PriorityLevel priority = op.getPriority();
		Node prev = null;
		Node node = front;
		while(node != null) {
			if(priority.ordinal() < node.priority.ordinal())
				break;
			prev = node;
			node = node.next;
		}
		node = new Node(op, node);
		if(prev == null)
			front = node;
		else
			prev.next = node;
		notify();
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
	public synchronized boolean hasNext() {
		return front != null;
	}

	/** Get the next operation from the queue (and remove it) */
	public synchronized Operation next() {
		work = null;
		while(!hasNext()) {
			try {
				wait();
			}
			catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		work = front.operation;
		front = front.next;
		return work;
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
	public synchronized void forEach(OperationHandler handler) {
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
