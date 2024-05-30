/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2024  Minnesota Department of Transportation
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * Helper class for DMS actions.
 *
 * @author Douglas Lau
 */
public class DmsActionHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private DmsActionHelper() {
		assert false;
	}

	/** Lookup the DMS action with the specified name */
	static public DmsAction lookup(String name) {
		return (DmsAction)namespace.lookupObject(DmsAction.SONAR_TYPE,
			name);
	}

	/** Get a DMS action iterator */
	static public Iterator<DmsAction> iterator() {
		return new IteratorWrapper<DmsAction>(namespace.iterator(
			DmsAction.SONAR_TYPE));
	}

	/** Get set of DMS controlled by an action plan */
	static public TreeSet<DMS> findSigns(ActionPlan ap) {
		Set<String> plan_hashtags = findHashtags(ap);
		TreeSet<DMS> plan_signs = new TreeSet<DMS>(
			new NumericAlphaComparator<DMS>());
		Iterator<DMS> it = DMSHelper.iterator();
		while (it.hasNext()) {
			DMS dms = it.next();
			for (String ht: plan_hashtags) {
				if (HashtagHelper.hasHashtag(dms, ht)) {
					plan_signs.add(dms);
					break;
				}
			}
		}
		return plan_signs;
	}

	/** Find all DMS hashtags associated with an action plan */
	static public Set<String> findHashtags(ActionPlan ap) {
		HashSet<String> hashtags = new HashSet<String>();
		Iterator<DmsAction> it = iterator();
		while (it.hasNext()) {
			DmsAction da = it.next();
			if (da.getActionPlan() == ap)
				hashtags.add(da.getDmsHashtag());
		}
		return hashtags;
	}

	/** Get set of hashtags with an action using a given message pattern */
	static public Set<String> findHashtags(MsgPattern pat) {
		HashSet<String> hashtags = new HashSet<String>();
		Iterator<DmsAction> it = iterator();
		while (it.hasNext()) {
			DmsAction da = it.next();
			if (da.getMsgPattern() == pat)
				hashtags.add(da.getDmsHashtag());
		}
		return hashtags;
	}
}
