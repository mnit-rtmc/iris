/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2013  Minnesota Department of Transportation
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
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LCSArray;
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
	protected final int pixels;

	/** Array of lane indication labels (icons) from left to right */
	private final LCSPanel[] lanes = new LCSPanel[MAX_SHIFT + 1];

	/** Handler for click events */
	protected ClickHandler handler;

	/** Set the click handler */
	public void setClickHandler(ClickHandler ch) {
		handler = ch;
	}

	/** Panel to display on LCS indication */
	protected class LCSPanel extends JLabel {
		protected Integer lane;
		protected LCSPanel() {
			addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					Integer ln = lane;
					if(ln != null)
						doClick(ln);
				}
			});
		}
	}

	/** Process a click on an LCS panel */
	protected void doClick(int ln) {
		ClickHandler ch = handler;
		if(ch != null)
			ch.handleClick(ln);
	}

	/**
	 * Create an LCS array panel
	 * @param p Pixel size for each LCS.
	 */
	public LCSArrayPanel(int p) {
		setLayout(null);
		pixels = p;
		int w = getX(MAX_SHIFT + 1) + 3;
		setMinimumSize(new Dimension(w, pixels + 4));
		setPreferredSize(new Dimension(w, pixels + 4));
		for(int i = 0; i < lanes.length; i++) {
			lanes[i] = new LCSPanel();
			add(lanes[i]);
			lanes[i].setBounds(getX(i), 2, pixels, pixels);
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
		for(int i = 0; i < lanes.length; i++) {
			LCSPanel lbl = lanes[i];
			int ln = shift + ilen - 1 - i;
			if(ln >= 0 && ln < ilen) {
				Icon icon = IndicationIcon.create(pixels,
					LaneUseIndication.fromOrdinal(ind[ln]));
				lbl.setIcon(icon);
				lbl.setOpaque(true);
				lbl.setBackground(Color.BLACK);
				lbl.lane = ln + 1;
			} else {
				lbl.setIcon(null);
				lbl.setOpaque(false);
				lbl.setBackground(null);
				lbl.lane = null;
			}
		}
	}

	/** Clear the LCS panel */
	public void clear() {
		setIndications(new Integer[0], 0);
	}
}
