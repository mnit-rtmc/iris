/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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

import java.util.ArrayList;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for sign config table form.
 *
 * @author Douglas Lau
 */
public class SignConfigModel extends ProxyTableModel<SignConfig> {

	/** Create a proxy descriptor */
	static private ProxyDescriptor<SignConfig> descriptor(final Session s) {
		return new ProxyDescriptor<SignConfig>(
			s.getSonarState().getDmsCache().getSignConfigs(),
			true,	/* has_properties */
			false,	/* has_create_delete */
			false	/* has_name */
		) {
			@Override
			public SignConfigProperties createPropertiesForm(
				SignConfig sc)
			{
				return new SignConfigProperties(s, sc);
			}
		};
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<SignConfig>> createColumns() {
		ArrayList<ProxyColumn<SignConfig>> cols =
			new ArrayList<ProxyColumn<SignConfig>>(3);
		cols.add(new ProxyColumn<SignConfig>("device.name", 100) {
			public Object getValueAt(SignConfig d) {
				return d.getName();
			}
		});
		cols.add(new ProxyColumn<SignConfig>("dms.pixel.width", 80,
			Integer.class)
		{
			public Object getValueAt(SignConfig sc) {
				return sc.getPixelWidth();
			}
		});
		cols.add(new ProxyColumn<SignConfig>("dms.pixel.height", 80,
			Integer.class)
		{
			public Object getValueAt(SignConfig sc) {
				return sc.getPixelHeight();
			}
		});
		return cols;
	}

	/** Create a new sign config table model */
	public SignConfigModel(Session s) {
		super(s, descriptor(s), 16);
	}
}
