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

import java.awt.Dimension;
import java.awt.GridLayout;
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
	static protected final int MAX_LANES = 6;

	/** Pixel size (height and width) of each LCS */
	protected final int pixels;

	/**
	 * Create an LCS array panel
	 * @param p Pixel size for each LCS.
	 */
	public LCSArrayPanel(int p) {
		super(new GridLayout(1, 0, 2, 0));
		pixels = p;
		setMinimumSize(new Dimension(MAX_LANES * pixels, pixels));
		setPreferredSize(new Dimension(MAX_LANES * pixels, pixels));
	}

	/** Set new indications */
	public void setIndications(int[] ind) {
		removeAll();
		for(int i = ind.length - 1; i >= 0; i--) {
			Icon icon = IndicationIcon.create(pixels,
				LaneUseIndication.fromOrdinal(ind[i]));
			add(new JLabel(icon));
		}
	}

	/** Clear the LCS panel */
	public void clear() {
		removeAll();
	}
}
