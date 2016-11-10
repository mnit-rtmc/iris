/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.HashSet;
import java.util.Set;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.proxy.ProxySelectionListener;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A MultipleSignTab is a GUI component for sending a message to multiple signs
 * at once within the DMS dispatcher.
 *
 * @author Douglas Lau
 */
public class MultipleSignTab extends JPanel {

	/** Label to display selected sign count */
	private final JLabel sel_lbl = new JLabel(I18N.get("dms.selected") +
		":");

	/** DMS sign group cache */
	private final TypeCache<DmsSignGroup> dms_sign_groups;

	/** Selected list model */
	private final DefaultListModel<DMS> sign_mdl =
		new DefaultListModel<DMS>();

	/** List model of all non-local sign groups */
	private final ProxyListModel<SignGroup> sign_group_model;

	/** Group list widget */
	private final JList<SignGroup> group_list;

	/** Selection model */
	private final ProxySelectionModel<DMS> sel_mdl;

	/** Selection listener */
	private final ProxySelectionListener sel_listener =
		new ProxySelectionListener()
	{
		public void selectionChanged() {
			doSelectionChanged();
		}
	};

	/** Create a new multiple sign tab */
	public MultipleSignTab(DmsCache cache, ProxySelectionModel<DMS> sm) {
		super(new GridBagLayout());
		TypeCache<SignGroup> gc = cache.getSignGroups();
		sign_group_model = new ProxyListModel<SignGroup>(gc) {
			@Override
			protected boolean check(SignGroup proxy) {
				return !proxy.getLocal();
			}
		};
		dms_sign_groups = cache.getDmsSignGroups();
		group_list = new JList<SignGroup>(sign_group_model);
		group_list.setSelectionMode(SINGLE_SELECTION);
		group_list.addListSelectionListener(new IListSelectionAdapter(){
			@Override
			public void valueChanged() {
				selectGroup();
			}
		});
		sel_mdl = sm;
	}

	/** Initialize the sign tab */
	public void initialize() {
		sign_group_model.initialize();
		layoutPanel();
		sel_mdl.addProxySelectionListener(sel_listener);
	}

	/** Layout the panel */
	private void layoutPanel() {
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.top = UI.vgap;
		bag.insets.left = UI.hgap;
		bag.insets.right = UI.hgap;
		bag.gridx = 0;
		bag.gridy = 0;
		bag.fill = GridBagConstraints.BOTH;
		add(sel_lbl, bag);
		bag.gridx = 1;
		add(new ILabel("dms.groups"), bag);
		bag.gridx = 0;
		bag.gridy = 1;
		bag.weightx = 1;
		bag.weighty = 1;
		bag.insets.bottom = UI.vgap;
		JList<DMS> list = new JList<DMS>(sign_mdl);
		list.setVisibleRowCount(6);
		list.setEnabled(false);
		JScrollPane pane = new JScrollPane(list);
		add(pane, bag);
		bag.gridx = 1;
		group_list.setVisibleRowCount(6);
		pane = new JScrollPane(group_list);
		add(pane, bag);
	}

	/** Dispose of the sign tab */
	public void dispose() {
		sel_mdl.removeProxySelectionListener(sel_listener);
		sign_group_model.dispose();
	}

	/** Select a new sign group */
	protected void selectGroup() {
		SignGroup group = getSelectedGroup();
		sel_mdl.setSelected(createGroupList(group));
	}

	/** Get the selected sign group */
	private SignGroup getSelectedGroup() {
		int i = group_list.getSelectedIndex();
		return (i >= 0) ? sign_group_model.getProxy(i) : null;
	}

	/** Called whenever the selection is changed */
	private void doSelectionChanged() {
		sign_mdl.clear();
		SignGroup group = getSelectedGroup();
		for (DMS dms : sel_mdl.getSelected()) {
			sign_mdl.addElement(dms);
			if (group != null && !isGroupMember(dms, group)) {
				group_list.clearSelection();
				group = null;
			}
		}
		updateSelectedLabel();
	}

	/** Update the selected count label */
	private void updateSelectedLabel() {
		sel_lbl.setText(I18N.get("dms.selected") + ": " +
			sel_mdl.getSelectedCount());
	}

	/** Check if a sign is a member of the specified group */
	private boolean isGroupMember(DMS dms, SignGroup group) {
		for (DmsSignGroup g: dms_sign_groups) {
			if (dms == g.getDms() && group == g.getSignGroup())
				return true;
		}
		return false;
	}

	/** Create a list of all signs in a group */
	private Set<DMS> createGroupList(SignGroup group) {
		HashSet<DMS> signs = new HashSet<DMS>();
		for (DmsSignGroup g: dms_sign_groups) {
			if (group == g.getSignGroup())
				signs.add(g.getDms());
		}
		return signs;
	}
}
