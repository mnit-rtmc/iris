/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2012  Minnesota Department of Transportation
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
package us.mn.state.dot.sonar.client;

import java.util.Map;

/**
 * An attribute map defined all the attributes of one proxy.
 *
 * @author Douglas Lau
 */
final class AttributeMap {

	/** Map of all attributes */
	public final Map<String, Attribute> attrs;

	/** Flag indicating the proxy is a zombie */
	public boolean zombie = false;

	/** Create a new attribute map */
	public AttributeMap(Map<String, Attribute> a) {
		attrs = a;
	}
}
