/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.widget;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import javax.mail.MessagingException;
import javax.naming.AuthenticationException;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.client.SonarShowException;
import us.mn.state.dot.sonar.client.PermissionException;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.InvalidMessageException;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.SEmail;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A swing dialog for displaying exception stack traces.
 *
 * @author Douglas Lau
 */
public class ExceptionDialog extends JDialog {

	/** Vertical box for components in the exception dialog */
	protected final Box box = Box.createVerticalBox();

	/** Owner of any new exception dialogs */
	static protected Frame owner;

	/** Set the owner to use for new exception dialogs */
	static public void setOwner(Frame f) {
		owner = f;
	}

	/** Flag for fatal exceptions */
	protected boolean fatal = false;

	/** Set the fatal status */
	protected void setFatal(boolean f) {
		fatal = f;
		if(fatal)
			setTitle("Program error");
		else
			setTitle("Warning");
	}

	/** Create a new ExceptionDialog */
	public ExceptionDialog(final Exception e) {
		super(owner, true);
		setFatal(false);
		e.printStackTrace();
		TextPanel tpanel = new TextPanel();
		tpanel.addGlue();
		try {
			throw e;
		}
		catch(AuthenticationException ee) {
			tpanel.addText("Authentication failed:");
			tpanel.addText(ee.getMessage());
			tpanel.addSpacing();
			tpanel.addText("Please make sure your user");
			tpanel.addText("name is correct, then");
			tpanel.addText("type your password again.");
		}
		catch(ChangeVetoException ee) {
			tpanel.addText("The change has been prevented");
			tpanel.addText("for the following reason:");
			tpanel.addSpacing();
			tpanel.addText(ee.getMessage());
		}
		catch(PermissionException ee) {
			tpanel.addText("Permission denied:");
			tpanel.addSpacing();
			tpanel.addText(ee.getMessage());
		}
		catch(SonarShowException ee) {
			tpanel.addText("The following message was");
			tpanel.addText("received from the IRIS server:");
			tpanel.addSpacing();
			tpanel.addText(ee.getMessage());
		}
		catch(NumberFormatException ee) {
			tpanel.addText("Number formatting error");
			tpanel.addSpacing();
			tpanel.addText("Please check all numeric");
			tpanel.addText("fields and try again.");
		}
		catch(InvalidMessageException ee) {
			tpanel.addText("Invalid message");
			tpanel.addSpacing();
			tpanel.addText("The sign is unable to display");
			tpanel.addText("the following message:");
			tpanel.addText(ee.getMessage());
			tpanel.addText("Please select a different message");
		}
		catch(ParseException ee) {
			tpanel.addText("Parsing error");
			tpanel.addText(ee.getMessage());
			tpanel.addText("Please try again.");
		}
		catch(SonarException ee) {
			setFatal(true);
			tpanel.addText("This program has encountered");
			tpanel.addText("a problem while communicating");
			tpanel.addText("with the IRIS server.");
			tpanel.addSpacing();
			tpanel.addText(ee.getMessage());
		}
		catch(Exception ee) {
			sendEmailAlert(e, tpanel);
			setFatal(true);
			tpanel.addText("This program has encountered");
			tpanel.addText("a serious problem.");
			addAssistanceMessage(tpanel);
		}
		tpanel.addSpacing();
		String lastLine = I18N.get("ExceptionForm.LastLine");
		if(lastLine != null)
			tpanel.addText(lastLine);
		tpanel.addGlue();
		box.add(tpanel);
		box.add(Box.createVerticalStrut(6));
		Box hbox = Box.createHorizontalBox();
		hbox.add(Box.createHorizontalGlue());
		JButton button = new JButton("OK");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				if(fatal)
					System.exit(-1);
				setVisible(false);
				dispose();
			}
		});
		hbox.add(button);
		hbox.add(Box.createHorizontalStrut(10));
		button = new JButton("Detail");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				new StackTraceDialog(e).setVisible(true);
			}
		});
		hbox.add(button);
		hbox.add(Box.createHorizontalGlue());
		box.add(hbox);
		box.add(Box.createVerticalStrut(6));
		JPanel panel = new JPanel();
		panel.add(box);
		Dimension size = panel.getPreferredSize();
		size.height += 24;
		size.width += 16;
		getContentPane().add(panel);
		setSize(size);
		Screen.centerOnCurrent(this);
	}

	/** Add a message about what to do for assistance */
	protected void addAssistanceMessage(TextPanel tpanel) {
		tpanel.addSpacing();
		tpanel.addText("For assistance, contact an");
		tpanel.addText("IRIS system administrator.");
	}

	/** Send an e-mail alert to the system administrators */
	protected void sendEmailAlert(Exception e, TextPanel tpanel) {
		String sender = SystemAttrEnum.EMAIL_SENDER_CLIENT.getString();
		String recipient =
			SystemAttrEnum.EMAIL_RECIPIENT_BUGS.getString();
		if(sender != null && recipient != null) {
			String trace = getStackTrace(e);
			tpanel.addSpacing();
			try {
				SEmail email = new SEmail(sender, recipient,
					"IRIS Exception", trace);
				email.send();
				tpanel.addText("A detailed error report");
				tpanel.addText("has been emailed to:");
				tpanel.addText(recipient);
			}
			catch(MessagingException ex) {
				ex.printStackTrace();
				tpanel.addText("Unable to send error");
				tpanel.addText("report to:");
				tpanel.addText(recipient);
			}
		}
	}

	/** Get stack trace as a string */
	protected String getStackTrace(Exception e) {
		StringWriter writer = new StringWriter(200);
		e.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}
}
