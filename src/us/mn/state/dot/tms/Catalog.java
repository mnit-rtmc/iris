/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
 * Catalog (camera play list sequence).
 *
 * @author Douglas Lau
 */
public interface Catalog extends SonarObject {

	/** SONAR type name */
	String SONAR_TYPE = "catalog";

	/** Set sequence number */
	void setSeqNum(int n);

	/** Get sequence number */
	int getSeqNum();

	/** Set the play lists in the catalog */
	void setPlayLists(PlayList[] pl);

	/** Get the play lists in the catalog */
	PlayList[] getPlayLists();
}
