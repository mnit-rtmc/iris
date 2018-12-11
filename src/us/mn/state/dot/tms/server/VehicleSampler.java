/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2018  Minnesota Department of Transportation
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
 * Vehicle sampler interface.
 * FIXME: use units package.
 *
 * @author Douglas Lau
 */
public interface VehicleSampler {

	/** Get a vehicle sample count */
	int getVehCount(long start, long end);

	/** Get a flow rate (vehicles per hour) */
	int getFlow(long start, long end);

	/** Get the current density (vehicles per mile) */
	float getDensity();

	/** Get the current speed (miles per hour) */
	float getSpeed();
}
