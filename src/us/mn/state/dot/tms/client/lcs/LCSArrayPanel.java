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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.mn.state.dot.tms.LaneUseIndication;
import static us.mn.state.dot.tms.R_Node.MAX_SHIFT;

/**
 * Scale GUI representation of a LCS array panel.
 *
 * @author Douglas Lau
 */
public class LCSArrayPanel extends JPanel {

	/** Interface to handle clicks */
	static public interface ClickHandler {
		void handleClick(int lane);
	}

	/** Pixel size (height and width) of each LCS */
	private final int pixels;

	/** Array of LCS indication panels (icons) from left to right */
	private final LCSPanel[] lcs_pnl = new LCSPanel[MAX_SHIFT + 1];

	/** Handler for click events */
	private ClickHandler handler;

	/** Set the click handler */
	public void setClickHandler(ClickHandler ch) {
		handler = ch;
	}

	/** Panel to display on LCS indication */
	private class LCSPanel extends JLabel {
		private Integer lane;
		private LCSPanel() {
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
				LaneUseIndication.fromOrdinal(ind)));
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
	public LCSArrayPanel(int p) {
		setLayout(null);
		pixels = p;
		int w = getX(MAX_SHIFT + 1);
		setMinimumSize(new Dimension(w, pixels));
		setPreferredSize(new Dimension(w, pixels));
		for (int i = 0; i < lcs_pnl.length; i++) {
			lcs_pnl[i] = new LCSPanel();
			add(lcs_pnl[i]);
			lcs_pnl[i].setBounds(getX(i), 0, pixels, pixels);
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
	public void setIndications(Integer[] ind, int shift) {
		int ilen = ind != null ? ind.length : 0;
		for (int i = 0; i < lcs_pnl.length; i++) {
			int ln = shift + ilen - 1 - i;
			if (ln >= 0 && ln < ilen)
				lcs_pnl[i].setIndication(ln, ind[ln]);
			else
				lcs_pnl[i].clearIndication();
		}
	}

	/** Clear the LCS panel */
	public void clear() {
		setIndications(new Integer[0], 0);
		handler = null;
	}
}
