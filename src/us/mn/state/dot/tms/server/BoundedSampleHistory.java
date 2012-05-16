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

	/** Sample cursor (most recent) */
	private int cursor = -1;

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
	 * Add one data sample to the history.
	 * @param sam Current sample data, or null for missing data.
	 */
	public void push(Double sam) {
		cursor = nextIndex(cursor);
		samples[cursor] = filterSample(sam);
		if(n_samples < samples.length)
			n_samples++;
	}

	/**
	 * Get the next sample array index.
	 * @param idx Index into samples array.
	 * @return Next index, rolling over if necessary.
	 */
	private int nextIndex(int idx) {
		idx++;
		return idx < samples.length ? idx : 0;
	}

	/**
	 * Filter out negative data samples.
	 * @param sam Sample data.
	 * @return Sample data, with negative values replaced with null.
	 */
	private Double filterSample(Double sam) {
		if(sam != null && sam >= 0)
			return sam;
		else
			return null;
	}

	/**
	 * Return sample at given time step index (in reversed direction).
	 * @param t Time-step index (0 for most recent).
	 * @return Sample data, or null for missing data.
	 */
	public Double get(int t) {
		if(t < n_samples) {
			int idx = cursor - t;
			if(idx < 0)
				idx += samples.length;
			return samples[idx];
		} else
			return null;
	}

	/**
	 * Clear sample history.
	 */
	public void clear() {
		cursor = -1;
		n_samples = 0;
	}

	/**
	 * Return the number of samples in history.
	 * @return Number of samples in history.
	 */
	public int size() {
		return n_samples;
	}

	/**
	 * Check if the history is full.
	 * @return true if the history is full.
	 */
	public boolean isFull() {
		return n_samples == samples.length;
	}

	/**
	 * Return the average of the specified number of samples.
	 * @param t Starting time-step index (0 for most recent).
	 * @param n_sam Number of samples to calculate average.
	 * @return Average of the specified samples, or null for missing data.
	 */
	public Double average(int t, int n_sam) {
		double sum = 0;
		int count = 0;
		for(int i = t; i < t + n_sam; i++) {
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

	/**
	 * Return the average of all saved samples.
	 * @return Average of the samples, or null for missing data.
	 */
	public Double average() {
		return average(0, n_samples);
	}
}
