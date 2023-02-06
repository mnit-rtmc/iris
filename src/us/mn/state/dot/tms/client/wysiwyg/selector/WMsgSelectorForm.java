/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  SRF Consulting Group
 * Copyright (C) 2022-2023  Minnesota Department of Transportation
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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.client.EditModeListener;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.client.widget.Widgets;
import us.mn.state.dot.tms.client.wysiwyg.editor.WMsgEditorForm;
import us.mn.state.dot.tms.client.wysiwyg.editor.WMsgNewMsgForm;
import us.mn.state.dot.tms.client.wysiwyg.editor.WPageList;
import us.mn.state.dot.tms.client.wysiwyg.editor.WController;
import us.mn.state.dot.tms.utils.I18N;

/**
 * WYSIWYG DMS Message Editor Selector Form
 *
 * @author Gordon Parikh, Michael Janson, and John L. Stanley - SRF Consulting
 */
@SuppressWarnings("serial")
public class WMsgSelectorForm extends AbstractForm {

	/** Device listing */
	private Map<String,DMS> dmsList = new HashMap<String,DMS>();
	private ArrayList<String> dmsNames;
	private ArrayList<String> signGroupNames;
	private ArrayList<String> messageList = new ArrayList<String>();

	/** Sign listing */
	private JList<String> dms_list;

	/** Scroll pane for device listing */
	private JScrollPane dms_pn;
	private int DMS_TAB = 0;

	/** Message list */
	private JList<String> msg_list;
	private JScrollPane msg_pn;

	/** User session */
	private final Session session;

	/** Edit mode enabled/disabled */
	boolean editMode;

	/** Current selections */
	private DMS selectedDMS;
	private MsgPattern selectedMessage;

	/** Controller for rendering a preview */
	private WController controller;

	/** Use the page list as the message preview */
	private WPageList msg_preview;
	private JScrollPane msg_preview_pn;

	/** Buttons */
	private JButton reload_btn;
	private JButton create_btn;
	private JButton edit_btn;
	private JButton clone_btn;
	private JButton delete_btn;

	/** Button enabled/disabled */
	private boolean createEnabled = false;
	private boolean editEnabled = false;
	private boolean cloneEnabled = false;
	private boolean deleteEnabled = false;

	/** Edit mode listener */
	private final EditModeListener edit_lsnr = new EditModeListener() {
		public void editModeChanged() {
			// check if edit mode is enabled based on whether or not we can write
			// a MsgPattern
			editMode = session.canWrite(MsgPattern.SONAR_TYPE);
			
			// update the button panel to reflect the change
			updateButtonPanel();
		}
	};

	/** Status bar panel */
	private JLabel status_msg;
	private String msgInfo;

	/** Default constructor - no selections
	 * (from {@code View -> Message Signs -> WYSIWYG Message Editor} menu)
	 */
	public WMsgSelectorForm(Session s) {
		super(I18N.get("wysiwyg.selector.title"), true);
		session = s;
		initForm();
	}

	/** Preselected sign (from DMS context menu) */
	public WMsgSelectorForm(Session s, DMS sign) {
		super(I18N.get("wysiwyg.selector.title"), true);
		session = s;
		initForm();
		selectedDMS = sign;

		// preselect the given DMS and scroll to it
		selectDMS(selectedDMS.getName());
	}

	/** Preselected message */
	public WMsgSelectorForm(Session s, MsgPattern pat) {
		super(I18N.get("wysiwyg.selector.title"), true);
		session = s;
		initForm();
		selectedMessage = pat;

		selectMsgPattern(pat.getName());
	}

	/** Select the DMS with the given name in the scroll pane list */
	private void selectDMS(String name) {
		int indx = dmsNames.indexOf(name);
		dms_list.setSelectedIndex(indx);
		dms_list.ensureIndexIsVisible(indx);

		// enable the create button
		enableCreateButton();
		updateUI();
	}

	/** Select the MsgPattern with the given name in the scroll pane list */
	private void selectMsgPattern(String name) {
		int indx = messageList.indexOf(name);
		msg_list.setSelectedIndex(indx);
		msg_list.ensureIndexIsVisible(indx);

		// enable the edit/clone/delete buttons
		enableEditButtons();
		updateUI();
	}

	/** Get the selectedDMS in the form */
	public DMS getSelectedDMS() {
		return selectedDMS;
	}

