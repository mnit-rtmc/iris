/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2024  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.gps;

import java.awt.GridLayout;
import us.mn.state.dot.tms.Gps;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying and editing GPS
 *
 * @author Douglas Lau
 */
public class GpsForm extends ProxyTableForm<Gps> {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(Gps.SONAR_TYPE);
	}

	/** Gps panel */
	private final GpsPanel gps_pnl;

	/** Create a new GPS form */
	public GpsForm(Session s) {
		super(I18N.get("gps"), new GpsTablePanel(s));
		gps_pnl = ((GpsTablePanel) panel).gps_pnl;
	}

	/** GPS table panel */
	static private class GpsTablePanel extends ProxyTablePanel<Gps> {
		private final GpsPanel gps_pnl;
		private GpsTablePanel(Session s) {
			super(new GpsModel(s));
			gps_pnl = new GpsPanel(s);
		}
		@Override
		public void initialize() {
			super.initialize();
			gps_pnl.initialize();
		}
		@Override
		public void dispose() {
			gps_pnl.dispose();
			super.dispose();
		}
		@Override
		protected void selectProxy() {
			super.selectProxy();
			gps_pnl.setGps(getSelectedProxy());
		}
	}

	/** Initialize the form */
	@Override
	public void initialize() {
		super.initialize();
		setLayout(new GridLayout(1, 2));
		add(gps_pnl);
	}
}
