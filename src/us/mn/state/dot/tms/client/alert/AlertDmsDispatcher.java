/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group, Inc.
 * Copyright (C) 2021  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.alert;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ActionPlan;
import us.mn.state.dot.tms.AlertInfo;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsActionHelper;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.GeoLocHelper;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignGroupHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.dms.DMSManager;
import us.mn.state.dot.tms.client.map.MapObject;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.client.widget.IWorker;
import us.mn.state.dot.tms.client.widget.ZTable;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.NumericAlphaComparator;

/**
 * An alert DMS dispatcher is a GUI panel for dispatching and reviewing DMS
 * involved in an automated alert deployment.
 *
 * @author Gordon Parikh
 * @author Douglas Lau
 */
public class AlertDmsDispatcher extends IPanel {

	/** Alert manager */
	private final AlertManager manager;

	/** DMS manager */
	private final DMSManager dms_manager;

	/** DMS sign group type cache */
	private final TypeCache<DmsSignGroup> dms_sign_groups;

	/** Table of DMS included in this alert */
	private final ZTable dms_tbl;

	/** A simple table model for DMS inclusion.  Just supports 3 columns -
	 *  the DMS name, location, and whether or not it will be included in
	 *  the alert.  */
	private class DmsIncludedModel extends DefaultTableModel {
		public DmsIncludedModel() {
			addColumn(I18N.get("dms"));
			addColumn(I18N.get("location"));
			addColumn(I18N.get("alert.dms.included"));
		}

		@Override
		public Class<?> getColumnClass(int col) {
			return (col == 2) ?  Boolean.class : Object.class;
		}

		@Override
		public boolean isCellEditable(int row, int col) {
			return col == 2;
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			super.setValueAt(value, row, col);
			if (value instanceof Boolean) {
				boolean selected = (Boolean) value;
				DMS dms = (DMS) dms_mdl.getValueAt(row, 0);
				if (selected)
					addToGroup(dms);
				else
					removeFromGroup(dms);
			}
		}
	}

	/** Find an active sign group for the alert */
	private SignGroup findActiveGroup(DMS dms) {
		AlertInfo ai = manager.getSelectionModel().getSingleSelection();
		SignConfig cfg = dms.getSignConfig();
		return (ai != null && cfg != null)
		      ? findActiveGroup(ai.getActionPlan(), cfg)
		      : null;
	}

	/** Find an active sign group for the alert */
	private SignGroup findActiveGroup(ActionPlan plan, SignConfig cfg) {
		Set<SignGroup> groups = DmsActionHelper.findGroups(plan, cfg);
		Iterator<SignGroup> it = groups.iterator();
		return it.hasNext() ? it.next() : null;
	}

	/** Add a DMS to its active sign group */
	private void addToGroup(DMS dms) {
		SignGroup sg = findActiveGroup(dms);
		if (sg != null)
			createDmsSignGroup(dms, sg);
	}

	/** Create a new DMS sign group */
	private void createDmsSignGroup(DMS dms, SignGroup sg) {
		String tmpl = sg.getName() + "_%d";
		String oname = DmsSignGroupHelper.createUniqueName(tmpl);
		HashMap<String, Object> attrs = new HashMap<String, Object>();
		attrs.put("dms", dms);
		attrs.put("sign_group", sg);
		dms_sign_groups.createObject(oname, attrs);
	}

	/** Remove a DMS from its active sign group */
	private void removeFromGroup(DMS dms) {
		SignGroup sg = findActiveGroup(dms);
		if (sg != null)
			removeDmsSignGroup(dms, sg);
	}

	/** Remove a DMS sign group */
	private void removeDmsSignGroup(DMS dms, SignGroup sg) {
		DmsSignGroup dsg = DmsSignGroupHelper.find(dms, sg);
		if (dsg != null)
			dsg.destroy();
	}

	/** Included DMS table model */
	private final DmsIncludedModel dms_mdl;

	/** DMS selection listener */
	private final ListSelectionListener dmsSelectionListener =
		new ListSelectionListener()
	{
		public void valueChanged(ListSelectionEvent e) {
			if (e.getValueIsAdjusting())
				return;
			int row = dms_tbl.getSelectedRow();
			if (row >= 0 && row < dms_mdl.getRowCount()) {
				DMS dms = (DMS) dms_mdl.getValueAt(row, 0);
				setSelectedDms(dms);
				return;
			}
			setSelectedDms(null);
		}
	};

