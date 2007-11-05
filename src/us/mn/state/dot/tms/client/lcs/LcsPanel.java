/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2005  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.client.lcs;

import java.awt.GridLayout;
import javax.swing.JPanel;

/**
 * Scale GUI representation of a LCS panel.
 *
 * @author    <a href="mailto:erik.engstrom@dot.state.mn.us">Erik Engstrom</a>
 * @author Douglas Lau
 */
public class LcsPanel extends JPanel {

	static protected final int LANES = 4;

	protected final int size;

	protected final LcsModule[] modules = new LcsModule[LANES];

	/**
	 * Create an LCS panel
	 *
	 * @param s Size (in pixels) to display each LCS module
	 */
	public LcsPanel(int s) {
		super(new GridLayout(1, LANES, 2, 0));
		size = s;
		for(int i = 0; i < modules.length; i++) {
			modules[i] = new LcsModule(size);
			add(modules[i]);
		}
	}

	/** Clear the LCS panel */
	public void clear() {
		for(int i = 0; i < modules.length; i++)
			modules[i].setVisible(false);
	}

	/**
	 * Set the selected LaneControlSignal object
	 *
	 * @param lcs  The new lcs value
	 */
	public void setLcs(LcsProxy lcs) {
		if(lcs == null)
			clear();
		else {
			int[] signals = lcs.getSignals();
			for(int i = 0; i < modules.length; i++) {
				if(i < signals.length) {
					modules[i].setSignal(signals[
						signals.length - i - 1]);
					modules[i].setVisible(true);
				} else
					modules[i].setVisible(false);
			}
		}
		repaint();
	}
}
