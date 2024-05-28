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

package us.mn.state.dot.tms.server.rwis;

import us.mn.state.dot.tms.RwisCondition;
import us.mn.state.dot.tms.RwisConditionSet;
import us.mn.state.dot.tms.server.DMSImpl;

/** RwisScratchpad
 * 
 * Scratchpad object used for performing RWIS
 * calculations and for holding RWIS sign
 * info between RWIS-update cycles.
 * 
 * @author John L. Stanley - SRF Consulting
 */

public class RwisScratchpad {

	private DMSImpl dms;

	/** Construct RwisScratchpad object */
	public RwisScratchpad(DMSImpl dms) {
		this.dms = dms;
	}

	/** Get DMS */
	public DMSImpl getDms() {
		return dms;
	}
	
	//---------- RwisConditionSet
	
	private RwisConditionSet conditions = new RwisConditionSet();

	/** Clear the condition set. */
	public void clearConditions() {
		conditions = new RwisConditionSet();
	}

	/** Add a new condition. */
	public void addCondition(RwisCondition newcon) {
		if (newcon == null)
			return;
		conditions.add(newcon);
	}

	public void setConditionSet(RwisConditionSet conditionSet) {
		conditions = new RwisConditionSet();
		if (conditionSet != null)
			conditions.add(conditionSet);
	}
}
