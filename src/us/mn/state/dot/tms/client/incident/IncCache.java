/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.IncAdvice;
import us.mn.state.dot.tms.IncDescriptor;
import us.mn.state.dot.tms.Incident;
import us.mn.state.dot.tms.IncidentDetail;
import us.mn.state.dot.tms.IncLocator;
import us.mn.state.dot.tms.client.SonarState;

/**
 * Cache for incident proxy objects.
 *
 * @author Douglas Lau
 */
public class IncCache {

	/** Cache of incident details */
	private final TypeCache<IncidentDetail> inc_details;

	/** Get the incident details object cache */
	public TypeCache<IncidentDetail> getIncidentDetails() {
		return inc_details;
	}

	/** Cache of incidents */
	private final TypeCache<Incident> incidents;

	/** Get the incident object cache */
	public TypeCache<Incident> getIncidents() {
		return incidents;
	}

	/** Cache of incident descriptors */
	private final TypeCache<IncDescriptor> inc_descriptors;

	/** Get the incident descriptor cache */
	public TypeCache<IncDescriptor> getIncDescriptors() {
		return inc_descriptors;
	}

	/** Cache of incident locators */
	private final TypeCache<IncLocator> inc_locators;

	/** Get the incident locator cache */
	public TypeCache<IncLocator> getIncLocators() {
		return inc_locators;
	}

	/** Cache of incident advices */
	private final TypeCache<IncAdvice> inc_advices;

	/** Get the incident advice cache */
	public TypeCache<IncAdvice> getIncAdvices() {
		return inc_advices;
	}

	/** Create a new incident cache */
	public IncCache(SonarState client) throws IllegalAccessException,
		NoSuchFieldException
	{
		inc_details = new TypeCache<IncidentDetail>(
			IncidentDetail.class, client);
		incidents = new TypeCache<Incident>(Incident.class, client);
		inc_descriptors = new TypeCache<IncDescriptor>(
			IncDescriptor.class, client);
		inc_locators = new TypeCache<IncLocator>(IncLocator.class,
			client);
		inc_advices = new TypeCache<IncAdvice>(IncAdvice.class, client);
	}

	/** Populate the type caches */
	public void populate(SonarState client) {
		client.populateReadable(inc_details);
		client.populateReadable(incidents);
		client.populateReadable(inc_descriptors);
		client.populateReadable(inc_locators);
		client.populateReadable(inc_advices);
	}
}
