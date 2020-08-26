/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  Minnesota Department of Transportation
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

import java.util.Iterator;
import us.mn.state.dot.tms.utils.UniqueNameCreator;

/**
 * Helper for comm configs.
 *
 * @author Douglas Lau
 */
public class CommConfigHelper extends BaseHelper {

	/** Name creator */
	static final UniqueNameCreator UNC =
		new UniqueNameCreator("cfg_%d", (n)->lookup(n));

	/** Create a unique comm_config record name */
	static public String createUniqueName() {
		return UNC.createUniqueName();
	}

	/** Disallow instantiation */
	private CommConfigHelper() {
		assert false;
	}

	/** Get a comm config iterator */
	static public Iterator<CommConfig> iterator() {
		return new IteratorWrapper<CommConfig>(namespace.iterator(
			CommConfig.SONAR_TYPE));
	}

	/** Lookup the CommConfig with the specified name */
	static public CommConfig lookup(String name) {
		return (CommConfig) namespace.lookupObject(
			CommConfig.SONAR_TYPE, name);
	}

	/** Get the selected comm protocol */
	static public CommProtocol getProtocol(CommConfig cc) {
		return (cc != null)
		      ? CommProtocol.fromOrdinal(cc.getProtocol())
		      : null;
	}
}
