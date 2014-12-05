/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2014  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.client.widget.AbstractForm;

/**
 * A proxy table form is a simple widget for displaying a table of proxy
 * objects.
 *
 * @author Douglas Lau
 */
public class ProxyTableForm<T extends SonarObject> extends AbstractForm {

	/** Proxy table panel */
	protected final ProxyTablePanel<T> panel;

	/** Create a new proxy table form */
	public ProxyTableForm(String t, ProxyTableModel<T> m) {
		super(t);
		panel = new ProxyTablePanel<T>(m);
	}

	/** Create a new proxy table form */
	public ProxyTableForm(String t, ProxyTablePanel<T> p) {
		super(t);
		panel = p;
	}

	/** Initialize the form */
	@Override
	public void initialize() {
		super.initialize();
		panel.initialize();
		add(panel);
	}

	/** Dispose of the form */
	@Override
	public void dispose() {
		panel.dispose();
		super.dispose();
	}
}
