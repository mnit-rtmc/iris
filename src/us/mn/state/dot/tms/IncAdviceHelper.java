/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for Incident Advices.
 *
 * @author Douglas Lau
 */
public class IncAdviceHelper extends BaseHelper {

	/** Don't instantiate */
	private IncAdviceHelper() {
		assert false;
	}

	/** Get an incident advice iterator */
	static public Iterator<IncAdvice> iterator() {
		return new IteratorWrapper<IncAdvice>(namespace.iterator(
			IncAdvice.SONAR_TYPE));
	}

	/** Find a matching incident advice */
	static public IncAdvice match(Set<SignGroup> groups, IncRange rng,
		Incident inc)
	{
		Iterator<IncAdvice> it = iterator();
		while (it.hasNext()) {
			IncAdvice adv = it.next();
			if (groups.contains(adv.getSignGroup()) &&
			    adv.getRange() == rng.ordinal() &&
			    adv.getLaneType() == inc.getLaneType() &&
			    impactMatches(adv.getImpact(), inc.getImpact()) &&
			    adv.getCleared() == inc.getCleared())
				return adv;
		}
		return null;
	}

	/** Check if impact matches */
	static private boolean impactMatches(String exp, String imp) {
		if (exp.length() != imp.length())
			return false;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < exp.length(); i++) {
			char c = exp.charAt(i);
			switch (c) {
			case ':':
				sb.append("[?!]");
				break;
			case ';':
				sb.append("[.?]");
				break;
			case ',':
				sb.append("[.?!]");
				break;
			case '.':
				sb.append("[.]");
				break;
			case '?':
				sb.append("[?]");
				break;
			case '!':
				sb.append("[!]");
				break;
			default:
				return false;
			}
		}
		return imp.matches(sb.toString());
	}
}
