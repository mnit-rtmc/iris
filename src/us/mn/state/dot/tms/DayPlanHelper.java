/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2017  Minnesota Department of Transportation
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

import java.util.Calendar;

/**
 * Helper class for day plans.
 *
 * @author Douglas Lau
 */
public class DayPlanHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private DayPlanHelper() {
		assert false;
	}

	/** Lookup the day plan with the specified name */
	static public DayPlan lookup(String name) {
		return (DayPlan) namespace.lookupObject(DayPlan.SONAR_TYPE,
			name);
	}

	/** Check if the given date is a holiday for the plan */
	static public boolean isHoliday(DayPlan plan, Calendar stamp) {
		/* A date is considered a holiday unless it matches
		 * a non-holiday matcher and no holiday matchers */
		boolean holiday = true;
		for (DayMatcher dm: plan.getDayMatchers()) {
			if (DayMatcherHelper.matches(dm, stamp)) {
				if (dm.getHoliday())
					return true;
				else
					holiday = false;
			}
		}
		return holiday;
	}
}
