/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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

/**
 * An interface for listening to proxy selection events.
 *
 * @author Douglas Lau
 */
public interface ProxySelectionListener<T extends SonarObject> {

	/** Fired when a proxy is added to the selection */
	void selectionAdded(T proxy);

	/** Fired when a proxy is removed from the selection */
	void selectionRemoved(T proxy);
}
