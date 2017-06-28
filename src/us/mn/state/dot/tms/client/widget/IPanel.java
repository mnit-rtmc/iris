/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.widget;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * IPanel is a panel for viewing and editing forms.  It provides a simpler
 * API for using a GridBagLayout.
 *
 * @author Douglas Lau
 */
public class IPanel extends JPanel {

	/** Stretch types */
	public enum Stretch {
		NONE(GridBagConstraints.NONE, 1, 1, GridBagConstraints.EAST,
			0.1f, 0),
		SOME(GridBagConstraints.NONE, 1, 1, GridBagConstraints.WEST,
			0.5f, 0),
		HALF(GridBagConstraints.BOTH, 1, 1, GridBagConstraints.CENTER,
			1, 1),
		WIDE(GridBagConstraints.HORIZONTAL, 1, 1,
			GridBagConstraints.WEST, 0, 0),
		DOUBLE(GridBagConstraints.HORIZONTAL, 2, 1,
				GridBagConstraints.WEST, 0.1f, 0),
		END(GridBagConstraints.HORIZONTAL, GridBagConstraints.REMAINDER,
			1, GridBagConstraints.WEST, 0, 0),
		CENTER(GridBagConstraints.NONE, GridBagConstraints.REMAINDER, 1,
			GridBagConstraints.CENTER, 0, 0),
		FULL(GridBagConstraints.BOTH, GridBagConstraints.REMAINDER, 1,
			GridBagConstraints.CENTER, 1, 1),
		LEFT(GridBagConstraints.NONE, GridBagConstraints.REMAINDER, 1,
			GridBagConstraints.WEST, 1, 0),
		RIGHT(GridBagConstraints.NONE, GridBagConstraints.REMAINDER, 1,
			GridBagConstraints.EAST, 0.1f, 0),
		TALL(GridBagConstraints.NONE, GridBagConstraints.REMAINDER, 2,
			GridBagConstraints.EAST, 0.1f, 0),
		LAST(GridBagConstraints.NONE, GridBagConstraints.REMAINDER, 1,
			GridBagConstraints.WEST, 0.1f, 0);
		private Stretch(int f, int w, int h, int a, float x, float y) {
			fill = f;
			width = w;
			height = h;
			anchor = a;
			wx = x;
			wy = y;
		}
		private final int fill;
		private final int width;
		private final int height;
		private final int anchor;
		private final float wx;
		private final float wy;
	}

	/** Color for value label text */
	static protected final Color DARK_BLUE = new Color(0, 0, 96);

	/** Create a value label */
	static public JLabel createValueLabel() {
		JLabel lbl = new JLabel();
		lbl.setForeground(DARK_BLUE);
		// By default, labels are BOLD
		lbl.setFont(lbl.getFont().deriveFont(Font.ITALIC));
		return lbl;
	}

	/** Create a value label */
	static public JLabel createValueLabel(String txt) {
		JLabel lbl = createValueLabel();
		lbl.setText(txt);
		return lbl;
	}

	/** Current row on the form */
	private int row = 0;

	/** Current column on the form */
	private int col = 0;

	/** Create a new panel */
	public IPanel() {
		super(new GridBagLayout());
	}

	/** Initialize the panel */
	public void initialize() {
		setBorder(UI.border);
	}

	/** Dispose of the panel */
	public void dispose() {
		removeAll();
	}

	/** Set the title */
	public void setTitle(String t) {
		setBorder(BorderFactory.createTitledBorder(t));
	}

	/** Create grid bag constraints */
	private GridBagConstraints createConstraints(Stretch s) {
		GridBagConstraints bag = new GridBagConstraints();
		bag.anchor = s.anchor;
		bag.fill = s.fill;
		bag.insets.left = UI.hgap / 2;
		bag.insets.right = UI.hgap / 2;
		bag.insets.top = UI.vgap / 2;
		bag.insets.bottom = UI.vgap / 2;
		bag.weightx = s.wx;
		bag.weighty = s.wy;
		bag.gridx = col;
		bag.gridy = row;
		bag.gridwidth = s.width;
		bag.gridheight = s.height;
		if(s.width == GridBagConstraints.REMAINDER) {
			row++;
			col = 0;
		} else
			col += s.width;
		return bag;
	}

	/** Add a label to the current row */
	public void add(String msg, Stretch s) {
		add(new ILabel(msg), s);
	}

	/** Add a label to the current row */
	public void add(String msg) {
		add(msg, Stretch.NONE);
	}

	/** Add a component to the current row */
	public void add(JComponent comp, Stretch s) {
		add(comp, createConstraints(s));
	}

	/** Add a component to the current row */
	public void add(JComponent comp) {
		add(comp, Stretch.SOME);
	}

	/** Add a text area to the current row */
	public void add(JTextArea area, Stretch s) {
		area.setWrapStyleWord(true);
		area.setLineWrap(true);
		add(createScrollPane(area), s);
	}

	/** Add a list to the current row */
	public void add(JList list, Stretch s) {
		add(createScrollPane(list), s);
	}

	/** Add a table to the current row */
	public void add(JTable table, Stretch s) {
		add(createScrollPane(table), s);
	}

	/** Create a scroll pane */
	private JScrollPane createScrollPane(JComponent comp) {
		return new JScrollPane(comp,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	}
}
