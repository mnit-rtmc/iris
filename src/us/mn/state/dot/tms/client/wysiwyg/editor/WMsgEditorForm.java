/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2018  SRF Consulting Group
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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.Widgets;
import us.mn.state.dot.tms.utils.I18N;

/**
 * WYSIWYG DMS Message Editor Selector Form
 *
 * @author Gordon Parikh, John L. Stanley, and Michael Janson - SRF Consulting
 */
@SuppressWarnings("serial")

public class WMsgEditorForm extends AbstractForm {
	
	/** User session */
	private final Session session;
	
	/* Sign/Group and Message being edited */
	private DMS sign;
	private SignGroup sg;
	private QuickMessage qm;
	
	/* Beacon and Prefix Check Boxes */ 
	private JCheckBox beacon_chk; 
	private JCheckBox prefix_chk;

	/* Sign group drop-down (not always present) */
	private Map<String,DMS> dmsList = new HashMap<String,DMS>();
	private String[] dmsNames;
	private JComboBox<String> dms_list;
	 
	/** Buttons */
	private JButton preview_btn;
	private JButton cancel_btn;
	private JButton save_as_btn;
	private JButton save_btn;
	
	public WMsgEditorForm(Session s) {
		// TODO need to add the message name to the title (somehow...)
		super(I18N.get("wysiwyg.editor.title"), true);
		session = s;
		initForm();
	}
	
	public WMsgEditorForm(Session s, DMS d) {
		super(I18N.get("wysiwyg.editor.title"), true);
		session = s;
		sign = d;
		initForm();
	}
	
	public WMsgEditorForm(Session s, SignGroup g) {
		super(I18N.get("wysiwyg.editor.title"), true);
		session = s;
		sg = g;
		initForm();
	}
	
	public WMsgEditorForm(Session s, QuickMessage q, DMS d) {
		super(I18N.get("wysiwyg.editor.title"), true);
		session = s;
		sign = d;
		qm = q;
		initForm();
	}
	
	public WMsgEditorForm(Session s, QuickMessage q, SignGroup g) {
		super(I18N.get("wysiwyg.editor.title"), true);
		session = s;
		sg = g;
		qm = q;
		initForm();
	}
	
	protected void initForm() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// TODO may want to change these dimensions
		setPreferredSize(new Dimension(800,400));
		
		/* Beacon and Prefix Check Boxes */ 
		beacon_chk = new JCheckBox(); 
		prefix_chk = new JCheckBox();
		
		/* Sign group drop-down - only present if editing for sign group */
		if (signGroupMessage()) {
			// get the list of signs in the sign group
			// look through the DmsSignGroups to find all signs with this group
			Iterator<DmsSignGroup> dsgit = DmsSignGroupHelper.iterator();
			while (dsgit.hasNext()) {
				DmsSignGroup dsg = dsgit.next();
				if (dsg.getSignGroup() == sg) {
					DMS dms = dsg.getDms();
					dmsList.put(dms.getName(), dms);
					
				}
			}
			
			// selection handler for the combo box
			class SignSelectionListener implements ActionListener {
				@SuppressWarnings("unchecked")
				public void actionPerformed(ActionEvent e) {
					JComboBox<String> cb = (JComboBox<String>) e.getSource();
					String dmsName = (String) cb.getSelectedItem();
					System.out.printf("Sign '%s' selected...\n", dmsName);
				}
			}
			
			// setup the combo box
			dmsNames = Arrays.stream(dmsList.keySet().toArray()).
					toArray(String[]::new);
			dms_list = new JComboBox<String>(dmsNames);
			dms_list.addActionListener(new SignSelectionListener());
		}
		
		// TODO fill in placeholders (see placeholders in initialize() below)
		
		
		/* Buttons - TODO finish implementing */
		preview_btn = new JButton(preview);
		cancel_btn = new JButton(cancel);
		save_as_btn = new JButton(saveas);
		save_btn = new JButton(save);
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
		gbc.weightx = 0;
		gbc.weighty = 0;
		
		/*** TODO need to add components below (and need to define above first) ***/
		
		/** TODO combine check box and label? (label would be on right) **/
		/* Beacon Label */
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		p.add(new JLabel(I18N.get("wysiwyg.editor.beacon")), gbc);
		
