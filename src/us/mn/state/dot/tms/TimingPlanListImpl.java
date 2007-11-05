/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2007  Minnesota Department of Transportation
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
import java.util.LinkedHashSet;
import java.rmi.RemoteException;

/**
 * This is a list of TimingPlan objects, sorted by Integer id.
 *
 * @author Douglas Lau
 */
class TimingPlanListImpl extends AbstractListImpl implements TimingPlanList {

	/** Linked hash set to hold all the timing plans */
	protected final LinkedHashSet<TMSObjectImpl> set;

	/** Create a new timing plan list */
	public TimingPlanListImpl() throws RemoteException {
		super(false);
		set = new LinkedHashSet<TMSObjectImpl>();
	}

	/** Get an iterator of the timing plans in the list */
	Iterator<TMSObjectImpl> iterator() {
		return set.iterator();
	}

	/** Append a timing plan to the list */
	public synchronized void append(TimingPlanImpl plan) {
		if(set.add(plan)) {
			notifyAdd(set.size() - 1, plan);
		}
	}

	/** Remove a timing plan from the list */
	public synchronized void remove(TimingPlan plan) {
		Iterator<TMSObjectImpl> it = iterator();
		for(int i = 0; it.hasNext(); i++) {
			if(plan.equals(it.next())) {
				it.remove();
				notifyRemove(i);
				return;
			}
		}
	}

	/** Update an element in the list */
	public synchronized TimingPlan update(TimingPlan plan) {
		Iterator<TMSObjectImpl> it = iterator();
		for(int i = 0; it.hasNext(); i++) {
			TimingPlan p = (TimingPlan)it.next();
			if(plan.equals(p)) {
				notifySet(i, p);
				return p;
			}
		}
		return null;
	}

	/** Lookup the matching impl for a timing plan */
	public synchronized TimingPlanImpl lookup(TimingPlan plan) {
		Iterator<TMSObjectImpl> it = iterator();
		while(it.hasNext()) {
			TimingPlanImpl p = (TimingPlanImpl)it.next();
			if(plan.equals(p))
				return p;
		}
		return null;
	}

	/** Get a single element from its index */
	public synchronized TimingPlan getElement(int index) {
		Iterator<TMSObjectImpl> it = iterator();
		for(int i = 0; it.hasNext(); i++) {
			TimingPlan plan = (TimingPlan)it.next();
			if(i == index)
				return plan;
		}
		return null;
	}

	/** Subscribe a listener to this list */
	public synchronized Object[] subscribe(RemoteList listener) {
		super.subscribe(listener);
		int count = set.size();
		if(count < 1)
			return null;
		TimingPlan[] plans = new TimingPlan[count];
		Iterator<TMSObjectImpl> it = iterator();
		for(int i = 0; it.hasNext(); i++)
			plans[i] = (TimingPlan)it.next();
		return plans;
	}
}
