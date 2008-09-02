/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
import us.mn.state.dot.sonar.Checker;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignText;

/**
 * Model for sign text messages.
 *
 * @author Douglas Lau
 */
public class SignMessageModel implements ProxyListener<DmsSignGroup> {

	/** DMS identifier */
	protected final String dms_id;

	/** DMS sign group type cache */
	protected final TypeCache<DmsSignGroup> dms_sign_groups;

	/** Sign text type cache */
	protected final TypeCache<SignText> sign_text;

	/** Listener for sign text proxies */
	protected final ProxyListener<SignText> listener;

	/** Create a new sign group table model */
	public SignMessageModel(String dms, TypeCache<DmsSignGroup> d,
		TypeCache<SignText> t)
	{
		dms_id = dms;
		dms_sign_groups = d;
		sign_text = t;
		listener = new ProxyListener<SignText>() {
			public void proxyAdded(SignText proxy) {
				if(isMember(proxy.getSignGroup()))
					addSignText(proxy);
			}
			public void enumerationComplete() { }
			public void proxyRemoved(SignText proxy) {
				if(isMember(proxy.getSignGroup()))
					removeSignText(proxy);
			}
			public void proxyChanged(SignText proxy, String a) {
				if(isMember(proxy.getSignGroup()))
					changeSignText(proxy);
			}
		};
	}

	/** Initialize the sign message model */
	public void initialize() {
		dms_sign_groups.addProxyListener(this);
		sign_text.addProxyListener(listener);
	}

	/** Dispose of the model */
	public void dispose() {
		sign_text.removeProxyListener(listener);
		dms_sign_groups.removeProxyListener(this);
	}

	/** Add a new proxy to the model */
	public void proxyAdded(DmsSignGroup proxy) {
		if(dms_id.equals(proxy.getDms()))
			addGroup(proxy.getSignGroup());
	}

	/** Enumeration of the proxy type is complete */
	public void enumerationComplete() {
		// We're not interested
	}

	/** Remove a proxy from the model */
	public void proxyRemoved(DmsSignGroup proxy) {
		if(dms_id.equals(proxy.getDms()))
			removeGroup(proxy.getSignGroup());
	}

	/** Change a proxy in the model */
	public void proxyChanged(DmsSignGroup proxy, String attrib) {
		// NOTE: this should never happen
	}

	/** Set of DMS member groups */
	protected final HashSet<String> groups = new HashSet<String>();

	/** Is the DMS a member of the specified group? */
	protected boolean isMember(SignGroup g) {
		return g != null && groups.contains(g.getName());
	}

	/** Add the DMS to a sign group */
	protected void addGroup(final SignGroup g) {
		groups.add(g.getName());
		sign_text.find(new Checker() {
			public boolean check(SonarObject o) {
				if(o instanceof SignText) {
					SignText t = (SignText)o;
					if(t.getSignGroup() == g)
						addSignText(t);
				}
				return false;
			}
		});
	}

	/** Remove the DMS from a sign group */
	protected void removeGroup(final SignGroup g) {
		groups.remove(g.getName());
		sign_text.find(new Checker() {
			public boolean check(SonarObject o) {
				if(o instanceof SignText) {
					SignText t = (SignText)o;
					if(t.getSignGroup() == g)
						removeSignText(t);
				}
				return false;
			}
		});
	}

	/** Mapping of line numbers to combo box models */
	protected final HashMap<Short, LineModel> lines =
		new HashMap<Short, LineModel>();

	/** Get the line model for the specified line */
	public LineModel getLineModel(short line) {
		if(lines.containsKey(line))
			return lines.get(line);
		else {
			LineModel m = new LineModel();
			lines.put(line, m);
			return m;
		}
	}

	/** Get the maximum line number */
	public short getMaxLine() {
		short m = 0;
		for(short i: lines.keySet())
			m = (short)Math.max(i, m);
		return m;
	}

	/** Add a sign message to the model */
	protected void addSignText(SignText t) {
		short line = t.getLine();
		LineModel m = getLineModel(line);
		m.add(t);
	}

	/** Remove a sign message from the model */
	protected void removeSignText(SignText t) {
		short line = t.getLine();
		LineModel m = getLineModel(line);
		m.remove(t);
	}

	/** Change a sign message in the model */
	protected void changeSignText(SignText t) {
		for(LineModel m: lines.values())
			m.change(t);
	}
}
