/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2012  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dinrelay;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.LCSIndication;
import us.mn.state.dot.tms.LCSIndicationHelper;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.DMSImpl;
import us.mn.state.dot.tms.server.LCSArrayImpl;
import us.mn.state.dot.tms.server.comm.OpDevice;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * An LCS array operation.
 *
 * @author Douglas Lau
 */
abstract public class OpLCS extends OpDevice<DinRelayProperty> {

	/** LCS array to query */
	protected final LCSArrayImpl lcs_array;

	/** DMS corresponsing to each LCS in the array */
	protected final DMSImpl[] dmss;

	/** Set of controllers for the LCS array */
	protected final Set<ControllerImpl> controllers;

	/** Create a new LCS operation */
	protected OpLCS(PriorityLevel p, LCSArrayImpl l) {
		super(p, l);
		lcs_array = l;
		dmss = lookupDMSs();
		controllers = lookupControllers();
	}

	/** Lookup DMSs for an LCS array */
	private DMSImpl[] lookupDMSs() {
		LCS[] lcss = LCSArrayHelper.lookupLCSs(lcs_array);
		DMSImpl[] _dmss = new DMSImpl[lcss.length];
		for(int i = 0; i < lcss.length; i++) {
			DMS dms = DMSHelper.lookup(lcss[i].getName());
			if(dms instanceof DMSImpl)
				_dmss[i] = (DMSImpl)dms;
		}
		return _dmss;
	}

	/** Lookup the set of controllers for an LCS array */
	private Set<ControllerImpl> lookupControllers() {
		HashSet<ControllerImpl> set = new HashSet<ControllerImpl>();
		Iterator<LCSIndication> it = LCSIndicationHelper.iterator();
		while(it.hasNext()) {
			LCSIndication li = it.next();
			if(li.getLcs().getArray() == lcs_array) {
				Controller c = li.getController();
				if(c instanceof ControllerImpl)
					set.add((ControllerImpl)c);
			}
		}
		return set;
	}
}
