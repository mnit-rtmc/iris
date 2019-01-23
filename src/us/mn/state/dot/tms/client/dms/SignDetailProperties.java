/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016-2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.SignDetail;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * This is a form for viewing sign detail properties.
 *
 * @author Douglas Lau
 */
public class SignDetailProperties extends SonarObjectForm<SignDetail> {

	/** Detail panel */
	private final PropDetail detail_pnl;

	/** Create a new sign detail properties form */
	public SignDetailProperties(Session s, SignDetail sd) {
		super(I18N.get("dms.detail") + ": ", s, sd);
		detail_pnl = new PropDetail(s, sd);
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<SignDetail> getTypeCache() {
		return state.getDmsCache().getSignDetails();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		detail_pnl.initialize();
		add(detail_pnl);
		super.initialize();
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		detail_pnl.updateAttribute(a);
	}
}