		/* Beacon Check Box */
		gbc.gridx = 1;
		gbc.gridy = 0;
		p.add(beacon_chk, gbc);
		
		/* Prefix Label */
		gbc.gridx = 2;
		gbc.gridy = 0;
		p.add(new JLabel(I18N.get("wysiwyg.editor.prefix")), gbc);
		
		/* Prefix Check Box */
		gbc.gridx = 3;
		gbc.gridy = 0;
		p.add(prefix_chk, gbc);
		
		/* Sign Label */
		gbc.gridx = 0;
		gbc.gridy = 1;
		p.add(new JLabel(getSignLabel()), gbc);
		
		/* Sign Name Label or Group(/Config TODO) Drop-Down */
		gbc.gridx = 1;
		gbc.gridy = 1;
		if (singleSignMessage())
			p.add(new JLabel(sign.getName()), gbc);
		else if (signGroupMessage())
			p.add(dms_list, gbc);
		
		/* Page # Label */
		gbc.gridx = 4;
		gbc.gridy = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		
		// TODO placeholder - this needs to be connected to the controller
		p.add(new JLabel(I18N.get("wysiwyg.editor.page") + " 1"), gbc);
		
		/* Page List Label */
		gbc.gridx = 0;
		gbc.gridy = 2;
		p.add(new JLabel(I18N.get("wysiwyg.editor.page_list")), gbc);
		
		/* Page List (may be a GBL itself) */
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridheight = 1;
		gbc.gridwidth = 4;
		gbc.weightx = 0.5;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		
		// TODO PLACEHOLDER
		p.add(new JScrollPane(new JList<String>(),
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), gbc);
		
		/* Editor (will be a GBL itself) */
		gbc.gridx = 4;
		gbc.gridy = 2;
		gbc.gridheight = 2;
		gbc.gridwidth = 6;
		gbc.fill = GridBagConstraints.BOTH;

		// TODO PLACEHOLDER
		p.add(new JScrollPane(new JList<String>(),
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), gbc);
		
		/* Preview Button */
		gbc.gridx = 6;
		gbc.gridy = 4;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		p.add(preview_btn, gbc);
		
		/* Cancel Button */
		gbc.gridx = 7;
		gbc.gridy = 4;
		p.add(cancel_btn, gbc);
		
		/* Save As Button */
		gbc.gridx = 8;
		gbc.gridy = 4;
		p.add(save_as_btn, gbc);
		
		/* Save Button */
		gbc.gridx = 9;
		gbc.gridy = 4;
		p.add(save_btn, gbc);
		
		
		/*** TODO evaluate need for constraints/fill rules like below ***/
		
//		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
//		gbc.fill = GridBagConstraints.NONE;
//		gbc.anchor = GridBagConstraints.BASELINE_LEADING;

		add(p);
	}
	
	/** Preview action */
	private final IAction preview = new IAction("wysiwyg.editor.preview") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
			throws Exception
		{
			System.out.println("Previewing...");
		}
	};
	
	/** Cancel action */
	private final IAction cancel = new IAction("wysiwyg.editor.cancel") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Cancelling...");
		}
	};
	
	/** Save As action */
	private final IAction saveas = new IAction("wysiwyg.editor.save_as") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Saving As...");
		}
	};
	
	/** Save action */
	private final IAction save = new IAction("wysiwyg.editor.save") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Saving...");
		}
	};
	
	/** Returns true if editing a message for a single sign, false otherwise
	 * (i.e. a group or TODO config). */
	private boolean singleSignMessage() {
		if (sign != null)
			return true;
		else
			return false;
	}
	
	/** Returns true if editing a sign group message */
	private boolean signGroupMessage() {
		if (sg != null)
			return true;
		else
			return false;
	}
	
	/** Returns the string that should be used for the label next to the sign
	 * name or sign group drop-down. */
	private String getSignLabel() {
		// check if editing the message for a single sign or a group
		if (singleSignMessage())
			// just have it say "Sign:"
			return I18N.get("wysiwyg.sign") + ":";
		else if (signGroupMessage())
			// "Sign Group {group_name}, Sign: "
			return I18N.get("wysiwyg.sign_group") + " " + sg.getName() + ", "
				+ I18N.get("wysiwyg.sign") + ":";
		// TODO change conditions above to avoid this?
		else
			return "";
	}
}
	