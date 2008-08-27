/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * email convenience methods.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SEmail {

	/** 
	 *  Send an email to the specified recipient 
	 *  @param props Property file, the 'mail.smtp.host' entry is required.
	 *  @param sender e.g. bob@example.com
	 *  @param recipient e.g. bob@example.com
	 *  @param subject Message subject
	 *  @param msgtext Text of the message to send
	 *  @return true on success.
	 */
	public static boolean sendEmail(Properties props,String sender, String recipient, 
		String subject, String msgtext) {

		if(sender==null || recipient==null || sender.length()<=0 || recipient.length()<=0)
			return false;
		subject = (subject==null ? "no subject" : subject);
		msgtext = (msgtext==null ? "no message" : msgtext);

		boolean ok;

		try {
			Session session = Session.getInstance(props, null);
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(sender));
			InternetAddress[] to = InternetAddress.parse(recipient);
			message.addRecipients(Message.RecipientType.TO, to);
			message.setSubject(subject);

			// build message text
			StringWriter writer = new StringWriter(200);
			PrintWriter print = new PrintWriter(writer);
			print.print(new Date().toString() + ": ");
			try {
				print.println("Host: " +
				InetAddress.getLocalHost().getHostName());
			}
			catch(UnknownHostException ee) {
				print.println("Host unknown");
			}
			print.println(msgtext);
			message.setText(writer.toString());

			Transport.send(message);
			ok=true;
		} catch(Exception ex) {
			System.err.println("Warning: SEmail.sendEmail() failed: ex="+ex);
			ok=false;
		}

		return ok;
	}
}

