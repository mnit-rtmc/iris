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

/**
 * Bounded Sample History container.
 *
 * @author Chongmyung Park (chongmyung.park@gmail.com)
 * @author Douglas Lau
 */
public class BoundedSampleHistory {

	/** Sample data history */
	private final Double[] samples;

	/** Head sample (most recent) */
	private int head = -1;

	/** Count of samples */
	private int n_samples = 0;

	/**
	 * Create a new bounded sample history.
	 * @param max_samples Maximum number of samples to retain.
	 */
	public BoundedSampleHistory(int max_samples) {
		samples = new Double[max_samples];
	}

	/**
	 * Add one sample to the history.
	 * @param sam Current sample data.
	 */
	public void push(Double sam) {
		head = nextIndex(head);
		samples[head] = sam;
		if(n_samples < samples.length)
			n_samples++;
	}

	/** Get the next sample index */
	private int nextIndex(int idx) {
		idx++;
		return idx < samples.length ? idx : 0;
	}

	/**
	 * Return sample at given step index (in reversed direction)
	 *   e.g. get(0) : most recent sample data
	 * @return Sample data
	 */
	public Double get(int i) {
		if(i < n_samples) {
			int idx = head - i;
			if(idx < 0)
				idx += samples.length;
			return samples[idx];
		} else
			return null;
	}

	/**
	 * Clear storage.
	 */
	public void clear() {
		head = -1;
		n_samples = 0;
	}

	/**
	 * Return current storage size.
	 * @return Number of samples stored
	 */
	public int size() {
		return n_samples;
	}

	/**
	 * Return average data
	 * @param fromIndex start index
	 * @param length length to calculate average
	 */
	public Double average(int fromIndex, int length) {
		double sum = 0;
		int count = 0;
		for(int i = fromIndex; i < fromIndex + length; i++) {
			Double d = get(i);
			if(d != null) {
				sum += d;
				count++;
			}
		}
		if(count > 0)
			return sum / count;
		else
			return null;
	}
}
