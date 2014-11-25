/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.detector;

import java.awt.event.ActionEvent;
import javax.swing.JButton;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.Station;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IPanel.Stretch;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying and editing stations
 *
 * @author Douglas Lau
 */
public class StationForm extends ProxyTableForm<Station> {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(Station.SONAR_TYPE);
	}

	/** R_Node selection model */
	private final ProxySelectionModel<R_Node> sel_model;

	/** Action to display the r_node */
	private final IAction r_node = new IAction("r_node") {
		protected void doActionPerformed(ActionEvent e) {
			Station s = getSelectedProxy();
			if (s != null)
				sel_model.setSelected(s.getR_Node());
		}
	};

	/** Create a new station form */
	public StationForm(Session s) {
		super(I18N.get("detector.station.plural"), new StationModel(s));
		sel_model = s.getR_NodeManager().getSelectionModel();
	}

	/** Add the table to the panel */
	@Override
	protected void addTable(IPanel p) {
		p.add(table, Stretch.FULL);
		p.add(new JButton(r_node), Stretch.RIGHT);
	}

	/** Get the row height */
	@Override
	protected int getRowHeight() {
		return 20;
	}

	/** Select a new proxy */
	@Override
	protected void selectProxy() {
		r_node.setEnabled(getSelectedProxy() != null);
	}
}
