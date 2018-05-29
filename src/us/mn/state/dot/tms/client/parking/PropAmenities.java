/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.parking;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import us.mn.state.dot.tms.ParkingArea;
import us.mn.state.dot.tms.ParkingAreaAmenities;
import us.mn.state.dot.tms.client.widget.IAction;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * Parking area properties amenities panel.
 *
 * @author Douglas Lau
 */
public class PropAmenities extends JPanel {

	/** Amenity checkboxes */
	private final JCheckBox[] amenities_cbx =
		new JCheckBox[ParkingAreaAmenities.SIZE];

	/** Parking area */
	private final ParkingArea proxy;

	/** Create a new parking area properties amenities panel */
	public PropAmenities(ParkingArea pa) {
		proxy = pa;
		for (int i = 0; i < ParkingAreaAmenities.SIZE; i++)
			amenities_cbx[i] = new JCheckBox();
	}

	/** Enable or disable the panel */
	@Override
	public void setEnabled(boolean e) {
		super.setEnabled(e);
		for (JCheckBox cbx : amenities_cbx)
			cbx.setEnabled(e);
	}

	/** Initialize the widgets on the panel */
	public void initialize() {
		setLayout(new GridLayout(0, 2, UI.hgap, 0));
		for (ParkingAreaAmenities a : ParkingAreaAmenities.values()) {
			JCheckBox cbx = amenities_cbx[a.ordinal()];
			cbx.setAction(new IAction(a.i18n()) {
				protected void doActionPerformed(ActionEvent e){
				    proxy.setAmenities(getSelectedAmenities());
				}
			});
			add(cbx);
		}
	}

	/** Get selected amenities as bit flags */
	private Integer getSelectedAmenities() {
		int bits = 0;
		for (int i = 0; i < ParkingAreaAmenities.SIZE; i++) {
			if (amenities_cbx[i].isSelected())
				bits |= (1 << i);
		}
		return (bits > 0) ? bits : null;
	}

	/** Update amenities attribute */
	public void updateAttribute() {
		Integer amenities = proxy.getAmenities();
		int bits = (amenities != null) ? amenities : 0;
		for (int i = 0; i < ParkingAreaAmenities.SIZE; i++) {
			int b = (1 << i);
			boolean a = (bits & b) != 0;
			amenities_cbx[i].setSelected(a);
		}
	}
}
