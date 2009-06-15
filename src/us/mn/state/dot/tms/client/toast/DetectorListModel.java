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
package us.mn.state.dot.tms.client.toast;

import java.util.Comparator;
import java.util.TreeSet;
import javax.swing.Icon;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.client.proxy.ProxyManager;
import us.mn.state.dot.tms.client.proxy.StyleListModel;

/**
 * A list model for detector styles.
 *
 * @author Douglas lau
 */
public class DetectorListModel extends StyleListModel<Detector> {

	/** Create an empty set of proxies */
	protected TreeSet<Detector> createProxySet() {
		return new TreeSet<Detector>(
			new Comparator<Detector>() {
				public int compare(Detector a, Detector b) {
					return DetectorHelper.compare(a, b);
				}
			}
		);
	}

	/** Create a new detector list model */
	public DetectorListModel(ProxyManager<Detector> m, String n, Icon l) {
		super(m, n, l);
	}
}
