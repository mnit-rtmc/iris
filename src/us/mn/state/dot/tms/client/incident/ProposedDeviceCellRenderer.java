/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.incident;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import us.mn.state.dot.tms.Device;
import us.mn.state.dot.tms.LCSArray;
import us.mn.state.dot.tms.client.Session;

/**
 * Proposed device cell renderer.
 *
 * @author Douglas Lau
 */
public class ProposedDeviceCellRenderer implements ListCellRenderer<Device> {

	/** List cell renderer */
	private final DefaultListCellRenderer cell =
		new DefaultListCellRenderer();

	/** List for configuring LCS cell renderer */
	private final JList<LCSArray> lcs_list = new JList<LCSArray>();

	/** LCS array cell renderer */
	private final ProposedLcsCellRenderer lcs_renderer;

	/** Create a new proposed LCS array cell renderere */
	public ProposedDeviceCellRenderer(Session s, DeviceDeployModel m) {
		lcs_renderer = new ProposedLcsCellRenderer(s, m);
	}

	/** Get component to render a device.
	 *
	 * @param list          JList to renderer.
	 * @param value         Device to render.
	 * @param index         List index of the device.
	 * @param isSelected    Is the device selected?
	 * @param cellHasFocus  Does the device have focus?
	 * @return              Component for rendering. */
	@Override
	public Component getListCellRendererComponent(
		JList<? extends Device> list, Device value, int index,
		boolean isSelected, boolean cellHasFocus)
	{
		if (value instanceof LCSArray) {
			LCSArray lcs = (LCSArray) value;
			return lcs_renderer.getListCellRendererComponent(
				lcs_list, lcs, index, isSelected, cellHasFocus);
		}
		return cell.getListCellRendererComponent(list, value, index,
			isSelected, cellHasFocus);
	}
}
