/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2010  Minnesota Department of Transportation
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

import java.util.Comparator;
import java.util.TreeSet;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DetectorHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.toast.ControllerForm;

/**
 * Table model for detectors
 *
 * @author Douglas Lau
 */
public class DetectorModel extends ProxyTableModel<Detector> {

	/** Create the columns in the model */
	protected ProxyColumn[] createColumns() {
	    // NOTE: half-indent to declare array
	    return new ProxyColumn[] {
		new ProxyColumn<Detector>("Detector", 60) {
			public Object getValueAt(Detector d) {
				return d.getName();
			}
		},
		new ProxyColumn<Detector>("Label", 150) {
			public Object getValueAt(Detector d) {
				return DetectorHelper.getLabel(d);
			}
		}
	    };
	}

	/** Create a new detector table model */
	public DetectorModel(Session s) {
		super(s, s.getSonarState().getDetCache().getDetectors());
	}

	/** Create an empty set of proxies */
	protected TreeSet<Detector> createProxySet() {
		return new TreeSet<Detector>(
			new Comparator<Detector>() {
				public int compare(Detector a, Detector b) {
					return DetectorHelper.compare(a, b);
				}
			}
		);
	}

	/** Get the SONAR type name */
	protected String getSonarType() {
		return Detector.SONAR_TYPE;
	}

	/** Create a controller form for one detector */
	protected ControllerForm createControllerForm(Detector d) {
		Controller c = d.getController();
		if(c != null)
			return new ControllerForm(session, c);
		else
			return null;
	}

	/** Determine if a controller form is available */
	public boolean hasController() {
		return true;
	}

	/** Determine if delete button is available */
	public boolean hasDelete() {
		return false;
	}
}
