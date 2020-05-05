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
 * WYSIWYG DMS Message Editor Confirmation Form for deleting QuickMessages.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
public class WMsgConfirmDeleteForm extends AbstractForm {

	/** User session */
	private final Session session;
	private WMsgSelectorForm selectorForm;
	private String messageName;
	
	/** Buttons */
	private JButton yes_btn;
	private JButton no_btn;
	
	public WMsgConfirmDeleteForm(Session s,
			WMsgSelectorForm sForm, String mName) {
		super(I18N.get("wysiwyg.confirm_delete.title"), true);
		session = s;
		selectorForm = sForm;
		messageName = mName;
		
		no_btn = new JButton(cancel);
		yes_btn = new JButton(delete);
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
		gbc.weightx = 0.5;
		gbc.weighty = 0.5;
		
		// warning text
		gbc.gridx = 0;
		gbc.gridy = 0;
		p.add(new JLabel(String.format(I18N.get(
				"wysiwyg.confirm_delete.message"), messageName)), gbc);
		
		/* Yes button */
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		p.add(yes_btn, gbc);
		
		/* No button */
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		p.add(no_btn, gbc);
		
		add(p);
	}
	
	/** Cancel action */
	private final IAction cancel = new IAction(
		"wysiwyg.confirm_delete.no") {
		protected void doActionPerformed(ActionEvent e)
			throws Exception {
			close(session.getDesktop());
		}
	};
	
	/** Delete action */
	private final IAction delete = new IAction(
			"wysiwyg.confirm_delete.yes") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception {
			// call the selector form's deleteSelectedMessage method then
			// close the warning
			selectorForm.deleteSelectedMessage();
			close(session.getDesktop());
		}
	};
}
