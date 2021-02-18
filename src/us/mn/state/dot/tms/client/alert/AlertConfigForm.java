/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.alert;

import java.awt.FlowLayout;
import us.mn.state.dot.tms.AlertConfig;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying and editing alert configurations.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class AlertConfigForm extends ProxyTableForm<AlertConfig> {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.isWritePermitted(AlertConfig.SONAR_TYPE);
	}

	/** Create a new alert config form */
	public AlertConfigForm(Session s) {
		super(I18N.get("alert.config.plural"), new TablePanel(s));
	}

	/** Alert config table panel */
	static private class TablePanel extends ProxyTablePanel<AlertConfig> {
		private final AlertConfigPanel pnl;
		private TablePanel(Session s) {
			super(new AlertConfigModel(s));
			pnl = new AlertConfigPanel(s);
		}
		@Override public void initialize() {
			super.initialize();
			pnl.initialize();
		}
		@Override public void dispose() {
			pnl.dispose();
			super.dispose();
		}
		@Override protected void selectProxy() {
			super.selectProxy();
			pnl.setAlertConfig(getSelectedProxy());
		}
	}

	/** Initialize the form */
	@Override
	public void initialize() {
		super.initialize();
		setLayout(new FlowLayout());
		add(((TablePanel) panel).pnl);
	}
}
