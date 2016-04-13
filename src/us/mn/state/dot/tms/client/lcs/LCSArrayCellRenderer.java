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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.IrisUserHelper;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.LCSArrayHelper;
import us.mn.state.dot.tms.client.roads.LaneConfigurationPanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Renderer for LCS array objects in a list.
 *
 * @author Douglas Lau
 */
public class LCSArrayCellRenderer extends JPanel
	implements ListCellRenderer<LCSArray>
{
	/** Size in pixels for each LCS in array */
	static private final int LCS_SIZE = UI.scaled(20);

	/** LCS array manager */
	private final LCSArrayManager manager;

	/** List cell renderer (needed for colors) */
	private final DefaultListCellRenderer cell =
		new DefaultListCellRenderer();

	/** Title bar */
	private final JPanel title = new JPanel();

	/** LCS array name label.  NOTE: this needs to be initialized to a
	 * non-empty string to force getPreferredSize() to give a sane value. */
	private final JLabel name_lbl = new JLabel(" ");

	/** Label for the user */
	private final JLabel user_lbl = new JLabel();

	/** Lane configuration panel */
	private final LaneConfigurationPanel lane_config =
		new LaneConfigurationPanel(LCS_SIZE, false);

	/** LCS array panel */
	private final LCSArrayPanel lcs_pnl = new LCSArrayPanel(LCS_SIZE);

	/** Location bar */
	private final Box location = Box.createHorizontalBox();

	/** Label for location.  NOTE: this needs to be initialized to a
	 * non-empty string to force getPreferredSize() to give a sane value. */
	private final JLabel loc_lbl = new JLabel(" ");

	/** Create a new LCS array cell renderer */
	public LCSArrayCellRenderer(LCSArrayManager m) {
		super(new BorderLayout());
		manager = m;
		Border b = UI.cellRendererBorder();
		setBorder(b);
		Insets bi = b.getBorderInsets(this);
		title.setLayout(new BoxLayout(title, BoxLayout.X_AXIS));
		title.add(name_lbl);
		title.add(Box.createGlue());
		title.add(user_lbl);
		location.add(loc_lbl);
		location.add(Box.createGlue());
		add(title, BorderLayout.NORTH);
		lane_config.add(lcs_pnl);
		add(lane_config, BorderLayout.CENTER);
		add(location, BorderLayout.SOUTH);
		int w = lcs_pnl.getPreferredSize().width + bi.left + bi.right;
		int h = bi.top + bi.bottom +
			name_lbl.getPreferredSize().height +
			lcs_pnl.getPreferredSize().height +
			loc_lbl.getPreferredSize().height;
		setMinimumSize(new Dimension(w, h));
		setPreferredSize(new Dimension(w, h));
	}

	/**
	 * Get a component configured to render an LCS array.
	 *
	 * @param list          JList needing rendering.
	 * @param value         The object to render.
	 * @param index         The list index of the object to render.
	 * @param isSelected    Is the object selected?
	 * @param cellHasFocus  Does the object have focus?
	 * @return              Component to use for rendering the LCS.
	 */
	@Override
	public Component getListCellRendererComponent(
		JList<? extends LCSArray> list, LCSArray value, int index,
		boolean isSelected, boolean cellHasFocus)
	{
		setLcsArray(value);
		if (isSelected) {
			Component temp = cell.getListCellRendererComponent(list,
				value, index, isSelected, cellHasFocus);
			title.setBackground(temp.getBackground());
		} else
			title.setBackground(name_lbl.getBackground());
		return this;
	}

	/** Set the LCS array */
	private void setLcsArray(LCSArray lcs_array) {
		lane_config.setConfiguration(manager.laneConfiguration(
			lcs_array));
		name_lbl.setText(lcs_array.getName());
		user_lbl.setText(IrisUserHelper.getNamePruned(
			getUser(lcs_array)));
		lcs_pnl.setIndications(getIndications(lcs_array),
			lcs_array.getShift());
		loc_lbl.setText(LCSArrayHelper.lookupLocation(lcs_array));
	}

	/** Get the user name (may be overridden) */
	protected User getUser(LCSArray lcs_array) {
		return lcs_array.getOwnerCurrent();
	}

	/** Get the indications (may be overridden) */
	protected Integer[] getIndications(LCSArray lcs_array) {
		return lcs_array.getIndicationsCurrent();
	}
}
