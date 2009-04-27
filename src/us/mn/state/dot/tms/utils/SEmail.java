/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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

import java.io.PrintWriter;
import java.io.StringWriter;
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
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * A simple email abstraction.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SEmail {

	/** Address of sender */
	protected final String sender;

	/** Address of recipient */
	protected final String recipient;

	/** Message subject */
	protected final String subject;

	/** Message text */
	protected final String text;

	/** Create a new email.
	 * @param snd Sender (e.g. bob@example.com)
	 * @param rcp Recipient (e.g. bob@example.com)
	 * @param sbj Message subject
	 * @param txt Message text */
	public SEmail(String snd, String rcp, String sbj, String txt) {
		assert snd != null;
		assert rcp != null;
		assert sbj != null;
		assert txt != null;
		sender = snd;
		recipient = rcp;
		subject = sbj;
		text = txt;
	}

	/** Send the email */
	public void send() throws MessagingException {
		MimeMessage message = createMessage();
		message.setFrom(new InternetAddress(sender));
		InternetAddress[] to = InternetAddress.parse(recipient);
		message.addRecipients(Message.RecipientType.TO, to);
		message.setSubject(subject);
		message.setText(buildText());
		Transport.send(message);
	}

	/** Create a MIME Message */
	protected MimeMessage createMessage() {
		Properties props = new Properties();
		props.setProperty("mail.smtp.host",
			SystemAttrEnum.EMAIL_SMTP_HOST.getString());
		Session session = Session.getInstance(props, null);
		return new MimeMessage(session);
	}

	/** Build message text */
	protected String buildText() {
		StringWriter writer = new StringWriter();
		PrintWriter print = new PrintWriter(writer);
		print.print(new Date().toString() + ": ");
		try {
			print.println("Host: " +
			InetAddress.getLocalHost().getHostName());
		}
		catch(UnknownHostException ee) {
			print.println("Host unknown");
		}
		print.println(text);
		return writer.toString();
	}
}
