/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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


package us.mn.state.dot.tms.client.wysiwyg.editor.tags;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import us.mn.state.dot.sonar.SonarObject;
import us.mn.state.dot.tms.ParkingArea;
import us.mn.state.dot.tms.ParkingAreaHelper;
import us.mn.state.dot.tms.client.wysiwyg.editor.WController;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtParkingAvail;

/**
 * WYSIWYG DMS Message Editor dialog form for editing Parking Availability
 * action tags.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
class WParkingAvailTagDialog extends WMultiTagDialog {
	protected WtParkingAvail editTok;
	private WTagParamSonarObjectField<ParkingArea> pidField;
	private WTagParamTextField l_txtField;
	private WTagParamTextField c_txtField;
	private String pid;
	private ParkingArea parkingArea;
	private String l_txt;
	private String c_txt;
	
	public WParkingAvailTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}
	
	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtParkingAvail) tok;
		pid = editTok.getParkingID();
		
		if (pid != null && pid.startsWith("pa"))
			parkingArea = ParkingAreaHelper.lookup(pid);
		
		l_txt = editTok.getParkingLowText();
		c_txt = editTok.getClosedText();
	}

	@Override
	protected void addTagForm() {
		// display a ComboBox of parking areas
		ParkingArea parkingAreas[] = getParkingAreas();
		pidField = new WTagParamSonarObjectField<ParkingArea>(
				parkingAreas, parkingArea, true);
		pidField.setRenderer(new ParkingAreaListRenderer());
		addField("wysiwyg.parking_avail_dialog.pid", pidField);
		
		l_txtField = new WTagParamTextField(l_txt, 10, true);
		addField("wysiwyg.parking_avail_dialog.l_txt", l_txtField);
		c_txtField = new WTagParamTextField(c_txt, 10, true);
		addField("wysiwyg.parking_avail_dialog.c_txt", c_txtField);
	}

	@Override
	protected WtParkingAvail makeNewTag() {
		// remove "pa" from the SONAR name to get the parking area ID
		parkingArea = pidField.getSelectedItem();
		pid = parkingArea.getName();
		
		l_txt = l_txtField.getText();
		c_txt = c_txtField.getText();
		return new WtParkingAvail(pid, l_txt, c_txt);
	}
	
	/** Return a list of Parking Areas */
	private static ParkingArea[] getParkingAreas() {
		ArrayList<ParkingArea> parkingAreas = new ArrayList<ParkingArea>();
		Iterator<ParkingArea> it = ParkingAreaHelper.iterator();
		while (it.hasNext()) {
			parkingAreas.add(it.next());
		}
		ParkingArea arr[] = new ParkingArea[parkingAreas.size()];
		parkingAreas.toArray(arr);
		return arr;
	}
	
	/** Renderer for displaying Parking Areas with "Facility Name: (Name)" */
	private class ParkingAreaListRenderer
				implements ListCellRenderer<SonarObject> {
		private DefaultListCellRenderer cell = new DefaultListCellRenderer();
		
		@Override  
		public Component getListCellRendererComponent(
				JList<?extends SonarObject> list, SonarObject o,
		      int index, boolean isSelected, boolean cellHasFocus) {
			ParkingArea p = (ParkingArea) o;
			cell.getListCellRendererComponent(
					list, p, index, isSelected, cellHasFocus);
			String txt = (p != null) ? String.format("%s (%s)",
					p.getFacilityName(), p.getName()) : "";
			cell.setText(txt);
		    return cell;
		  }
	}
}
