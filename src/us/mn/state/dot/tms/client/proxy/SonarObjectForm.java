/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
import java.awt.Color;
import javax.swing.Action;
import javax.swing.JComboBox;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.EditModeListener;
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
	extends AbstractForm implements ProxyView<T>
{
	/** Lighter gray for background */
	static public final Color LIGHTER_GRAY = new Color(208, 208, 208);

	/** SONAR object proxy */
	protected final T proxy;

	/** User Session */
	protected final Session session;

	/** SONAR state */
	protected final SonarState state;

	/** Proxy watcher */
	private final ProxyWatcher<T> watcher;

	/** Edit mode listener */
	private final EditModeListener edit_lsnr = new EditModeListener() {
		public void editModeChanged() {
			updateEditMode();
		}
	};

	/** Create a new SONAR object form */
	protected SonarObjectForm(String prefix, Session s, T p) {
		super(prefix + p.getName());
		proxy = p;
		session = s;
		state = s.getSonarState();
		watcher = new ProxyWatcher<T>(getTypeCache(), this, true);
		setLayout(new BorderLayout());
		setBackground(LIGHTER_GRAY);
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		super.initialize();
		watcher.initialize();
		watcher.setProxy(proxy);
		updateEditMode();
		doUpdateAttribute(null);
		session.addEditModeListener(edit_lsnr);
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		session.removeEditModeListener(edit_lsnr);
		watcher.dispose();
		super.dispose();
	}

	/** Get the SONAR type cache */
	abstract protected TypeCache<T> getTypeCache();

	/** Update the edit mode */
	protected void updateEditMode() { }

	/** Update one attribute on the form */
	@Override
	public final void update(T p, String a) {
		if(proxy == p)
			doUpdateAttribute(a);
	}

	/** Update one attribute on the form */
	abstract protected void doUpdateAttribute(String a);

	/** Clear the proxy view */
	@Override
	public final void clear() {
		close(session.getDesktop());
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
