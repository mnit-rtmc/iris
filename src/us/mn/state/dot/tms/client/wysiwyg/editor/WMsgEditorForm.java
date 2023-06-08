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
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.MsgPattern;
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

	/* Sign and pattern being edited */
	private DMS sign;
	private MsgPattern pattern;

	/* Controller - for handling back and forth between the GUI and renderer */
	private WController controller;

	/* Currently selected page (defaults to first available) */
	private JLabel pg_num_lbl;

	/* Menu Bar */
	private WMsgEditorMenuBar menu_bar;

	/* Standby Check Box */ 
	private JCheckBox standby_chk;

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
		controller = new WController(this, sign);
		initForm();
	}

	public WMsgEditorForm(Session s, MsgPattern pat, DMS d) {
		super(getWindowTitle(pat), true);
		session = s;
		sign = d;
		pattern = pat;
		controller = new WController(this, pattern, sign);
		initForm();
	}

	/** Get the current client session */
	public Session getSession() {
		return session;
	}

	public void setWindowTitle(MsgPattern pat) {
		title = getWindowTitle(pat);
		frame.setTitle(title);
	}

	public static String getWindowTitle(MsgPattern pat) {
		String editorName = I18N.get("wysiwyg.editor.title");
		String msgName = pat != null ? pat.getName() : "<Untitled>";
		return String.format("%s - %s", editorName, msgName);
	}

	protected void initForm() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		setPreferredSize(new Dimension(1150,600));

		/* Menu Bar */
		menu_bar = new WMsgEditorMenuBar();
		
		/* Standby Check Boxes */ 
		standby_chk = new JCheckBox(controller.toggleStandbyMsg);

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

		/* Sign Label and sign name label or MultiConfig drop-down */
		gbc.gridx = 0;
		gbc.gridy = 0;
		JPanel lPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		lPanel.add(new JLabel(getSignLabel()), gbc);

		/* Sign Name Label */
		lPanel.add(new JLabel(controller.getSign().getName()), gbc);
		gbPanel.add(lPanel, gbc);

		/* Error label (only appears when a bad MultiConfig is used) */
		gbc.gridx = 1;
		gbc.gridy = 0;
		multiConfigError = new JLabel();
		multiConfigError.setForeground(Color.RED);
		gbPanel.add(multiConfigError, gbc);

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
		
		/* Beacon */
		/* Standby CheckBox */
		gbc.gridx = 1;
		gbc.gridy = 3;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		JPanel pPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		gbPanel.add(pPanel, gbc);
		pPanel.add(new ILabel("wysiwyg.editor.standby"));
		pPanel.add(standby_chk);
		gbPanel.add(pPanel, gbc);
		standby_chk.setSelected(controller.isStandby());
		
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

	/** Returns the string that should be used for the label next to the sign
	 * name drop-down. */
	private String getSignLabel() {
		// just have it say "Sign:"
		return I18N.get("wysiwyg.sign") + ":";
	}

	/** Return the state of the standby message box. */
	public boolean getStandby() {
		return standby_chk.isSelected();
	}

	public String getMultiPanelContents() {
		return epanel.getMultiPanelContents();
	}

	public WMsgEditorPanel getEditorPanel() {
		return epanel;
	}
}
