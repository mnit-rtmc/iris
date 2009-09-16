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

import us.mn.state.dot.sonar.Checker;

/**
 * Helper for controllers.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class ControllerHelper extends BaseHelper {

	/** Disallow instantiation */
	private ControllerHelper() {
		assert false;
	}

	/** Find a controller using a Checker */
	static public Controller find(final Checker<Controller> checker) {
		return (Controller)namespace.findObject(Controller.SONAR_TYPE,
			checker);
	}

	/** Get the AWS controller associated with the AWS comm link.
	 * @return The AWS controller or null if one is not defined. */
	static public Controller getAwsController() {
		final CommLink awscl = CommLinkHelper.getAwsCommLink();
		if(awscl == null)
			return null;
		return find(new Checker<Controller>() {
			public boolean check(Controller c) {
				return c.getCommLink() == awscl;
			}
		});
	}

	/** Update the controller operation intermediate status. This
	 *  method is in the helper so the system attribute can be used
	 *  to conrol if the field is updated, so no network traffic will
	 *  be generated if it is not enabled. */
	static public void updateInterStatus(Controller ctrl, String is) {
		// intermediate status enabled?
		if(SystemAttrEnum.DMS_INTERMEDIATE_STATUS_ENABLE.getBoolean())
			ctrl.setInterStatus(is);
	}
}
