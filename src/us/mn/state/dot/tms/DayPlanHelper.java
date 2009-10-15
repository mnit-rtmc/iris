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

import java.util.Calendar;
import us.mn.state.dot.sonar.Checker;

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
		return (DayPlan)namespace.lookupObject(DayPlan.SONAR_TYPE,name);
	}

	/** Find day plan using a Checker */
	static public DayPlan find(final Checker<DayPlan> checker) {
		return (DayPlan)namespace.findObject(DayPlan.SONAR_TYPE, 
			checker);
	}

	/** Check if the given date/time matches a holiday for the plan */
	static public boolean isHoliday(DayPlan plan, Calendar stamp) {
		for(Holiday h: plan.getHolidays()) {
			if(HolidayHelper.matches(h, stamp))
				return true;
		}
		return false;
	}
}
