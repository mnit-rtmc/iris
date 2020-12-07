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
import javax.swing.JPanel;

import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.Widgets;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Confirmation form displayed when exiting the WYSIWYG DMS message editor
 * with unsaved changes.
 * 
 * NOTE this could be implemented with a JOptionPane, but this is already
 * written...
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
public class WConfirmExitForm extends AbstractForm {

	/** User session */
	private final Session session;
	private WMsgEditorForm editorForm;
	
	/** Buttons */
	private JButton saveBtn;
	private JButton dontSaveBtn;
	private JButton cancelBtn;
	
	public WConfirmExitForm(Session s, WMsgEditorForm eForm) {
		super(I18N.get("wysiwyg.editor.confirm_exit.title"), true);
		session = s;
		editorForm = eForm;
		
		saveBtn = new JButton(save);
		dontSaveBtn = new JButton(dontSave);
		cancelBtn = new JButton(cancel);
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
		p.add(new ILabel("wysiwyg.editor.confirm_exit.message"), gbc);
		
		/* Save button */
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		p.add(saveBtn, gbc);
		
		/* Don't Save Button */
		gbc.gridx = 1;
		p.add(dontSaveBtn, gbc);
		
		/* Cancel button */
		gbc.gridx = 2;
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		p.add(cancelBtn, gbc);
		
		add(p);
	}
	
	/** Save action */
	private final IAction save = new IAction(
			"wysiwyg.editor.confirm_exit.save") {
		protected void doActionPerformed(ActionEvent e) {
			// have the controller save the message, then close both forms
			editorForm.save.actionPerformed(e);
			editorForm.close();
			close(session.getDesktop());
		}
	};
	
	/** Don't Save action */
	private final IAction dontSave = new IAction(
			"wysiwyg.editor.confirm_exit.dont_save") {
		protected void doActionPerformed(ActionEvent e) {
			// close the editor form and this form without saving
			editorForm.close();
			close(session.getDesktop());
		}
	};
	
	/** Cancel action */
	private final IAction cancel = new IAction(
		"wysiwyg.editor.confirm_exit.cancel") {
		protected void doActionPerformed(ActionEvent e) {
			// close this form without doing anything else
			close(session.getDesktop());
		}
	};
	
}
