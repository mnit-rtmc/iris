/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  SRF Consulting Group
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.TreeSet;

import us.mn.state.dot.tms.RwisCondition;

/** Set of RwisConditions in reverse order
 *  (highest priority condition first).
 * 
 * Any condition added to this will only be
 * reported once in the getArray() generated
 * array.
 * 
 * @author John L. Stanley - SRF Consulting
 */

public class RwisConditionSet {

	/** TreeSet of RwisCondition objects
	 *  in highest-priority-first order. */
	TreeSet<RwisCondition> condSet = 
			new TreeSet<RwisCondition>(Collections.reverseOrder());

	/** Create an empty set */
	public RwisConditionSet() {
	}

	/** create a set for a specific ESS */
	public RwisConditionSet(WeatherSensor ess) {
		createConditionSet(ess);
	}

	/** Create shallow clone of an RwisConditionSet */
	@SuppressWarnings("unchecked")
	public RwisConditionSet clone() {
		RwisConditionSet newSet = new RwisConditionSet();
		newSet.condSet = (TreeSet<RwisCondition>) this.condSet.clone();
		return newSet;
	}

	public void add(RwisCondition con) {
		if (con != null)
			condSet.add(con);
	}
	
	public void add(RwisConditionSet set) {
		if (set == null)
			return;
		for (RwisCondition con: set.condSet)
			if (con != null)
				condSet.add(con);
	}

	/** ArrayList of RwisCondition objects */
	static private ArrayList<RwisCondition> conditionArray = null;

	/** Get RwisCondition from an RWIS priority number.
	 *  Returns null if no such RwisCondition exists. */
	static private RwisCondition getCondition(int priority) {
		if (conditionArray == null) {
			// initialize array of RwisCondition objects
			conditionArray = new ArrayList<RwisCondition>();
			Iterator<RwisCondition> it = RwisConditionHelper.iterator();
			while (it.hasNext()) {
				RwisCondition rc = it.next();
				int i = rc.getPriority();
				while (conditionArray.size() < i)
					conditionArray.add(null);
				conditionArray.set(i-1, rc);
			}
		}
		try {
			return conditionArray.get(priority-1);
		}
		catch (IndexOutOfBoundsException ex) {
			return null;
		}
	}
	
	/** Create RwisConditionSet for a specific ESS. */
	//FIXME:  Replace with more robust calculation engine.
	private void createConditionSet(WeatherSensor ess) {
		Integer windgust   = ess.getMaxWindGustSpeed();
		Integer visibility = ess.getVisibility();
		Integer surftemp   = ess.getSurfTemp();
		Integer friction   = ess.getPvmtFriction();
		
		try { // Slippery?
			if ((surftemp > 0) && (friction <= 70)) {
				add(getCondition(1));
			}
		}
		catch (NullPointerException ex) { /* ignore */ }
		
		try { // ReducedVisib?
			if ((visibility >= 402) && (visibility < 1609)) {
				add(getCondition(2));
			}
		}
		catch (NullPointerException ex) { /* ignore */ }
		
		try { // Wind40mph?
			if ((windgust > 64) && (windgust <= 96)) {
				add(getCondition(3));
			}
		}
		catch (NullPointerException ex) { /* ignore */ }
		
		try { // Wind60mph?
			if (windgust > 96) {
				add(getCondition(4));
			}
		}
		catch (NullPointerException ex) { /* ignore */ }
		
		try { // VerySlippery?
			if ((surftemp < 0) && (friction > 60) && (friction <= 70)) {
				add(getCondition(5));
			}
		}
		catch (NullPointerException ex) { /* ignore */ }
		
		try { // LowVisib?
			if (visibility < 402) {
				add(getCondition(6));
			}
		}
		catch (NullPointerException ex) { /* ignore */ }
		
		try { // IceDetected?
			if ((surftemp < 0) && (friction <= 60))
				add(getCondition(7));
		}
		catch (NullPointerException ex) { /* ignore */ }
	}
	
	/** Get array of RwisCondition objects
	 *  in highest-priority-first order. */
	public RwisCondition[] getArray() {
		return condSet.toArray(new RwisCondition[condSet.size()]);
	}
	
	/** Get comma separated list of condition names.
	 * First condition will always be highest priority.
	 * If no conditions, returns empty string. */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (RwisCondition c: condSet) {
			if (sb.length() != 0)
				sb.append(", ");
			sb.append(c.getName());
		}
		return sb.toString();
	}
}
