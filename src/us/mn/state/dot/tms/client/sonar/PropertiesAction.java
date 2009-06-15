/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.sonar;

import javax.swing.Action;
import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.client.proxy.ProxyAction;

/**
 * Action to access the properties of a SONAR map object.
 *
 * @author Douglas Lau
 */
abstract public class PropertiesAction<T extends SonarObject>
	extends ProxyAction
{
	/** Create a new properties action */
	public PropertiesAction(T p) {
		super(p);
		putValue(Action.NAME, "Properties");
		putValue(Action.SHORT_DESCRIPTION, "Access object properties");
		putValue(Action.LONG_DESCRIPTION, "Access the " +
			p.getTypeName() + " properties page");
	}
}
