/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2011  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.Name;
import us.mn.state.dot.sonar.Namespace;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.client.Session;

/**
 * This is a utility class to create sign text messages.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignTextCreator {

	/** Maximum number of lines for a sign */
	static protected final int MAX_LINES = 12;

	/** Create a SONAR name to check for allowed updates */
	static protected Name createSignTextName(String oname) {
		return new Name(SignText.SONAR_TYPE, oname);
	}

	/** Sign text type cache, list of all sign text lines */
	protected final TypeCache<SignText> sign_text;

	/** SONAR namespace */
	protected final Namespace namespace;

	/** SONAR User for permission checks */
	protected final User user;

	/** Unique ID for sign text naming */
	protected int uid = 0;

	/** Create a new sign text creator */
	public SignTextCreator(Session s) {
		sign_text = s.getSonarState().getDmsCache().getSignText();
		namespace = s.getSonarState().getNamespace();
		user = s.getUser();
	}

	/** 
	 * Create a new sign text and add to the sign text library.
	 * @param sg SignGroup the new message will be associated with.
	 * @param line Combobox line number.
	 * @param multi MULTI string.
	 * @param priority Message sort priority
	 */
	public void create(SignGroup sg, short line, String multi,
		short priority)
	{
		multi = MultiString.normalize(multi);
		String name = createUniqueSignTextName(sg);
		if(canAddSignText(name)) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("sign_group", sg);
			attrs.put("line", new Short(line));
			attrs.put("multi", multi);
			attrs.put("priority", new Short(priority));
			sign_text.createObject(name, attrs);
		}
	}

	/** Check if the user can add the named sign text */
	public boolean canAddSignText(String name) {
		return name != null && namespace.canAdd(user,
			createSignTextName(name));
	}

	/** Check if the user can update the named sign text */
	public boolean canUpdateSignText(String name) {
		return name != null && namespace.canUpdate(user,
			createSignTextName(name));
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
	protected HashSet<String> createSignTextNameSet(final SignGroup sg) {
		final HashSet<String> names = new HashSet<String>();
		sign_text.findObject(new Checker<SignText>() {
			public boolean check(SignText st) {
				if(st.getSignGroup() == sg)
					names.add(st.getName());
				return false;
			}
		});
		return names;
	}
}
