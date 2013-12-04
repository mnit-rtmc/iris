/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2013  Minnesota Department of Transportation
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

import java.awt.event.ActionEvent;
import us.mn.state.dot.sonar.SonarObject;

/**
 * Action to access the properties of a SONAR map object.
 *
 * @author Douglas Lau
 */
public class PropertiesAction<T extends SonarObject> extends ProxyAction {

	/** Proxy manager */
	private final ProxyManager<T> manager;

	/** Proxy object (compile error trying to use ProxyAction.proxy) */
	private final T _proxy;

	/** Create a new properties action */
	public PropertiesAction(ProxyManager<T> m, T p) {
		super("device.properties", p);
		manager = m;
		_proxy = p;
	}

	/** Perform action */
	@Override
	protected void doActionPerformed(ActionEvent e) {
		manager.showPropertiesForm(_proxy);
	}
}
