/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.detector;

import java.util.ArrayList;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.StationHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for stations.
 *
 * @author Douglas Lau
 */
public class StationModel extends ProxyTableModel<Station> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<Station> descriptor(Session s) {
		return new ProxyDescriptor<Station>(
			s.getSonarState().getDetCache().getStations(),
			true,	/* has_properties */
			false,	/* has_create_delete */
			false	/* has_name */
		);
	}

	/** R_Node selection model */
	private final ProxySelectionModel<R_Node> sel_model;

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Station>> createColumns() {
		ArrayList<ProxyColumn<Station>> cols =
			new ArrayList<ProxyColumn<Station>>(2);
		cols.add(new ProxyColumn<Station>("detector.station", 60) {
			public Object getValueAt(Station s) {
				return s.getName();
			}
		});
		cols.add(new ProxyColumn<Station>("detector.label", 150) {
			public Object getValueAt(Station s) {
				return StationHelper.getLabel(s);
			}
		});
		return cols;
	}

	/** Create a new station table model */
	public StationModel(Session s) {
		super(s, descriptor(s));
		sel_model = s.getR_NodeManager().getSelectionModel();
	}

	/** Show the properties form */
	@Override
	public void showPropertiesForm(Station proxy) {
		sel_model.setSelected(proxy.getR_Node());
	}

	/** Get the row height */
	@Override
	public int getRowHeight() {
		return 20;
	}
}
