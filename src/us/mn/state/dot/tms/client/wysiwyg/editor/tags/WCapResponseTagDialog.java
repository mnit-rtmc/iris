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

import us.mn.state.dot.tms.CapResponseTypeEnum;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.wysiwyg.editor.WController;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtCapResponse;

/**
 * WYSIWYG DMS Message Editor dialog form for editing IPAWS alert CAP response
 * type substitution fields.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
class WCapResponseTagDialog extends WMultiTagDialog {
	protected WtCapResponse editTok;
	private ArrayList<CapResponseTypeEnum> responseTypes =
			new ArrayList<CapResponseTypeEnum>();
	private ArrayList<String> responseTypeStrs = new ArrayList<String>();
	private ArrayList<WTagParamEnumField<CapResponseTypeEnum>> rTypeFields =
			new ArrayList<WTagParamEnumField<CapResponseTypeEnum>>();
	private JPanel rTypePanel;
	private final ArrayList<JPanel> rTypePanels =
			new ArrayList<JPanel>();
	private JButton addFieldBtn;
	private JButton deleteFieldBtn;
	
	public WCapResponseTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}

	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtCapResponse) tok;
		responseTypes = new ArrayList<CapResponseTypeEnum>();
		responseTypeStrs = new ArrayList<String>();
		
		for (String rt: editTok.getResponseTypes()) {
			responseTypeStrs.add(rt);
			CapResponseTypeEnum crte = CapResponseTypeEnum.fromValue(rt);
			if (crte != null)
				responseTypes.add(crte);
		}
	}

	@Override
	protected void addTagForm() {
		// TODO something is weird about the order in which the methods in
		// this class are called requiring us to call this an extra time
		if (editTok != null)
			loadFields(editTok);
		
		// by default there are no fields, but the user can add some
		// add an empty panel so we can add response type fields later
		rTypePanel = new JPanel();
		rTypePanel.setLayout(new BoxLayout(rTypePanel, BoxLayout.Y_AXIS));
		
		// set the preferred height of this panel to several (this is
		// easier than figuring out how to get the whole frame to resize)
		Dimension d = rTypePanel.getPreferredSize();
		d.height = 200;
		rTypePanel.setPreferredSize(d);
		add(rTypePanel);
		
		// add buttons to trigger adding or deleting fields
		addFieldBtn = new JButton(addResponseTypeFieldAction);
		deleteFieldBtn = new JButton(deleteResponseTypeFieldAction);
		JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
		p.add(addFieldBtn);
		p.add(deleteFieldBtn);
		add(p);
		
		// if we already have response type values, add more fields and enable
		// the delete button
		if (responseTypes.size() > 0) {
			for (CapResponseTypeEnum crt: responseTypes)
				addResponseTypeField(crt);
			deleteFieldBtn.setEnabled(true);
		} else
			deleteFieldBtn.setEnabled(false);
	}
	
	private void refresh() {
		revalidate();
		repaint();
	}
	
	/** Add a new response type field. */
	private void addResponseTypeField(CapResponseTypeEnum crt) {
		// make a new field and add it to the panel
		WTagParamEnumField<CapResponseTypeEnum> crtf =
				new WTagParamEnumField<CapResponseTypeEnum>(
						CapResponseTypeEnum.values(), crt, true);
		rTypeFields.add(crtf);
		JPanel p = makeFieldPanel(
				"wysiwyg.cap_response_tag_dialog.response_type",  crtf);
		rTypePanels.add(p);
		rTypePanel.add(p);
		refresh();
		
		// make sure the delete button is enabled
		deleteFieldBtn.setEnabled(true);
	}
	
	/** Action to add a new response type field. */
	private final IAction addResponseTypeFieldAction = new IAction(
			"wysiwyg.cap_response_tag_dialog.add_field") {
		@Override
		protected void doActionPerformed(ActionEvent ev) throws Exception {
			addResponseTypeField(null);
		}
	};
	
	/** Action to delete a response type field. */
	private final IAction deleteResponseTypeFieldAction = new IAction(
			"wysiwyg.cap_response_tag_dialog.delete_field") {
		@Override
		protected void doActionPerformed(ActionEvent ev) throws Exception {
			// check the number of fields
			if (rTypeFields.size() > 0) {
				// if we have any, delete the last one
				rTypeFields.remove(rTypeFields.size()-1);
				rTypePanel.remove(rTypePanels.remove(rTypePanels.size()-1));
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
		// get any/all response fields and pack them into an array
		responseTypes.clear();
		responseTypeStrs.clear();
		for (WTagParamEnumField<CapResponseTypeEnum> crtf: rTypeFields) {
			CapResponseTypeEnum crt = crtf.getSelectedItem();
			responseTypes.add(crt);
			responseTypeStrs.add(crt.value);
		}
		String rtypes[] = new String[responseTypeStrs.size()];
		responseTypeStrs.toArray(rtypes);
		return new WtCapResponse(rtypes);
	}
	
}
