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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
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
public class LCSArrayCellRenderer extends JPanel implements ListCellRenderer {

	/** Size in pixels for each LCS in array */
	static private final int LCS_SIZE = UI.scaled(20);

	/** LCS array manager */
	private final LCSArrayManager manager;

	/** List cell renderer (needed for colors) */
	protected final DefaultListCellRenderer cell =
		new DefaultListCellRenderer();

	/** Title bar */
	protected final JPanel title = new JPanel();

	/** LCS array name label.  NOTE: this needs to be initialized to a
	 * non-empty string to force getPreferredSize() to give a sane value. */
	protected final JLabel nameLbl = new JLabel(" ");

	/** Label for the user */
	protected final JLabel userLbl = new JLabel();

	/** Lane configuration panel */
	private final LaneConfigurationPanel lane_config =
		new LaneConfigurationPanel(LCS_SIZE, false);

	/** LCS array panel */
	protected final LCSArrayPanel lcsPnl = new LCSArrayPanel(LCS_SIZE);

	/** Location bar */
	protected final Box location = Box.createHorizontalBox();

	/** Label for location.  NOTE: this needs to be initialized to a
	 * non-empty string to force getPreferredSize() to give a sane value. */
	protected final JLabel locationLbl = new JLabel(" ");

	/** Create a new LCS array cell renderer */
	public LCSArrayCellRenderer(LCSArrayManager m) {
		super(new BorderLayout());
		manager = m;
		Border b = BorderFactory.createCompoundBorder(
		          BorderFactory.createEmptyBorder(1, 1, 1, 1),
		          BorderFactory.createRaisedBevelBorder());
		setBorder(b);
		Insets bi = b.getBorderInsets(this);
		title.setLayout(new BoxLayout(title, BoxLayout.X_AXIS));
		title.add(nameLbl);
		title.add(Box.createGlue());
		title.add(userLbl);
		location.add(locationLbl);
		location.add(Box.createGlue());
		add(title, BorderLayout.NORTH);
		lane_config.add(lcsPnl);
		add(lane_config, BorderLayout.CENTER);
		add(location, BorderLayout.SOUTH);
		int w = lcsPnl.getPreferredSize().width + bi.left + bi.right;
		int h = bi.top + bi.bottom +
			nameLbl.getPreferredSize().height +
			lcsPnl.getPreferredSize().height +
			locationLbl.getPreferredSize().height;
		setMinimumSize(new Dimension(w, h));
		setPreferredSize(new Dimension(w, h));
	}

	/**
	 * Get a component configured to render an LCS array.
	 *
	 * @param list          JList needing the rendering
	 * @param value         The object to render.
	 * @param index         The List index of the object to render.
	 * @param isSelected    Is the object selected?
	 * @param cellHasFocus  Does the object have focus?
	 * @return              Component to use for rendering the LCS.
	 */
	public Component getListCellRendererComponent(JList list, Object value,
		int index, boolean isSelected, boolean cellHasFocus)
	{
		if(value instanceof LCSArray)
			setLcsArray((LCSArray)value);
		if(isSelected) {
			Component temp = cell.getListCellRendererComponent(list,
				value, index, isSelected, cellHasFocus);
			title.setBackground(temp.getBackground());
		} else
			title.setBackground(nameLbl.getBackground());
		return this;
	}

	/** Set the LCS array */
	protected void setLcsArray(LCSArray lcs_array) {
		lane_config.setConfiguration(manager.laneConfiguration(
			lcs_array));
		nameLbl.setText(lcs_array.getName());
		userLbl.setText(IrisUserHelper.getNamePruned(
			getUser(lcs_array)));
		lcsPnl.setIndications(getIndications(lcs_array),
			lcs_array.getShift());
		locationLbl.setText(LCSArrayHelper.lookupLocation(lcs_array));
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
