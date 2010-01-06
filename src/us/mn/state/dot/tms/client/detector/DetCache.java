/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.detector;

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.client.SonarState;

/**
 * Cache for detector-related proxy objects.
 *
 * @author Douglas Lau
 */
public class DetCache {

	/** Cache of r_nodes */
	protected final TypeCache<R_Node> r_nodes;

	/** Get the r_node cache */
	public TypeCache<R_Node> getR_Nodes() {
		return r_nodes;
	}

	/** Cache of stations */
	protected final TypeCache<Station> stations;

	/** Get the station cache */
	public TypeCache<Station> getStations() {
		return stations;
	}

	/** Cache of detectors */
	protected final TypeCache<Detector> detectors;

	/** Get the detector cache */
	public TypeCache<Detector> getDetectors() {
		return detectors;
	}

	/** Create a new det cache */
	public DetCache(SonarState client) throws IllegalAccessException,
		NoSuchFieldException
	{
		r_nodes = new TypeCache<R_Node>(R_Node.class, client);
		stations = new TypeCache<Station>(Station.class, client);
		detectors = new TypeCache<Detector>(Detector.class, client);
	}

	/** Populate the type caches */
	public void populate(SonarState client) {
		client.populateReadable(r_nodes);
		client.populateReadable(stations);
		client.populateReadable(detectors);
	}
}
