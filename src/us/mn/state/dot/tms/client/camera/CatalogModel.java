/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Catalog;
import us.mn.state.dot.tms.CatalogHelper;
import us.mn.state.dot.tms.PlayList;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for catalogs.
 *
 * @author Douglas Lau
 */
public class CatalogModel extends ProxyTableModel<Catalog> {

	/** Create a proxy descriptor */
	static private ProxyDescriptor<Catalog> descriptor(final Session s) {
		return new ProxyDescriptor<Catalog>(
			s.getSonarState().getCamCache().getCatalogs(),
			true,	/* has_properties */
			true,	/* has_create_delete */
			false	/* has_name */
		) {
			@Override
			public CatalogProperties createPropertiesForm(Catalog c)
			{
				return new CatalogProperties(s, c);
			}
		};
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Catalog>> createColumns() {
		ArrayList<ProxyColumn<Catalog>> cols =
			new ArrayList<ProxyColumn<Catalog>>(3);
		cols.add(new ProxyColumn<Catalog>("catalog", 120) {
			public Object getValueAt(Catalog c) {
				return c.getName();
			}
		});
		cols.add(new ProxyColumn<Catalog>("catalog.seq", 120) {
			public Object getValueAt(Catalog c) {
				return c.getSeqNum();
			}
		});
		cols.add(new ProxyColumn<Catalog>("catalog.desc", 200) {
			public Object getValueAt(Catalog c) {
				return c.getDescription();
			}
			public boolean isEditable(Catalog c) {
				return canWrite(c);
			}
			public void setValueAt(Catalog c, Object value) {
				String v = value.toString().trim();
				c.setDescription((v.length() > 0) ? v : null);
			}
		});
		return cols;
	}

	/** Create a new catalog table model */
	public CatalogModel(Session s) {
		super(s, descriptor(s), 12);
	}

	/** Create a new catalog */
	@Override
	public void createObject(String n) {
		// Ignore name given to us
		String name = createUniqueName();
		if (name != null)
			descriptor.cache.createObject(name);
	}

	/** Create a unique catalog name */
	private String createUniqueName() {
		for (int i = PlayList.NUM_MIN; i <= PlayList.NUM_MAX; i++) {
			String n = "CAT_" + i;
			if (CatalogHelper.lookup(n) == null)
				return n;
		}
		assert false;
		return null;
	}
}
