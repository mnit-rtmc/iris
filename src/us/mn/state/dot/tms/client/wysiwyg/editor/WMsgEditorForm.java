/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2019  SRF Consulting Group
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

/**
 * WYSIWYG DMS Message Editor Form
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
	
	/* Currently selected page (defaults to first available) */
	private WMsgSignPage selectedPage;
	private JLabel pg_num_lbl;
	
	/* Menu Bar */
	private WMsgEditorMenuBar menu_bar;
	
	/* Beacon and Prefix Check Boxes */ 
	private JCheckBox beacon_chk; 
	private JCheckBox prefix_chk;

	/* Sign group drop-down (not always present) */
	private Map<String,DMS> dmsList = new HashMap<String,DMS>();
	private String[] dmsNames;
	private JComboBox<String> dms_list;
	
	/* Page List */
	private JPanel page_btn_pnl;
	private JButton page_add_btn;
	private JButton page_del_btn;
	private JButton page_mv_up_btn;
	private JButton page_mv_down_btn;
	private JList<WMsgSignPage> page_list;
	
	/* Main Editor Panel */
	private WMsgEditorPanel editor;
	
	/** Buttons */
	private JButton preview_btn;
	private JButton cancel_btn;
	private JButton save_as_btn;
	private JButton save_btn;
	
	public WMsgEditorForm(Session s) {
		// TODO need to add the message name to the title (somehow...)
		super(getWindowTitle(null), true);
		session = s;
		initForm();
	}
	
	// TODO need to figure out how to deal with new QuickMessages (where/when/
	// how is name created???)
	
	public WMsgEditorForm(Session s, DMS d) {
		super(getWindowTitle(null), true);
		session = s;
		sign = d;
		initForm();
	}
	
	public WMsgEditorForm(Session s, SignGroup g) {
		super(getWindowTitle(null), true);
		session = s;
		sg = g;
		initForm();
	}
	
	public WMsgEditorForm(Session s, QuickMessage q, DMS d) {
		super(getWindowTitle(q), true);
		session = s;
		sign = d;
		qm = q;
		initForm();
	}
	
	public WMsgEditorForm(Session s, QuickMessage q, SignGroup g) {
		super(getWindowTitle(q), true);
		session = s;
		sg = g;
		qm = q;
		initForm();
	}
	
	public static String getWindowTitle(QuickMessage q) {
		String editorName = I18N.get("wysiwyg.editor.title");
		String msgName = q != null ? q.getName() : "<Untitled>";
		return String.format("%s - %s", editorName, msgName);
	}
	
	protected void initForm() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// TODO may want to change these dimensions
		setPreferredSize(new Dimension(800,400));
		
		/* Menu Bar */
		menu_bar = new WMsgEditorMenuBar();
		
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
		
		/* Page number label (default to 1) */
		pg_num_lbl = new JLabel(String.format(I18N.get("wysiwyg.editor.page_number"), 1));
		
		/* Page buttons */
		page_add_btn = new JButton(pageAdd);
		ImageIcon pg_add_icon = Icons.getIconByPropName("wysiwyg.editor.page_add");
		page_add_btn.setIcon(pg_add_icon);
		page_add_btn.setHideActionText(true);
		
		page_del_btn = new JButton(pageDelete);
		ImageIcon pg_del_icon = Icons.getIconByPropName("wysiwyg.editor.page_delete");
		page_del_btn.setIcon(pg_del_icon);
		page_del_btn.setHideActionText(true);
		
		page_mv_up_btn = new JButton(pageMoveUp);
		ImageIcon pg_mv_up_icon = Icons.getIconByPropName("wysiwyg.editor.page_move_up");
		page_mv_up_btn.setIcon(pg_mv_up_icon);
		page_mv_up_btn.setHideActionText(true);
		
		page_mv_down_btn = new JButton(pageMoveDown);
		ImageIcon pg_mv_down_icon = Icons.getIconByPropName("wysiwyg.editor.page_move_down");
		page_mv_down_btn.setIcon(pg_mv_down_icon);
		page_mv_down_btn.setHideActionText(true);
		
		/* Main Editor Panel */
		editor = new WMsgEditorPanel(this);
		
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
			p.add(new JLabel(sign.getName()), gbc);
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
		
		/* Page Buttons */
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 4;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		page_btn_pnl = new JPanel();
		page_btn_pnl.setLayout(new FlowLayout(FlowLayout.CENTER));
