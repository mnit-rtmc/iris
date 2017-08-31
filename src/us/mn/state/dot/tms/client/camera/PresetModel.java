/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2014-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera;

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;
import static us.mn.state.dot.tms.client.widget.SwingRunner.runSwing;
import us.mn.state.dot.sonar.client.ProxyListener;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraPreset;
import us.mn.state.dot.tms.Direction;
import static us.mn.state.dot.tms.CameraPreset.MAX_PRESET;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyColumn;

/**
 * Table model for camera presets.
 *
 * @author Douglas Lau
 */
public class PresetModel extends AbstractTableModel {

	/** User session */
	private final Session session;

	/** Proxy type cache */
	private final TypeCache<CameraPreset> cache;

	/** Proxy columns */
	private final ArrayList<ProxyColumn<CameraPreset>> columns;

	/** Camera to assign presets */
	private final Camera camera;

	/** Array of all presets */
	private final CameraPreset[] proxies = new CameraPreset[MAX_PRESET];

	/** Listener for SONAR proxy events */
	private final ProxyListener<CameraPreset> proxy_listener =
		new ProxyListener<CameraPreset>()
	{
		public void proxyAdded(final CameraPreset proxy) {
			// Don't hog the SONAR TaskProcessor thread
			runSwing(new Runnable() {
				public void run() {
					proxyAddedSwing(proxy);
				}
			});
		}
		public void proxyRemoved(final CameraPreset proxy) {
			// Don't hog the SONAR TaskProcessor thread
			runSwing(new Runnable() {
				public void run() {
					proxyRemovedSwing(proxy);
				}
			});
		}
		public void proxyChanged(final CameraPreset proxy,
			final String attrib)
		{
			// Don't hog the SONAR TaskProcessor thread
			runSwing(new Runnable() {
				public void run() {
					proxyChangedSwing(proxy, attrib);
				}
			});
		}
		public void enumerationComplete() {
			// Nothing to do
		}
	};

	/** Create a new camera preset table model */
	public PresetModel(Session s, Camera c) {
		session = s;
		cache = s.getSonarState().getCamCache().getPresets();
		camera = c;
		columns = createColumns();
	}

	/** Initialize the proxy table model */
	public void initialize() {
		cache.addProxyListener(proxy_listener);
	}

	/** Dispose of the proxy table model */
	public void dispose() {
		cache.removeProxyListener(proxy_listener);
	}

	/** Column for preset number */
	private final ProxyColumn<CameraPreset> col_number =
		new ProxyColumn<CameraPreset>("camera.preset.number", 60,
		                              Integer.class)
	{
		public Object getValueAt(CameraPreset c) {
			return c.getPresetNum();
		}
	};

	/** Column for preset enabled */
	private final ProxyColumn<CameraPreset> col_enabled =
		new ProxyColumn<CameraPreset>("camera.preset.enabled", 60,
		                              Boolean.class)
	{
		public Object getValueAt(CameraPreset c) {
			return c != null;
		}
		public boolean isEditable(CameraPreset c) {
			return ((c == null) && canAdd())
			    || ((c != null) && canWrite(c));
		}
	};

	/** Column for preset direction */
	private final ProxyColumn<CameraPreset> col_direction =
		new ProxyColumn<CameraPreset>("camera.preset.direction", 120)
	{
		public Object getValueAt(CameraPreset p) {
			return Direction.fromOrdinal(p.getDirection());
		}
		public boolean isEditable(CameraPreset p) {
			return canWrite(p, "direction");
		}
		public void setValueAt(CameraPreset p, Object value) {
			if (value instanceof Direction) {
				Direction d = (Direction) value;
				p.setDirection((short) d.ordinal());
			}
		}
		protected TableCellEditor createCellEditor() {
			JComboBox<Direction> cbx =
				new JComboBox<Direction>(Direction.values());
			return new DefaultCellEditor(cbx);
		}
	};

	/** Create the columns in the model */
	private ArrayList<ProxyColumn<CameraPreset>> createColumns() {
		ArrayList<ProxyColumn<CameraPreset>> cols =
			new ArrayList<ProxyColumn<CameraPreset>>(3);
		cols.add(col_number);
		cols.add(col_enabled);
		cols.add(col_direction);
		return cols;
	}

	/** Get the count of columns in the table */
	@Override
	public int getColumnCount() {
		return columns.size();
	}

	/** Get the proxy column at the given column index */
	public ProxyColumn<CameraPreset> getProxyColumn(int col) {
		return (col >= 0 && col < columns.size())
		      ? columns.get(col)
		      : null;
	}

	/** Get the class of the specified column */
	@Override
	public Class getColumnClass(int col) {
		ProxyColumn<CameraPreset> pc = getProxyColumn(col);
		return (pc != null) ? pc.getColumnClass() : null;
	}

