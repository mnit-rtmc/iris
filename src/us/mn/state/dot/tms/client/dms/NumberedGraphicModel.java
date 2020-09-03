/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2020  Minnesota Department of Transportation
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

import java.util.Comparator;
import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;

/**
 * A NumberedGraphicModel is a ProxyListModel sorted by g_number.
 *
 * @author Douglas Lau
 */
public class NumberedGraphicModel extends ProxyListModel<Graphic> {

	/** Create a new numbered graphic model */
	static public NumberedGraphicModel create(Session s) {
		NumberedGraphicModel mdl = new NumberedGraphicModel(s);
		mdl.initialize();
		return mdl;
	}

	/** Create a new numbered graphic model */
	private NumberedGraphicModel(Session s) {
		super(s.getSonarState().getGraphics());
	}

	/** Get a graphic comparator */
	@Override
	protected Comparator<Graphic> comparator() {
		return new Comparator<Graphic>() {
			public int compare(Graphic g0, Graphic g1) {
				Integer n0 = g0.getGNumber();
				Integer n1 = g1.getGNumber();
				return n0.compareTo(n1);
			}
		};
	}
}
