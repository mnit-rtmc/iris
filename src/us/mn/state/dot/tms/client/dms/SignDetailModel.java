/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.SignDetail;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for sign detail table form.
 *
 * @author Douglas Lau
 */
public class SignDetailModel extends ProxyTableModel<SignDetail> {

	/** Create a proxy descriptor */
	static private ProxyDescriptor<SignDetail> descriptor(final Session s) {
		return new ProxyDescriptor<SignDetail>(
			s.getSonarState().getDmsCache().getSignDetails(),
			true,	/* has_properties */
			false,	/* has_create_delete */
			false	/* has_name */
		) {
			@Override
			public SignDetailProperties createPropertiesForm(
				SignDetail sd)
			{
				return new SignDetailProperties(s, sd);
			}
		};
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<SignDetail>> createColumns() {
		ArrayList<ProxyColumn<SignDetail>> cols =
			new ArrayList<ProxyColumn<SignDetail>>(3);
		cols.add(new ProxyColumn<SignDetail>("device.name", 100) {
			public Object getValueAt(SignDetail d) {
				return d.getName();
			}
		});
		cols.add(new ProxyColumn<SignDetail>("dms.software.make", 80,
			Integer.class)
		{
			public Object getValueAt(SignDetail sd) {
				return sd.getSoftwareMake();
			}
		});
		cols.add(new ProxyColumn<SignDetail>("dms.software.model", 80,
			Integer.class)
		{
			public Object getValueAt(SignDetail sd) {
				return sd.getSoftwareModel();
			}
		});
		return cols;
	}

	/** Create a new sign detail table model */
	public SignDetailModel(Session s) {
		super(s, descriptor(s), 16);
	}
}