	/** Set the selectedDMS in the form */
	public void setSelectedDMS(DMS dms) {
		selectedDMS = dms;
		if (dms != null) {
			enableCreateButton();
			disableEditButtons();
			setSelectedMessage(null);
			msg_list.clearSelection();
			controller.setSign(selectedDMS);
			controller.setMsgPattern(null);
			if (controller.multiConfigUseable())
				msg_preview.updatePageList("", controller.getMultiConfig());
			else
				msg_preview.clearPageList();
		} else {
			disableButtons();
			updateMessageList();
			controller.setSign(null);
			controller.setMsgPattern(null);
			msg_preview.clearPageList();
		}
	}

	/** Get the selectedMessage in the form */
	public MsgPattern getSelectedMessage() {
		return selectedMessage;
	}

	/** Set the selectedMessage in the form */
	public void setSelectedMessage(MsgPattern pat) {
		selectedMessage = pat;
		if (selectedMessage != null) {
			// enable edit buttons if not null
			enableEditButtons();

			// build the status message
			String msgName = selectedMessage.getName();
			int msgLen = selectedMessage.getMulti().length();
			msgInfo = String.format(
				"Msg: \"%s\", Length: %d",
				msgName, msgLen
			);

			// update the controller
			controller.setMsgPattern(selectedMessage);

			// update the page list
			if (controller.getMultiConfig() != null) {
				msg_preview.updatePageList(selectedMessage.getMulti(),
						controller.getMultiConfig());
			} else
				msg_preview.clearPageList();
		} else {
			// we got a null - disable edit buttons
			disableEditButtons();

			// reset the status message
			msgInfo = "";

			// rest the controller
			controller.setMsgPattern(null);

			// update the page list
			if (controller.getMultiConfig() != null)
				msg_preview.updatePageList("", controller.getMultiConfig());
		}
		status_msg.setText(msgInfo);
	}

	/** A class for disabling selection in a list */
	class DisabledSelectionModel extends DefaultListSelectionModel {
		@Override
		public void setSelectionInterval(int index0, int index1) {
			super.setSelectionInterval(-1, -1);
	 	}

		@Override
		public void addSelectionInterval(int index0, int index1) {
			super.setSelectionInterval(-1, -1);
		}
	}

	protected void initForm() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		/** Create sign/sign group lists */
		updateDeviceLists();

		/* Initialize a controller for rendering a preview */
		controller = new WController();
		msg_preview = controller.getPagePreviewList();
		msg_preview_pn = createScrollPane(msg_preview);
		msg_preview_pn.setPreferredSize(new Dimension(400,200));

		/** Sign list */
		ListModel<String> dmsNamesModel = new AbstractListModel<String>() {
			public int getSize() { return dmsNames.size(); }

			public String getElementAt(int index) {
				return dmsNames.get(index);
			}
		};

		dms_list = new JList<String>(dmsNamesModel);

		// force single select only
		dms_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// set up a handler for when signs are selected
		WMsgSelectorForm sForm = this;
		class SignListSelectionHandler implements ListSelectionListener {
			public void valueChanged(ListSelectionEvent e) {
				// don't do anything if the selection is still adjusting
				if (!e.getValueIsAdjusting()) {
					// get the list selection model and the selected index
					ListSelectionModel lsm =
							(ListSelectionModel) e.getSource();
					int indx = lsm.getMinSelectionIndex();

					if (indx != -1) {
						// get the selected sign
						String dmsName = dmsNames.get(indx);

						/* start a background job to check if the sign exists
						 * and retrieve a list of messages
						 */
						WMsgSelectorSignProcess proc =
								new WMsgSelectorSignProcess(
								session, dmsName, sForm);
						proc.execute();
					} else {
						// deselection - disable the buttons and reset the
						// message list
						setSelectedDMS(null);
					}
				}
			}
		}

		dms_list.getSelectionModel().addListSelectionListener(
				new SignListSelectionHandler());

		dms_pn = createScrollPane(dms_list);
		dms_pn.setPreferredSize(new Dimension(250,200));

		/* Set the focus on the Sign list */
		dms_list.requestFocusInWindow();
		
