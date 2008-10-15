/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2006-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.roads;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.mn.state.dot.map.MapBean;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.R_Node;
import us.mn.state.dot.tms.client.sonar.SonarLayer;
import us.mn.state.dot.tms.client.toast.WrapperComboBoxModel;

/**
 * This component allows a corridor to be chosen from a list
 *
 * @author Douglas Lau
 */
public class CorridorChooser extends JPanel {

	/** Combo box to select a freeway corridor */
	protected final JComboBox corridor_combo = new JComboBox();

	/** Roadway node manager */
	protected final R_NodeManager manager;

	/** Roadway node layer */
	protected final SonarLayer<R_Node> layer;

	/** Map bean */
	protected final MapBean map;

	/** Corridor node list component */
	protected final CorridorList clist;

	/** Create a new corridor chooser */
	public CorridorChooser(R_NodeManager man, MapBean m, CorridorList c) {
		super(new GridBagLayout());
		manager = man;
		layer = man.getLayer();
		map = m;
		clist = c;
		corridor_combo.setModel(new WrapperComboBoxModel(
			manager.getCorridorModel()));
		new ActionJob(this, corridor_combo) {
			public void perform() {
				Object s = corridor_combo.getSelectedItem();
				if(s instanceof String)
					setCorridor((String)s);
				else
					setCorridor("");
			}
		};
		setBorder(BorderFactory.createTitledBorder(
			"Selected Freeway Corridor"));
		GridBagConstraints bag = new GridBagConstraints();
		bag.insets = new Insets(2, 4, 2, 4);
		bag.anchor = GridBagConstraints.EAST;
		add(new JLabel("Corridor"), bag);
		bag.anchor = GridBagConstraints.WEST;
		bag.fill = GridBagConstraints.HORIZONTAL;
		bag.weightx = 1;
		add(corridor_combo, bag);
	}

	/** Set a new selected corridor */
	protected void setCorridor(String c) {
		manager.setCorridor(c);
		clist.setCorridor(manager.createSet());
		layer.updateExtent();
		map.zoomTo(layer.getExtent());
	}

	/** Dispose of the corridor chooser */
	public void dispose() {
		removeAll();
	}
}
