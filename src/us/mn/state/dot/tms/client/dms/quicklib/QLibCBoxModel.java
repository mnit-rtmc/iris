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
package us.mn.state.dot.tms.client.dms.quicklib;

import java.util.TreeSet;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.SwingUtilities;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

//FIXME: QLibCBoxModel should be removed, and instead use ProxyListModel 
//       and WrapperComboBoxModel like in the rest of the client.

/**
 * Model for a quick library combobox.
 * @see QLibCBox, NumericAlphaComparator
 * @author Michael Darter
 * @author Doug Lau
 */
public class QLibCBoxModel extends AbstractListModel implements ComboBoxModel
{
	/** Set of sorted proxies */
	protected final TreeSet<QuickMessage> m_proxies;

	/** sonar type cache */
	protected final TypeCache<QuickMessage> m_tc;

	/** Listener for proxy updates */
	protected final ProxyListener<QuickMessage> m_listener;

	/** Selected proxy */
	protected QuickMessage m_selected;

	/** container */
	protected final QLibCBox m_cbox;

	/** Constructor */
	protected QLibCBoxModel(QLibCBox cbox, TypeCache<QuickMessage> tc, 
		NumericAlphaComparator c)
	{
		m_cbox = cbox;
		m_tc = tc;
		m_proxies = new TreeSet<QuickMessage>(c);
		addProxy(QLibCBox.BLANK_QMESSAGE);
		m_listener = new ProxyListener<QuickMessage>() {
			public void proxyAdded(QuickMessage p) {
				addProxy(p);
			}
			public void enumerationComplete() { }
			public void proxyRemoved(QuickMessage p) {
				removeProxy(p);
			}
			public void proxyChanged(QuickMessage p, String a) {
				changeProxy(p);
			}
		};
		m_tc.addProxyListener(m_listener);
	}

	/** Get the element at the specified index (from AbstractListModel) */
	public synchronized QuickMessage getElementAt(int index) {
		int i = 0;
		for(QuickMessage p: m_proxies) {
			if(i == index)
				return p;
			i++;
		}
		return null;
	}

	/** Get the number of proxies in the model (from AbstractListModel) */
	public int getSize() {
		return m_proxies.size();
	}

	/** Get the selected item (ComboBoxModel interface) */
	public Object getSelectedItem() {
		return m_selected;
	}

	/** Set the selected proxy (ComboBoxModel interface). 
	 *  @param s A quick library message. */
	public void setSelectedItem(Object obj) {
		if(obj instanceof QuickMessage) {
			if(!same((QuickMessage)obj)) {
				m_selected = (QuickMessage)obj;
				// via AbstractListModel, results in call 
				// to editor's setSelectedItem method
				fireContentsChanged(this, -1, -1);
			}

		}
	}

	/** Return true if the currently selected item is equal to the arg. */
	protected boolean same(QuickMessage selected) {
		if(m_selected == null && selected == null)
			return true;
		else if(m_selected == null || selected == null)
			return false;
		String name = m_selected.getName();
		// name may be null if current qlib msg deleted
		if(name == null)
			return false;
		return name.equals(selected.getName());
	}

	/** Determine if the specified quick library proxy exists.
	 * @param n Name of quick library message, e.g. the xxx in qlib_xxx. 
	 * @return True if proxy exists else false. */
	public boolean existsProxy(String n) {
		if(n == null)
			return false;
		return null != lookupProxy(n);
	}

	/** Lookup a proxy given a quick lib message name.
	 * @param n Name of quick library message.
	 * @return Proxy if found, else null. */
	public QuickMessage lookupProxy(String n) {
		if(n == null)
			return null;
		return m_tc.lookupObject(n);
	}

	/** Find the index of the proxy */
	protected synchronized int findIndex(QuickMessage arg_proxy) {
		if(arg_proxy == null || !isMember(arg_proxy))
			return -1;
		int i = 0;
		String pname = arg_proxy.getName();
		for(QuickMessage p: m_proxies) {
			if(pname.equals(p.getName()))
				return i;
			i++;
		}
		return -1;
	}

	/** Is the proxy a member of the model? */
	protected boolean isMember(QuickMessage p) {
		return p != null;
	}

	/** Add a proxy to the model */
	protected synchronized void addProxy(QuickMessage p) {
		if(!isMember(p))
			return;
		if(!m_proxies.add(p))
			return;
		final int i = findIndex(p);
		assert i >= 0 : "Find failed in QLibCBoxModel.addProxy()";
		if(i >=0 ) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireIntervalAdded(this, i, i);
				}
			});
		}
		// new proxy might change current selection
		m_cbox.updateSelected();
	}

	/** Remove a proxy from the model */
	protected synchronized void removeProxy(QuickMessage p) {
		if(!isMember(p))
			return;
		final int i = findIndex(p);
		if(i >= 0) {
			m_proxies.remove(p);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireIntervalRemoved(this, i, i);
				}
			});
			// removed proxy might be the current selection
			m_cbox.updateSelected();
		}
	}

	/** Change a proxy in the model */
	protected synchronized void changeProxy(QuickMessage p) {
		if(!isMember(p))
			return;
		final int i = findIndex(p);
		if(i < 0)
			addProxy(p);
		// changed proxy might change cbox selection
		m_cbox.updateSelected();
	}
}
