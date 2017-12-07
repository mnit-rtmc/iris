/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2017  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.QuickMessageHelper;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * The quick message combobox is a widget which allows the user to select
 * a precomposed "quick" message. When the user changes a quick message
 * selection via this combobox, the dispatcher is flagged that it should update
 * its widgets with the newly selected message.
 *
 * @see DMSDispatcher, QuickMessage
 * @author Michael Darter
 * @author Douglas Lau
 */
public class QuickMessageCBox extends JComboBox<QuickMessage> {

	/** Given a QuickMessage or String, return the cooresponding quick 
	 * message name or an empty string if none exists. */
	static private String getQuickLibMsgName(Object obj) {
		if (obj instanceof String)
			return (String) obj;
		else if (obj instanceof QuickMessage)
			return ((QuickMessage) obj).getName();
		else
			return "";
	}

	/** Combo box model for quick messages */
	private final DefaultComboBoxModel<QuickMessage> model =
		new DefaultComboBoxModel<QuickMessage>();

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

	/** Create a new quick message combo box */
	public QuickMessageCBox(DMSDispatcher d) {
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
		QuickMessage qm = QuickMessageHelper.lookup(name);
		if (qm != null) {
			model.setSelectedItem(qm);
			updateDispatcher(qm);
		}
	}

	/** Update the dispatcher with the selected quick message */
	private void updateDispatcher() {
		QuickMessage qm = getSelectedProxy();
		if (qm != null)
			updateDispatcher(qm);
	}

	/** Get the currently selected proxy */
	private QuickMessage getSelectedProxy() {
		Object obj = getSelectedItem();
		if (obj instanceof QuickMessage)
			return (QuickMessage) obj;
		else
			return null;
	}

	/** Update the dispatcher with the specified quick message */
	private void updateDispatcher(QuickMessage qm) {
		String ms = qm.getMulti();
		if (adjusting == 0 && !ms.isEmpty()) {
			dispatcher.setComposedMulti(ms);
			dispatcher.selectPreview(true);
		}
	}

	/** Set the composed MULTI string */
	public void setComposedMulti(String ms) {
		adjusting++;
		if (ms.isEmpty())
			setSelectedItem(null);
		else
			setSelectedItem(QuickMessageHelper.find(ms));
		adjusting--;
	}

	/** Set selected item, but only if it is different from the 
	 * currently selected item. Triggers a call to actionPerformed().
	 * @param obj May be a String, or QuickMessage. */
	public void setSelectedItem(Object obj) {
		String nametoset = getQuickLibMsgName(obj);
		String namecur = getSelectedName();
		if (!namecur.equals(nametoset)) {
			if (nametoset.isEmpty())
				super.setSelectedItem(null);
			else {
				QuickMessage qm = QuickMessageHelper.lookup(
					nametoset);
				super.setSelectedItem(qm);
			}
		}
	}

	/** Get the name of the currently selected quick message */
	private String getSelectedName() {
		return getQuickLibMsgName(getSelectedItem());
	}

	/** Populate the quick message model, with sorted quick messages */
	public void populateModel(DMS dms) {
		TreeSet<QuickMessage> msgs = createMessageSet(dms);
		adjusting++;
		model.removeAllElements();
		for (QuickMessage qm: msgs)
			model.addElement(qm);
		adjusting--;
	}

	/** Create a set of quick messages for the specified DMS */
	private TreeSet<QuickMessage> createMessageSet(DMS dms) {
		TreeSet<QuickMessage> msgs = new TreeSet<QuickMessage>(
			new NumericAlphaComparator<QuickMessage>());
		Iterator<DmsSignGroup> it = DmsSignGroupHelper.iterator();
		while (it.hasNext()) {
			DmsSignGroup dsg = it.next();
			if (dsg.getDms() == dms) {
				SignGroup sg = dsg.getSignGroup();
				Iterator<QuickMessage> qit =
					QuickMessageHelper.iterator();
				while (qit.hasNext()) {
					QuickMessage qm = qit.next();
					if (qm.getSignGroup() == sg)
						msgs.add(qm);
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
