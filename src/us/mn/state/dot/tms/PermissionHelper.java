/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2024  Minnesota Department of Transportation
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
import java.util.Set;

/**
 * Helper class for permissions.
 *
 * @author Douglas Lau
 */
public class PermissionHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private PermissionHelper() {
		assert false;
	}

	/** Lookup the permission with the specified name */
	static public Permission lookup(String name) {
		return (Permission) namespace.lookupObject(Permission.SONAR_TYPE,
			name);
	}

	/** Get a permission iterator */
	static public Iterator<Permission> iterator() {
		return new IteratorWrapper<Permission>(namespace.iterator(
			Permission.SONAR_TYPE));
	}

	/** Find all hashtags for scratch video_monitor permissions */
	static public Set<String> findScratch(Role role) {
		String notes = "";
		Iterator<Permission > it = iterator();
		while (it.hasNext()) {
			Permission p = it.next();
			String tag = p.getHashtag();
			if (tag != null &&
			    p.getRole() == role &&
			    p.getBaseResource() == PlayList.SONAR_BASE &&
			    p.getAccessLevel() == AccessLevel.MANAGE.ordinal())
			{
				notes = Hashtags.add(notes, tag);
			}
		}
		return new Hashtags(notes).tags();
	}
}
