/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2012  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;

/**
 * Table model for detectors
 *
 * @author Douglas Lau
 */
public class DetectorModel extends ProxyTableModel<Detector> {

	/** Create the columns in the model */
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

	/** Create a new detector table model */
	public DetectorModel(Session s) {
		super(s, s.getSonarState().getDetCache().getDetectors());
	}

	/** Get the count of rows in the table */
	public int getRowCount() {
		synchronized(proxies) {
			return proxies.size();
		}
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return Detector.SONAR_TYPE;
	}

	/** Determine if delete button is available */
	public boolean hasDelete() {
		return false;
	}
}
