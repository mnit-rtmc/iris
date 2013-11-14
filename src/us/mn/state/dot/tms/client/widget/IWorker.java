/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2013  Minnesota Department of Transportation
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

import javax.swing.SwingWorker;

/**
 * IRIS worker, which handles some of the icky details of SwingWorkers.
 *
 * @author Douglas Lau
 */
abstract public class IWorker<T> extends SwingWorker<T, Void> {

	/** Get the result of the worker */
	protected final T getResult() {
		try {
			return get();
		}
		catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
