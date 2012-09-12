/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import java.util.HashMap;

/**
 * A sample data set contains all samples for the most recent period.
 *
 * @author Douglas Lau
 */
public class SampleDataSet {

	/** Mapping of sensor ID to sample data */
	protected final HashMap<String, SensorSample> samples =
		new HashMap<String, SensorSample>();

	/** Mapping of sensor ID to sample data for next interval */
	protected final HashMap<String, SensorSample> next_samples =
		new HashMap<String, SensorSample>();

	/** Update one sample */
	public synchronized void updateSample(SensorSample s) {
		next_samples.put(s.id, s);
	}

	/** Swap the samples */
	public synchronized void swapSamples() {
		samples.clear();
		samples.putAll(next_samples);
		next_samples.clear();
	}

	/** Clear the samples */
	public synchronized void clearSamples() {
		samples.clear();
		next_samples.clear();
	}

	/** Get a sample for the given sensor ID */
	public synchronized SensorSample getSample(String sid) {
		return samples.get(sid);
	}
}
