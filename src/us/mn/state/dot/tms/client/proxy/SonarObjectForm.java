/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2013  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.proxy;

import java.awt.BorderLayout;
import javax.swing.Action;
import javax.swing.JComboBox;
import static us.mn.state.dot.sched.SwingRunner.runSwing;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.SmartDesktop;

/**
 * SonarObjectForm is an abstract Swing dialog for SonarObject property forms
 *
 * @author Douglas Lau
 */
abstract public class SonarObjectForm<T extends SonarObject>
	extends AbstractForm implements ProxyListener<T>
{
	/** SONAR object proxy */
	protected final T proxy;

	/** User Session */
	protected final Session session;

	/** SONAR state */
	protected final SonarState state;

	/** Create a new SONAR object form */
	protected SonarObjectForm(String prefix, Session s, T p) {
		super(prefix + p.getName());
		proxy = p;
		session = s;
		state = s.getSonarState();
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		setLayout(new BorderLayout());
		TypeCache<T> cache = getTypeCache();
		cache.watchObject(proxy);
		cache.addProxyListener(this);
	}

	/** Get the SONAR type cache */
	abstract protected TypeCache<T> getTypeCache();

	/** A new proxy has been added */
	public void proxyAdded(T p) {
		// we're not interested
	}

	/** Enumeration of the proxy type has completed */
	public void enumerationComplete() {
		// we're not interested
	}

	/** A proxy has been removed */
	public void proxyRemoved(T p) {
		if(proxy == p)
			closeForm();
	}

	/** Close the form */
	protected void closeForm() {
		runSwing(new Runnable() {
			public void run() {
				close();
			}
		});
	}

	/** A proxy has been changed */
	public void proxyChanged(T p, final String a) {
		if(proxy == p)
			updateAttribute(a);
	}

	/** Update one attribute on the form */
	protected final void updateAttribute(final String a) {
		runSwing(new Runnable() {
			public void run() {
				doUpdateAttribute(a);
			}
		});
	}

	/** Update one attribute on the form */
	abstract protected void doUpdateAttribute(String a);

	/** Update one combo box attribute on the form.
	 * @param a Name of current attribute.
	 * @param an Attribute name for combo box.
	 * @param cmb Combo box to update.
	 * @param idx New index to select in combo box.
	 * @param act Action for combo box. */
	protected final void updateComboBox(String a, String an, JComboBox cmb,
		int idx, Action act)
	{
		if(a == null || a.equals(an)) {
			cmb.setAction(null);
			cmb.setSelectedIndex(idx);
			boolean up = canUpdate(an);
			if(act != null)
				act.setEnabled(up);
			else
				cmb.setEnabled(up);
			cmb.setAction(act);
		}
	}

	/** Dispose of the form */
	protected void dispose() {
		TypeCache<T> cache = getTypeCache();
		cache.removeProxyListener(this);
		cache.ignoreObject(proxy);
	}

	/** Show another form */
	protected void showForm(SonarObjectForm form) {
		SmartDesktop desktop = session.getDesktop();
		desktop.show(form);
	}

	/** Check if the user can update an attribute */
	protected boolean canUpdate() {
		return session.canUpdate(proxy);
	}

	/** Check if the user can update an attribute */
	protected boolean canUpdate(String aname) {
		return session.canUpdate(proxy, aname);
	}

	/** Check if the user is permitted to update an attribute */
	protected boolean isUpdatePermitted(String aname) {
		return session.isUpdatePermitted(proxy, aname);
	}
}