	/** DMS double-click listener (for centering map) */
	private MouseAdapter dmsMouseAdapter = new MouseAdapter() {
		public void mousePressed(MouseEvent e) {
			ZTable t = (ZTable) e.getSource();
			Point p = e.getPoint();
			int row = t.getSelectedRow();
			if (e.getClickCount() == 2 && row >= 0) {
				DMS dms = (DMS) dms_mdl.getValueAt(row, 0);
				manager.centerMap(dms, 15);
			}
		}
	};

	/** Create a new alert DMS dispatcher */
	public AlertDmsDispatcher(Session s, AlertManager m) {
		manager = m;
		dms_manager = s.getDMSManager();
		dms_sign_groups = s.getSonarState().getDmsCache()
			.getDmsSignGroups();
		dms_tbl = new ZTable();
		dms_tbl.setAutoCreateColumnsFromModel(true);
		dms_tbl.setVisibleRowCount(8);
		dms_tbl.getSelectionModel().addListSelectionListener(
			dmsSelectionListener);
		dms_tbl.addMouseListener(dmsMouseAdapter);
		dms_mdl = new DmsIncludedModel();
		dms_tbl.setModel(dms_mdl);
	}

	/** Initialize the widgets on the panel */
	@Override
	public void initialize() {
		super.initialize();
		setTitle(I18N.get("alert.dms"));
		add(dms_tbl, Stretch.FULL);
	}

	/** Set the selected alert, updating the list of DMS. */
	public void setSelectedAlert(AlertInfo ai) {
		if (ai != null)
			updateDmsList(ai);
	}

	/** Clear the selected alert. */
	public void clearSelectedAlert() {
		dms_mdl.setRowCount(0);
		manager.updateMap();
	}

	/** Update the DMS list model with the list of DMS provided.  Runs a
	 *  SwingWorker to get DMS info before updating the GUI. */
	private void updateDmsList(AlertInfo ai) {
		IWorker<HashMap<DMS, Boolean>> dmsWorker =
			new IWorker<HashMap<DMS, Boolean>>()
		{
			@Override
			protected HashMap<DMS, Boolean> doInBackground() {
				return lookupDmsMap(ai);
			}

			@Override
			public void done() {
				HashMap<DMS, Boolean> dm = getResult();
				if (dm != null)
					finishUpdateDmsList(dm);
			}
		};
		dmsWorker.execute();
	}

	/** Lookup DMS for alert */
	private HashMap<DMS, Boolean> lookupDmsMap(AlertInfo ai) {
		// use a HashMap to support a check box for controlling
		// inclusion in the alert and a list to control sorting
		HashMap<DMS, Boolean> dm = new HashMap<DMS, Boolean>();
		if (ai != null) {
			// Find all DMS included in action plan
			ActionPlan plan = ai.getActionPlan();
			Set<DMS> included = DmsActionHelper.findSigns(plan);
			// Find all DMS for alert info
			SignGroup sg = ai.getSignGroup();
			for (DMS d: SignGroupHelper.getAllSigns(sg))
				dm.put(d, included.contains(d));
		}
		return dm;
	}

	/** Finish updating the DMS list */
	private void finishUpdateDmsList(HashMap<DMS, Boolean> dm) {
		ArrayList<DMS> dms_list = new ArrayList<DMS>();
		for (DMS dms: dm.keySet())
			dms_list.add(dms);
		dms_list.sort(new NumericAlphaComparator<DMS>());
		dms_mdl.setRowCount(0);
		for (DMS dms: dms_list) {
			Object[] row = {
				dms,
				GeoLocHelper.getLocation(dms.getGeoLoc()),
				dm.get(dms),
			};
			dms_mdl.addRow(row);
		}
		dms_mdl.fireTableDataChanged();
	}

	/** Set the selected DMS */
	public void setSelectedDms(DMS dms) {
		manager.setSelectedDms(dms);
		manager.updateMap();
	}

	/** Select a DMS at the specified point */
	public void selectDmsInTable(Point2D p) {
		MapObject mo = dms_manager.getLayerState().search(p);
		DMS dms = dms_manager.findProxy(mo);
		selectDmsInTable(dms);
	}

	/** Select a DMS in the table */
	private void selectDmsInTable(DMS dms) {
		for (int row = 0; row < dms_mdl.getRowCount(); row++) {
			Object qd = dms_mdl.getValueAt(row, 0);
			if (qd.equals(dms)) {
				dms_tbl.setRowSelectionInterval(row, row);
				dms_tbl.scrollRectToVisible(new Rectangle(
					dms_tbl.getCellRect(row, 0, true)));
				return;
			}
		}
		dms_tbl.clearSelection();
	}
}
