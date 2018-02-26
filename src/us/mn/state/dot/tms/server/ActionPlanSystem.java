/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Iteris Inc.
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.util.Date;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Server-side Static methods that support Action Plans.
 *
 * @author Michael Darter
 * @author Douglas Lau
 */
public class ActionPlanSystem {

	/** Disallow instantiation */
	private ActionPlanSystem() { }

	/** Should the specified user name trigger an Action Plan alert? */
	static private boolean userTriggersAlert(String uname) {
		String ulist =
			SystemAttrEnum.ACTION_PLAN_ALERT_LIST.getString();
		String[] csv = ulist.trim().split(",");
		for (String user : csv) {
			if (uname.trim().equals(user.trim()))
				return true;
		}
		return false;
	}

	/** Send an email alert.  This method does not block.
	 * @param usr User name.
	 * @param active True if plan is being activated.
	 * @param pname Plan name being activated. */
	static public void sendEmailAlert(String usr, boolean active,
		String pname)
	{
		if (userTriggersAlert(usr)) {
			String sub = "IRIS Action Plan Alert";
			String msg = "User " + usr +
				(active ? " actived" : " deactivated") +
				" action plan " + "'" + pname + "' on " +
				new Date().toString();
			String recip = SystemAttrEnum.
				EMAIL_RECIPIENT_ACTION_PLAN.getString();
			EmailHandler.sendEmail(sub, msg, recip);
		}
	}
}
