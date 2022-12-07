/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2022  Minnesota Department of Transportation
 * Copyright (C) 2010  AHMCT, University of California
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgPatternHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * The message pattern combobox is a widget which allows the user to select
 * a message pattern.  When the user changes a message pattern
 * selection via this combobox, the dispatcher is flagged that it should update
 * its widgets with the newly selected message.
 *
 * @see us.mn.state.dot.tms.MsgPattern
 * @see us.mn.state.dot.tms.client.dms.DMSDispatcher
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class MsgPatternCBox extends JComboBox<MsgPattern> {

	/** Check if a message pattern should be included in combo box */
	static private boolean isValidMulti(MsgPattern pat) {
		MultiString ms = new MultiString(pat.getMulti());
		return ms.isValid() && !ms.isSpecial();
	}

	/** Lookup a message pattern by name, or MsgPattern object.
	 * @return Message pattern or null if not found. */
	static private MsgPattern lookupMsgPattern(Object obj) {
		if (obj instanceof MsgPattern)
			return (MsgPattern) obj;
		else if (obj instanceof String)
			return MsgPatternHelper.lookup((String) obj);
		else
			return null;
	}

	/** Combo box model for message patterns */
	private final DefaultComboBoxModel<MsgPattern> model =
		new DefaultComboBoxModel<MsgPattern>();

	/** DMS dispatcher */
	private final DMSDispatcher dispatcher;

	/** Focus listener for editor component */
	private final FocusListener focus_listener;

	/** Action listener for combo box */
	private final ActionListener action_listener;

	/** Counter to indicate we're adjusting widgets.  This needs to be
	 * incremented before calling dispatcher methods which might cause
	 * callbacks to this class.  This prevents infinite loops. */
	private int adjusting = 0;

	/** Create a new message pattern combo box */
	public MsgPatternCBox(DMSDispatcher d) {
		setModel(model);
		dispatcher = d;
		setEditable(true);
		focus_listener = new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				getEditor().selectAll();
			}
			public void focusLost(FocusEvent e) {
				handleEditorFocusLost(e);
			}
		};
		getEditor().getEditorComponent().addFocusListener(
			focus_listener);
		action_listener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateDispatcher();
			}
		};
		addActionListener(action_listener);
	}

	/** Handle editor focus lost */
	private void handleEditorFocusLost(FocusEvent e) {
		Object item = getEditor().getItem();
		if (item instanceof String)
			handleEditorFocusLost((String) item);
	}

	/** Handle editor focus lost */
	private void handleEditorFocusLost(String item) {
		String name = item.replace(" ", "");
		getEditor().setItem(name);
		MsgPattern pat = lookupMsgPattern(name);
		if (pat != null)
			setSelectedItem(pat);
	}

	/** Update the dispatcher with the selected message pattern */
	private void updateDispatcher() {
		if (adjusting == 0) {
			dispatcher.setMsgPattern(getSelectedMessage());
			dispatcher.selectPreview(true);
		}
	}

	/** Get the currently selected message pattern */
	public MsgPattern getSelectedMessage() {
		Object item = getSelectedItem();
		return (item instanceof MsgPattern)
		      ? (MsgPattern) item
		      : null;
	}

	/** Set selected item, but only if it is different from the
	 * currently selected item.  Triggers a call to actionPerformed().
	 * @param obj May be a String, or MsgPattern. */
	@Override
	public void setSelectedItem(Object obj) {
		MsgPattern pat = lookupMsgPattern(obj);
		if (pat != getSelectedMessage())
			super.setSelectedItem(pat);
	}

	/** Populate the message pattern model, sorted */
	public void populateModel(DMS dms) {
		TreeSet<MsgPattern> msgs = createMessageSet(dms);
		adjusting++;
		model.removeAllElements();
		model.addElement(null);
		for (MsgPattern pat: msgs)
			model.addElement(pat);
		adjusting--;
	}

	/** Create a set of message patterns for the specified DMS */
	private TreeSet<MsgPattern> createMessageSet(DMS dms) {
		TreeSet<MsgPattern> msgs = new TreeSet<MsgPattern>(
			new NumericAlphaComparator<MsgPattern>());
		Iterator<DmsSignGroup> it = DmsSignGroupHelper.iterator();
		while (it.hasNext()) {
			DmsSignGroup dsg = it.next();
			if (dsg.getDms() == dms) {
				SignGroup sg = dsg.getSignGroup();
				Iterator<MsgPattern> pit =
					MsgPatternHelper.iterator();
				while (pit.hasNext()) {
					MsgPattern pat = pit.next();
					if (pat.getSignGroup() == sg) {
						if (isValidMulti(pat))
							msgs.add(pat);
					}
				}
			}
		}
		return msgs;
	}

	/** Set the enabled status */
	@Override
	public void setEnabled(boolean e) {
		super.setEnabled(e);
		if (!e) {
			setSelectedItem(null);
			removeAllItems();
		}
	}

	/** Dispose */
	public void dispose() {
		removeActionListener(action_listener);
		getEditor().getEditorComponent().
			removeFocusListener(focus_listener);
	}
}
