/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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
package us.mn.state.dot.tms.client.wysiwyg.editor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgPatternHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IWorker;
import us.mn.state.dot.tms.client.widget.Widgets;
import us.mn.state.dot.tms.client.wysiwyg.selector.WMsgSelectorForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * WYSIWYG DMS Message Editor New Message Form
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
public class WMsgNewMsgForm extends AbstractForm {

	/** Amount of time in nanoseconds to wait for a message to be created */
	static private final long MAX_WAIT = 10000000000L;

	/** User session */
	private final Session session;

	/** Handle to controller or selector form */
	private WController controller;
	private WMsgSelectorForm selectorForm;

	/** Sign we were given */
	private DMS sign;

	/** Message pattern (for cloning, not always provided) */
	private MsgPattern pattern;

	/** Info message */
	private JLabel infoMsg;

	/** Text entry field */
	private JTextField msgNameInput;

	/** Buttons */
	private JButton ok_btn;
	private JButton cancel_btn;

	public WMsgNewMsgForm(Session s, WMsgSelectorForm sForm, DMS d) {
		super(I18N.get("wysiwyg.new_message.title"), true);
		session = s;
		selectorForm = sForm;
		sign = d;
		initForm();

		// the OK button does something slightly different depending on how
		// we were called
		ok_btn = new JButton(createMsg);
	}

	public WMsgNewMsgForm(Session s, WMsgSelectorForm sForm, DMS d, MsgPattern pat) {
		super(I18N.get("wysiwyg.new_message.title"), true);
		session = s;
		selectorForm = sForm;
		sign = d;
		pattern = pat;
		initForm();

		// prefill the text with the previous message name
		msgNameInput.setText(pattern.getName());
		ok_btn = new JButton(cloneMsg);
	}

	public WMsgNewMsgForm(Session s, WController c, String prefill) {
		super(I18N.get("wysiwyg.new_message.title"), true);
		session = s;
		controller = c;
		initForm();
		ok_btn = new JButton(saveMsgAs);

		// prefill the text with the previous message name (or whatever really)
		msgNameInput.setText(prefill);
	}

	private void initForm() {
		infoMsg = new JLabel(I18N.get("wysiwyg.new_message.info"));
		msgNameInput = new JTextField();
		cancel_btn = new JButton(cancel);
	}

	/** Initialize the form */
	@Override
	protected void initialize() {
		// initialize layout
		GridBagLayout gbl = new GridBagLayout();
		JPanel p = new JPanel(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridheight = 1;
		gbc.gridwidth = 2;
		gbc.insets = Widgets.UI.insets();
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;

		/* info text */
		gbc.gridx = 0;
		gbc.gridy = 0;
		p.add(infoMsg, gbc);

		/* New Message name input */
		gbc.gridy = 1;
		p.add(msgNameInput, gbc);

		/* OK button */
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		p.add(ok_btn, gbc);

		/* Cancel button */
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		p.add(cancel_btn, gbc);

		add(p);
	}

	/** Add a warning to the info text that the message exists already */
	private void setWarningText() {
		infoMsg.setText(I18N.get("wysiwyg.new_message.warning"));
	}

	/** Add an error to the info text that there was a problem creating the
	 *  message.
	 */
	private void setErrorText() {
		infoMsg.setText(I18N.get("wysiwyg.new_message.error"));
	}

	/** Reset the info label to the original prompt
	 *  TODO not sure if this is actually needed. */
	private void resetInfoLabel() {
		infoMsg.setText(I18N.get("wysiwyg.new_message.info"));
	}

	/** Cancel action */
	private final IAction cancel = new IAction(
		"wysiwyg.new_message.cancel") {
		protected void doActionPerformed(ActionEvent e)
			throws Exception {
			close(session.getDesktop());
		}
	};

	/** Create Message action */
	private final IAction createMsg = new IAction(
			"wysiwyg.new_message.ok") {
		protected void doActionPerformed(ActionEvent e) {
			// try to create a new message pattern
			String newMsgName = createNewMsgPattern();

			// create and start a worker if we get one
			if (newMsgName != null) {
				IWorker<MsgPattern> worker = new IWorker<MsgPattern>() {
					@Override
					protected MsgPattern doInBackground() {
						// create the message
						if (sign != null) {
							WMsgSelectorForm.CreateMsg(
								session, sign, newMsgName);
						}

						// wait for SONAR to create the new message
						MsgPattern pat = null;
						long tStart = System.nanoTime();
						while (pat == null) {
							pat = MsgPatternHelper.lookup(newMsgName);
							long tElapsed = System.nanoTime() - tStart;
							if (tElapsed > MAX_WAIT)
								break;
						}
						return pat;
					}

					@Override
					public void done() {
						pattern = getResult();
						try {
							if (pattern != null) {
								selectorForm.reloadForm();

								if (sign != null) {
									WMsgSelectorForm.EditMsg(
										session, pattern, sign);
								}
								close(session.getDesktop());
							} else
								setErrorText();
						} catch (Exception e) {
							setErrorText();
						}
					}
				};
				worker.execute();
			} else
				// if not, show a warning
				setWarningText();
		}
	};

	/** Clone Message action */
	private final IAction cloneMsg = new IAction(
			"wysiwyg.new_message.ok") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception {
			// try to create a new message pattern
			String newMsgName = createNewMsgPattern();

			// create and start a worker if we get one
			if (newMsgName != null) {
				IWorker<MsgPattern> worker = new IWorker<MsgPattern>() {
					@Override
					protected MsgPattern doInBackground() {
						// the clone operation is the same for signs and
						// groups (we use the same sign group as the existing
						// message pattern)
						WMsgSelectorForm.CloneMsg(session, pattern, newMsgName);

						// wait for SONAR to create the new message
						MsgPattern pat = null;
						long tStart = System.nanoTime();
						while (pat == null) {
							pat = MsgPatternHelper.lookup(newMsgName);
							long tElapsed = System.nanoTime() - tStart;
							if (tElapsed > MAX_WAIT)
								break;
						}
						return pat;
					}

					@Override
					public void done() {
						pattern = getResult();
						try {
							if (pattern != null) {
								selectorForm.reloadForm();

								if (sign != null) {
									WMsgSelectorForm.EditMsg(
										session, pattern, sign);
								}
								close(session.getDesktop());
							} else
								setErrorText();
						} catch (Exception e) {
							setErrorText();
						}
					}
				};
				worker.execute();
			} else
				// if not, show a warning
				setWarningText();
		}
	};

	/** Save Message As action */
	private final IAction saveMsgAs = new IAction(
			"wysiwyg.new_message.ok") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception {
			// try to create a new message pattern
			String newMsgName = createNewMsgPattern();

			// if it works (i.e. there is no existing message with that name)
			// then tell the controller to save as and close this form
			if (newMsgName != null) {
				controller.saveNewMsgPattern(newMsgName);
				close(session.getDesktop());
			} else {
				// if not, show a warning
				setWarningText();
			}
		}
	};

	/** Create a new MsgPattern from the current text input
	 *  @return the new message pattern that was created, or null if it existed
	 */
	private String createNewMsgPattern() {
		// get the message name from the text input field
		String newMsgName = msgNameInput.getText().trim();
		System.out.println(String.format(
			"Creating new MsgPattern '%s' ...", newMsgName));

		// check if the message exists already
		MsgPattern pat = MsgPatternHelper.lookup(newMsgName);
		return (pat != null) ? null : newMsgName;
	}
}
