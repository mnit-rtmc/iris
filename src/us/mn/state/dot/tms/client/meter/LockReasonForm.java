/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.meter;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import us.mn.state.dot.tms.utils.ActionJob;
import us.mn.state.dot.tms.client.toast.AbstractForm;

/**
 * LockReasonForm is a swing dialog for entering a reason for locking a meter
 *
 * @author Douglas Lau
 */
public class LockReasonForm extends AbstractForm {

	/** Field to enter reason */
	protected final JTextField reason = new JTextField(20);

	/** OK button */
	protected final JButton ok = new JButton("OK");

	/** Meter proxy */
	protected final MeterProxy proxy;

	/** Create a LockReasonForm */
	public LockReasonForm(MeterProxy p) {
		super("Lock Meter " + p.getId());
		proxy = p;
	}

	/** Initialize the form */
	protected void initialize() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(Box.createVerticalGlue());
		add(new JLabel("What is the reason for locking?"));
		add(Box.createVerticalStrut(VGAP));
		add(reason);
		add(Box.createVerticalStrut(VGAP));
		add(ok);
		add(Box.createVerticalGlue());
		new ActionJob(this, ok) {
			public void perform() throws Exception {
				pressedOk();
			}
		};
		reason.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent ev) {
				if(ev.getKeyCode() == KeyEvent.VK_ENTER)
					ok.doClick();
			}
		});
	}

	/** Add an item to the list */
	protected void pressedOk() throws Exception {
		proxy.meter.setLocked(true, reason.getText());
		close();
	}
}
