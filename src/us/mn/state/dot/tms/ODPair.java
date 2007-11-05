/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

/**
 * An O/D pair is a tuple containing an origin and destination location.
 *
 * @author Douglas Lau
 */
public class ODPair {

	/** Origin location */
	protected final LocationImpl origin;

	/** Get the origin location */
	public LocationImpl getOrigin() {
		return origin;
	}

	/** Destination location */
	protected final LocationImpl destination;

	/** Get the destination location */
	public LocationImpl getDestination() {
		return destination;
	}

	/** Create a new O/D pair */
	public ODPair(LocationImpl o, LocationImpl d) {
		origin = o;
		destination = d;
	}

	/** Get a string representation */
	public String toString() {
		return "o: " + origin.getDescription() + ", d: " +
			destination.getDescription();
	}

	/** Get the corridor name (if O/D on same corridor) */
	public String getCorridor() {
		if(origin.isSameCorridor(destination))
			return origin.getCorridor();
		else
			return null;
	}
}
