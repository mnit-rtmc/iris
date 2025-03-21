/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2025  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.LcsHelper;
import us.mn.state.dot.tms.LcsIndication;
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

	/** Array of LCS indication combo boxes (left to right) */
	private final ArrayList<JComboBox<LcsIndication>> indications =
		new ArrayList<JComboBox<LcsIndication>>(N_LANES);

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
			JComboBox<LcsIndication> cbx =
				new JComboBox<LcsIndication>();
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
	public void setLcs(Lcs lcs) {
		n_lanes = LcsHelper.countLanes(lcs);
		shift = lcs.getShift();
		for (int i = 0; i < indications.size(); i++) {
			JComboBox<LcsIndication> cbx = indications.get(i);
			int ln = shift + n_lanes - i;
			if (ln > 0 && ln <= n_lanes) {
				cbx.setModel(createModel(lcs, ln));
				cbx.setVisible(true);
			} else {
				cbx.setModel(createModel(null, 0));
				cbx.setVisible(false);
			}
		}
	}

	/** Create a combo box model for selecting lane-use indications */
	private ComboBoxModel<LcsIndication> createModel(Lcs lcs, int ln) {
		if (lcs != null) {
			return new DefaultComboBoxModel<LcsIndication>(
				LcsHelper.lookupIndications(lcs, ln));
		} else
			return new DefaultComboBoxModel<LcsIndication>();
	}

	/** Enable/disable all widgets */
	public void setEnabled(boolean enabled) {
		for (JComboBox<LcsIndication> cbx: indications) {
			cbx.setEnabled(enabled);
			if (!enabled) {
				cbx.setVisible(false);
				cbx.removeAllItems();
			}
		}
	}

	/** Set the selected indications */
	public void setIndications(int[] ind) {
		if (ind.length != n_lanes)
			return;
		for (int i = 0; i < indications.size(); i++) {
			int ln = shift + n_lanes - 1 - i;
			if (ln >= 0 && ln < ind.length) {
				LcsIndication lui = 
					LcsIndication.fromOrdinal(ind[ln]);
				JComboBox<LcsIndication> cbx =
					indications.get(i);
				cbx.setSelectedItem(lui != null ? lui :
					LcsIndication.DARK);
			}
		}
	}

	/** Get the selected indications */
	public int[] getIndications() {
		int[] ind = new int[n_lanes];
		for (int i = 0; i < indications.size(); i++) {
			int ln = shift + n_lanes - 1 - i;
			if (ln >= 0 && ln < ind.length) {
				JComboBox<LcsIndication> cbx =
					indications.get(i);
				LcsIndication lui = (LcsIndication)
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
