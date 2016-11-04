/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import java.util.ArrayList;
import java.util.HashMap;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyDescriptor;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for r_node detectors
 *
 * @author Douglas Lau
 */
public class R_NodeDetectorModel extends ProxyTableModel<Detector> {

	/** Create a proxy descriptor */
	static public ProxyDescriptor<Detector> descriptor(Session s) {
		return new ProxyDescriptor<Detector>(
			s.getSonarState().getDetCache().getDetectors(), false
		);
	}

	/** Create the columns in the model */
	@Override
	protected ArrayList<ProxyColumn<Detector>> createColumns() {
		ArrayList<ProxyColumn<Detector>> cols =
			new ArrayList<ProxyColumn<Detector>>(2);
		cols.add(new ProxyColumn<Detector>("detector", 60) {
			public Object getValueAt(Detector d) {
				return d.getName();
			}
		});
		cols.add(new ProxyColumn<Detector>("detector.label", 150) {
			public Object getValueAt(Detector d) {
				return DetectorHelper.getLabel(d);
			}
		});
		return cols;
	}

	/** R_Node in question */
	private final R_Node r_node;

	/** Create a new r_node detector table model */
	public R_NodeDetectorModel(Session s, R_Node n) {
		super(s, descriptor(s), 16);
		r_node = n;
	}

	/** Check if a proxy is included in the list */
	@Override
	protected boolean check(Detector proxy) {
		return proxy.getR_Node() == r_node;
	}

	/** Create a detector with the given name */
	@Override
	public void createObject(String name) {
		String n = name.trim();
		if (n.length() > 0 && canAdd(n)) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("r_node", r_node);;
			descriptor.cache.createObject(n, attrs);
		}
	}

	/** Transfer a detector to the r_node */
	public void transfer(Detector det) {
		det.setR_Node(r_node);
	}
}
