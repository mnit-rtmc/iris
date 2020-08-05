/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020 SRF Consulting Group
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

import javax.swing.DefaultListSelectionModel;

/**
 * A class for disabling selection in a JList.
 * 
 * @author Gordon Parikh
 */
public class DisabledSelectionModel extends DefaultListSelectionModel {
	@Override
	public void setSelectionInterval(int index0, int index1) {
		super.setSelectionInterval(-1, -1);
 	}

	@Override
	public void addSelectionInterval(int index0, int index1) {
		super.setSelectionInterval(-1, -1);
	}
}
