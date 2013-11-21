/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import static us.mn.state.dot.sched.SwingRunner.runSwing;
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
public class MultipleSignTab extends JPanel implements
	ProxySelectionListener<DMS>
{
	/** Label to display selected sign count */
	private final JLabel selectedLbl = new JLabel(I18N.get("dms.selected") +
		":");

	/** DMS sign group cache */
	private final TypeCache<DmsSignGroup> dms_sign_groups;

	/** Selected list model */
	protected final DefaultListModel sel_model = new DefaultListModel();

	/** Selected sign list */
	protected final JList sel_list = new JList(sel_model);

	/** List model of all non-local sign groups */
	protected final ProxyListModel<SignGroup> sign_group_model;

	/** Group list widget */
	protected final JList group_list;

	/** Selection model */
	protected final ProxySelectionModel<DMS> selectionModel;

	/** Create a new multiple sign tab */
	public MultipleSignTab(DmsCache cache, ProxySelectionModel<DMS> sm) {
		super(new GridBagLayout());
		TypeCache<SignGroup> gc = cache.getSignGroups();
		sign_group_model = new ProxyListModel<SignGroup>(gc) {
			protected int doProxyAdded(SignGroup proxy) {
				if(!proxy.getLocal())
					return super.doProxyAdded(proxy);
				else
					return -1;
			}
		};
		sign_group_model.initialize();
		dms_sign_groups = cache.getDmsSignGroups();
		group_list = new JList(sign_group_model);
		group_list.addListSelectionListener(new IListSelectionAdapter(){
			@Override
			public void valueChanged() {
				selectGroup();
			}
		});
		selectionModel = sm;
		selectionModel.addProxySelectionListener(this);
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets.top = UI.vgap;
		bag.insets.left = UI.hgap;
		bag.insets.right = UI.hgap;
		bag.gridx = 0;
		bag.gridy = 0;
		bag.fill = GridBagConstraints.BOTH;
		add(selectedLbl, bag);
		bag.gridx = 1;
		add(new ILabel("dms.groups"), bag);
		bag.gridx = 0;
		bag.gridy = 1;
		bag.weightx = 1;
		bag.weighty = 1;
		bag.insets.bottom = UI.vgap;
		sel_list.setVisibleRowCount(6);
		sel_list.setEnabled(false);
		JScrollPane pane = new JScrollPane(sel_list);
		add(pane, bag);
		bag.gridx = 1;
		group_list.setVisibleRowCount(6);
		pane = new JScrollPane(group_list);
		add(pane, bag);
	}

	/** Dispose of the sign tab */
	public void dispose() {
		selectionModel.removeProxySelectionListener(this);
		sign_group_model.dispose();
	}

	/** Select a new sign group */
	protected void selectGroup() {
		SignGroup group = getSelectedGroup();
		List<DMS> selected = selectionModel.getSelected();
		List<DMS> in_group = createGroupList(group);
		for(DMS dms: in_group) {
			if(!selected.contains(dms))
				selectionModel.addSelected(dms);
		}
		for(DMS dms: selected) {
			if(!in_group.contains(dms))
				selectionModel.removeSelected(dms);
		}
	}

	/** Get the selected sign group */
	protected SignGroup getSelectedGroup() {
		ListSelectionModel s = group_list.getSelectionModel();
		return sign_group_model.getProxy(s.getMinSelectionIndex());
	}

	/** Called whenever a sign is added to the selection */
	@Override
	public void selectionAdded(final DMS dms) {
		runSwing(new Runnable() {
			public void run() {
				sel_model.addElement(dms);
				updateSelectedLabel();
				SignGroup group = getSelectedGroup();
				if(group != null && !isGroupMember(dms, group))
					group_list.clearSelection();
			}
		});
	}

	/** Called whenever a sign is removed from the selection */
	@Override
	public void selectionRemoved(final DMS dms) {
		runSwing(new Runnable() {
			public void run() {
				sel_model.removeElement(dms);
				updateSelectedLabel();
				SignGroup group = getSelectedGroup();
				if(group != null && isGroupMember(dms, group))
					group_list.clearSelection();
			}
		});
	}

	/** Update the selected count label */
	protected void updateSelectedLabel() {
		selectedLbl.setText(I18N.get("dms.selected") + ": " +
			selectionModel.getSelectedCount());
	}

	/** Check if a sign is a member of the specified group */
	private boolean isGroupMember(DMS dms, SignGroup group) {
		for(DmsSignGroup g: dms_sign_groups) {
			if(dms == g.getDms() && group == g.getSignGroup())
				return true;
		}
		return false;
	}

	/** Create a list of all signs in a group */
	private List<DMS> createGroupList(SignGroup group) {
		LinkedList<DMS> signs = new LinkedList<DMS>();
		for(DmsSignGroup g: dms_sign_groups) {
			if(group == g.getSignGroup())
				signs.add(g.getDms());
		}
		return signs;
	}
}
