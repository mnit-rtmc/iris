/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.sched.ListSelectionJob;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.PixelMapBuilder;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.SystemAttributeHelper;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.TmsForm;
import us.mn.state.dot.tms.client.toast.ZTable;
import us.mn.state.dot.tms.utils.I18NMessages;

/**
 * MessagesTab is a GUI tab for displaying and editing sign messages on a
 * DMS properties form.
 *
 * @author Douglas Lau
 */
public class MessagesTab extends JPanel {

	/** Create a SONAR sign text name to check for allowed updates */
	static public String createSignTextName(String name) {
		return SignText.SONAR_TYPE + "/" + name;
	}

	/** Sign group table model */
	protected final SignGroupTableModel sign_group_model;

	/** Sign group table component */
	protected final ZTable group_table = new ZTable();

	/** Button to delete a sign group */
	protected final JButton delete_group = new JButton("Delete Group");

	/** Sign text table model */
	protected SignTextTableModel sign_text_model;

	/** Sign text table component */
	protected final ZTable sign_text_table = new ZTable();

	/** Button to delete sign text message */
	protected final JButton delete_text = new JButton("Delete Message");

	/** Sign pixel panel */
	protected final SignPixelPanel pixel_panel = new SignPixelPanel(true);

	/** AWS allowed component */
	protected final JCheckBox awsAllowed = new JCheckBox(
		I18NMessages.get("dms.aws") + " allowed");

	/** AWS controlled component */
	protected final JCheckBox awsControlled = new JCheckBox(
		I18NMessages.get("dms.aws") + " controlled");

	/** Sonar state */
	protected final SonarState state;

	/** DMS proxy */
	protected final DMS proxy;

	/** SONAR user */
	protected final User user;

	/** Create a new messages tab.
	 * @param tc TmsConnection
	 * @param sign DMS proxy object */
	public MessagesTab(TmsConnection tc, DMS sign) {
		super(new GridBagLayout());
		state = tc.getSonarState();
		proxy = sign;
		user = state.lookupUser(tc.getUser().getName());
		sign_group_model = new SignGroupTableModel(sign,
			state.getDmsSignGroups(), state.getSignGroups(), user);
		initGroupTable();
		initSignTextTable();
		createActions();
		initWidgets();
	}

	/** Dispose of the form */
	public void dispose() {
		sign_group_model.dispose();
		if(sign_text_model != null)
			sign_text_model.dispose();
	}

	/** Initialize the widgets on the tab */
	protected void initWidgets() {
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.top = TmsForm.VGAP;
		bag.insets.left = TmsForm.HGAP;
		bag.insets.right = TmsForm.HGAP;
		bag.insets.bottom = TmsForm.VGAP;
		bag.fill = GridBagConstraints.BOTH;
		bag.gridx = 0;
		bag.gridy = 0;
		bag.weightx = 1;
		bag.weighty = 1;
		JScrollPane scroll = new JScrollPane(group_table);
		add(scroll, bag);
		scroll = new JScrollPane(sign_text_table);
		bag.gridx = 1;
		bag.gridy = 0;
		add(scroll, bag);
		bag.fill = GridBagConstraints.NONE;
		bag.gridx = 0;
		bag.gridy = 1;
		bag.weightx = 0;
		bag.weighty = 0;
		delete_group.setEnabled(false);
		add(delete_group, bag);
		bag.gridx = 1;
		delete_text.setEnabled(false);
		add(delete_text, bag);
		bag.gridx = 0;
		bag.gridy = 2;
		bag.gridwidth = 2;
		bag.fill = GridBagConstraints.BOTH;
		bag.weightx = 0.1f;
		bag.weighty = 0.1f;
		add(createPreviewPanel(), bag);
		if(SystemAttributeHelper.isAwsEnabled()) {
			bag.gridy = 3;
			bag.gridwidth = 1;
			bag.fill = GridBagConstraints.NONE;
			bag.weightx = 0;
			bag.weighty = 0;
			add(awsAllowed, bag);
			bag.gridx = 1;
			add(awsControlled, bag);
		}
	}

