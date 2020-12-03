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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import us.mn.state.dot.tms.CorridorBase;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.StationHelper;
import us.mn.state.dot.tms.client.roads.CorridorList;
import us.mn.state.dot.tms.client.roads.R_NodeManager;
import us.mn.state.dot.tms.client.widget.IWorker;
import us.mn.state.dot.tms.client.wysiwyg.editor.WController;
import us.mn.state.dot.tms.utils.Multi.OverLimitMode;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtTravelTime;

import us.mn.state.dot.sonar.SonarObject;

/**
 * WYSIWYG DMS Message Editor dialog form for editing Travel Time action tags.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
class WTravelTimeTagDialog extends WMultiTagDialog {
	protected WtTravelTime editTok;
	private DefaultComboBoxModel<CorridorBase<R_Node>> corridorModel;
	private WTagParamObjectField<CorridorBase<R_Node>> corridors;
	private CorridorBase<R_Node> corridor;
	
	private DefaultComboBoxModel<Station> stationModel;
	private WTagParamSonarObjectField<Station> stations;
	
	private WTagParamEnumField<OverLimitMode> modeField;
	private WTagParamTextField o_txtField;
	private String sid;
	private Station station;
	private OverLimitMode mode;
	private String o_txt;
	
	public WTravelTimeTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
		
		// setup the corridor model right away
		getCorridorModel();
	}
	
	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtTravelTime) tok;
		sid = editTok.getStationId();
		
		if (sid != null) {
			station = StationHelper.lookup(sid);
			if (station != null)
				setCorridorFromStation(station);
		}
		
		mode = editTok.getOverLimitMode();
		o_txt = editTok.getOverLimitText();
	}
	
	@Override
	protected void addTagForm() {
		// display a ComboBox of corridors
		corridors = new WTagParamObjectField<CorridorBase<R_Node>>(
				corridorModel, null, true);
		corridors.addActionListener(setCorridorAction);
		addField("wysiwyg.travel_time_dialog.corridor", corridors);
		
		// display a ComboBox of stations in the selected corridor
		// start with an empty model
		stationModel = new DefaultComboBoxModel<Station>();
		stations = new WTagParamSonarObjectField<Station>(
				stationModel, station, true);
		stations.setRenderer(new StationListRenderer());
		addField("wysiwyg.travel_time_dialog.station", stations);
		
		modeField = new WTagParamEnumField<OverLimitMode>(
				OverLimitMode.values(), mode, false);
		addField("wysiwyg.travel_time_dialog.mode", modeField);
		o_txtField = new WTagParamTextField(o_txt, 10, false);
		addField("wysiwyg.travel_time_dialog.o_txt", o_txtField);
	}
	
	@Override
	protected WtTravelTime makeNewTag() {
		station = stations.getSelectedItem();
		sid = station.getName();
		mode = modeField.getSelectedItem();
		o_txt = o_txtField.getText();
		return new WtTravelTime(sid, mode, o_txt);
	}
	
	/** Get the corridor model from the session's R_Node manager. This creates
	 *  a copy of the model to avoid linking this and the R_Node tab.
	 */
	private DefaultComboBoxModel<CorridorBase<R_Node>> getCorridorModel() {
		corridorModel = new DefaultComboBoxModel<CorridorBase<R_Node>>();
		R_NodeManager rnm = session.getR_NodeManager();
		ComboBoxModel<CorridorBase<R_Node>> model = rnm.getCorridorModel();
		for (int i = 0; i < model.getSize(); ++i)
			corridorModel.addElement(model.getElementAt(i));
		return corridorModel;
	}
	
	/** Find the corridor on which the Station provided is found. */
	private void setCorridorFromStation(Station s) {
		IWorker <CorridorBase<R_Node>> worker = 
				new IWorker<CorridorBase<R_Node>>() {
			@Override
			protected CorridorBase<R_Node> doInBackground() {
				// look through all corridors in the model
				for (int i = 0; i < corridorModel.getSize(); ++i) {
					CorridorBase<R_Node> c = corridorModel.getElementAt(i);
					if (CorridorList.checkCorridor(
							c, s.getR_Node().getGeoLoc())) {
						return c;
					}
				}
				return null;
			}
			@Override
			public void done() {
				corridor = getResult();
				corridorModel.setSelectedItem(corridor);
			}
		};
		worker.execute();
	}
	
	/** Set the corridor and update the ComboBox of stations. */
	private void setCorridor(CorridorBase<R_Node> c) {
		corridor = c;
		IWorker <DefaultComboBoxModel<Station>> worker = 
				new IWorker<DefaultComboBoxModel<Station>>() {
			@Override
			protected DefaultComboBoxModel<Station> doInBackground() {
				// create a new model
				DefaultComboBoxModel<Station> model =
						new DefaultComboBoxModel<Station>();
				
				// find stations on this corridor and add to the model
				Iterator<Station> it = StationHelper.iterator();
				while (it.hasNext()) {
					Station s = it.next();
					if (CorridorList.checkCorridor(
							corridor, s.getR_Node().getGeoLoc())) {
						model.addElement(s);
					}
				}
				return model;
			}
			@Override
			public void done() {
				DefaultComboBoxModel<Station> model = getResult();
				if (model != null) {
					stationModel = model;
					stations.setComboBoxModel(stationModel);
				}
			}
		};
		worker.execute();
	}
	
	private ActionListener setCorridorAction = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			setCorridor(corridors.getSelectedItem());
		}
	};
	
	/** Renderer for displaying Stations with "Description: (Name)" */
	private class StationListRenderer
				implements ListCellRenderer<SonarObject> {
		private DefaultListCellRenderer cell = new DefaultListCellRenderer();
		
		@Override  
		public Component getListCellRendererComponent(
		        JList<?extends SonarObject> list, SonarObject o,
		        int index, boolean isSelected, boolean cellHasFocus) {
			Station s = (Station) o;
			cell.getListCellRendererComponent(
					list, s, index, isSelected, cellHasFocus);
			String txt = (s != null) ? String.format("%s (%s)",
					StationHelper.getDescription(s), s.getName()) : "";
			cell.setText(txt);
			return cell;
		}
	}
}
