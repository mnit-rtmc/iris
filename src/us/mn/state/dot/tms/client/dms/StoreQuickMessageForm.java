/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  AHMCT, University of California
 * Copyright (C) 2016-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.Widgets;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A GUI form for storing a composed message into the quick-message library.
 *
 * @author Travis Swanston
 */
public class StoreQuickMessageForm extends AbstractForm {

	/** Width, in characters, of the name field */
	static private final int FIELDWIDTH_NAME = 12;

	/** Width, in characters, of the MULTI field */
	static private final int FIELDWIDTH_MULTI = 48;

	/** User session */
	private final Session session;

	/** QuickMessage cache */
	private final TypeCache<QuickMessage> q_msgs;

	/** MULTI string */
	private final String multi;

	/** Quick-message name text field */
	private final JTextField name_txt;

	/** SignGroup combo box */
	private final SignGroupComboBox group_cbx;

	/** MULTI text field */
	private final JTextField multi_txt;

	/** Store action */
	private final IAction store = new IAction(
		"dms.quick.message.store.form.store")
	{
		protected void doActionPerformed(ActionEvent e)
			throws Exception
		{
			doStore();
		}
	};

	/** Store button */
	private final JButton store_btn;

	/** Cancel action */
	private final IAction cancel = new IAction(
		"dms.quick.message.store.form.cancel")
	{
		protected void doActionPerformed(ActionEvent e)
			throws Exception
		{
			close(session.getDesktop());
		}
	};

	/** Cancel button */
	private final JButton cancel_btn;

	/** DocumentListener to handle changes to name_txt and multi_txt */
	private final DocumentListener text_listener = new DocumentListener() {
		public void changedUpdate(DocumentEvent e) {
			refreshButtonState();
		}
		public void removeUpdate(DocumentEvent e) {
			refreshButtonState();
		}
		public void insertUpdate(DocumentEvent e) {
			refreshButtonState();
		}
	};

	/** ActionListener to handle changes to the sign group combo box */
	private final ActionListener group_listener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			refreshButtonState();
		}
	};

	/** Create a new form */
	public StoreQuickMessageForm(Session s, String m) {
		super(I18N.get("dms.quick.message.store.form"));
		session = s;
		q_msgs = s.getSonarState().getDmsCache().getQuickMessages();
		multi = m;
		name_txt = new JTextField(FIELDWIDTH_NAME);
		group_cbx = new SignGroupComboBox(session);
		multi_txt = new JTextField(FIELDWIDTH_MULTI);
		store_btn = new JButton(store);
		cancel_btn = new JButton(cancel);
	}

	/** Initialize the form */
	@Override
	protected void initialize() {

		// initialize layout
		GridBagLayout gbl = new GridBagLayout();
		JPanel p = new JPanel(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.insets = Widgets.UI.insets();
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.weightx = 0.5;
		gbc.weighty = 0.5;
		// name label
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		p.add(new ILabel("dms.quick.message.store.form.name"), gbc);
		// name field
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		p.add(name_txt, gbc);
		// group label
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		p.add(new ILabel("dms.quick.message.store.form.group"), gbc);
		// group combobox
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		p.add(group_cbx, gbc);
		// multi label
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		p.add(new ILabel("dms.quick.message.store.form.multi"), gbc);
		// multi field
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		p.add(multi_txt, gbc);
		// cancel button
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		p.add(cancel_btn, gbc);
		// store button
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		p.add(store_btn, gbc);
		//
		add(p);

		// initialize elements
		store_btn.setEnabled(false);
		name_txt.getDocument().addDocumentListener(text_listener);
		group_cbx.addActionListener(group_listener);
		multi_txt.getDocument().addDocumentListener(text_listener);
		multi_txt.setText(multi);
	}

	/**
	 * Get the trimmed contents of the name field.
	 * @return The trimmed contents of the name field, never null.
	 */
	private String getNameTrimmed() {
		return name_txt.getText().trim();
	}

	/**
	 * Get the selected SignGroup.
	 * @return The selected SignGroup, or null if none selected.
	 */
	private SignGroup getSignGroup() {
		return (SignGroup) group_cbx.getSelectedItem();
	}

	/**
	 * Get the contents of the MULTI field.
	 * @return The contents of the MULTI field, never null.
	 */
	private String getMulti() {
		return multi_txt.getText();
	}

	/**
	 * Refresh UI status, namely the enabled status of the store button,
	 * based on field validity, SONAR permissions, and session edit mode.
	 */
	private void refreshButtonState() {
		String name = getNameTrimmed();
		SignGroup group = getSignGroup();
		String multiNorm = getMulti();
		boolean canWrite = session.canWrite(QuickMessage.SONAR_TYPE,
			name);
		boolean nameOk = !("".equals(name));
		boolean groupOk = (group != null);
		boolean multiOk = !("".equals(multiNorm));
		if (canWrite && nameOk && groupOk && multiOk)
			store_btn.setEnabled(true);
		else
			store_btn.setEnabled(false);
	}

	/** Do the store */
	private void doStore() {
		String name = getNameTrimmed();
		SignGroup group = getSignGroup();
		String multi = getMulti();

		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("sign_group", group);
		attrs.put("multi", multi);
		q_msgs.createObject(name, attrs);
		close(session.getDesktop());
	}
}
