/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023       SRF Consulting Group
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
package us.mn.state.dot.tms.client.rwis;

import java.util.ArrayList;

import us.mn.state.dot.tms.RwisSign;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for RwisSign table form.
 *
 * @author John L. Stanley - SRF Consulting
 */
public class RwisSignModel extends ProxyTableModel<RwisSign> {

	/** Create a proxy descriptor */
	static private ProxyDescriptor<RwisSign> descriptor(final Session s) {
		return new ProxyDescriptor<RwisSign>(
			s.getSonarState().getRwisSigns(),
			false,	/* has_properties */
			false,	/* has_create_delete */
			false	/* has_name */
		) {
//			@Override
//			public SignConfigProperties createPropertiesForm(
//				RwisSign rwisSign)
//			{
//				DMS dms = DMSHelper.lookup(rwisSign.getName());
//				if (dms != null)
//					return new SignConfigProperties(s, dms);
//				return null;
//			}
		};
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<RwisSign>> createColumns() {
		ArrayList<ProxyColumn<RwisSign>> cols =
			new ArrayList<ProxyColumn<RwisSign>>(3);
		cols.add(new ProxyColumn<RwisSign>("rwis.status.name", 100) {
			public Object getValueAt(RwisSign rwisSign) {
				return rwisSign.getName();
			}
		});
		cols.add(new ProxyColumn<RwisSign>("rwis.status.conditions", 380,
			String.class)
		{
			public Object getValueAt(RwisSign rwisSign) {
				return rwisSign.getRwisConditions();
			}
		});
		cols.add(new ProxyColumn<RwisSign>("rwis.status.selected.msg", 100,
			String.class)
		{
			public Object getValueAt(RwisSign rwisSign) {
				return rwisSign.getMsgPattern();
			}
		});
		return cols;
	}

	/** Create a new sign config table model */
	public RwisSignModel(Session s) {
		super(s, descriptor(s), 16);
	}
}
