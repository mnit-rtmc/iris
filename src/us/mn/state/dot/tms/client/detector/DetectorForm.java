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

import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;
import us.mn.state.dot.tms.client.toast.FormPanel;

/**
 * A form for displaying and editing detectors
 *
 * @author Douglas Lau
 */
public class DetectorForm extends ProxyTableForm<Detector> {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(Detector.SONAR_TYPE);
	}

	/** Detector redirector */
	protected final DetectorRedirector detector = new DetectorRedirector();

	/** Detector panel */
	protected final DetectorPanel det_pnl;

	/** Create a new detector form */
	public DetectorForm(Session s) {
		super("Detectors", new DetectorModel(s));
		det_pnl = new DetectorPanel(s, detector);
		det_pnl.initialize();
	}

	/** Dispose of the form */
	protected void dispose() {
		det_pnl.dispose();
		super.dispose();
	}

	/** Add the table to the panel */
	protected void addTable(FormPanel panel) {
		panel.setFill();
		panel.setWidth(1);
		panel.add(table);
		panel.setFill();
		panel.addRow(det_pnl);
	}

	/** Get the row height */
	protected int getRowHeight() {
		return 20;
	}

	/** Select a new proxy */
	protected void selectProxy() {
		super.selectProxy();
		detector.setDetector(getSelectedProxy());
		det_pnl.doUpdateAttribute(null);
	}
}
