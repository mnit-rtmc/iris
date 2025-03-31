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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.mn.state.dot.tms.LcsIndication;
import static us.mn.state.dot.tms.R_Node.MAX_SHIFT;

/**
 * Scale GUI representation of a LCS array panel.
 *
 * @author Douglas Lau
 */
public class LcsPanel extends JPanel {

	/** Interface to handle clicks */
	static public interface ClickHandler {
		void handleClick(int lane);
	}

	/** Pixel size (height and width) of each LCS */
	private final int pixels;

	/** Array of indication panels (icons) from left to right */
	private final LanePanel[] panels = new LanePanel[MAX_SHIFT + 1];

	/** Handler for click events */
	private ClickHandler handler;

	/** Set the click handler */
	public void setClickHandler(ClickHandler ch) {
		handler = ch;
	}

	/** Panel to display one LCS indication */
	private class LanePanel extends JLabel {
		private Integer lane;
		private LanePanel() {
			setBackground(Color.BLACK);
			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					Integer ln = lane;
					if (ln != null)
						doClick(ln);
				}
			});
		}
		private void setIndication(int ln, Integer ind) {
			setIcon(IndicationIcon.create(pixels,
				LcsIndication.fromOrdinal(ind)));
			setOpaque(true);
			lane = ln + 1;
		}
		private void clearIndication() {
			setIcon(null);
			setOpaque(false);
			lane = null;
		}
	}

	/** Process a click on an LCS panel */
	private void doClick(int ln) {
		ClickHandler ch = handler;
		if (ch != null)
			ch.handleClick(ln);
	}

	/**
	 * Create an LCS array panel
	 * @param p Pixel size for each LCS.
	 */
	public LcsPanel(int p) {
		setLayout(null);
		pixels = p;
		int w = getX(MAX_SHIFT + 1);
		setMinimumSize(new Dimension(w, pixels));
		setPreferredSize(new Dimension(w, pixels));
		for (int i = 0; i < panels.length; i++) {
			panels[i] = new LanePanel();
			add(panels[i]);
			panels[i].setBounds(getX(i), 0, pixels, pixels);
		}
		setOpaque(false);
	}

	/** Get the X pixel value of a shift.
	 * @param shift Lane shift.
	 * @return X pixel value at lane shift. */
	private int getX(int shift) {
		return 6 + shift * (pixels + 6);
	}

	/** Set new indications */
	public void setIndications(int[] ind, int shift) {
		int ilen = (ind != null) ? ind.length : 0;
		for (int i = 0; i < panels.length; i++) {
			int ln = shift + ilen - 1 - i;
			if (ln >= 0 && ln < ilen)
				panels[i].setIndication(ln, ind[ln]);
			else
				panels[i].clearIndication();
		}
	}

	/** Clear the LCS panel */
	public void clear() {
		setIndications(null, 0);
		handler = null;
	}
}
