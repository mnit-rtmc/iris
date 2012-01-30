/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2001-2012  Minnesota Department of Transportation
 * Copyright (C) 2011  University of Minnesota Duluth (NATSRL)
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
package us.mn.state.dot.tms.server;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Class : Limited Queue
 * @param <T>
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 * @author Douglas Lau
 */
public class LimitedQueue<T> {

	/** Storage limit */
	private final int limit;

	/** Linked list to store data */
	private final Queue<T> queue = new LinkedList<T>();

	/**
	 * Construct
	 * @param limit storage limit
	 */
	public LimitedQueue(int limit) {
		this.limit = limit;
	}

	/**
	 * Add data
	 * @param obj
	 * @return
	 */
	public boolean push(T obj) {
		boolean res = this.queue.offer(obj);
		if(this.queue.size() > this.limit)
			this.queue.poll();
		return res;
	}

	/**
	 * Return head data
	 * @return T data
	 */
	public T head() {
		return this.queue.poll();
	}

	/**
	 * Return tail data
	 * @return T data
	 */
	public T tail() {
		return this.get(0);
	}

	/**
	 * Return data at given index (in reversed direction)
	 *   e.g. get(0) : most recent data
	 * @return T data
	 */
	public T get(int index) {
		int idx = this.queue.size() - 1;
		for(T obj : this.queue) {
			if(index == idx)
				return obj;
			idx--;
		}
		return null;
	}

	/**
	 * Clear storage
	 */
	public void clear() {
		this.queue.clear();
	}

	/**
	 * Return current storage size
	 * @return queue size
	 */
	public int size() {
		return this.queue.size();
	}

	/**
	 * Return average data
	 * @param fromIndex start index
	 * @param length length to calculate average
	 */
	public Double getAverage(int fromIndex, int length) {
		Double sum = 0D;
		int count = 0;
		for(int i = fromIndex; i < fromIndex + length; i++) {
			Double d = (Double) this.get(i);
			if(d == null)
				break;
			sum += d;
			count++;
		}
		if(count > 0)
			return sum / count;
		else
			return 0D;
	}
}
