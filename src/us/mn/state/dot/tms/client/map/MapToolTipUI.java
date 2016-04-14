/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.map;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicToolTipUI;

/**
 * Tool tip UI which allows for multi-line tooltips, using a JTextArea
 *
 * @author Erik Engstrom
 */
class MapToolTipUI extends BasicToolTipUI {

	protected final JTextArea textArea = new JTextArea();

	protected final CellRendererPane rendererPane = new CellRendererPane();

	static public ComponentUI createUI(JComponent c) {
		return new MapToolTipUI(c);
	}

	protected MapToolTipUI(JComponent c) {
		textArea.setBackground(c.getBackground());
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(false);
		rendererPane.add(textArea);
	}

	public void installUI(JComponent c) {
		super.installUI(c);
		c.add(rendererPane);
	}

	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);
		c.remove(rendererPane);
	}

	public void paint(Graphics g, JComponent c) {
		Dimension size = c.getSize();
		rendererPane.paintComponent(g, textArea, c, 1, 1,
			size.width, size.height, true);
	}

	public Dimension getPreferredSize(JComponent c) {
		if(c instanceof JToolTip) {
			String t = ((JToolTip)c).getTipText();
			if(t != null) {
				textArea.setText(t);
				Dimension d = textArea.getPreferredSize();
				d.height += 3;
				d.width += 3;
				return d;
			}
		}
		return new Dimension(0, 0);
	}

	public Dimension getMinimumSize(JComponent c) {
		return getPreferredSize(c);
	}

	public Dimension getMaximumSize(JComponent c) {
		return getPreferredSize(c);
	}
}
