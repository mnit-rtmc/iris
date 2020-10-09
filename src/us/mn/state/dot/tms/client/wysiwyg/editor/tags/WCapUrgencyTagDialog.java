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

package us.mn.state.dot.tms.client.wysiwyg.editor.tags;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

import us.mn.state.dot.tms.CapUrgencyEnum;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.wysiwyg.editor.WController;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtCapUrgency;

/**
 * WYSIWYG DMS Message Editor dialog form for editing IPAWS alert CAP urgency
 * substitution fields.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
class WCapUrgencyTagDialog extends WMultiTagDialog {
	protected WtCapUrgency editTok;
	private ArrayList<CapUrgencyEnum> urgencyValues =
			new ArrayList<CapUrgencyEnum>();
	private ArrayList<String> urgencyValStrs = new ArrayList<String>();
	private ArrayList<WTagParamEnumField<CapUrgencyEnum>> rTypeFields =
			new ArrayList<WTagParamEnumField<CapUrgencyEnum>>();
	private JPanel uValPanel;
	private final ArrayList<JPanel> uValPanels =
			new ArrayList<JPanel>();
	private JButton addFieldBtn;
	private JButton deleteFieldBtn;
	
	public WCapUrgencyTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}

	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtCapUrgency) tok;
		urgencyValues = new ArrayList<CapUrgencyEnum>();
		urgencyValStrs = new ArrayList<String>();
		
		for (String rt: editTok.getResponseTypes()) {
			urgencyValStrs.add(rt);
			CapUrgencyEnum crte = CapUrgencyEnum.fromValue(rt);
			if (crte != null)
				urgencyValues.add(crte);
		}
	}

	@Override
	protected void addTagForm() {
		// TODO something is weird about the order in which the methods in
		// this class are called requiring us to call this an extra time
		if (editTok != null)
			loadFields(editTok);
		
		// by default there are no fields, but the user can add some
		// add an empty panel so we can add urgency fields later
		uValPanel = new JPanel();
		uValPanel.setLayout(new BoxLayout(uValPanel, BoxLayout.Y_AXIS));
		
		// set the preferred height of this panel to several fields (this is
		// easier than figuring out how to get the whole frame to resize)
		Dimension d = uValPanel.getPreferredSize();
		d.height = 200;
		uValPanel.setPreferredSize(d);
		add(uValPanel);
		
		// add buttons to trigger adding or deleting fields
		addFieldBtn = new JButton(addUrgencyFieldAction);
		deleteFieldBtn = new JButton(deleteResponseTypeFieldAction);
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(addFieldBtn);
		p.add(deleteFieldBtn);
		add(p);
		
		// if we already have urgency values, add more fields and enable the
		// delete button
		if (urgencyValues.size() > 0) {
			for (CapUrgencyEnum crt: urgencyValues)
				addUrgencyField(crt);
			deleteFieldBtn.setEnabled(true);
		} else
			deleteFieldBtn.setEnabled(false);
	}
	
	private void refresh() {
		revalidate();
		repaint();
	}
	
	/** Add a new urgency field. */
	private void addUrgencyField(CapUrgencyEnum crt) {
		// make a new field and add it to the panel
		WTagParamEnumField<CapUrgencyEnum> crtf =
				new WTagParamEnumField<CapUrgencyEnum>(
						CapUrgencyEnum.values(), crt, true);
		rTypeFields.add(crtf);
		JPanel p = makeFieldPanel(
				"wysiwyg.cap_urgency_dialog.urgency",  crtf);
		uValPanels.add(p);
		uValPanel.add(p);
		refresh();
		
		// make sure the delete button is enabled
		deleteFieldBtn.setEnabled(true);
	}
	
	/** Action to add a new urgency field. */
	private final IAction addUrgencyFieldAction = new IAction(
			"wysiwyg.cap_urgency_dialog.add_field") {
		@Override
		protected void doActionPerformed(ActionEvent ev) throws Exception {
			addUrgencyField(null);
		}
	};
	
	/** Action to delete a urgency field. */
	private final IAction deleteResponseTypeFieldAction = new IAction(
			"wysiwyg.cap_urgency_dialog.delete_field") {
		@Override
		protected void doActionPerformed(ActionEvent ev) throws Exception {
			// check the number of fields
			if (rTypeFields.size() > 0) {
				// if we have any, delete the last one
				rTypeFields.remove(rTypeFields.size()-1);
				uValPanel.remove(uValPanels.remove(uValPanels.size()-1));
			}
			
			// disable the delete button once all fields are gone
			if (rTypeFields.size() == 0)
				deleteFieldBtn.setEnabled(false);
			
			refresh();
		}
	};
	
	@Override
	protected boolean validateForm() {
		return validateFields(new ArrayList<WTagParamComponent>(rTypeFields));
	}
	
	@Override
	protected WToken makeNewTag() {
		// get any/all urgency fields and pack them into an array
		urgencyValues.clear();
		urgencyValStrs.clear();
		for (WTagParamEnumField<CapUrgencyEnum> crtf: rTypeFields) {
			CapUrgencyEnum crt = crtf.getSelectedItem();
			urgencyValues.add(crt);
			urgencyValStrs.add(crt.value);
		}
		String rtypes[] = new String[urgencyValStrs.size()];
		urgencyValStrs.toArray(rtypes);
		return new WtCapUrgency(rtypes);
	}
	
}
