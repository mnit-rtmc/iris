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

import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.LinkedList;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.LCSHelper;

/**
 * Selector for lane-use control siganl arrays.
 *
 * @author Douglas Lau
 */
public class IndicationSelector extends JPanel {

	/** Font for "L" and "R" labels */
	static protected final Font FONT = new Font(null, Font.BOLD, 24);

	/** List of combo boxes for selecting lane-use indications */
	protected final LinkedList<JComboBox> indications =
		new LinkedList<JComboBox>();

	/** Create a new indication selector */
	public IndicationSelector() {
		super(new GridLayout(1, 0, 1, 0));
	}

	/** Dispose of the indication selector */
	public void dispose() {
		removeAll();
		indications.clear();
	}

	/** Set the LCS array */
	public void setLCSArray(LCSArray lcs_array) {
		removeAll();
		indications.clear();
		LCS[] lcss = LCSArrayHelper.lookupLCSs(lcs_array);
		for(LCS lcs: lcss) {
			if(lcs == null)
				return;
			indications.add(createCombo(lcs));
		}
		addLabel("L");
		for(JComboBox combo: indications.descendingIterator())
			add(combo);
		addLabel("R");
	}

	/** Create a combo box for selecting lane-use indications */
	protected JComboBox createCombo(LCS lcs) {
		return new JComboBox(LCSHelper.lookupIndications(lcs));
	}

	/** Add a label to the selector */
	protected void addLabel(String t) {
		JLabel label = new JLabel(t, SwingConstants.CENTER);
		label.setFont(FONT);
		add(label);
	}

	/** Enable/disable all widgets */
	public void setEnabled(boolean enabled) {
		for(Component c: getComponents())
			c.setEnabled(enabled);
	}
}
