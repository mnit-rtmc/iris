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

package us.mn.state.dot.tms.client.wysiwyg.editor;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.RasterBuilder;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.dms.DMSPanelPager;
import us.mn.state.dot.tms.client.dms.SignFacePanel;
import us.mn.state.dot.tms.client.dms.SignPixelPanel;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.Icons;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.client.widget.Widgets;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.wysiwyg.WPage;

/**
 * WYSIWYG DMS Message Editor Form
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WMsgEditorForm extends AbstractForm {
	
	/** User session */
	private final Session session;
	
	/** Frame containing this form */
	private JInternalFrame frame;
	
	/* Sign/Group and Message being edited */
	private DMS sign;
	private SignGroup sg;
	private QuickMessage qm;
	
	/* what "mode" (single sign or sign group) we're in */ 
	private Boolean singleSign;
	
	/* Controller - for handling back and forth between the GUI and renderer */
	private WController controller;
	
	/* Currently selected page (defaults to first available) */
//	private WMsgSignPage selectedPage;
	private JLabel pg_num_lbl;
	
	/* Menu Bar */
	private WMsgEditorMenuBar menu_bar;
	
	/* Beacon and Prefix Check Boxes */ 
	private JCheckBox beacon_chk; 
	private JCheckBox prefix_chk;

	/* Sign drop-down (only present for groups) */
	private JComboBox<String> dms_list;
	
	/* Page List */
	private JPanel page_btn_pnl;
	private JButton page_add_btn;
	private JButton page_del_btn;
	private JButton page_mv_up_btn;
	private JButton page_mv_down_btn;
	private JList<WPage> page_list;
	private JScrollPane page_list_pn;
	
	/* Main Editor Panel */
	private WMsgEditorPanel epanel;
	
	/** Buttons */
	private JButton preview_btn;
	private JButton cancel_btn;
	private JButton save_as_btn;
	private JButton save_btn;
	
	public WMsgEditorForm(Session s) {
		// TODO need to add the message name to the title (somehow...)
		super(getWindowTitle(null), true);
		session = s;
		controller = new WController(this);
		initForm();
	}
	
	// TODO need to figure out how to deal with new QuickMessages (where/when/
	// how is name created???)
	
	public WMsgEditorForm(Session s, DMS d) {
		super(getWindowTitle(null), true);
		session = s;
		sign = d;
		singleSign = true;
		controller = new WController(this, sign);
		initForm();
	}
	
	public WMsgEditorForm(Session s, SignGroup g) {
		super(getWindowTitle(null), true);
		session = s;
		sg = g;
		singleSign = false;
		controller = new WController(this, sg);
		initForm();
	}
	
	public WMsgEditorForm(Session s, QuickMessage q, DMS d) {
		super(getWindowTitle(q), true);
		session = s;
		sign = d;
		qm = q;
		singleSign = true;
		controller = new WController(this, qm, sign);
		initForm();
	}
	
	public WMsgEditorForm(Session s, QuickMessage q, SignGroup g) {
		super(getWindowTitle(q), true);
		session = s;
		sg = g;
		qm = q;
		singleSign = false;
		controller = new WController(this, qm, sg);
		initForm();
	}
	
	/** Get the current client session */
	public Session getSession() {
		return session;
	}
	
	public void setWindowTitle(QuickMessage q) {
		frame.setTitle(getWindowTitle(q));
	}
	
	public static String getWindowTitle(QuickMessage q) {
		String editorName = I18N.get("wysiwyg.editor.title");
		String msgName = q != null ? q.getName() : "<Untitled>";
		return String.format("%s - %s", editorName, msgName);
	}
	
	protected void initForm() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// TODO may want to change these dimensions
		// OR BETTER YET - figure out how to make it more adaptive...
		setPreferredSize(new Dimension(1100,600));
		
		/* Menu Bar */
		menu_bar = new WMsgEditorMenuBar();
		
		/* Beacon and Prefix Check Boxes */ 
		beacon_chk = new JCheckBox(); 
		prefix_chk = new JCheckBox();
		
		/* Sign group drop-down - only present if editing for sign group */
		if (signGroupMessage()) {
			dms_list = controller.getSignGroupComboBox();
		}
		
		/* Page number label (default to 1) */
		pg_num_lbl = new JLabel(String.format(I18N.get("wysiwyg.editor.page_number"), 1));
		
		/* Page List */
		page_list = controller.getPageList();
		
		/* Page buttons */
		page_add_btn = new JButton(controller.pageAdd);
		ImageIcon pg_add_icon = Icons.getIconByPropName("wysiwyg.editor.page_add");
		page_add_btn.setIcon(pg_add_icon);
		page_add_btn.setHideActionText(true);
		page_add_btn.setMargin(new Insets(0,0,0,0));
		
		page_del_btn = new JButton(controller.pageDelete);
		ImageIcon pg_del_icon = Icons.getIconByPropName("wysiwyg.editor.page_delete");
		page_del_btn.setIcon(pg_del_icon);
		page_del_btn.setHideActionText(true);
		page_del_btn.setMargin(new Insets(0,0,0,0));
		
		page_mv_up_btn = new JButton(controller.pageMoveUp);
		ImageIcon pg_mv_up_icon = Icons.getIconByPropName("wysiwyg.editor.page_move_up");
		page_mv_up_btn.setIcon(pg_mv_up_icon);
		page_mv_up_btn.setHideActionText(true);
		page_mv_up_btn.setMargin(new Insets(0,0,0,0));
		
		page_mv_down_btn = new JButton(controller.pageMoveDown);
		ImageIcon pg_mv_down_icon = Icons.getIconByPropName("wysiwyg.editor.page_move_down");
		page_mv_down_btn.setIcon(pg_mv_down_icon);
		page_mv_down_btn.setHideActionText(true);
		page_mv_down_btn.setMargin(new Insets(0,0,0,0));
		
		/* Main Editor Panel */
		epanel = new WMsgEditorPanel(controller);
		
		/* Buttons - TODO finish implementing */
		preview_btn = new JButton(preview);
		cancel_btn = new JButton(cancel);
		save_as_btn = new JButton(saveas);
		save_btn = new JButton(save);
		
		// TODO temporary
		preview_btn.setEnabled(false);
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
		
		/** TODO combine check box and label? (label would be on right) **/
		/* Beacon Label */
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
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
			p.add(new JLabel(controller.getSign().getName()), gbc);
		else if (signGroupMessage())
			p.add(dms_list, gbc);
		
		/* Page # Label */
		gbc.gridx = 4;
		gbc.gridy = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		p.add(pg_num_lbl, gbc);
		
		/* Page List Label */
		gbc.gridx = 0;
		gbc.gridy = 2;
		p.add(new JLabel(I18N.get("wysiwyg.editor.page_list")), gbc);

		/* Page List */
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridheight = 1;
		gbc.gridwidth = 4;
		gbc.weightx = 0.5;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		
		page_list_pn = new JScrollPane(page_list,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		page_list_pn.setPreferredSize(new Dimension(450, 400));
		p.add(page_list_pn, gbc);
		
		/* Page Buttons */
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridwidth = 4;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		page_btn_pnl = new JPanel();
		page_btn_pnl.setLayout(new FlowLayout(FlowLayout.CENTER));
		page_btn_pnl.add(page_add_btn);
		page_btn_pnl.add(page_del_btn);
		page_btn_pnl.add(Box.createHorizontalStrut(10));
		page_btn_pnl.add(page_mv_up_btn);
		page_btn_pnl.add(page_mv_down_btn);
		p.add(page_btn_pnl, gbc);
		
		/* Editor (its own panel with multiple tabs) */
		gbc.gridx = 4;
		gbc.gridy = 2;
		gbc.gridheight = 3;
		gbc.gridwidth = 6;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.BOTH;
		p.add(epanel, gbc);
		
		/* Preview Button */
		gbc.gridx = 6;
		gbc.gridy = 5;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		p.add(preview_btn, gbc);
		
		/* Cancel Button */
		gbc.gridx = 7;
		gbc.gridy = 5;
		p.add(cancel_btn, gbc);
		
		/* Save As Button */
		gbc.gridx = 8;
		gbc.gridy = 5;
		p.add(save_as_btn, gbc);
		
		/* Save Button */
		gbc.gridx = 9;
		gbc.gridy = 5;
		p.add(save_btn, gbc);
		
		add(p);
		
		// finish initializing the controller now that everything is in place
		controller.postInit();
	}
	
	/** Get the WController for this form */
	public WController getController() {
		return controller;
	}

	/** Get the pixel panel from the WYSIWYG editor panel */
	public WImagePanel getWImagePanel() {
		return epanel.getWImagePanel();
	}
	
	/** Set the frame (which should be this form's frame. */
	public void setFrame(JInternalFrame f) {
		frame = f;
		frame.setJMenuBar(menu_bar);
	}
	
	public void setPageNumberLabel(String pnl) {
		pg_num_lbl.setText(pnl);
	}
	
	/** Update the main WYSIWYG panel with any changes from the editor form
	 *  TODO do we want to do this here, and like this?
	 */
	public void updateWysiwygPanel() {
		// use the currently selected page to update the main WYSIWYG panel
		WPage selectedPage = controller.getSelectedPage();
		if (selectedPage != null)
			epanel.updateWysiwygSignPage(selectedPage);
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
			// close the frame without doing anything else
			close(session.getDesktop());
		}
	};
	
	/** Save As action */
	private final IAction saveas = new IAction("wysiwyg.editor.save_as") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			System.out.println("Saving As...");
			
			// open a text input form for the user to input the name of a new
			// message
			controller.saveMessageAs.actionPerformed(e);
		}
	};
	
	/** Save action */
	private final IAction save = new IAction("wysiwyg.editor.save") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			controller.saveMessage.actionPerformed(e);
		}
	};
	
	/** Returns true if editing a message for a single sign, false otherwise
	 * (i.e. a group or TODO config). */
	private boolean singleSignMessage() {
		return singleSign;
	}
	
	/** Returns true if editing a sign group message */
	private boolean signGroupMessage() {
		return !singleSign;
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
	