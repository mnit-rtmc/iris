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
package us.mn.state.dot.tms.client.comm;

import java.util.Comparator;
import us.mn.state.dot.tms.CommConfig;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;

/**
 * A CommConfigListModel is a ProxyListModel sorted by description.
 *
 * @author Douglas Lau
 */
public class CommConfigListModel extends ProxyListModel<CommConfig> {

	/** Create a new comm config list model */
	public CommConfigListModel(Session s) {
		super(s.getSonarState().getConCache().getCommConfigs());
	}

	/** Get a comm config comparator */
	@Override
	protected Comparator<CommConfig> comparator() {
		return new Comparator<CommConfig>() {
			public int compare(CommConfig cc0, CommConfig cc1) {
				String d0 = cc0.getDescription().toLowerCase();
				String d1 = cc1.getDescription().toLowerCase();
				return d0.compareTo(d1);
			}
		};
	}
}
