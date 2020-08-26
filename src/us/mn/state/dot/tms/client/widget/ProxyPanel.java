/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.widget;

import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.client.EditModeListener;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;

/**
 * A panel for editing the properties of a proxy object.
 *
 * @author Douglas Lau
 */
abstract public class ProxyPanel<T extends SonarObject> extends IPanel {

	/** Proxy action */
	abstract protected class ProxyAction extends IAction {
		protected ProxyAction(String text_id) {
			super(text_id);
		}
		@Override
		protected final void doActionPerformed(ActionEvent e) {
			T p = proxy;
			if (p != null)
				do_perform(p);
		}
		abstract protected void do_perform(T p);
		@Override
		protected final void doUpdateSelected() {
			T p = proxy;
			if (p != null)
				do_selected(p);
		}
		protected void do_selected(T p) { }
	}

	/** Proxy change */
	abstract protected class ProxyChange implements ChangeListener {
		public void stateChanged(ChangeEvent e) {
			T p = proxy;
			if (p != null)
				do_perform(p);
		}
		abstract protected void do_perform(T p);
	}

	/** Proxy focus */
	abstract protected class ProxyFocus extends FocusAdapter {
		@Override
		public void focusLost(FocusEvent e) {
			T p = proxy;
			if (p != null)
				do_perform(p);
		}
		abstract protected void do_perform(T p);
	}

	/** User session */
	private final Session session;

	/** Proxy watcher */
	private final ProxyWatcher<T> watcher;

	/** Proxy view */
	private final ProxyView<T> view = new ProxyView<T>() {
		public void enumerationComplete() { }
		public void update(T p, String a) {
			if (a == null) {
				proxy = p;
				updateEditMode(p);
			}
			updateAttrib(p, a);
		}
		public void clear() {
			proxy = null;
			clearView();
		}
	};

	/** Edit mode listener */
	private final EditModeListener edit_lsnr = new EditModeListener() {
		public void editModeChanged() {
			updateEditMode(proxy);
		}
	};

	/** Proxy being edited */
	private T proxy;

	/** Set the proxy */
	public final void setProxy(T p) {
		watcher.setProxy(p);
	}

	/** Create the proxy panel */
	public ProxyPanel(Session s, TypeCache<T> cache) {
		session = s;
		watcher = new ProxyWatcher<T>(cache, view, false);
	}

	/** Initialize the panel */
	@Override
	public void initialize() {
		super.initialize();
		watcher.initialize();
		view.clear();
		session.addEditModeListener(edit_lsnr);
	}

	/** Dispose of the panel */
	@Override
	public void dispose() {
		view.clear();
		session.removeEditModeListener(edit_lsnr);
		watcher.dispose();
		super.dispose();
	}

	/** Update the edit mode */
	abstract protected void updateEditMode(T p);

	/** Update one attribute */
	abstract protected void updateAttrib(T p, String a);

	/** Clear the view */
	abstract protected void clearView();
}
