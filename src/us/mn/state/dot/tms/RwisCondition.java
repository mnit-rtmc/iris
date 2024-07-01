/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  SRF Consulting Group
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

import us.mn.state.dot.sonar.SonarObject;

/**
 * Interface for information about an RWIS
 *  condition/priority/trigger-function.
 * 
 * @author John L. Stanley - SRF Consulting 
 * 
 */
public interface RwisCondition extends SonarObject,
	Comparable<RwisCondition>
{
	/** SONAR type name */
	String SONAR_TYPE = "rwis_condition";

	/** Get priority number */
	public Integer getPriority(); 

	/** Get formula (trigger function) */
	public String getFormula(); 

	/** Compare priorities */
	default int compareTo(RwisCondition cso2) {
		return Integer.compare(getPriority(), cso2.getPriority());
	}
}
