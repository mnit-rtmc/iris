/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2017  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.Session;

/**
 * A descriptor for SONAR proxy types.
 *
 * @author Douglas Lau
 */
public class ProxyDescriptor<T extends SonarObject> {

	/** Proxy type cache */
	public final TypeCache<T> cache;

	/** Sonar type name */
	public final String tname;

	/** Flag to enable properties button */
	public final boolean has_properties;

	/** Flag to enable create / delete buttons */
	public final boolean has_create_delete;

	/** Flag to enable name entry widget */
	public final boolean has_name;

	/** Create a new proxy descriptor */
	public ProxyDescriptor(TypeCache<T> c, boolean hp, boolean hcd,
		boolean hn)
	{
		cache = c;
		tname = cache.tname;
		has_properties = hp;
		has_create_delete = hcd;
		has_name = hn;
	}

	/** Create a new proxy descriptor */
	public ProxyDescriptor(TypeCache<T> c, boolean hp) {
		this(c, hp, true, true);
	}

	/** Create a properties form for a proxy */
	public SonarObjectForm<T> createPropertiesForm(T proxy) {
		return null;
	}

	/** Make a table form */
	public ProxyTableForm<T> makeTableForm() {
		return null;
	}
}
