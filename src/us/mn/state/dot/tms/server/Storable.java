/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2009  Minnesota Department of Transportation
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

import java.util.Map;

/**
 * Interface for objects which are storable by a SQLConnection.
 *
 * @author Douglas Lau
 */
public interface Storable {

	/** Get the database table name */
	String getTable();

	/** Get the primary key name */
	String getKeyName();

	/** Get the primary key */
	String getKey();

	/** Get a mapping of the columns */
	Map<String, Object> getColumns();
}
