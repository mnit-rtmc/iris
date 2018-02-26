/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Iteris Inc.
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
import javax.mail.MessagingException;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.Emailer;

/**
 * Server-side Static methods that support Action Plans.
 * @author Michael Darter
 */
public class ActionPlanSystem extends Thread {

	 /** Thread for email jobs */
	static private final Scheduler EMAIL = new Scheduler("email");

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

	/** Log a message to stderr */
	static private void logStderr(String msg) {
		System.err.println(TimeSteward.currentDateTimeString(true) +
			" IRIS Action Plan System " + msg);
	}

	/** Log an error to stderr */
	static private void logEmailError(String msg, String reason) {
		logStderr("Alert!  " + msg + ", " + reason);
	}

	/** Send an email alert. This method does not block.
	 * @arg usr User name
	 * @arg active True if plan is being activated
	 * @arg pname Plan name being activated */
	static public void sendEmailAlert(final String usr,
		final boolean active, final String pname)
	{
		final int PRESEND_WAIT_MS = 2000; // arbitrary
		EMAIL.addJob(new Job(PRESEND_WAIT_MS) {
			@Override public String getName() {
				return "sendEmail";
			}
			@Override public void perform() {
				if (userTriggersAlert(usr)) {
					logStderr("sending email...");
					doSendEmailAlert(usr, active, pname);
				}
			}
		});
	}

	/** Send an email alert. This method blocks while sending.
	 * @arg usr User name
	 * @arg active True if plan is being activated
	 * @arg pname Plan name being activated */
	static private void doSendEmailAlert(String usr, boolean active,
		String pname)
	{
		String msg = "User " + usr +
			(active ? " actived" : " deactivated") +
			" action plan " + "'" + pname + "' on " +
			new Date().toString();
		String host = SystemAttrEnum.EMAIL_SMTP_HOST.getString();
		if (host == null || host.length() <= 0) {
			logEmailError(msg, "invalid host");
			return;
		}
		String sender = SystemAttrEnum.EMAIL_SENDER_SERVER.getString();
		if (sender == null || sender.length() <= 0) {
			logEmailError(msg, "invalid sender");
			return;
		}
		String recip =
			SystemAttrEnum.EMAIL_RECIPIENT_ACTION_PLAN.getString();
		if (recip == null || recip.length() <= 0) {
			logEmailError(msg, "invalid recipient");
			return;
		}
		try {
			String sub = "IRIS Action Plan Alert";
			Emailer email = new Emailer(host, sender, recip);
			email.send(sub, msg);
			logStderr("sent email: sub=" + sub + " msg=" + msg);
		} catch (MessagingException e) {
			logEmailError(msg, "email failed: " + e.getMessage());
		}
	}
}
