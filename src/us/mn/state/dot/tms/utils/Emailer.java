/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * A simple email abstraction.
 *
 * @author Douglas Lau
 */
public class Emailer {

	/** Email properties */
	private final Properties props = new Properties();

	/** Address of sender */
	private final InternetAddress sender;

	/** Address of recipient(s) */
	private final InternetAddress[] recip;

	/** Create a new emailer.
	 * @param h Email host.
	 * @param s Sender email address.
	 * @param r Recipient email address(es). */
	public Emailer(String h, String s, String r) throws MessagingException {
		props.setProperty("mail.smtp.host", h);
		sender = new InternetAddress(s);
		recip = InternetAddress.parse(r);
	}

	/** Send an email.
	 * @param subject Subject of mail.
	 * @param text Text of mail. */
	public void send(String subject, String text) throws MessagingException{
		MimeMessage mm = createMessage();
		mm.setSubject(subject);
		mm.setText(buildText(text));
		Transport.send(mm);
	}

	/** Create a MIME Message */
	private MimeMessage createMessage() throws MessagingException {
		Session session = Session.getInstance(props, null);
		MimeMessage mm = new MimeMessage(session);
		mm.setFrom(sender);
		mm.addRecipients(Message.RecipientType.TO, recip);
		return mm;
	}

	/** Build message text */
	private String buildText(String text) {
		StringBuilder sb = new StringBuilder();
		sb.append(new Date().toString());
		sb.append(": Host: ");
		sb.append(getHostName());
		sb.append('\n');
		sb.append(text);
		sb.append('\n');
		return sb.toString();
	}

	/** Get the local host name */
	private String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		}
		catch(UnknownHostException e) {
			return "unknown";
		}
	}
}
