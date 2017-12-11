/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.proxy;

import us.mn.state.dot.sonar.SonarObject;

/**
 * An interface for watching proxy objects.  These methods will be called on the
 * event dispatch thread (EDT).
 *
 * @author Douglas Lau
 */
public interface ProxyView<T extends SonarObject> {

	/** Called when all proxies have been enumerated */
	void enumerationComplete();

	/** Update the view to the specified proxy and attribute.
	 * @param p Proxy to be updated.
	 * @param a Attribute to update, or null for all. */
	void update(T p, String a);

	/** Clear the view */
	void clear();
}
