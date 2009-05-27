/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms.quicklib;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import javax.swing.ComboBoxEditor;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.client.NumericAlphaComparator;
import us.mn.state.dot.tms.client.dms.DMSDispatcher;
import us.mn.state.dot.tms.MultiString;
import us.mn.state.dot.tms.SignMessage;

/**
 * Quick library combobox. This combobox stores quick messages.
 * When the user changes a quick message selection via this
 * combobox, the dispatcher is flagged that it should update its
 * widgets with this cbox's currently selected message.
 * @see DMSDispatcher, QuickMessage, QLibCBoxModel, SignMessageComposer
 * @author Michael Darter
 */
public class QLibCBox extends JComboBox implements ActionListener
{
	/** sonar type cache */
	protected final TypeCache<QuickMessage> m_tc;

	/** container of this cbox */
	protected final DMSDispatcher m_dispatcher;

	/** constructor */
	public QLibCBox(DMSDispatcher d, TypeCache<QuickMessage> tc) {
		m_dispatcher = d;
		m_tc = tc;
		setModel(new QLibCBoxModel(this, tc, 
			new NumericAlphaComparator()));
		setEditable(true);
		handleEditorFocusEvents();
		addActionListener(this);
	}

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

		/** MULTI string */
		protected String multi = "";

		/** Get MULTI string */
		public String getMulti() {
			return multi;
		}

		/** Set MULTI string */
		public void setMulti(String s) {
			multi = s;
		}

		/** destroy */
		public void destroy() {}
	};

	/** focus listener */
	private FocusListener m_flistener;

	/** Setup handling of focus events */
	public void handleEditorFocusEvents() {
		m_flistener = new FocusAdapter() {
			public void focusGained(FocusEvent e) {
				// when focus gained, select existing text
				getEditor().selectAll();
			}
			public void focusLost(FocusEvent e) {
				handleEditorFocusLost(e);
			}
		};
		getEditor().getEditorComponent().addFocusListener(m_flistener);
	}

	/** Handle editor focus lost. */
	protected void handleEditorFocusLost(FocusEvent e) {
		Object item = getEditor().getItem();
		if(item == null)
			return;
		// only care about typed free-form text
		if(!(item instanceof String))
			return;

		// lookup proxy
		String name = setEditorText((String)item);
		QuickMessage sm = ((QLibCBoxModel)getModel()).
			lookupProxy(name);
		if(sm == null)
			return;

		getModel().setSelectedItem(sm);
		m_dispatcher.updateWidgetsUsingQuickLib();
	}

	/** Return the currently selected proxy */
	public QuickMessage getSelectedProxy() {
		Object obj = getSelectedItem();
		if(obj instanceof QuickMessage)
			return (QuickMessage)obj;
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
	 *  up/down, lost focus (e.g. tab pressed). Also called after
	 *  a setSelectedItem() call. */
	public void actionPerformed(ActionEvent e) {
		if("comboBoxChanged".equals(e.getActionCommand()))
			m_dispatcher.updateWidgetsUsingQuickLib();
	}

	/** Update the currently selected item. This is called by 
	 *  the model when a QuickMessage proxy changes, is added, 
	 *  or deleted, which may effect the current cbox selection. */
	public void updateSelected() {
		m_dispatcher.updateTextQLibCBox(true);
	}

	/** Set the editor's text */
	protected String setEditorText(String t) {
		t = (t == null ? "" : t.replace(" ",""));
		getEditor().setItem(t);
		return t;
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
				QuickMessage qm = ((QLibCBoxModel)getModel()).
					lookupProxy(nametoset);
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
			QuickMessage qm = lookupMulti(sm.getMulti());
			return (qm == null ? "" : qm.getName());
		}
		return "";
	}

	/** Lookup a quick message in the library using a MULTI string.
	 *  @param multi A MULTI string, which is normalized.
	 *  @return The quick message in the quick library that matches multi
	 *	    or null if it doesn't exist in the library. */
	protected QuickMessage lookupMulti(String arg_multi) {
		if(arg_multi == null || m_tc == null)
			return null;
		final String multi = new MultiString(arg_multi).normalize();
		QuickMessage m = m_tc.findObject(new Checker<QuickMessage>() {
			public boolean check(QuickMessage qm) {
				if(qm == null)
					return false;
				return multi.equals(new MultiString(
					qm.getMulti()).normalize());
			}
		});
		return m;
	}

	/** Dispose */
	public void dispose() {
		getEditor().getEditorComponent().
			removeFocusListener(m_flistener);
		removeActionListener(this);
	}
}
