/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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

/**
 * Action plan state enumeration.
 *
 * @author Douglas Lau
 */
public enum ActionPlanState {

	/** Deployment states for action plans */
	undeployed, deploying, deployed, undeploying;

	/** Get action plan state from an ordinal value */
	static public ActionPlanState fromOrdinal(int o) {
		for(ActionPlanState ps: ActionPlanState.values()) {
			if(ps.ordinal() == o)
				return ps;
		}
		return null;
	}

	/** Check if a state value is deploying or deployed */
	static public boolean isDeployed(int s) {
		return isDeployed(fromOrdinal(s));
	}

	/** Check if a state is deploying or deployed */
	static public boolean isDeployed(ActionPlanState s) {
		return s == deploying || s == deployed;
	}
}
