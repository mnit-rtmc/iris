/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2024  Minnesota Department of Transportation
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
import java.util.HashMap;
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
			new ArrayList<ProxyColumn<PlayList>>(4);
		cols.add(new ProxyColumn<PlayList>("play.list", 90) {
			public Object getValueAt(PlayList pl) {
				return pl.getName();
			}
		});
		cols.add(new ProxyColumn<PlayList>("play.list.meta", 50,
			Boolean.class)
		{
			public Object getValueAt(PlayList pl) {
				return pl.getMeta();
			}
		});
		cols.add(new ProxyColumn<PlayList>("play.list.seq_num", 80,
			Integer.class)
		{
			public Object getValueAt(PlayList pl) {
				return pl.getSeqNum();
			}
			public boolean isEditable(PlayList pl) {
				return canWrite(pl);
			}
			public void setValueAt(PlayList pl, Object value) {
				Integer sn = (value instanceof Integer)
					? (Integer) value
					: null;
				pl.setSeqNum(sn);
			}
		});
		cols.add(new ProxyColumn<PlayList>("play.list.notes", 300) {
			public Object getValueAt(PlayList pl) {
				return pl.getNotes();
			}
			public boolean isEditable(PlayList pl) {
				return canWrite(pl);
			}
			public void setValueAt(PlayList pl, Object value) {
				String v = value.toString().trim();
				pl.setNotes((v.length() > 0) ? v : null);
			}
		});
		return cols;
	}

	/** Create a new play list table model */
	public PlayListModel(Session s) {
		super(s, descriptor(s), 12);
	}

	/** Create a new play list */
	public void createObject(boolean meta) {
		String name = createUniqueName(meta);
		if (name != null) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("meta", meta);
			descriptor.cache.createObject(name, attrs);
		}
	}

	/** Create a unique play list name */
	private String createUniqueName(boolean meta) {
		String prefix = (meta) ? "CAT_" : "PL_";
		for (int i = PlayList.NUM_MIN; i <= PlayList.NUM_MAX; i++) {
			String n = prefix + i;
			if (PlayListHelper.lookup(n) == null)
				return n;
		}
		assert false;
		return null;
	}
}
