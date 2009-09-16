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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Iterator;
import java.util.LinkedList;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import us.mn.state.dot.tms.LaneUseIndication;
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

	/** List of combo boxes for selecting lane-use indications */
	protected final LinkedList<JComboBox> indications =
		new LinkedList<JComboBox>();

	/** Create a new indication selector */
	public IndicationSelector() {
		super(new GridBagLayout());
	}

	/** Dispose of the indication selector */
	public void dispose() {
		removeAll();
		indications.clear();
	}

	/** Set the LCS array */
	public void setLCSArray(LCSArray lcs_array) {
		removeAll();
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets = new Insets(1, 1, 1, 1);
		bag.anchor = GridBagConstraints.EAST;
		bag.gridy = 0;
		indications.clear();
		LCS[] lcss = LCSArrayHelper.lookupLCSs(lcs_array);
		for(LCS lcs: lcss) {
			if(lcs == null)
				return;
			indications.add(createCombo(lcs));
		}
		Iterator<JComboBox> it = indications.descendingIterator();
		while(it.hasNext()) {
			add(it.next(), bag);
			bag.anchor = GridBagConstraints.WEST;
		}
		revalidate();
		repaint();
	}

	/** Create a combo box for selecting lane-use indications */
	protected JComboBox createCombo(LCS lcs) {
		JComboBox c = new JComboBox(LCSHelper.lookupIndications(lcs));
		c.setRenderer(new IndicationRenderer(32));
		return c;
	}

	/** Enable/disable all widgets */
	public void setEnabled(boolean enabled) {
		for(Component c: getComponents())
			c.setEnabled(enabled);
	}

	/** Set the selected indications */
	public void setIndications(Integer[] ind) {
		if(ind.length != indications.size())
			return;
		for(int i = 0; i < ind.length; i++) {
			JComboBox combo = indications.get(i);
			LaneUseIndication lui = 
				LaneUseIndication.fromOrdinal(ind[i]);
			combo.setSelectedItem(lui != null ? lui :
				LaneUseIndication.DARK);
		}
	}

	/** Get the selected indications */
	public Integer[] getIndications() {
		Integer[] ind = new Integer[indications.size()];
		for(int i = 0; i < ind.length; i++) {
			JComboBox combo = indications.get(i);
			LaneUseIndication lui =
				(LaneUseIndication)combo.getSelectedItem();
			if(lui != null)
				ind[i] = lui.ordinal();
			else
				return null;
		}
		return ind;
	}
}
