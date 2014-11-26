/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
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

import java.awt.GridLayout;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.utils.I18N;

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

	/** Detector panel */
	private final DetectorPanel det_pnl;

	/** Create a new detector form */
	public DetectorForm(Session s) {
		super(I18N.get("detector.plural"), new DetTablePanel(s));
		det_pnl = ((DetTablePanel)panel).det_pnl;
	}

	/** Detector table panel */
	static private class DetTablePanel extends ProxyTablePanel<Detector> {
		private final DetectorPanel det_pnl;
		private DetTablePanel(Session s) {
			super(new DetectorModel(s));
			det_pnl = new DetectorPanel(s, true);
		}
		@Override
		public void initialize() {
			super.initialize();
			det_pnl.initialize();
		}
		@Override
		public void dispose() {
			det_pnl.dispose();
			super.dispose();
		}
		@Override
		protected void selectProxy() {
			super.selectProxy();
			det_pnl.setDetector(getSelectedProxy());
		}
	}

	/** Initialize the form */
	@Override
	public void initialize() {
		super.initialize();
		setLayout(new GridLayout(1, 2));
		add(det_pnl);
	}
}
