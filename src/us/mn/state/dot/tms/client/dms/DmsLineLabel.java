/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * A line label for DMS cell renderers
 *
 * @author Douglas Lau
 */
public class DmsLineLabel extends JLabel {

	/** Create a new DMS line label */
	public DmsLineLabel() {
		setHorizontalAlignment(SwingConstants.CENTER);
		setForeground(Color.YELLOW);
		setBackground(Color.BLACK);
		setFont(new Font("Dialog", Font.BOLD, 12));
	}

	/** Paint the component */
	protected void paintComponent(Graphics g) {
		FontMetrics m = g.getFontMetrics();
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, m.getDescent(), getWidth(),
			m.getAscent() - m.getDescent());
		super.paintComponent(g);
	}
}
