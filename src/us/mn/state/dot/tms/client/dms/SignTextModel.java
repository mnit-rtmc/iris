/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2017  Minnesota Department of Transportation
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

import java.util.HashMap;
import java.util.HashSet;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import static us.mn.state.dot.tms.client.widget.SwingRunner.runSwing;

/**
 * Model for sign text messages.  This class is instantiated and contained by
 * SignMessageComposer.  One SignTextModel is associated with a single DMS.
 * It creates and contains SignTextComboBoxModel objects for each combobox
 * in SignMessageComposer.  This object listens for changes to sign_text and
 * dms_sign_groups and is responsible for updating its model accordingly.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignTextModel {

	/** DMS associated with this object */
	private final DMS dms;

	/** DMS sign group type cache, relates dms to sign groups */
	private final TypeCache<DmsSignGroup> dms_sign_groups;

	/** Sign text type cache, list of all sign text lines */
	private final TypeCache<SignText> sign_text;

	/** Sign text creator */
	private final SignTextCreator creator;

	/** Set of DMS member groups.  Access must be synchronized because
	 * the Swing EDT and sonar threads can access. */
	private final HashSet<String> groups = new HashSet<String>();

	/** Mapping of line numbers to combo box models */
	private final HashMap<Short, SignTextComboBoxModel> lines =
		new HashMap<Short, SignTextComboBoxModel>();

	/** Last line in model */
	private short last_line = 1;

	/** Listener for sign text proxies */
	private final ProxyListener<SignText> listener =
		new ProxyListener<SignText>()
	{
		public void proxyAdded(SignText proxy) {
			if (isAssociated(proxy))
				doAddSignText(proxy);
		}
		public void enumerationComplete() { }
		public void proxyRemoved(SignText proxy) {
			if (isAssociated(proxy))
				doRemoveSignText(proxy);
		}
		public void proxyChanged(SignText proxy, String a) {
			if (isAssociated(proxy))
				doChangeSignText(proxy);
		}
	};

	/** Listener for DMS sign groups */
	private final ProxyListener<DmsSignGroup> dsg_listener =
		new ProxyListener<DmsSignGroup>()
	{
		public void proxyAdded(DmsSignGroup proxy) {
			if (dms == proxy.getDms())
				addGroup(proxy.getSignGroup());
		}
		public void enumerationComplete() { }
		public void proxyRemoved(DmsSignGroup proxy) {
			if (dms == proxy.getDms())
				removeGroup(proxy.getSignGroup());
		}
		public void proxyChanged(DmsSignGroup proxy, String attrib) { }
	};


	/** Create a new sign text model */
	public SignTextModel(Session s, DMS proxy) {
		dms = proxy;
		SonarState st = s.getSonarState();
		dms_sign_groups = st.getDmsCache().getDmsSignGroups();
		sign_text = st.getDmsCache().getSignText();
		creator = new SignTextCreator(s);
	}

	/** Initialize the sign text model */
	public void initialize() {
		dms_sign_groups.addProxyListener(dsg_listener);
		sign_text.addProxyListener(listener);
	}

	/** Dispose of the model */
	public void dispose() {
		sign_text.removeProxyListener(listener);
		dms_sign_groups.removeProxyListener(dsg_listener);
	}

	/** Add a SignText to the model */
	private void doAddSignText(final SignText st) {
		// NOTE: updating last_line can't be deferred to the
		//       swing thread, because getLastLine is called
		//       before the Runnables get a chance to run.
		last_line = (short)Math.max(last_line, st.getLine());
		runSwing(new Runnable() {
			public void run() {
				addSignText(st);
			}
		});
	}

	/** Add a SignText to the model */
	private void addSignText(SignText st) {
		getLineModel(st.getLine()).add(st);
	}

	/** Remove a SignText from the model */
	private void doRemoveSignText(final SignText st) {
		runSwing(new Runnable() {
			public void run() {
				removeSignText(st);
			}
		});
	}

	/** Remove a SignText from the model */
	private void removeSignText(SignText st) {
		getLineModel(st.getLine()).remove(st);
	}

	/** Change a SignText in the model */
	private void doChangeSignText(final SignText st) {
		runSwing(new Runnable() {
			public void run() {
				changeSignText(st);
			}
		});
	}

	/** Change a SignText in the model */
	private void changeSignText(SignText st) {
		// iterate through all combobox models because the line
		// may have changed, moving it between comboboxes
		for (SignTextComboBoxModel m: lines.values())
			m.remove(st);
		addSignText(st);
	}

	/** Is the sign text associated with the DMS? */
	private boolean isAssociated(SignText st) {
		return isMember(st.getSignGroup());
	}

	/** Is the DMS a member of the specified group? */
	private boolean isMember(SignGroup g) {
		synchronized (groups) {
			return g != null && groups.contains(g.getName());
		}
	}

	/** Add all SignText in one sign group to the model */
	private void addGroup(SignGroup g) {
		synchronized (groups) {
			groups.add(g.getName());
		}
		for (SignText st: sign_text) {
			if (st.getSignGroup() == g)
				doAddSignText(st);
		}
	}

	/** Remove all SignText in one sign group from the model */
	private void removeGroup(SignGroup g) {
		synchronized (groups) {
			groups.remove(g.getName());
		}
		for (SignText st: sign_text) {
			if (st.getSignGroup() == g)
				doRemoveSignText(st);
		}
	}

	/**
	 * Get the local SignGroup for the DMS.
	 * @return local SignGroup if it exists, otherwise null
	 */
	private SignGroup getLocalSignGroup() {
		for (DmsSignGroup g: dms_sign_groups) {
			if (isLocalSignGroup(g))
				return g.getSignGroup();
		}
		return null;
	}

	/** Check if the given sign group is a local group for the DMS */
	private boolean isLocalSignGroup(DmsSignGroup g) {
		return g.getDms() == dms && g.getSignGroup().getLocal();
	}

	/** Check if user is permitted to add sign text to local sign group */
	public boolean isLocalSignTextAddPermitted() {
		SignGroup sg = getLocalSignGroup();
		if (sg != null) {
			String oname = sg.getName() + "_XX";
			return creator.isWritePermitted(oname);
		} else
			return false;
	}

	/** Get the combobox line model for the specified line */
	public SignTextComboBoxModel getLineModel(short line) {
		if (lines.containsKey(line))
			return lines.get(line);
		else {
			SignTextComboBoxModel m = new SignTextComboBoxModel(
				line);
			lines.put(line, m);
			return m;
		}
	}

	/** Get the last line number with sign text */
	public short getLastLine() {
		return last_line;
	}

	/** Update the message library with the currently selected messages */
	public void updateMessageLibrary() {
		for (SignTextComboBoxModel m: lines.values()) {
			ClientSignText st = m.getEditedSignText();
			if (st != null)
				createSignText(st);
		}
	}

	/** Add a SignText to the local sign text library.
	 * @param st SignText to create. */
	private void createSignText(ClientSignText st) {
		SignGroup sg = getLocalSignGroup();
		if (sg != null) {
			creator.create(sg, st.getLine(), st.getMulti(),
				st.getRank());
		}
	}
}
