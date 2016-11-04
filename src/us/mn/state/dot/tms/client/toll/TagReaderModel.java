/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toll;

import java.util.ArrayList;
import us.mn.state.dot.tms.TagReader;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for tag readers.
 *
 * @author Douglas Lau
 */
public class TagReaderModel extends ProxyTableModel<TagReader> {

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<TagReader>> createColumns() {
		ArrayList<ProxyColumn<TagReader>> cols =
			new ArrayList<ProxyColumn<TagReader>>(2);
		cols.add(new ProxyColumn<TagReader>("tag_reader", 120) {
			public Object getValueAt(TagReader tr) {
				return tr.getName();
			}
		});
		cols.add(new ProxyColumn<TagReader>("location", 300) {
			public Object getValueAt(TagReader tr) {
				return GeoLocHelper.getDescription(
					tr.getGeoLoc());
			}
		});
		return cols;
	}

	/** Create a new tag reader table model */
	public TagReaderModel(Session s) {
		super(s, TagReaderManager.descriptor(s));
	}

	/** Get the visible row count */
	@Override
	public int getVisibleRowCount() {
		return 12;
	}
}
