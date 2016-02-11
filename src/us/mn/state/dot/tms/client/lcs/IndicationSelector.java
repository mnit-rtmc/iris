/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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

import java.awt.Dimension;
import java.util.ArrayList;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.LCSHelper;
import static us.mn.state.dot.tms.R_Node.MAX_SHIFT;

/**
 * Selector for lane-use control siganl arrays.
 *
 * @author Douglas Lau
 */
public class IndicationSelector extends JPanel {

	/** Maximum number of lanes */
	static private final int N_LANES = MAX_SHIFT + 1;

	/** Pixel width of each lane */
	private final int pixels;

	/** Array of lane-use indication combo boxes (left to right) */
	private final ArrayList<JComboBox<LaneUseIndication>> indications =
		new ArrayList<JComboBox<LaneUseIndication>>(N_LANES);

	/** Number of lanes */
	private int n_lanes = 0;

	/** Lane shift */
	private int shift = 0;

	/** Create a new indication selector */
	public IndicationSelector(int p) {
		setLayout(null);
		pixels = p;
		int w = getX(MAX_SHIFT + 1);
		setMinimumSize(new Dimension(w, p - 16));
		setPreferredSize(new Dimension(w, p - 16));
		IndicationRenderer ir = new IndicationRenderer(p - 26);
		for (int i = 0; i < N_LANES; i++) {
			JComboBox<LaneUseIndication> cbx =
				new JComboBox<LaneUseIndication>();
			cbx.setRenderer(ir);
			add(cbx);
			indications.add(cbx);
			cbx.setBounds(getX(i), 0, pixels, p - 18);
		}
		setOpaque(false);
	}

	/** Get the X pixel value of a shift.
	 * @param shift Lane shift.
	 * @return X pixel value at lane shift. */
	private int getX(int shift) {
		return 6 + shift * (pixels + 6);
	}

	/** Dispose of the indication selector */
	public void dispose() {
		removeAll();
	}

	/** Set the LCS array */
	public void setLCSArray(LCSArray lcs_array) {
		LCS[] lcss = LCSArrayHelper.lookupLCSs(lcs_array);
		n_lanes = lcss.length;
		shift = lcs_array.getShift();
		for (int i = 0; i < indications.size(); i++) {
			JComboBox<LaneUseIndication> cbx = indications.get(i);
			int ln = shift + n_lanes - 1 - i;
			if (ln >= 0 && ln < n_lanes) {
				cbx.setModel(createModel(lcss[ln]));
				cbx.setVisible(true);
			} else {
				cbx.setModel(createModel(null));
				cbx.setVisible(false);
			}
		}
	}

	/** Create a combo box model for selecting lane-use indications */
	private ComboBoxModel<LaneUseIndication> createModel(LCS lcs) {
		if (lcs != null) {
			return new DefaultComboBoxModel<LaneUseIndication>(
				LCSHelper.lookupIndications(lcs));
		} else
			return new DefaultComboBoxModel<LaneUseIndication>();
	}

	/** Enable/disable all widgets */
	public void setEnabled(boolean enabled) {
		for (JComboBox<LaneUseIndication> cbx: indications) {
			cbx.setEnabled(enabled);
			if (!enabled) {
				cbx.setVisible(false);
				cbx.removeAllItems();
			}
		}
	}

	/** Set the selected indications */
	public void setIndications(Integer[] ind) {
		if (ind.length != n_lanes)
			return;
		for (int i = 0; i < indications.size(); i++) {
			int ln = shift + n_lanes - 1 - i;
			if (ln >= 0 && ln < ind.length) {
				LaneUseIndication lui = 
					LaneUseIndication.fromOrdinal(ind[ln]);
				JComboBox<LaneUseIndication> cbx =
					indications.get(i);
				cbx.setSelectedItem(lui != null ? lui :
					LaneUseIndication.DARK);
			}
		}
	}

	/** Get the selected indications */
	public Integer[] getIndications() {
		Integer[] ind = new Integer[n_lanes];
		for (int i = 0; i < indications.size(); i++) {
			int ln = shift + n_lanes - 1 - i;
			if (ln >= 0 && ln < ind.length) {
				JComboBox<LaneUseIndication> cbx =
					indications.get(i);
				LaneUseIndication lui = (LaneUseIndication)
					cbx.getSelectedItem();
				if (lui != null)
					ind[ln] = lui.ordinal();
				else
					return null;
			}
		}
		return ind;
	}
}
