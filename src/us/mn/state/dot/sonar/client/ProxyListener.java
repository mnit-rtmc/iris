/*
 * SONAR -- Simple Object Notification And Replication
 * Copyright (C) 2006-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.sonar.client;

import java.util.EventListener;
import us.mn.state.dot.sonar.SonarObject;

/**
 * Listener for proxy updates.
 *
 * @author Douglas Lau
 */
public interface ProxyListener<T extends SonarObject> extends EventListener {

	/** A new proxy has been added */
	void proxyAdded(T proxy);

	/** All proxies have been enumerated */
	void enumerationComplete();

	/** A proxy has been removed */
	void proxyRemoved(T proxy);

	/** A proxy has been changed */
	void proxyChanged(T proxy, String a);
}
