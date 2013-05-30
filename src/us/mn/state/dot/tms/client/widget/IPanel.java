/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2013  Minnesota Department of Transportation
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

	/** Color for value label text */
	static private final Color DARK_BLUE = new Color(0, 0, 128);

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

	/** Create a new panel */
	public IPanel() {
		super(new GridBagLayout());
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
	private GridBagConstraints createConstraints(boolean last) {
		GridBagConstraints bag = new GridBagConstraints();
		bag.anchor = GridBagConstraints.WEST;
		bag.fill = GridBagConstraints.NONE;
		bag.insets.left = UI.hgap / 2;
		bag.insets.right = UI.hgap / 2;
		bag.insets.top = UI.vgap / 2;
		bag.insets.bottom = UI.vgap / 2;
		bag.gridx = GridBagConstraints.RELATIVE;
		bag.gridy = row;
		bag.gridwidth = last ? GridBagConstraints.REMAINDER : 1;
		if(last)
			row++;
		return bag;
	}

	/** Create filled grid bag constraints */
	private GridBagConstraints createFill(boolean last) {
		GridBagConstraints bag = new GridBagConstraints();
		bag.anchor = GridBagConstraints.CENTER;
		bag.fill = GridBagConstraints.BOTH;
		bag.insets.left = UI.hgap / 2;
		bag.insets.right = UI.hgap / 2;
		bag.insets.top = UI.vgap / 2;
		bag.insets.bottom = UI.vgap / 2;
		bag.weightx = 1;
		bag.weighty = 1;
		bag.gridx = GridBagConstraints.RELATIVE;
		bag.gridy = row;
		bag.gridwidth = last ? GridBagConstraints.REMAINDER : 1;
		if(last)
			row++;
		return bag;
	}

	/** Add a label to the current row */
	public void add(String msg, boolean last) {
		GridBagConstraints bag = createConstraints(last);
		bag.anchor = GridBagConstraints.EAST;
		add(new ILabel(msg), bag);
	}

	/** Add a label to the current row */
	public void add(String msg) {
		add(msg, false);
	}

	/** Add a component to the current row */
	public void add(JComponent comp, boolean last) {
		GridBagConstraints bag = createConstraints(last);
		add(comp, bag);
	}

	/** Add a component to the current row */
	public void add(JComponent comp) {
		add(comp, false);
	}

	/** Add a text area to the current row */
	public void add(JTextArea area, boolean last) {
		GridBagConstraints bag = createFill(last);
		area.setWrapStyleWord(true);
		area.setLineWrap(true);
		add(createScrollPane(area), bag);
	}

	/** Add a list to the current row */
	public void add(JList list, boolean last) {
		GridBagConstraints bag = createFill(last);
		add(createScrollPane(list), bag);
	}

	/** Add a table to the current row */
	public void add(JTable table, boolean last) {
		GridBagConstraints bag = createFill(last);
		add(createScrollPane(table), bag);
	}

	/** Create a scroll pane */
	private JScrollPane createScrollPane(JComponent comp) {
		return new JScrollPane(comp,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	}
}
