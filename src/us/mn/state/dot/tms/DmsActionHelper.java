/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2022  Minnesota Department of Transportation
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
		Set<SignGroup> plan_groups = findGroups(ap);
		TreeSet<DMS> plan_signs = new TreeSet<DMS>(
			new NumericAlphaComparator<DMS>());
		Iterator<DmsSignGroup> git = DmsSignGroupHelper.iterator();
		while (git.hasNext()) {
			DmsSignGroup dsg = git.next();
			if (plan_groups.contains(dsg.getSignGroup()))
				plan_signs.add(dsg.getDms());
		}
		return plan_signs;
	}

	/** Find all sign groups associated with an action plan */
	static public Set<SignGroup> findGroups(ActionPlan ap) {
		HashSet<SignGroup> plan_groups = new HashSet<SignGroup>();
		Iterator<DmsAction> dit = iterator();
		while (dit.hasNext()) {
			DmsAction da = dit.next();
			if (da.getActionPlan() == ap)
				plan_groups.add(da.getSignGroup());
		}
		return plan_groups;
	}

	/** Find sign groups associated with an action plan and sign config */
	static public Set<SignGroup> findGroups(ActionPlan ap, SignConfig cfg) {
		HashSet<SignGroup> groups = new HashSet<SignGroup>();
		Iterator<DmsAction> dit = iterator();
		while (dit.hasNext()) {
			DmsAction da = dit.next();
			if (da.getActionPlan() == ap) {
				MsgPattern pat = da.getMsgPattern();
				if (pat.getSignConfig() == cfg)
					groups.add(da.getSignGroup());
			}
		}
		return groups;
	}
}
