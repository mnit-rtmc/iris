/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  SRF Consulting Group
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

package us.mn.state.dot.tms.client.wysiwyg.selector;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.Widgets;
import us.mn.state.dot.tms.utils.I18N;

/**
 * WYSIWYG DMS Message Editor Warning Form
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
public class WMsgWarningForm extends AbstractForm {

	/** User session */
	private final Session session;
	private WMsgSelectorForm selectorForm;
	private String missingType;
	
	/** Buttons */
	private JButton reload_btn;
	private JButton cancel_btn;
	
	public WMsgWarningForm(Session s, WMsgSelectorForm sForm, String mType) {
		super(I18N.get("wysiwyg.warning.title"), true);
		session = s;
		selectorForm = sForm;
		missingType = mType;
		
		cancel_btn = new JButton(cancel);
		reload_btn = new JButton(reload);
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
		gbc.gridwidth = 3;
		gbc.insets = Widgets.UI.insets();
		gbc.ipadx = 0;
		gbc.ipady = 0;
		gbc.weightx = 0.5;
		gbc.weighty = 0.5;
		
		// warning text
		gbc.gridx = 0;
		gbc.gridy = 0;
		p.add(new JLabel(String.format(I18N.get("wysiwyg.warning.message"), missingType)), gbc);
		
		/* Reload button */
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 0.5;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		p.add(reload_btn, gbc);
		
		/* Cancel button */
		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		p.add(cancel_btn, gbc);
		
		add(p);
	}
	
	/** Cancel action */
	private final IAction cancel = new IAction(
		"wysiwyg.warning.cancel") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
			throws Exception {
			close(session.getDesktop());
		}
	};
	
	/** Reload action */
	private final IAction reload = new IAction(
			"wysiwyg.warning.reload") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception {
			// call the selector form's reload method then close the warning
			selectorForm.reloadForm();
			close(session.getDesktop());
		}
	};
}