		/* Panes and model for message list */
		ListModel<String> messageListModel = new AbstractListModel<String>() {
			public int getSize() { return messageList.size(); }

			public String getElementAt(int index) {
				return messageList.get(index);
			}
		};
		msg_list = new JList<String>(messageListModel);
		msg_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		class MessageListSelectionHandler implements ListSelectionListener {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					ListSelectionModel lsm = 
							(ListSelectionModel) e.getSource();
					int indx = lsm.getMinSelectionIndex();
					
					if (indx != -1) {
						String messageName = messageList.get(indx);
						WMsgSelectorMessageProcess proc =
								new WMsgSelectorMessageProcess(
								session, messageName, sForm);
						proc.execute();
					} else {
						// reset the selected message
						setSelectedMessage(null);
					}
				}
			}
		}

		msg_list.getSelectionModel().addListSelectionListener(
			new MessageListSelectionHandler());
		msg_pn = createScrollPane(msg_list);
		msg_pn.setPreferredSize(new Dimension(280,200));

		/* Setup buttons */
		reload_btn = new JButton(reload);
		create_btn = new JButton(create);
		edit_btn = new JButton(edit);
		clone_btn = new JButton(clone);
		delete_btn = new JButton(deleteConfirm);

		/* By default, all buttons besides reload are disabled */
		disableButtons();

		/** Initialize the message list */
		updateMessageList();

		/** Add an edit mode listener for the buttons in the form */
		session.addEditModeListener(edit_lsnr);

		/** Check edit mode right now */
		editMode = session.canWrite(MsgPattern.SONAR_TYPE);

		/** Add a panel for the status bar */
		msgInfo = "";
		status_msg = new JLabel(msgInfo);
		status_msg.setPreferredSize(new Dimension(450,20));

		/** Setup key bindings and mouse listener */
		setupKeyBindings();

		/** Mouse click listener */
		MouseAdapter mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent event) {
				if (event.getButton() == MouseEvent.BUTTON1 &&
						event.getClickCount() == 2)
					// double-clicked left button - edit selected message
					editSelectedMessage();
			}
		};
		msg_list.addMouseListener(mouseListener);
	}

	/** Setup key bindings */
	protected void setupKeyBindings() {
		/** Global Key Bindings */
		/** Window Input/Action Maps */
		InputMap wiMap = getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap waMap = getActionMap();

		/* Esc - Close selector */
		wiMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
		waMap.put("cancel", cancel);

		/* Tab - Toggle focus between sign/group list and message list */
		dms_list.setFocusTraversalKeysEnabled(false);
		msg_list.setFocusTraversalKeysEnabled(false);
		wiMap.put(KeyStroke.getKeyStroke(
				KeyEvent.VK_TAB, 0), "swapListFocus");
		waMap.put("swapListFocus", swapListFocus);

		/* F3 - Reload */
		wiMap.put(KeyStroke.getKeyStroke("F3"), "reload");
		waMap.put("reload", reload);

		/* F4 - Create */
		wiMap.put(KeyStroke.getKeyStroke("F4"), "create");
		waMap.put("create", create);

		/** Message-List Key Bindings */
		InputMap miMap = msg_list.getInputMap(JComponent.WHEN_FOCUSED);
		ActionMap maMap = msg_list.getActionMap();

		/* Enter - Edit Message */
		miMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "edit");
		maMap.put("edit", edit);

		/* F5 - Reserved (?) */

		/* F6 - Edit Message */
		miMap.put(KeyStroke.getKeyStroke("F6"), "edit");

		/* F7 - Clone Message */
		miMap.put(KeyStroke.getKeyStroke("F7"), "clone");
		maMap.put("clone", clone);

		/* Delete - Delete message with confirmation */
		miMap.put(KeyStroke.getKeyStroke(
				KeyEvent.VK_DELETE, 0), "deleteConfirm");
		maMap.put("deleteConfirm", deleteConfirm);

		/* Shift Delete - Delete message with NO confirmation */
		miMap.put(KeyStroke.getKeyStroke(
				KeyEvent.VK_DELETE, KeyEvent.SHIFT_DOWN_MASK),
				"deleteNoConfirm");
		maMap.put("deleteNoConfirm", deleteNoConfirm);
	}

	/** Close Form Action */
	private final IAction cancel = new IAction(
		"wysiwyg.selector.cancel")
	{
		protected void doActionPerformed(ActionEvent e)
			throws Exception
		{
			close(session.getDesktop());
		}
	};

	/** Action to swap focus between sign list and message list */
	private final Action swapListFocus = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// if the DMS list has focus, change focus to the message list
			if (dms_list.isFocusOwner())
				msg_list.requestFocusInWindow();
		}
	};

	/** Dispose of the panel */
	public void dispose() {
		session.removeEditModeListener(edit_lsnr);
		removeAll();
	}

	/** Disable all buttons besides reload */
	protected void disableButtons() {
		// save the button states for when edit mode is changed
		createEnabled = false;
		editEnabled = false;
		cloneEnabled = false;
		deleteEnabled = false;

		// update the button panel to disable the buttons
		updateButtonPanel();
	}

	protected void disableEditButtons() {
		editEnabled = false;
		cloneEnabled = false;
		deleteEnabled = false;
		updateButtonPanel();
	}

	protected void enableCreateButton() {
		createEnabled = true;
		updateButtonPanel();
	}

	protected void enableEditButtons() {
		editEnabled = true;
		cloneEnabled = true;
		deleteEnabled = true;
		updateButtonPanel();
	}

	/** Enable buttons based on selections and edit mode */
	protected void updateButtonPanel() {
		// set the buttons based on edit mode and the respective enabled state
		create_btn.setEnabled(createEnabled && editMode);
		edit_btn.setEnabled(editEnabled && editMode);
		clone_btn.setEnabled(cloneEnabled && editMode);
		delete_btn.setEnabled(deleteEnabled && editMode);
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

		/* Sign Selector */
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;

		p.add(dms_pn, gbc);

		/* Message Selector Label */
		gbc.gridx = 3;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.weighty = 0;
		p.add(new JLabel(I18N.get("wysiwyg.message")), gbc);

		/* Message List */
		gbc.gridx = 3;
		gbc.gridy = 1;
		gbc.gridwidth = 6;
		gbc.weightx = 0.5;
		gbc.weighty = 0.5;
		p.add(msg_pn, gbc);

		/* Reload button */
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		p.add(reload_btn, gbc);

		/* Create button */
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 5;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.BASELINE_TRAILING;
		p.add(create_btn, gbc);

		/* Edit button */
		gbc.gridx = 6;
		gbc.gridy = 2;
		p.add(edit_btn, gbc);

		/* Clone button */
		gbc.gridx = 7;
		gbc.gridy = 2;
		p.add(clone_btn, gbc);

		/* Delete button */
		gbc.gridx = 8;
		gbc.gridy = 2;
		p.add(delete_btn, gbc);

		/* Status bar panel */
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 9;
		gbc.weightx = 1;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		p.add(status_msg, gbc);

		/* Preview Pane Label */
		gbc.gridx = 9;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		p.add(new JLabel(I18N.get("wysiwyg.selector.preview")), gbc);

		/* Preview Pane */
		gbc.gridx = 9;
		gbc.gridy = 1;
		gbc.gridheight = 1;
		gbc.weightx = 1;
		gbc.weighty = 1;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		p.add(msg_preview_pn, gbc);

		add(p);
	}

	/** Create a scroll pane */
	@SuppressWarnings("rawtypes")
	private JScrollPane createScrollPane(JList l) {
		return new JScrollPane(l,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	}

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		// we'll let anyone use the selector, I guess...
		return true;
	}

	/** Initialize the list of signs */
	private void updateDeviceLists() {
		// clear the lists
		dmsList.clear();

		// get a list of all DMS
		Iterator<DMS> dit = DMSHelper.iterator();
		while (dit.hasNext()) {
			DMS dms = dit.next();
			dmsList.put(dms.getName(), dms);
		}

		// sort alphabetically (case insensitive)
		dmsNames = new ArrayList<String>(dmsList.keySet());
		dmsNames.sort(String::compareToIgnoreCase);
	}

	/** Reset the list of messages */
	public void updateMessageList() {
		messageList.clear();
		messageList.add(I18N.get("wysiwyg.selector.select_sign"));
		msg_list.updateUI();
	}

	/** Reset the list of messages */
	public void updateMessageList(boolean error) {
		messageList.clear();
		messageList.add(I18N.get("wysiwyg.selector.error"));
		msg_list.updateUI();
	}

	/** Update the list of messages from a list of MsgPattern objects */
	public void updateMessageList(ArrayList<MsgPattern> patList) {
		// reset the messages list then add the message names
		messageList.clear();
		for (MsgPattern pat : patList) {
			messageList.add(pat.getName());
		}
		messageList.sort(String::compareToIgnoreCase);
		msg_list.updateUI();

		// select a message if there is one selected
		if (selectedMessage != null) {
			// make sure it's in our list and not a remnant
			String mName = selectedMessage.getName();
			if (messageList.contains(mName)) {
				selectMsgPattern(mName);
			} else {
				// if it's not in our list, reset
				selectedMessage = null;
			}
		}
	}

	WMsgSelectorForm sForm = this; 
	/** Reload action */
	private final IAction reload = new IAction("wysiwyg.selector.reload") {
		protected void doActionPerformed(ActionEvent e)
			throws Exception
		{
			reloadForm();
		}
	};

	public final void reloadForm() {
		// refresh the device lists and message list
		updateDeviceLists();
		updateUI();

		// figure out if the selected sign still exists
		String selectedSignName = null;
		if (selectedDMS != null)
			selectedSignName = selectedDMS.getName();

		// if it does, update things accordingly
		if (selectedSignName != null) {
			WMsgSelectorSignProcess proc = new WMsgSelectorSignProcess(
				session, selectedSignName, sForm);
			proc.execute();
		}
	}

	/** Create action */
	private final IAction create = new IAction("wysiwyg.selector.create") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception {
			// open a form to get the name of the new message and create it
			if (editMode) {
				if (selectedDMS != null) {
					session.getDesktop().show(
						new WMsgNewMsgForm(session,
						selectorForm, selectedDMS)
					);
				}
			}
		}
	};

	/** Edit action */
	private final void editSelectedMessage() {
		if (editMode && selectedMessage != null) {
			// check what we have selected and call the appropriate EditMsg
			// method
			if (selectedDMS != null)
				EditMsg(session, selectedMessage, selectedDMS);
		}
	}

	private final IAction edit = new IAction("wysiwyg.selector.edit") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			editSelectedMessage();
		}
	};

	/** Clone action */
	private final IAction clone = new IAction("wysiwyg.selector.clone") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			// open a form to get the name of the new message, then copy the
			// MULTI from this message
			if (editMode && selectedMessage != null) {
				if (selectedDMS != null) {
					session.getDesktop().show(
						new WMsgNewMsgForm(session,
						selectorForm, selectedDMS,
						selectedMessage)
					);
				}
			}
		}
	};

	/** Delete methods/actions */
	public final void deleteSelectedMessage() {
		// delete the selected message if edit mode is enabled
		if (editMode && selectedMessage != null) {
			DeleteMsg(selectedMessage);
			reloadForm();
		}
	}

	WMsgSelectorForm selectorForm = this;
	private final IAction deleteConfirm = new IAction(
			"wysiwyg.selector.delete") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			// show the confirmation dialogue
			String messageName = selectedMessage.getName();
			session.getDesktop().show(
					new WMsgConfirmDeleteForm(
							session, selectorForm, messageName));
		}
	};

	private final Action deleteNoConfirm = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			// call delete with no confirmation
			deleteSelectedMessage();
			reloadForm();
		}
	};	

	/** Methods for creating/editing/cloning/deleting messages */

	/** Create a message for a sign */
	public static void CreateMsg(Session s, DMS sign, String msgName) {
		// create a new message pattern
		TypeCache<MsgPattern> cache = s.getSonarState().
			getDmsCache().getMsgPatterns();
		HashMap<String, Object> attrs = new HashMap<String, Object>();
//		attrs.put("sign_group", sg);
		attrs.put("multi", "");
		cache.createObject(msgName, attrs);
	}

	/** Edit an existing message for a sign */
	public static void EditMsg(Session s, MsgPattern pat, DMS sign) {
		// launch the editor
		SmartDesktop desktop = s.getDesktop();
		WMsgEditorForm editor = new WMsgEditorForm(s, pat, sign);
		JInternalFrame frame = desktop.show(editor);
		editor.setFrame(frame);
	}

	/** Clone a message for a sign - we get the sign from the message
	 * pattern */
	public static void CloneMsg(Session s, MsgPattern pat, String msgName) {
		// create a new message pattern with the same
		// MULTI as the original but with a new name
		TypeCache<MsgPattern> cache = s.getSonarState().
			getDmsCache().getMsgPatterns();
		HashMap<String, Object> attrs = new HashMap<String, Object>();

		// get either the group or config - one has to be not null
		attrs.put("multi", pat.getMulti());
		cache.createObject(msgName, attrs);
	}

	/** Delete a message */
	public static void DeleteMsg(MsgPattern pat) {
		pat.destroy();
	}
}
