/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014  AHMCT, University of California
 * Copyright (C) 2016-2017  Minnesota Department of Transportation
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

import javax.swing.JComboBox;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.client.Session;

/**
 * SignGroup combo box.
 *
 * @author Travis Swanston
 */
public class SignGroupComboBox extends JComboBox<SignGroup> {

	/** SignGroup combo box model */
	private SignGroupComboBoxModel model;

	/** Create a new SignGroup combo box */
	public SignGroupComboBox(Session s) {
		TypeCache<SignGroup> sign_groups = s.getSonarState()
			.getDmsCache().getSignGroups();
		model = SignGroupComboBoxModel.create(sign_groups);
		setModel(model);
	}

	/** Dispose of this combo box */
	public void dispose() {
	}

	/** Set enabled/disabled */
	@Override
	public void setEnabled(boolean enable) {
		super.setEnabled(enable);
		if (!enable)
			setSelectedItem(null);
	}
}
