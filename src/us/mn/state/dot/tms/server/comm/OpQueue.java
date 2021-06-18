/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2021  Minnesota Department of Transportation
 * Copyright (C) 2017       SRF Consulting Group
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
 * class.  Operations with the same priority are sorted FIFO.
 *
 * @author Douglas Lau
 * @author John L. Stanley
 */
public final class OpQueue<T extends ControllerProperty> {

	/** Inner class for nodes in the queue */
	static private final class Node<T extends ControllerProperty> {
		final OpController<T> operation;
		final PriorityLevel priority;
		Node<T> next;
		Node(OpController<T> op, Node<T> n) {
			operation = op;
			priority = op.getPriority();
			next = n;
		}
	}

	/** Front node in the queue */
	private Node<T> front = null;

	/** Current working operation.  This is needed so that an "equal"
	 * operation cannot be added while work is in progress. */
	private OpController<T> work = null;

	/** Flag to tell when the poller is closing */
	private boolean closing = false;

	/** Close the queue for new operations */
	public synchronized void close() {
		closing = true;
	}

	/** Check if the queue is open */
	public boolean isOpen() {
		return !closing;
	}

	/** Check if the queue is empty */
	public synchronized boolean isEmpty() {
		return (work == null) && (front == null);
	}

	/** Check if the queue has any more ops to process.
	 * (Like isEmpty(), but ignores the current work op.) */
	public synchronized boolean noMoreOps() {
		return (front == null);
	}

	/** Enqueue a new operation */
	public synchronized boolean enqueue(OpController<T> op) {
		if (shouldAdd(op)) {
			op.begin();
			add(op);
			return true;
		} else
			return false;
	}

	/** Check if an operation should be added to the queue */
	private boolean shouldAdd(OpController<T> op) {
		return isOpen() && !contains(op);
	}

	/** Check if the queue contains a given operation */
	private boolean contains(OpController<T> op) {
		if (op.equals(work) && !work.isDone())
			return true;
		Node<T> node = front;
		while (node != null) {
			OpController<T> nop = node.operation;
			if (op.equals(nop) && !nop.isDone())
				return true;
			node = node.next;
		}
		return false;
	}

	/** Add an operation to the queue */
	private void add(OpController<T> op) {
		PriorityLevel priority = op.getPriority();
		Node<T> prev = null;
		Node<T> node = front;
		while (node != null) {
			if (priority.ordinal() < node.priority.ordinal())
				break;
			prev = node;
			node = node.next;
		}
		node = new Node<T>(op, node);
		if (prev == null)
			front = node;
		else
			prev.next = node;
		notify();
	}

	/** Requeue an in-progress operation */
	public synchronized boolean requeue(OpController<T> op) {
		if ((remove(op) == op) && isOpen()) {
			add(op);
			return true;
		} else
			return false;
	}

	/** Remove an operation from the queue */
	private OpController<T> remove(OpController<T> op) {
		if (op == work) {
			work = null;
			return op;
		}
		Node<T> prev = null;
		Node<T> node = front;
		while (node != null) {
			if (node.operation == op) {
				if (prev == null)
					front = node;
				else
					prev.next = node;
				return op;
			}
			prev = node;
			node = node.next;
		}
		return null;
	}

	/** Get the next operation from the queue (and remove it).
	 * Waits until an operation is added, the timeout expires or the thread
	 * is interrupted (destroyed).
	 * @param idle_ms Idle timeout (ms); 0 indicates no timeout.
	 * @return Operation at front of queue.
	 * @throws DisconnectException If idle timeout expires or comm thread is
	 *                             destroyed. */
	public synchronized OpController<T> next(long idle_ms)
		throws DisconnectException
	{
		work = null;
		while (null == front) {
			try {
				wait(idle_ms);
			}
			catch (InterruptedException e) {
				throw new DisconnectException("DESTROYED");
			}
			if (idle_ms > 0 && null == front) {
				// Empty msg (status) doesn't fail controllers
				throw new DisconnectException("");
			}
		}
		work = front.operation;
		front = front.next;
		return work;
	}

	/** Get the next operation from the queue (and remove it).
	 * If there's no op in the queue, immediately return null. */
	public synchronized OpController<T> tryNext() {
		OpController<T> w = work;
		if (w != null) {
			work = null;
			return w;
		}
		OpController<T> op = null;
		if (front != null) {
			op = front.operation;
			front = front.next;
		}
		return op;
	}

	/** Do something to each operation in the queue */
	public synchronized boolean forEach(OpHandler<T> handler) {
		OpController<T> w = work;
		boolean flag = (w != null) ? handler.handle(w) : true;
		Node<T> node = front;
		while (node != null) {
			flag &= handler.handle(node.operation);
			node = node.next;
		}
		return flag;
	}
}