	/** Get the value at the specified cell */
	@Override
	public Object getValueAt(int row, int col) {
		ProxyColumn<CameraPreset> pc = getProxyColumn(col);
		if (pc == col_number)
			return row + 1;
		else if (pc != null) {
			CameraPreset proxy = getRowProxy(row);
			if (proxy != null)
				return pc.getValueAt(proxy);
		}
		return null;
	}

	/** Check if the specified cell is editable */
	@Override
	public boolean isCellEditable(int row, int col) {
		ProxyColumn<CameraPreset> pc = getProxyColumn(col);
		return (pc != null) && pc.isEditable(getRowProxy(row));
	}

	/** Set the value at the specified cell */
	@Override
	public void setValueAt(Object value, int row, int col) {
		ProxyColumn<CameraPreset> pc = getProxyColumn(col);
		if (pc == col_enabled) {
			if (value instanceof Boolean)
				setPresetEnabled(row, (Boolean)value);
		} else if (pc != null)
			pc.setValueAt(getRowProxy(row), value);
	}

	/** Set preset enabled value */
	private void setPresetEnabled(int row, boolean e) {
		if (e)
			createPreset(row + 1);
		else
			destroyPreset(row);
	}

	/** Create a new camera preset.
	 * @param pn Preset number */
	private void createPreset(int pn) {
		String name = createUniqueName();
		if (canAdd(name)) {
			HashMap<String, Object> attrs =
				new HashMap<String, Object>();
			attrs.put("camera", camera);
			attrs.put("preset_num", new Integer(pn));
			cache.createObject(name, attrs);
		}
	}

	/** Create a unique preset name */
	private String createUniqueName() {
		for (int uid = 1; uid <= 99999; uid++) {
			String n = "PRE_" + uid;
			if (cache.lookupObject(n) == null)
				return n;
		}
		assert false;
		return null;
	}

	/** Delete the specified row */
	private void destroyPreset(int row) {
		CameraPreset proxy = getRowProxy(row);
		if (canWrite(proxy))
			proxy.destroy();
	}

	/** Get the count of rows in the table */
	@Override
	public int getRowCount() {
		return MAX_PRESET;
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		for (int i = 0; i < columns.size(); ++i)
			columns.get(i).addColumn(m, i);
		return m;
	}

	/** Fire an update event */
	private void fireUpdate(final int row) {
		if (row >= 0)
			fireTableRowsUpdated(row, row);
	}

	/** Add a new proxy to the table model */
	private void proxyAddedSwing(CameraPreset proxy) {
		fireUpdate(doProxyAdded(proxy));
	}

	/** Add a new proxy to the table model */
	private int doProxyAdded(CameraPreset proxy) {
		if (proxy.getCamera() == camera) {
			int row = proxy.getPresetNum() - 1;
			if (row >= 0 && row < MAX_PRESET) {
				proxies[row] = proxy;
				return row;
			}
		}
		return -1;
	}

	/** Remove a proxy from the table model */
	private void proxyRemovedSwing(CameraPreset proxy) {
		fireUpdate(doProxyRemoved(proxy));
	}

	/** Remove a proxy from the table model */
	private int doProxyRemoved(CameraPreset proxy) {
		int row = getRow(proxy);
		if (row >= 0)
			proxies[row] = null;
		return row;
	}

	/** Change a proxy in the table model */
	private void proxyChangedSwing(CameraPreset proxy, String attrib) {
		fireUpdate(getRow(proxy));
	}

	/** Get the proxy at the specified row */
	public CameraPreset getRowProxy(int row) {
		return (row >= 0 && row < MAX_PRESET) ? proxies[row] : null;
	}

	/** Get the row for the specified proxy.
	 * @param proxy Proxy object.
	 * @return Row number in table, or -1. */
	private int getRow(CameraPreset proxy) {
		for (int row = 0; row < proxies.length; row++) {
			if (proxies[row] == proxy)
				return row;
		}
		return -1;
	}

	/** Check if the user can add a proxy */
	public boolean canAdd(String n) {
		String tname = getSonarType();
		return (tname != null) ? session.canAdd(tname, n) : false;
	}

	/** Check if the user can add a proxy */
	public boolean canAdd() {
		return canAdd("oname");
	}

	/** Get the SONAR type name */
	private String getSonarType() {
		return CameraPreset.SONAR_TYPE;
	}

	/** Check if the user can write a proxy */
	public boolean canWrite(CameraPreset proxy) {
		return session.canWrite(proxy);
	}

	/** Check if the user can write a proxy */
	public boolean canWrite(CameraPreset proxy, String aname) {
		return session.canWrite(proxy, aname);
	}
}