	/** Create a message preview panel */
	protected JPanel createPreviewPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(
			"Message Preview"));
		panel.add(pixel_panel, BorderLayout.CENTER);
		return panel;
	}

	/** Create actions for button widgets */
	protected void createActions() {
		new ActionJob(this, delete_group) {
			public void perform() {
				SignGroup group = getSelectedGroup();
				if(group != null)
					group.destroy();
			}
		};
		new ActionJob(this, delete_text) {
			public void perform() throws Exception {
				SignText sign_text = getSelectedSignText();
				if(sign_text != null)
					sign_text.destroy();
			}
		};
		new ActionJob(this, awsAllowed) {
			public void perform() {
				proxy.setAwsAllowed(awsAllowed.isSelected());
			}
		};
		new ActionJob(this, awsControlled) {
			public void perform() {
				proxy.setAwsControlled(
					awsControlled.isSelected());
			}
		};
	}

	/** Initialize the sign group table */
	protected void initGroupTable() {
		final ListSelectionModel s = group_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectGroup();
			}
		};
		group_table.setAutoCreateColumnsFromModel(false);
		group_table.setColumnModel(
			SignGroupTableModel.createColumnModel());
		group_table.setModel(sign_group_model);
		group_table.setVisibleRowCount(12);
	}

	/** Select a new sign group */
	protected void selectGroup() {
		SignGroup group = getSelectedGroup();
		if(group != null) {
			if(sign_text_model != null)
				sign_text_model.dispose();
			sign_text_model = new SignTextTableModel(group,
				state.getSignText(), user);
			sign_text_table.setModel(sign_text_model);
			delete_group.setEnabled(isGroupDeletable(group));
		} else {
			sign_text_table.setModel(new DefaultTableModel());
			delete_group.setEnabled(false);
		}
	}

	/** Check if a sign group is deletable */
	protected boolean isGroupDeletable(SignGroup group) {
		return hasNoReferences(group) && canRemove(group);
	}

	/** Check if a sign group has no references */
	protected boolean hasNoReferences(SignGroup group) {
		return !(hasMembers(group) || hasSignText(group));
	}

	/** Check if a sign group has any members */
	protected boolean hasMembers(final SignGroup group) {
		TypeCache<DmsSignGroup> dms_sign_groups =
			state.getDmsSignGroups();
		return null != dms_sign_groups.findObject(
			new Checker<DmsSignGroup>()
		{
			public boolean check(DmsSignGroup g) {
				return g.getSignGroup() == group;
			}
		});
	}

	/** Check if a sign group has any sign text messages */
	protected boolean hasSignText(final SignGroup group) {
		TypeCache<SignText> sign_text = state.getSignText();
		return null != sign_text.findObject(new Checker<SignText>() {
			public boolean check(SignText t) {
				return t.getSignGroup() == group;
			}
		});
	}

	/** Check if the user can remove the specified sign group */
	protected boolean canRemove(SignGroup group) {
		return user.canRemove(SignGroupTableModel.createSignGroupName(
			group.getName()));
	}

	/** Get the selected sign group */
	protected SignGroup getSelectedGroup() {
		ListSelectionModel s = group_table.getSelectionModel();
		return sign_group_model.getProxy(s.getMinSelectionIndex());
	}

	/** Initialize the sign text table */
	protected void initSignTextTable() {
		final ListSelectionModel s =
			sign_text_table.getSelectionModel();
		s.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		new ListSelectionJob(this, s) {
			public void perform() {
				if(!event.getValueIsAdjusting())
					selectSignText();
			}
		};
		sign_text_table.setAutoCreateColumnsFromModel(false);
		sign_text_table.setColumnModel(
			SignTextTableModel.createColumnModel());
		sign_text_table.setVisibleRowCount(12);
	}

	/** Select a new sign text message */
	protected void selectSignText() {
		Integer w = proxy.getFaceWidth();
		Integer lh = getLineHeightPixels();
		Integer hp = proxy.getHorizontalPitch();
		Integer vp = proxy.getVerticalPitch();
		Integer hb = proxy.getHorizontalBorder();
		if(w != null && lh != null && hp != null && vp != null &&
		   hb != null)
		{
			int h = lh * vp;
			pixel_panel.setPhysicalDimensions(w, h, hb, 0, hp, vp);
		}
		Integer wp = proxy.getWidthPixels();
		Integer cw = proxy.getCharWidthPixels();
		if(wp != null && lh != null && cw != null)
			pixel_panel.setLogicalDimensions(wp, lh, cw, 0);
		pixel_panel.verifyDimensions();
		SignText st = getSelectedSignText();
		if(st != null)
			pixel_panel.setGraphic(renderMessage(st));
		else
			pixel_panel.setGraphic(null);
		delete_text.setEnabled(canRemove(st));
	}

	/** Check if the user can remove the specified sign text */
	protected boolean canRemove(SignText st) {
		return st != null &&
		       user.canRemove(createSignTextName(st.getName()));
	}

	/** Get the line height of the sign */
	protected Integer getLineHeightPixels() {
		Integer w = proxy.getWidthPixels();
		Integer h = proxy.getHeightPixels();
		Integer cw = proxy.getCharWidthPixels();
		Integer ch = proxy.getCharHeightPixels();
		if(w == null || h == null || cw == null || ch == null)
			return null;
		PixelMapBuilder b = new PixelMapBuilder(state.getNamespace(),
			w, h, cw, ch);
		return b.getLineHeightPixels();
	}

	/** Render a message to a bitmap graphic */
	protected BitmapGraphic renderMessage(SignText st) {
		MultiString multi = new MultiString(st.getMessage());
		BitmapGraphic[] pages = renderPages(multi);
		if(pages.length > 0)
			return pages[0];
		else
			return null;
	}

	/** Render the pages of a text message */
	protected BitmapGraphic[] renderPages(MultiString multi) {
		Integer w = proxy.getWidthPixels();
		Integer h = getLineHeightPixels();
		Integer cw = proxy.getCharWidthPixels();
		Integer ch = proxy.getCharHeightPixels();
		if(w == null || h == null || cw == null || ch == null)
			return new BitmapGraphic[0];
		PixelMapBuilder b = new PixelMapBuilder(state.getNamespace(),
			w, h, cw, ch);
		multi.parse(b, b.getDefaultFontNumber());
		return b.getPixmaps();
	}

	/** Get the selected sign text message */
	protected SignText getSelectedSignText() {
		SignTextTableModel m = sign_text_model;
		if(m == null)
			return null;
		ListSelectionModel s = sign_text_table.getSelectionModel();
		return m.getProxy(s.getMinSelectionIndex());
	}

	/** Update one attribute on the form tab */
	public void updateAttribute(String a) {
		if(a == null || a.equals("awsAllowed"))
			awsAllowed.setSelected(proxy.getAwsAllowed());
		if(a == null || a.equals("awsControlled"))
			awsControlled.setSelected(proxy.getAwsControlled());
		// NOTE: messageCurrent attribute changes after all sign
		//       dimension attributes are updated.
		if(a == null || a.equals("messageCurrent"))
			selectSignText();
	}
}
