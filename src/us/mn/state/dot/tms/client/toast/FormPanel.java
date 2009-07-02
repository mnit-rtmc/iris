/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2007-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toast;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * FormPanel is a panel for viewing and editing forms. It provides a simpler
 * API for using a GridBagLayout.
 *
 * @author Douglas Lau
 */
public class FormPanel extends JPanel {

	/** Create a non-editable text field */
	static public JTextField createTextField() {
		JTextField f = new JTextField();
		f.setEditable(false);
		return f;
	}

	/** Flag if components should be enabled */
	protected final boolean enable;

	/** Current row on the form */
	protected int row = 0;

	/** Current grid bag constraints state */
	protected GridBagConstraints bag;

	/** Create a new form panel */
	public FormPanel(boolean e) {
		super(new GridBagLayout());
		enable = e;
		setBorder(TmsForm.BORDER);
		finishRow();
	}

	/** Set the form title */
	public void setTitle(String t) {
		setBorder(BorderFactory.createTitledBorder(t));
	}

	/** Create default grid bag constraints */
	public void finishRow() {
		bag = new GridBagConstraints();
		bag.anchor = GridBagConstraints.EAST;
		bag.insets.right = TmsForm.HGAP;
		bag.insets.bottom = TmsForm.VGAP;
		bag.gridx = GridBagConstraints.RELATIVE;
		bag.gridy = row++;
	}

	/** Set the grid width state */
	public void setWidth(int width) {
		bag.gridwidth = width;
	}

	/** Set the anchor state to WEST */
	public void setWest() {
		bag.anchor = GridBagConstraints.WEST;
		bag.fill = GridBagConstraints.BOTH;
	}

	/** Set the anchor state to CENTER */
	public void setCenter() {
		bag.anchor = GridBagConstraints.CENTER;
		bag.fill = GridBagConstraints.NONE;
		bag.gridwidth = GridBagConstraints.REMAINDER;
	}

	/** Set the fill mode */
	public void setFill() {
		bag.anchor = GridBagConstraints.CENTER;
		bag.fill = GridBagConstraints.BOTH;
		bag.gridwidth = GridBagConstraints.REMAINDER;
		bag.weightx = 1;
		bag.weighty = 1;
	}

	/** Set the anchor state to EAST */
	public void setEast() {
		bag.anchor = GridBagConstraints.EAST;
		bag.fill = GridBagConstraints.NONE;
	}

	/** Add one component with the current grid bag state */
	public void add(JComponent comp) {
		add(comp, bag);
		comp.setEnabled(enable);
	}

	/** Add a pair of components to the panel */
	public void add(JComponent c1, JComponent c2) {
		setEast();
		add(c1);
		setWest();
		add(c2);
	}

	/** Add a component with a label on the left side */
	public void add(String name, JComponent comp) {
		add(new JLabel(name), comp);
	}

	/** Add a component to the panel */
	public void addRow(JComponent comp) {
		setWidth(GridBagConstraints.REMAINDER);
		add(comp);
		finishRow();
	}

	/** Add a pair of components to the panel */
	public void addRow(JComponent c1, JComponent c2) {
		setEast();
		add(c1);
		setWest();
		setWidth(GridBagConstraints.REMAINDER);
		add(c2);
		finishRow();
	}

	/** Add a component with a label on the left side */
	public void addRow(String name, JComponent comp) {
		addRow(new JLabel(name), comp);
	}

	/** Add a component with a label on the left side and a button */
	public void addRow(String name, JComponent comp, JButton btn) {
		setEast();
		add(new JLabel(name));
		setWest();
		setWidth(4);
		add(comp);
		setCenter();
		add(btn);
		finishRow();
	}

	/** Add a text area component with a label on the left side */
	public void addRow(String name, JTextArea area) {
		setEast();
		add(new JLabel(name));
		area.setWrapStyleWord(true);
		area.setLineWrap(true);
		setFill();
		addRow(new JScrollPane(area,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
	}

	/** Add a table component */
	public void addRow(JTable table) {
		setFill();
		addRow(new JScrollPane(table,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
	}
}