//		page_btn_pnl.add(Box.createHorizontalStrut(20));
		page_btn_pnl.add(page_add_btn);
		page_btn_pnl.add(page_del_btn);
		page_btn_pnl.add(Box.createHorizontalStrut(20));
		page_btn_pnl.add(page_mv_up_btn);
		page_btn_pnl.add(page_mv_down_btn);
//		page_btn_pnl.add(Box.createHorizontalStrut(20));
		p.add(page_btn_pnl, gbc);
		
		/* Page List */
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridheight = 1;
		gbc.weightx = 0.5;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		
		// TODO this needs to update when the sign changes (for sign groups)
		
		// get the pages for the message
		String ms = qm.getMulti();
		MultiString mso = new MultiString(ms);
		
		updatePageList();
		System.out.println(page_list.getModel().getSize());
		p.add(new JScrollPane(page_list,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), gbc);
		initPageSelector();
		
		/* Editor (will be a GBL itself) */
		gbc.gridx = 4;
		gbc.gridy = 2;
		gbc.gridheight = 3;
		gbc.gridwidth = 6;
		gbc.fill = GridBagConstraints.BOTH;
		
//		SignFacePanel sfp = new SignFacePanel();
//		SignPixelPanel spp = sfp.setSign(sign);
//		RasterBuilder rb = DMSHelper.createRasterBuilder(sign);
////		String ms = qm.getMulti();
////		MultiString mso = new MultiString(ms);
//		
//		RasterGraphic[] rg = null;
//		try {
//			rg = rb.createPixmaps(mso);
//		} catch (IndexOutOfBoundsException e) {
//		} catch (InvalidMsgException e) {
//		}
//		DMSPanelPager dpp = new DMSPanelPager(spp, rg, ms);
//		p.add(sfp, gbc);
		p.add(editor, gbc);
		
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
		
		
		/*** TODO evaluate need for constraints/fill rules like below ***/
		
//		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
//		gbc.fill = GridBagConstraints.NONE;
//		gbc.anchor = GridBagConstraints.BASELINE_LEADING;

		add(p);
	}
	
	/** Set the menu bar of the frame (which should be this form's frame. */
	public void setMenuBar(JInternalFrame frame) {
		frame.setJMenuBar(menu_bar);
	}
	
	/** Initialize the page selection handler. */
	private void initPageSelector() {
		page_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		class PageSelectionHandler implements ListSelectionListener {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					ListSelectionModel lsm = (ListSelectionModel) e.getSource();
					int indx = lsm.getMinSelectionIndex();
					
					if (indx != -1) {
						selectedPage = page_list.getModel().getElementAt(indx);
						System.out.println(String.format("Selected page %s",
								selectedPage.getPageNumberLabel()));
						pg_num_lbl.setText(selectedPage.getPageNumberLabel());
					}
				}
			}
		}
		page_list.getSelectionModel().addListSelectionListener(new PageSelectionHandler());
	}
	
	/** Update the page list from the selected/created message */
	private void updatePageList() {
		DefaultListModel model = new DefaultListModel();
		
		// get the pages for the message
		String ms = qm.getMulti();
		MultiString mso = new MultiString(ms);
		for (int i = 0; i < mso.getNumPages(); i++) {
			WMsgSignPage sp = new WMsgSignPage(sign, mso, i);
			model.addElement(sp);
		}

		// reset the list
		page_list = new JList<WMsgSignPage>(model);
		
		// set the renderer on the list
		ListCellRenderer rndr = new WMsgSignPageListRenderer();
		page_list.setCellRenderer(rndr);
		
		// set the selected page if one isn't selected
		if (selectedPage == null) {
			selectedPage = (WMsgSignPage) model.get(0);
			pg_num_lbl.setText(selectedPage.getPageNumberLabel());
		}
	}

	/** Page Add action */
	private final IAction pageAdd = new IAction("wysiwyg.editor.page_add") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
			throws Exception
		{
			// TODO need to keep track of what page we're on somewhere
			System.out.println("Adding page...");
		}
	};
	
	/** Page Delete action */
	private final IAction pageDelete = new IAction("wysiwyg.editor.page_delete") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			// TODO need to keep track of what page we're on somewhere
			System.out.println("Deleting page...");
		}
	};
	
	/** Page Move Up action */
	private final IAction pageMoveUp = new IAction("wysiwyg.editor.page_move_up") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			// TODO need to keep track of what page we're on somewhere
			System.out.println("Moving page up...");
		}
	};
	
	/** Page Move Down action */
	private final IAction pageMoveDown = new IAction("wysiwyg.editor.page_move_down") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			// TODO need to keep track of what page we're on somewhere
			System.out.println("Moving page down...");
		}
	};
	
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
	