/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.help;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.net.ConnectException;
import java.text.ParseException;
import javax.naming.AuthenticationException;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import us.mn.state.dot.sonar.SonarException;
import us.mn.state.dot.sonar.client.SonarShowException;
import us.mn.state.dot.sonar.client.PermissionException;
import us.mn.state.dot.tms.ChangeVetoException;
import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.client.IrisClient;
import us.mn.state.dot.tms.client.widget.Screen;
import us.mn.state.dot.tms.client.widget.TextPanel;

/**
 * A swing dialog for displaying exception stack traces.
 *
 * @author Douglas Lau
 */
public class ExceptionDialog extends JDialog {

	/** Set the title */
	private void setTitle(boolean e) {
		if (e)
			setTitle(I18N.get("help.exception.error"));
		else
			setTitle(I18N.get("help.exception.warning"));
	}

	/** Iris client frame */
	private final IrisClient client;

	/** Action listener to dismiss dialog */
	private final ActionListener dismiss = new ActionListener() {
		public void actionPerformed(ActionEvent a) {
			setVisible(false);
			dispose();
		}
	};

	/** Action listener to exit client */
	private final ActionListener exit = new ActionListener() {
		public void actionPerformed(ActionEvent a) {
			setVisible(false);
			dispose();
			if (client != null)
				client.logout();
			System.exit(-1);
		}
	};

	/** Create a new exception dialog without an owner */
	public ExceptionDialog() {
		super();
		client = null;
		setResizable(false);
	}

	/** Create a new exception dialog */
	public ExceptionDialog(IrisClient ic) {
		super(ic, true);
		client = ic;
		setResizable(false);
	}

	/** Show an exception */
	public void show(Exception e) {
		e.printStackTrace();
		TextPanel pnl = createMessagePanel(e);
		getContentPane().removeAll();
		getContentPane().add(pnl);
		pack();
		Screen.centerOnCurrent(this);
		setVisible(true);
	}

	/** Create a text panel for an exception */
	private TextPanel createMessagePanel(final Exception e) {
		boolean fatal = false;
		boolean detail = false;
		TextPanel p = new TextPanel();
		p.addGlue();
		if (e instanceof ConnectException)
			p.addText(I18N.get("help.exception.connect"));
		else if (e instanceof EOFException) {
			fatal = true;
			p.addText(I18N.get("help.exception.disconnect"));
		}
		else if (e instanceof AuthenticationException) {
			p.addText(I18N.get("help.exception.auth.failed"));
			p.addText(e.getMessage());
			p.addSpacing();
			p.addText(I18N.get("help.exception.auth.advice"));
		}
		else if (e instanceof ChangeVetoException) {
			p.addText(I18N.get("help.exception.change.veto"));
			p.addSpacing();
			p.addText(e.getMessage());
		}
		else if (e instanceof PermissionException) {
			p.addText(I18N.get("help.exception.permission.denied"));
			p.addSpacing();
			p.addText(e.getMessage());
		}
		else if (e instanceof SonarShowException) {
			p.addText(I18N.get("help.exception.show"));
			p.addSpacing();
			p.addText(e.getMessage());
		}
		else if (e instanceof NumberFormatException) {
			p.addText(I18N.get("help.exception.number.format"));
			p.addSpacing();
			p.addText(I18N.get("help.exception.number.advice"));
		}
		else if (e instanceof InvalidMsgException) {
			p.addText(I18N.get("help.exception.invalid.msg"));
			p.addSpacing();
			p.addText(e.getMessage());
		}
		else if (e instanceof ParseException) {
			p.addText(I18N.get("help.exception.parsing"));
			p.addSpacing();
			p.addText(e.getMessage());
		}
		else if (e instanceof SonarException) {
			fatal = true;
			detail = true;
			p.addText(I18N.get("help.exception.sonar"));
			p.addSpacing();
			p.addText(e.getMessage());
			p.addText(I18N.get("help.exception.assist"));
		}
		else if (e instanceof Exception) {
			fatal = true;
			detail = true;
			p.addText(I18N.get("help.exception.unknown"));
			p.addSpacing();
			p.addText(I18N.get("help.exception.assist"));
		}
		p.addGlue();
		p.addSpacing();
		setTitle(detail);
		p.add(createButtonBox(e, fatal, detail));
		return p;
	}

	/** Create a button box */
	private Box createButtonBox(final Exception e, boolean fatal,
		boolean detail)
	{
		Box hbox = Box.createHorizontalBox();
		hbox.add(Box.createHorizontalGlue());
		JButton btn = new JButton(I18N.get("help.exception.dismiss"));
		btn.addActionListener((fatal) ? (exit) : (dismiss));
		hbox.add(btn);
		if (detail) {
			hbox.add(Box.createHorizontalStrut(10));
			JButton dtl = new JButton(I18N.get(
				"help.exception.detail"));
			dtl.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent a) {
					JDialog std = new StackTraceDialog(e);
					std.setVisible(true);
				}
			});
			hbox.add(dtl);
		}
		hbox.add(Box.createHorizontalGlue());
		return hbox;
	}
}
