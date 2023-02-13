/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2023  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgLine;
import us.mn.state.dot.tms.MsgLineHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * This is a utility class to create message lines.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class MsgLineCreator {

	/** Maximum number of lines for a sign */
	static private final int MAX_LINES = 12;

	/** Message line type cache */
	private final TypeCache<MsgLine> cache;

	/** User session */
	private final Session session;

	/** Unique ID for message line naming */
	private int uid = 0;

	/** Create a new message line creator */
	public MsgLineCreator(Session s) {
		session = s;
		cache = s.getSonarState().getDmsCache().getMsgLine();
	}

	/**
	 * Create a new message line.
	 * @param pat Pattern the message line will be associated with.
	 * @param line Combobox line number.
	 * @param multi MULTI string.
	 * @param rank Message rank.
	 */
	public String create(MsgPattern pat, short line, String multi,
		short rank)
	{
		multi = new MultiString(multi).normalizeLine().toString();
		String name = createUniqueMsgLineName(pat);
		if (isWritePermitted(name)) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("msg_pattern", pat);
			attrs.put("line", Short.valueOf(line));
			attrs.put("multi", multi);
			attrs.put("rank", Short.valueOf(rank));
			cache.createObject(name, attrs);
			return name;
		} else
			return null;
	}

	/** Check if the user is permitted to write the named message line */
	public boolean isWritePermitted(String name) {
		return session.isWritePermitted(MsgLine.SONAR_TYPE, name);
	}

	/** Create a unique MsgLine name */
	private String createUniqueMsgLineName(MsgPattern pat) {
		// NOTE: uid needs to persist between calls so that calling
		// this method twice in a row doesn't return the same name
		final int uid_max = cache.size() + MAX_LINES;
		for (int i = 0; i < uid_max; i++) {
			final int _uid = (uid + i) % uid_max + 1;
			String n = "ml_" + _uid;
			if (MsgLineHelper.lookup(n) == null) {
				uid = _uid;
				return n;
			}
		}
		assert false;
		return null;
	}
}
