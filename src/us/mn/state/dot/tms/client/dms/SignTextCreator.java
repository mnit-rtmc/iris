/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.util.HashMap;
import java.util.HashSet;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * This is a utility class to create sign text messages.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignTextCreator {

	/** Maximum number of lines for a sign */
	static protected final int MAX_LINES = 12;

	/** Sign text type cache, list of all sign text lines */
	private final TypeCache<SignText> sign_text;

	/** User session */
	private final Session session;

	/** Unique ID for sign text naming */
	protected int uid = 0;

	/** Create a new sign text creator */
	public SignTextCreator(Session s) {
		session = s;
		sign_text = s.getSonarState().getDmsCache().getSignText();
	}

	/**
	 * Create a new sign text and add to the sign text library.
	 * @param sg SignGroup the new message will be associated with.
	 * @param line Combobox line number.
	 * @param multi MULTI string.
	 * @param rank Message rank.
	 */
	public void create(SignGroup sg, short line, String multi,
		short rank)
	{
		multi = new MultiString(multi).normalize();
		String name = createUniqueSignTextName(sg);
		if (isWritePermitted(name)) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("sign_group", sg);
			attrs.put("line", new Short(line));
			attrs.put("multi", multi);
			attrs.put("rank", new Short(rank));
			sign_text.createObject(name, attrs);
		}
	}

	/** Check if the user is permitted to write the named sign text */
	public boolean isWritePermitted(String name) {
		return session.isWritePermitted(SignText.SONAR_TYPE, name);
	}

	/**
	 * Create a SignText name, which is in this form:
	 *    sign_group.name + "_" + uniqueid
	 *    where uniqueid is a sequential integer.
	 * @return A unique string for a new SignText entry, e.g. V1_23
	 */
	protected String createUniqueSignTextName(SignGroup sg) {
		HashSet<String> names = createSignTextNameSet(sg);
		// NOTE: uid needs to persist between calls so that calling
		// this method twice in a row doesn't return the same name
		final int uid_max = names.size() + MAX_LINES;
		for(int i = 0; i < uid_max; i++) {
			final int _uid = (uid + i) % uid_max + 1;
			String n = sg.getName() + "_" + _uid;
			if(!names.contains(n)) {
				uid = _uid;
				return n;
			}
		}
		assert false;
		return null;
	}

	/**
	 * Create a HashSet containing all SignText names for the given
	 * sign group.
	 * @param sg Sign group to search
	 * @return A HashSet with entries as SignText names, e.g. V1_23
	 */
	private HashSet<String> createSignTextNameSet(SignGroup sg) {
		HashSet<String> names = new HashSet<String>();
		for(SignText st: sign_text) {
			if(st.getSignGroup() == sg)
				names.add(st.getName());
		}
		return names;
	}
}
