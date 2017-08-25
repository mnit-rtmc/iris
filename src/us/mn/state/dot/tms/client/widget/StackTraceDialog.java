/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2017  Minnesota Department of Transportation
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * A swing dialog for displaying exception stack traces.
 *
 * @author Douglas Lau
 */
public class StackTraceDialog extends JDialog {

	/** Create a new stack trace dialog */
	protected StackTraceDialog(Exception e) {
		setTitle("Exception detail");
		setModal(true);
		Box box = Box.createVerticalBox();
		JTextArea area = new JTextArea();
		area.append(getStackTrace(e));
		JScrollPane scroll = new JScrollPane(area);
		box.add(scroll);
		box.add(Box.createVerticalStrut(6));
		JButton ok = new JButton("OK");
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				setVisible(false);
				dispose();
			}
		});
		box.add(ok);
		box.add(Box.createVerticalStrut(6));
		Dimension size = box.getPreferredSize();
		size.height += 32;
		size.width += 16;
		getContentPane().add(box);
		setSize(size);
		Screen.centerOnCurrent(this);
	}

	/** Get stack trace as a string */
	private String getStackTrace(Exception e) {
		StringWriter writer = new StringWriter(200);
		e.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}
}
