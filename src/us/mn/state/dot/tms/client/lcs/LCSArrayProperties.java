/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.lcs;

import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.TmsConnection;
import us.mn.state.dot.tms.client.toast.SonarObjectForm;

/**
 * LCSArrayProperties is a dialog for editing the properties of an LCS array.
 *
 * @author Douglas Lau
 */
public class LCSArrayProperties extends SonarObjectForm<LCSArray> {

	/** Frame title */
	static private final String TITLE = "LCS Array: ";

	/** SONAR state */
	protected final SonarState state;

	/** Create a new lane control signal properties form */
	public LCSArrayProperties(TmsConnection tc, LCSArray proxy) {
		super(TITLE, tc, proxy);
		state = tc.getSonarState();
	}

	/** Get the SONAR type cache */
	protected TypeCache<LCSArray> getTypeCache() {
		return state.getLCSArrays();
	}

	/** Initialize the widgets on the form */
	protected void initialize() {
		super.initialize();
		JTabbedPane tab = new JTabbedPane();
		tab.add("Setup", createSetupPanel());
		tab.add("Timing Plans", createTimingPlanPanel());
		tab.add("Status", createStatusPanel());
		add(tab);
		updateAttribute(null);
		setBackground(Color.LIGHT_GRAY);
	}

	/** Create setup panel */
	protected JPanel createSetupPanel() {
		JPanel panel = new JPanel();
		return panel;
	}
}
