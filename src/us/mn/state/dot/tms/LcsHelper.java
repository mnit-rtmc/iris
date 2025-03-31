/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
import java.util.TreeSet;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * Helper class for Lcs arrays.
 *
 * @author Douglas Lau
 */
public class LcsHelper extends BaseHelper {

	/** Prevent object creation */
	private LcsHelper() {
		assert false;
	}

	/** Get an LCS array iterator */
	static public Iterator<Lcs> iterator() {
		return new IteratorWrapper<Lcs>(namespace.iterator(
			Lcs.SONAR_TYPE));
	}

	/** Count the number of lanes in an LCS array */
	static public int countLanes(Lcs lcs) {
		int lanes = 0;
		Iterator<LcsState> it = LcsStateHelper.iterator();
		while (it.hasNext()) {
			LcsState ls = it.next();
			if (ls.getLcs() == lcs)
				lanes = Math.max(lanes, ls.getLane());
		}
		return lanes;
	}

	/** Make an array of indications for an LCS array */
	static public int[] makeIndications(Lcs lcs, LcsIndication li) {
		int[] ind = new int[countLanes(lcs)];
		for (int ln = 0; ln < ind.length; ln++)
			ind[ln] = li.ordinal();
		return ind;
	}

	/** Make a JSON array of indications for an LCS array */
	static public JSONArray makeIndications(int[] ind) {
		try {
			return new JSONArray(ind);
		}
		catch (JSONException e) {
			System.err.println("makeIndications: " +
				e.getMessage());
			return null;
		}
	}

	/** Lookup LCS states */
	static public LcsState[] lookupStates(Lcs lcs) {
		TreeSet<LcsState> states = new TreeSet<LcsState>();
		Iterator<LcsState> it = LcsStateHelper.iterator();
		while (it.hasNext()) {
			LcsState ls = it.next();
			if (ls.getLcs() == lcs)
				states.add(ls);
		}
		return states.toArray(new LcsState[0]);
	}

	/** Lookup LCS state */
	static public LcsState lookupState(Lcs lcs, int ln, int ind) {
		Iterator<LcsState> it = LcsStateHelper.iterator();
		while (it.hasNext()) {
			LcsState ls = it.next();
			if (ls.getLcs() == lcs &&
			    ls.getLane() == ln &&
			    ls.getIndication() == ind)
				return ls;
		}
		return null;
	}

	/** Check if an LCS array is offline */
	static public boolean isOffline(Lcs lcs) {
		return ItemStyle.OFFLINE.checkBit(lcs.getStyles());
	}

	/** Check if an LCS array is deployed */
	static public boolean isDeployed(Lcs lcs) {
		return ItemStyle.DEPLOYED.checkBit(lcs.getStyles());
	}

	/** Get optional LCS array status attribute, or null */
	static private Object optStatus(Lcs lcs, String key) {
		String status = (lcs != null) ? lcs.getStatus() : null;
		return optJson(status, key);
	}

	/** Get optional LCS indications, or null */
	static public int[] optIndications(Lcs lcs) {
		Object o = optStatus(lcs, Lcs.INDICATIONS);
		if (o instanceof JSONArray) {
			JSONArray arr = (JSONArray) o;
			try {
				int[] ind = new int[arr.length()];
				for (int i = 0; i < arr.length(); i++)
					ind[i] = arr.getInt(i);
				return ind;
			}
			catch (JSONException e) {
				System.err.println("optIndications: " +
					e.getMessage());
			}
		}
		return null;
	}

	/** Get optional LCS array faults, or null */
	static public String optFaults(Lcs lcs) {
		Object faults = optStatus(lcs, Lcs.FAULTS);
		return (faults != null) ? faults.toString() : null;
	}

	/** Test if an LCS array has faults */
	static public boolean hasFaults(Lcs lcs) {
		return optFaults(lcs) != null;
	}

	/** Get current indications of an LCS array */
	static public int[] getIndications(Lcs lcs) {
		int[] ind = optIndications(lcs);
		return (ind != null)
		      ? ind
		      : makeIndications(lcs, LcsIndication.UNKNOWN);
	}

	/** Lookup all indications for one lane of an LCS array */
	static public LcsIndication[] lookupIndications(Lcs lcs, int ln) {
		TreeSet<LcsIndication> ind = new TreeSet<LcsIndication>();
		Iterator<LcsState> it = LcsStateHelper.iterator();
		while (it.hasNext()) {
			LcsState ls = it.next();
			if (ls.getLcs() == lcs && ls.getLane() == ln) {
				LcsIndication li = LcsIndication.fromOrdinal(
					ls.getIndication()
				);
				ind.add(li);
			}
		}
		return ind.toArray(new LcsIndication[0]);
	}
}
