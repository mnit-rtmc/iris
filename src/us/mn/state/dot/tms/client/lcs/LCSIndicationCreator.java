/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import java.util.HashMap;
import java.util.HashSet;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSIndication;

/**
 * This is a utility class to create LCS indications.
 *
 * @author Douglas Lau
 */
public class LCSIndicationCreator {

	/** Create a SONAR name to check for allowed updates */
	static protected Name createLCSIName(String oname) {
		return new Name(LCSIndication.SONAR_TYPE, oname);
	}

	/** SONAR namespace */
	protected final Namespace namespace;

	/** LCS indication type cache */
	protected final TypeCache<LCSIndication> indications;

	/** SONAR User for permission checks */
	protected final User user;

	/** Unique ID for naming */
	protected int uid = 0;

	/** Create a new LCS indication creator */
	public LCSIndicationCreator(Namespace ns, TypeCache<LCSIndication> tc,
		User u)
	{
		namespace = ns;
		indications = tc;
		user = u;
	}

	/** 
	 * Create a new LCS indication.
	 * @param lcs LCS association
	 * @param ind LaneUseIndication ordinal
	 */
	public void create(LCS lcs, int ind) {
		String name = createUniqueName();
		if(canAdd(name)) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("lcs", lcs);
			attrs.put("indication", new Integer(ind));
			indications.createObject(name, attrs);
		}
	}

	/** Check if the user can add the named LCS indication */
	public boolean canAdd(String oname) {
		return oname != null &&
			namespace.canAdd(user, createLCSIName(oname));
	}

	/** Check if the user can update the named LCS indication */
	public boolean canRemove(String oname) {
		return oname != null &&
			namespace.canRemove(user, createLCSIName(oname));
	}

	/** Create a LCSIndication name */
	protected String createUniqueName() {
		// NOTE: uid needs to persist between calls so that calling
		// this method twice in a row doesn't return the same name
		final int uid_max = indications.size() + 1;
		for(int i = 0; i < uid_max; i++) {
			final int _uid = (uid + i) % uid_max + 1;
			StringBuilder b = new StringBuilder();
			b.append(_uid);
			while(b.length() < 5)
				b.insert(0, '0');
			String n = "LCSI_" + b;
			if(indications.lookupObject(n) == null) {
				uid = _uid;
				return n;
			}
		}
		assert false;
		return null;
	}
}
