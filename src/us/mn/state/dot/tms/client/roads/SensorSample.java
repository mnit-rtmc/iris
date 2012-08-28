/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2012  Minnesota Department of Transportation
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

/**
 * Traffic data sample for one sensor (or station)
 *
 * @author Douglas Lau
 */
public class SensorSample {

	/** Sensor ID */
	public final String id;

	/** Flow rate (vehicles per hour per lane) */
	protected final Integer flow;

	/** Get the flow rate (vehicles per hour per lane) */
	public Integer getFlow() {
		return flow;
	}

	/** Sampled speed (miles per hour) */
	protected final Integer speed;

	/** Get the sampled speed (miles per hour) */
	public Integer getSpeed() {
		return speed;
	}

	/** Create a new sensor sample */
	public SensorSample(String i, Integer f, Integer s) {
		id = i;
		flow = f;
		speed = s;
	}

	/** Get the density (vehicles per mile per lane) */
	public Integer getDensity() {
		if(flow != null && speed != null && speed > 0)
			return Math.round((float)flow / (float)speed);
		else
			return null;
	}
}
