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

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * This is a form for viewing sign configurations.
 *
 * @author Douglas Lau
 */
public class SignConfigForm extends SonarObjectForm<SignConfig> {

	/** Configuration panel */
	private final PropConfiguration config_pnl;

	/** Create a new sign configuration form */
	public SignConfigForm(Session s, SignConfig sc) {
		super(I18N.get("dms.config") + ": ", s, sc);
		config_pnl = new PropConfiguration(s, sc);
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<SignConfig> getTypeCache() {
		return state.getDmsCache().getSignConfigs();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		config_pnl.initialize();
		add(config_pnl);
		super.initialize();
	}

	/** Update the edit mode */
	@Override
	protected void updateEditMode() {
		config_pnl.updateEditMode();
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		config_pnl.updateAttribute(a);
	}
}
