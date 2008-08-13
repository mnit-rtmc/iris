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
package us.mn.state.dot.tms.client.toast;

import java.awt.BorderLayout;
import javax.swing.SwingUtilities;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.TmsConnection;

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

	/** TMS connection */
	protected final TmsConnection connection;

	/** Administrator privilege flag */
	protected final boolean admin;

	/** Create a new SONAR object form */
	protected SonarObjectForm(String prefix, TmsConnection tc, T p) {
		super(prefix + p.getName());
		proxy = p;
		connection = tc;
		admin = connection.isAdmin();
	}

	/** Get the TMS connection */
	public TmsConnection getConnection() {
		return connection;
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		setLayout(new BorderLayout());
		TypeCache<T> cache = getTypeCache(connection.getSonarState());
		cache.addProxyListener(this);
	}

	/** Get the SONAR type cache */
	abstract protected TypeCache<T> getTypeCache(SonarState st);

	/** A new proxy has been added */
	public void proxyAdded(T p) {
		// we're not interested
	}

	/** A proxy has been removed */
	public void proxyRemoved(T p) {
		if(proxy == p) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					close();
				}
			});
		}
	}

	/** A proxy has been changed */
	public void proxyChanged(T p, final String a) {
		if(proxy == p) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					updateAttribute(a);
				}
			});
		}
	}

	/** Update one attribute on the form */
	abstract protected void updateAttribute(String a);

	/** Dispose of the form */
	protected void dispose() {
		TypeCache<T> cache = getTypeCache(connection.getSonarState());
		cache.removeProxyListener(this);
	}
}
