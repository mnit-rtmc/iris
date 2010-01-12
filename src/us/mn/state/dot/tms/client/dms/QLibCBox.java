/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2010  Minnesota Department of Transportation
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
import javax.swing.JComboBox;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.QuickMessageHelper;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Quick library combobox. This combobox stores quick messages.
 * When the user changes a quick message selection via this
 * combobox, the dispatcher is flagged that it should update its
 * widgets with this cbox's currently selected message.
 *
 * @see DMSDispatcher, QuickMessage, QLibCBoxModel, SignMessageComposer
 * @author Michael Darter
 * @author Douglas Lau
 */
public class QLibCBox extends JComboBox implements ActionListener {

	/** Canned blank quick library message */
	static public final QuickMessage BLANK_QMESSAGE = new QuickMessage() {

		/** Get the SONAR object name */
		public String getName() {
			return "";
		}

		/** To string */
		public String toString() {
			return getName();
		}

		/** Get the SONAR type name */
		public String getTypeName() {
			return SONAR_TYPE;
		}

		/** Get MULTI string */
		public String getMulti() {
			return "";
		}

		/** Set MULTI string */
		public void setMulti(String s) {
			// not settable
		}

		/** destroy */
		public void destroy() {}
	};

	/** container of this cbox */
	protected final DMSDispatcher m_dispatcher;

	/** Focus listener for editor component */
	private final FocusListener m_flistener;

	/** Create a new quick message combo box */
	public QLibCBox(DMSDispatcher d) {
		m_dispatcher = d;
		setEditable(true);
		m_flistener = createFocusListener();
		getEditor().getEditorComponent().addFocusListener(m_flistener);
		addActionListener(this);
	}

	/** Create focus listener for editor */
	protected FocusListener createFocusListener() {
		return new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				getEditor().selectAll();
			}
			public void focusLost(FocusEvent e) {
				handleEditorFocusLost(e);
			}
		};
	}

	/** Handle editor focus lost */
	protected void handleEditorFocusLost(FocusEvent e) {
		Object item = getEditor().getItem();
		if(item instanceof String)
			handleEditorFocusLost((String)item);
	}

	/** Handle editor focus lost */
	protected void handleEditorFocusLost(String item) {
		String name = item.replace(" ", "");
		getEditor().setItem(name);
		QuickMessage qm = QuickMessageHelper.lookup(name);
		if(qm != null) {
			getModel().setSelectedItem(qm);
			m_dispatcher.updateWidgetsUsingQuickLib();
		}
	}

	/** Return the currently selected proxy */
	public QuickMessage getSelectedProxy() {
		Object obj = getSelectedItem();
		if(obj instanceof QuickMessage)
			return (QuickMessage)obj;
		else
			return null;
	}

	/** return the name of the currently selected proxy */
	public String getSelectedName() {
		Object obj = getSelectedItem();
		if(obj == null) {
			return "";
		} else if(obj instanceof String) {
			return (String)obj;
		} else if(obj instanceof QuickMessage) {
			return ((QuickMessage)obj).getName();
		}
		return "";
	}

	/** Catch events: enter pressed, cbox item clicked, cursor
	 * up/down, lost focus (e.g. tab pressed). Also called after
	 * a setSelectedItem() call. */
	public void actionPerformed(ActionEvent e) {
		if("comboBoxChanged".equals(e.getActionCommand()))
			m_dispatcher.updateWidgetsUsingQuickLib();
	}

	/** Set selected item, but only if it is different from the 
	 *  currently selected item. Triggers a call to actionPerformed().
	 *  @param obj May be a String, SignMessage, QuickMessage. */
	public void setSelectedItem(Object obj) {
		String nametoset = "";
		if(obj == null) {
			nametoset = "";
		} else if(obj instanceof String) {
			nametoset = (String)obj;
		} else if(obj instanceof QuickMessage) {
			nametoset = ((QuickMessage)obj).getName();
		} else if(obj instanceof SignMessage) {
			nametoset = getQuickLibMsgName(obj);
		}
		nametoset = (nametoset == null ? "" : nametoset);
		// set if different from current
		String namecur = getSelectedName();
		namecur = (namecur == null ? "" : namecur);
		if(!namecur.equals(nametoset)) {
			if(nametoset.isEmpty()) {
				super.setSelectedItem(BLANK_QMESSAGE);
			} else {
				QuickMessage qm = QuickMessageHelper.lookup(
					nametoset);
				super.setSelectedItem(qm);
			}
		}
	}

	/** Given a QuickMessage or String, return the cooresponding quick 
	 *  library message name or an empty string if none exists. */
	protected String getQuickLibMsgName(Object obj) {
		if(obj == null) {
			return "";
		} else if(obj instanceof String) {
			return (String)obj;
		} else if(obj instanceof QuickMessage) {
			return ((QuickMessage)obj).getName();
		} else if(obj instanceof SignMessage) {
			// lookup based on multi equality
			SignMessage sm = (SignMessage)obj;
			QuickMessage qm =
				QuickMessageHelper.find(sm.getMulti());
			return (qm == null ? "" : qm.getName());
		}
		return "";
	}

	/** Dispose */
	public void dispose() {
		getEditor().getEditorComponent().
			removeFocusListener(m_flistener);
		removeActionListener(this);
	}

	/** is this control IRIS enabled? */
	public static boolean getIEnabled() {
		return SystemAttrEnum.DMS_QLIB_ENABLE.getBoolean();
	}
}
