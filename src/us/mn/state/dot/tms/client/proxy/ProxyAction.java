/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2013  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.client.widget.IAction;

/**
 * ProxyAction is an abstract action which is associated with one type
 * of SONAR proxy. It also passes off calls to actionPerformed to a
 * worker thread so SONAR calls never happen on the Swing thread.
 *
 * @author Douglas Lau
 */
abstract public class ProxyAction<T extends SonarObject> extends IAction {

	/** Sonar proxy */
	protected final T proxy;

	/** Create a new proxy action */
	protected ProxyAction(String tid, T p) {
		super(tid);
		proxy = p;
		setEnabled(p != null);
	}
}
