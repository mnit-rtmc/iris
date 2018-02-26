/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013-2018  Minnesota Department of Transportation
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

import javax.mail.MessagingException;
import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.Emailer;

/**
 * Handler for email.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class EmailHandler {

	 /** Scheduler for email jobs */
	static private final Scheduler EMAIL = new Scheduler("email");

	/** Disallow instantiation */
	private EmailHandler() { }

	/** Log an error to stderr */
	static private void logEmailError(String msg, String reason) {
		System.err.println(TimeSteward.currentDateTimeString(true) +
			" Email not sent (" + msg + "), " + reason);
	}

	/** Send an email.  This method does not block.
	 * @param sub Subject of email.
	 * @param msg Text of email.
	 * @param recip Recipient of email. */
	static public void sendEmail(final String sub, final String msg,
		final String recip)
	{
		EMAIL.addJob(new Job(0) {
			@Override public String getName() {
				return sub;
			}
			@Override public void perform() {
				doSendEmail(sub, msg, recip);
			}
		});
	}

	/** Send an email.  This method blocks while sending.
	 * @param sub Subject of email.
	 * @param msg Text of email.
	 * @param recip Recipient of email. */
	static private void doSendEmail(String sub, String msg, String recip) {
		if (null == recip || recip.length() <= 0) {
			logEmailError(msg, "invalid recipient");
			return;
		}
		String host = SystemAttrEnum.EMAIL_SMTP_HOST.getString();
		if (null == host || host.length() <= 0) {
			logEmailError(msg, "invalid host");
			return;
		}
		String sender = SystemAttrEnum.EMAIL_SENDER_SERVER.getString();
		if (null == sender || sender.length() <= 0) {
			logEmailError(msg, "invalid sender");
			return;
		}
		try {
			Emailer email = new Emailer(host, sender, recip);
			email.send(sub, msg);
		}
		catch (MessagingException e) {
			logEmailError(msg, "failed: " + e.getMessage());
		}
	}
}
