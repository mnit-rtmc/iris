/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera;

import java.util.ArrayList;
import us.mn.state.dot.tms.PlayList;
import us.mn.state.dot.tms.PlayListHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for play lists.
 *
 * @author Douglas Lau
 */
public class PlayListModel extends ProxyTableModel<PlayList> {

	/** Create a proxy descriptor */
	static private ProxyDescriptor<PlayList> descriptor(final Session s) {
		return new ProxyDescriptor<PlayList>(
			s.getSonarState().getCamCache().getPlayLists(),
			true,	/* has_properties */
			true,	/* has_create_delete */
			false	/* has_name */
		) {
			@Override
			public PlayListProperties createPropertiesForm(
				PlayList pl)
			{
				return new PlayListProperties(s, pl);
			}
		};
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<PlayList>> createColumns() {
		ArrayList<ProxyColumn<PlayList>> cols =
			new ArrayList<ProxyColumn<PlayList>>(2);
		cols.add(new ProxyColumn<PlayList>("play.list", 120) {
			public Object getValueAt(PlayList pl) {
				return pl.getName();
			}
		});
		cols.add(new ProxyColumn<PlayList>("play.list.num", 120) {
			public Object getValueAt(PlayList pl) {
				return pl.getNum();
			}
		});
		return cols;
	}

	/** Create a new play list table model */
	public PlayListModel(Session s) {
		super(s, descriptor(s), 12);
	}

	/** Create a new play list */
	@Override
	public void createObject(String n) {
		// Ignore name given to us
		String name = createUniqueName();
		if (name != null)
			descriptor.cache.createObject(name);
	}

	/** Create a unique play list name */
	private String createUniqueName() {
		for (int i = PlayList.NUM_MIN; i <= PlayList.NUM_MAX; i++) {
			String n = "PL_" + i;
			if (PlayListHelper.lookup(n) == null)
				return n;
		}
		assert false;
		return null;
	}
}
