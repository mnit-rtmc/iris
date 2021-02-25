/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2021  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A panel for selecting DMS within a sign group.
 *
 * @author Douglas Lau
 */
public class SignGroupPanel extends ProxyTablePanel<SignGroup> {

	/** User session */
	private final Session session;

	/** DMS table panel */
	final ProxyTablePanel<DMS> dms_panel;

	/** Proxy view */
	private final ProxyView<SignGroup> view = new ProxyView<SignGroup>() {
		@Override public void enumerationComplete() { }
		@Override public void update(SignGroup sg, String a) {
			if (a == null)
				setSignGroup(sg);
		}
		@Override public void clear() {
			setSignGroup(null);
		}
	};

	/** Set the sign group */
	private void setSignGroup(SignGroup sg) {
		dms_panel.setModel(new DmsGroupModel(session, sg));
	}

	/** Proxy watcher */
	private final ProxyWatcher<SignGroup> watcher;

	/** Select a sign group proxy */
	@Override
	protected void selectProxy() {
		super.selectProxy();
		watcher.setProxy(getSelectedProxy());
	}

	/** Create the sign group panel */
	public SignGroupPanel(Session s) {
		super(new SignGroupModel(s));
		session = s;
		TypeCache<SignGroup> cache =
			s.getSonarState().getDmsCache().getSignGroups();
		watcher = new ProxyWatcher<SignGroup>(cache, view, false);
		dms_panel = new ProxyTablePanel<DMS>(
			new DmsGroupModel(s, null));
	}

	/** Initialize the panel */
	@Override
	public void initialize() {
		super.initialize();
		watcher.initialize();
		dms_panel.initialize();
	}

	/** Dispose of the panel */
	@Override
	public void dispose() {
		view.clear();
		watcher.dispose();
		dms_panel.dispose();
		super.dispose();
	}
}
