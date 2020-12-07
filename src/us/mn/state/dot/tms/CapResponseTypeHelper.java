/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import us.mn.state.dot.tms.utils.UniqueNameCreator;

/**
 * Helper class for CAP response type substitution values. Used on the client
 * and server.
 *
 * @author Gordon Parikh
 */
public class CapResponseTypeHelper extends BaseHelper {

	/** Don't instantiate */
	private CapResponseTypeHelper() {
		assert false;
	}
	
	/** Lookup the response type substitution value with the specified name */
	static public CapResponseType lookup(String name) {
		return (CapResponseType) namespace.lookupObject(
				CapResponseType.SONAR_TYPE, name);
	}
	
	/** Lookup the response type substitution value corresponding to the given
	 *  event and response type.
	 */
	static public CapResponseType lookupFor(String event, String rType) {
		if (event == null || rType == null)
			return null;
		
		Iterator<CapResponseType> it = iterator();
		while (it.hasNext()) {
			CapResponseType crt = it.next();
			if (event.equals(crt.getEvent())
					&& rType.equals(crt.getResponseType())) {
				return crt;
			}
		}
		return null;
	}
	
	/** Get an CapResponseType object iterator */
	static public Iterator<CapResponseType> iterator() {
		return new IteratorWrapper<CapResponseType>(namespace.iterator(
				CapResponseType.SONAR_TYPE));
	}

	/** Return all known response type substitution MULTI strings that match
	 *  the given response types (or any response types, if rtypes is empty).
	 */
	static public ArrayList<String> getMaxLen(String[] rtypes) {
		// make a HashSet of the response types to evaluate inclusion
		HashSet<String> rts = null;
		if (rtypes.length > 0)
			rts = new HashSet<String>(Arrays.asList(rtypes));
		
		// go through all response type substitution MULTI strings
		ArrayList<String> multiStrs = new ArrayList<String>();
		Iterator<CapResponseType> it = iterator();
		while (it.hasNext()) {
			CapResponseType crt = it.next();
			if (rts == null || rts.contains(crt.getResponseType())) {
				String multi = crt.getMulti();
				if (multi != null)
					multiStrs.add(multi);
			}
		}
		return multiStrs;
	}
	
	/** Name creator */
	static UniqueNameCreator UNC;
	static {
		UNC = new UniqueNameCreator("cap_resp_type_%d", (n)->lookup(n));
		UNC.setMaxLength(24);
	}

	/** Create a unique CapResponseType record name */
	static public String createUniqueName() {
		return UNC.createUniqueName();
	}
}
