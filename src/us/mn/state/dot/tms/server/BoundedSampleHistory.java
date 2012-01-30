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
 * Bounded Sample History container.
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 * @author Douglas Lau
 */
public class BoundedSampleHistory {

	/** Maximum number of samples in history */
	private final int max_samples;

	/** Linked list to store data */
	private final Queue<Double> queue = new LinkedList<Double>();

	/**
	 * Create a new bounded sample history.
	 * @param max_samples Maximum number of samples to retain.
	 */
	public BoundedSampleHistory(int max_samples) {
		this.max_samples = max_samples;
	}

	/**
	 * Add one sample to the history.
	 * @param sam Current sample data.
	 * @return
	 */
	public boolean push(Double sam) {
		boolean res = queue.offer(sam);
		if(queue.size() > max_samples)
			queue.poll();
		return res;
	}

	/**
	 * Return tail data
	 * @return Double data
	 */
	public Double tail() {
		return get(0);
	}

	/**
	 * Return sample at given step index (in reversed direction)
	 *   e.g. get(0) : most recent sample data
	 * @return Sample data
	 */
	public Double get(int index) {
		int idx = queue.size() - 1;
		for(Double sam : queue) {
			if(index == idx)
				return sam;
			idx--;
		}
		return null;
	}

	/**
	 * Clear storage.
	 */
	public void clear() {
		queue.clear();
	}

	/**
	 * Return current storage size
	 * @return queue size
	 */
	public int size() {
		return queue.size();
	}

	/**
	 * Return average data
	 * @param fromIndex start index
	 * @param length length to calculate average
	 */
	public Double getAverage(int fromIndex, int length) {
		double sum = 0;
		int count = 0;
		for(int i = fromIndex; i < fromIndex + length; i++) {
			Double d = get(i);
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
