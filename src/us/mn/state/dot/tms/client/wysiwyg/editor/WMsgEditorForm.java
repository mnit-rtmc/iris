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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.Icons;
import us.mn.state.dot.tms.client.widget.Widgets;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiConfig;
import us.mn.state.dot.tms.utils.wysiwyg.WEditorErrorManager;
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
	
	/* Prefix Check Boxes */ 
	private JCheckBox prefix_chk;

	/* Sign drop-down (only present for groups) */
//	private JComboBox<String> dms_list;
	private WMultiConfigComboBox multiConfigList;
	
	/* Page List */
	private JPanel page_btn_pnl;
	private JButton page_add_btn;
	private JButton page_del_btn;
	private JButton page_mv_up_btn;
	private JButton page_mv_down_btn;
	private WPageList page_list;
	private JScrollPane page_list_pn;
	
	/** Error label to display when MultiConfig generation fails */
	private JLabel multiConfigError;
	
	/** Main Editor Panel */
	private WMsgEditorPanel epanel;
	
	/** Buttons */
	private JButton cancel_btn;
	private JButton save_as_btn;
	private JButton save_btn;

	/** Key Bindings */
	private WEditorKeyBindings editorKeyBindings;
	
	public WMsgEditorForm(Session s) {
		super(getWindowTitle(null), true);
		session = s;
		controller = new WController(this);
		initForm();
	}
	
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
		title = getWindowTitle(q);
		frame.setTitle(title);
	}
	
	public static String getWindowTitle(QuickMessage q) {
		String editorName = I18N.get("wysiwyg.editor.title");
		String msgName = q != null ? q.getName() : "<Untitled>";
		return String.format("%s - %s", editorName, msgName);
	}
	
	protected void initForm() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		setPreferredSize(new Dimension(1150,600));
		
		/* Menu Bar */
		menu_bar = new WMsgEditorMenuBar();
		
		/* Prefix Check Boxes */ 
		prefix_chk = new JCheckBox();
		
		/* Sign group drop-down - only present if editing for sign group */
		if (signGroupMessage()) {
			multiConfigList = controller.getConfigComboBox();
		}
		
		/* Page number label (default to 1) */
		pg_num_lbl = new JLabel(String.format(I18N.get(
				"wysiwyg.editor.page_number"), 1));
		
		/* Page List */
		page_list = controller.getPageList();
		
		/* Page buttons */
		page_add_btn = new JButton(controller.pageAdd);
		ImageIcon pg_add_icon = Icons.getIconByPropName(
				"wysiwyg.editor.page_add");
		page_add_btn.setIcon(pg_add_icon);
		page_add_btn.setHideActionText(true);
		page_add_btn.setMargin(new Insets(0,0,0,0));
		
		page_del_btn = new JButton(controller.pageDelete);
		ImageIcon pg_del_icon = Icons.getIconByPropName(
				"wysiwyg.editor.page_delete");
		page_del_btn.setIcon(pg_del_icon);
		page_del_btn.setHideActionText(true);
		page_del_btn.setMargin(new Insets(0,0,0,0));
		
		page_mv_up_btn = new JButton(controller.pageMoveUp);
		ImageIcon pg_mv_up_icon = Icons.getIconByPropName(
				"wysiwyg.editor.page_move_up");
		page_mv_up_btn.setIcon(pg_mv_up_icon);
		page_mv_up_btn.setHideActionText(true);
		page_mv_up_btn.setMargin(new Insets(0,0,0,0));
		
		page_mv_down_btn = new JButton(controller.pageMoveDown);
		ImageIcon pg_mv_down_icon = Icons.getIconByPropName(
				"wysiwyg.editor.page_move_down");
		page_mv_down_btn.setIcon(pg_mv_down_icon);
		page_mv_down_btn.setHideActionText(true);
		page_mv_down_btn.setMargin(new Insets(0,0,0,0));
		
		/* Main Editor Panel */
		epanel = new WMsgEditorPanel(controller);
		
		/* Buttons */
		cancel_btn = new JButton(cancel);
		save_as_btn = new JButton(saveas);
		save_btn = new JButton(save);
	}

	/** Initialize the form */
	@Override
	protected void initialize() {

		// initialize layout
		GridBagLayout gbl = new GridBagLayout();
		JPanel gbPanel = new JPanel(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.insets = Widgets.UI.insets();
		gbc.ipadx = 10;
		gbc.ipady = 0;
		gbc.weightx = 0;
		gbc.weighty = 0;
		
		/* Sign/Group Label and sign name label or MultiConfig drop-down */
		gbc.gridx = 0;
		gbc.gridy = 0;
		JPanel lPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		lPanel.add(new JLabel(getSignLabel()), gbc);
		
		/* Sign Name Label or Group Drop-Down */
		if (singleSignMessage())
			lPanel.add(new JLabel(controller.getSign().getName()), gbc);
		else if (signGroupMessage() && multiConfigList != null)
			lPanel.add(multiConfigList, gbc);
		gbPanel.add(lPanel, gbc);
		
		/* Error label (only appears when a bad MultiConfig is used) */
		gbc.gridx = 1;
		gbc.gridy = 0;
		multiConfigError = new JLabel();
		multiConfigError.setForeground(Color.RED);
		gbPanel.add(multiConfigError, gbc);
		if (signGroupMessage() && multiConfigList == null
				|| !controller.multiConfigUseable()) {
			multiConfigError.setText(I18N.get("wysiwyg.editor.bad_config"));
		}
		
		/* Page # Label */
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbPanel.add(pg_num_lbl, gbc);
		
		/* Page List Label */
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbPanel.add(new JLabel(I18N.get("wysiwyg.editor.page_list")), gbc);

		/* Page List */
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		
		page_list_pn = new JScrollPane(page_list,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		Dimension d = page_list_pn.getPreferredSize();
		d.width = 450;
		page_list_pn.setPreferredSize(d);
		page_list_pn.setMinimumSize(d);
		gbPanel.add(page_list_pn, gbc);
		
		/* Page Buttons */
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.CENTER;
		page_btn_pnl = new JPanel();
		page_btn_pnl.setLayout(new FlowLayout(FlowLayout.CENTER));
		page_btn_pnl.add(page_add_btn);
		page_btn_pnl.add(page_del_btn);
		page_btn_pnl.add(Box.createHorizontalStrut(10));
		page_btn_pnl.add(page_mv_up_btn);
		page_btn_pnl.add(page_mv_down_btn);
		gbPanel.add(page_btn_pnl, gbc);
		
		/* Editor (its own panel with multiple tabs) */
		gbc.gridx = 1;
		gbc.gridy = 2;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		gbPanel.add(epanel, gbc);
		
		/* Beacon and prefix CheckBoxes */
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		JPanel pPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pPanel.add(new ILabel("wysiwyg.editor.prefix"));
		pPanel.add(prefix_chk);
		gbPanel.add(pPanel, gbc);
		prefix_chk.setSelected(controller.getPrefixPage());
		
		/* Cancel/Save As/Save Buttons */
		JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.gridx = 1;
		gbc.gridy = 4;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		btnPanel.add(cancel_btn);
		btnPanel.add(save_as_btn);
		btnPanel.add(save_btn);
		gbPanel.add(btnPanel, gbc);
		
		add(gbPanel);
		
		// finish initializing the controller now that everything is in place
		controller.postInit();
		
		// also set up key bindings
		setupKeyBindings();
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
		
		// add a frame listener to prompt a confirmation dialog when there are
		// unsaved changes
		frame.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosing(InternalFrameEvent e) {
				// call the cancel action to take care of this
				cancel.actionPerformed(null);
			}
		});
		
		// disable the default close operation so we can handle it
		frame.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
		
		// what the hell, call postInit again
		controller.postInit();
	}
	
	/** Setup key bindings for various components in the form. */
	public void setupKeyBindings() {
		// initialize key bindings
		editorKeyBindings = new WEditorKeyBindings(controller);
		editorKeyBindings.setEditorPanelKeyBindings(getWImagePanel(),
				JComponent.WHEN_FOCUSED);
		editorKeyBindings.setPageListKeyBindings(page_list, WHEN_FOCUSED);
	}
	
	public void setPageNumberLabel(String pnl) {
		pg_num_lbl.setText(pnl);
	}

	/** Enable or disable functions that correspond to any tags not supported
	 *  by the MultiConfig provided.
	 */
	public void setActiveMultiConfig(MultiConfig mc) {
		epanel.setActiveMultiConfig(mc);
		
		// show the error label if there is a problem (hide if not)
		if (mc != null && mc.isUseable())
			multiConfigError.setText("");
		else
			multiConfigError.setText(I18N.get("wysiwyg.editor.bad_config"));
	}
	
	/** Set the current page displayed on the WYSIWYG panel. */
	public void setPage(WPage sp) {
		epanel.setPage(sp);
	}
	
	/** Add the dynamic error panel for displaying current MULTI/renderer
	 *  errors. This tab only appears when there are errors.
	 */
	public void addErrorPanel(WEditorErrorManager errMan) {
		// dispatch to the EditorPanel
		epanel.addErrorPanel(errMan);
	}

	/** Return whether or not the error panel is currently showing. */
	public boolean hasErrorPanel() {
		return epanel.hasErrorPanel();
	}

	/** Update the error panel with the current MULTI/renderer errors from the
	 *  error manager. The error panel must have been initialized first.
	 */
	public void updateErrorPanel() {
		epanel.updateErrorPanel();
	}
	
	/** Remove the dynamic error panel. Performed when errors are addressed
	 *  by the user.
	 */
	public void removeErrorPanel() {
		epanel.removeErrorPanel();
	}
	
	/** Update the text toolbar */
	public void updateTextToolbar() {
		epanel.updateTextToolbar();
	}
	
	public void updateTagEditButton(boolean state) {
		epanel.updateTagEditButton(state);
	}
	
	/** Update the non-text tag button state to */
	public void updateNonTextTagButton(boolean state) {
		epanel.updateNonTextTagButton(state);
	}
	
	/** Update the non-text tag info label, optionally including a color icon
	 *  (if c is null, no color is shown).
	 */
	public void updateNonTextTagInfo(String s, Color c) {
		epanel.updateNonTextTagInfo(s, c);
	}
	
	public void close() {
		if (frame != null) {
			frame.dispose();
		}
	}
	
	/** Cancel (close) action */
	private WMsgEditorForm eForm = this;
	private final IAction cancel = new IAction("wysiwyg.editor.cancel") {
		protected void doActionPerformed(ActionEvent e)
		{
			// check if the current message is saved
			if (!controller.isMessageSaved()) {
				// if it's not, show a confirmation dialog
				session.getDesktop().show(
						new WConfirmExitForm(session, eForm));
			} else
				// if it is, close the frame without doing anything else
				close();
		}
	};
	
	/** Save As action */
	private final IAction saveas = new IAction("wysiwyg.editor.save_as") {
		protected void doActionPerformed(ActionEvent e)
		{
			System.out.println("Saving As...");
			
			// open a text input form for the user to input the name of a new
			// message
			controller.saveMessageAs.actionPerformed(e);
		}
	};
	
	/** Save action */
	public final IAction save = new IAction("wysiwyg.editor.save") {
		protected void doActionPerformed(ActionEvent e)
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
			return I18N.get("wysiwyg.sign_group") + ": "
				+ sg.getName() + "    ";
		return "";
	}
	
	/** Return the state of the prefix page box. */
	public boolean getPrefixPage() {
		return prefix_chk.isSelected();
	}

	public String getMultiPanelContents() {
		return epanel.getMultiPanelContents();
	}
	
	public WMsgEditorPanel getEditorPanel() {
		return epanel;
	}
}
	