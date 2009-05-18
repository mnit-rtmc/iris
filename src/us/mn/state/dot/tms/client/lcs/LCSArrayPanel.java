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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.mn.state.dot.tms.LaneUseIndication;

/**
 * Scale GUI representation of a LCS array panel.
 *
 * @author Douglas Lau
 */
public class LCSArrayPanel extends JPanel {

	/** Maximum number of lanes */
	static protected final int MAX_LANES = 5;

	/** Pixel size (height and width) of each LCS */
	protected final int pixels;

	/**
	 * Create an LCS array panel
	 * @param p Pixel size for each LCS.
	 */
	public LCSArrayPanel(int p) {
		super(new GridBagLayout());
		pixels = p;
		int w = MAX_LANES * pixels + MAX_LANES * 2;
		setMinimumSize(new Dimension(w, pixels + 2));
		setPreferredSize(new Dimension(w, pixels + 2));
	}

	/** Set new indications */
	public void setIndications(Integer[] ind) {
		removeAll();
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets = new Insets(1, 1, 1, 1);
		bag.anchor = GridBagConstraints.EAST;
		bag.gridy = 0;
		for(int i = ind.length - 1; i >= 0; i--) {
			Icon icon = IndicationIcon.create(pixels,
				LaneUseIndication.fromOrdinal(ind[i]));
			JLabel lbl = new JLabel(icon);
			lbl.setOpaque(true);
			lbl.setBackground(Color.BLACK);
			add(lbl, bag);
			bag.anchor = GridBagConstraints.WEST;
		}
		revalidate();
		repaint();
	}

	/** Clear the LCS panel */
	public void clear() {
		removeAll();
	}
}
