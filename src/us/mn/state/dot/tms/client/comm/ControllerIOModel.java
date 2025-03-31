/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2025  Minnesota Department of Transportation
 * Copyright (C) 2016-2017  SRF Consulting Group
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
package us.mn.state.dot.tms.client.comm;

import java.awt.Component;
import java.util.LinkedList;
import javax.swing.AbstractCellEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.Alarm;
import us.mn.state.dot.tms.Beacon;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.FlowStream;
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.Gps;
import us.mn.state.dot.tms.Lcs;
import us.mn.state.dot.tms.LcsIndication;
import us.mn.state.dot.tms.LcsState;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.TagReader;
import us.mn.state.dot.tms.VideoMonitor;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.IListCellRenderer;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Special table model for Controller I/O pins.
 *
 * @author Douglas Lau
 * @author John L. Stanley
 */
public class ControllerIOModel extends AbstractTableModel {

	/** Count of columns in table model */
	static private final int COLUMN_COUNT = 3;

	/** Pin column number */
	static private final int COL_PIN = 0;

	/** Device type column number */
	static private final int COL_TYPE = 1;

	/** Device column number */
	static private final int COL_DEVICE = 2;

	/** Device types which can be associated with controller IO */
	private enum DeviceType {
		Alarm, Camera, Detector, DMS, Flow_Stream, Gate_Arm, Gps,
		Lcs, Lcs_State, Ramp_Meter, Beacon, Video_Monitor,
		Weather_Sensor, Tag_Reader
	}

	/** Types of IO devices */
	static private final LinkedList<DeviceType> IO_TYPE =
		new LinkedList<DeviceType>();
	static {
		IO_TYPE.add(null);
		IO_TYPE.add(DeviceType.Alarm);
		IO_TYPE.add(DeviceType.Camera);
		IO_TYPE.add(DeviceType.Detector);
		IO_TYPE.add(DeviceType.DMS);
		IO_TYPE.add(DeviceType.Flow_Stream);
		IO_TYPE.add(DeviceType.Gate_Arm);
		IO_TYPE.add(DeviceType.Gps);
		IO_TYPE.add(DeviceType.Lcs);
		IO_TYPE.add(DeviceType.Lcs_State);
		IO_TYPE.add(DeviceType.Ramp_Meter);
		IO_TYPE.add(DeviceType.Beacon);
		IO_TYPE.add(DeviceType.Video_Monitor);
		IO_TYPE.add(DeviceType.Weather_Sensor);
		IO_TYPE.add(DeviceType.Tag_Reader);
	}

	/** Get the type of the specified ControllerIO device */
	static private DeviceType getType(ControllerIO cio) {
		if (cio instanceof Alarm)
			return DeviceType.Alarm;
		else if (cio instanceof Camera)
			return DeviceType.Camera;
		else if (cio instanceof Detector)
			return DeviceType.Detector;
		else if (cio instanceof DMS)
			return DeviceType.DMS;
		else if (cio instanceof FlowStream)
			return DeviceType.Flow_Stream;
		else if (cio instanceof GateArm)
			return DeviceType.Gate_Arm;
		else if (cio instanceof Gps)
			return DeviceType.Gps;
		else if (cio instanceof Lcs)
			return DeviceType.Lcs;
		else if (cio instanceof LcsState)
			return DeviceType.Lcs_State;
		else if (cio instanceof RampMeter)
			return DeviceType.Ramp_Meter;
		else if (cio instanceof Beacon)
			return DeviceType.Beacon;
		else if (cio instanceof VideoMonitor)
			return DeviceType.Video_Monitor;
		else if (cio instanceof WeatherSensor)
			return DeviceType.Weather_Sensor;
		else if (cio instanceof TagReader)
			return DeviceType.Tag_Reader;
		else
			return null;
	}

	/** Compare two device types for equality */
	static private boolean compareTypes(DeviceType t0, DeviceType t1) {
		if (t0 == null)
			return t1 == null;
		else
			return t0 == t1;
	}

	/** Login session */
	private final Session session;

	/** SONAR state */
	private final SonarState state;

	/** Controller object */
	private final Controller controller;

	/** Device cell editor */
	private final DeviceCellEditor cell_editor;

	/** Array of ControllerIO assignments */
	private final ControllerIO[] io;

	/** Array of ControllerIO device types */
	private final DeviceType[] types;

	/** Controller IO list for null device type */
	private final ControllerIOList null_list;

	/** Controller IO list for alarms */
	private final ControllerIOList a_list;

	/** Controller IO list for cameras */
	private final ControllerIOList c_list;

	/** Controller IO list for detectors */
	private final ControllerIOList dt_list;

	/** Controller IO list for DMSs */
	private final ControllerIOList dms_list;

	/** Controller IO list for flow streams */
	private final ControllerIOList flow_list;

	/** Controller IO list for gate arms */
	private final ControllerIOList gate_list;

	/** Controller IO list for Gps */
	private final ControllerIOList gps_list;

	/** Controller IO list for LCS arrays */
	private final ControllerIOList lcs_list;

	/** Controller IO list for LCS states */
	private final ControllerIOList lcs_state_list;

	/** Controller IO list for ramp meters */
	private final ControllerIOList m_list;

	/** Controller IO list for video monitors */
	private final ControllerIOList v_list;

	/** Controller IO list for beacons */
	private final ControllerIOList b_list;

	/** Controller IO list for weather sensors */
	private final ControllerIOList wsensor_list;

	/** Controller IO list for toll readers */
	private final ControllerIOList tr_list;

	/** Device combo box */
	private final JComboBox<ControllerIO> d_combo =
		new JComboBox<ControllerIO>();

	/** Create a new controller IO model */
	public ControllerIOModel(Session s, Controller c) {
		session = s;
		state = s.getSonarState();;
		controller = c;
		cell_editor = new DeviceCellEditor();
		io = new ControllerIO[Controller.ALL_PINS + 1];
		types = new DeviceType[Controller.ALL_PINS + 1];
		d_combo.setRenderer(new IListCellRenderer<ControllerIO>() {
			@Override
			protected String valueToString(ControllerIO value) {
				return getDeviceLabel(value).toString();
			}
		});
		null_list = new ControllerIOList(null);
		a_list = new ControllerIOList(state.getAlarms());
		c_list = new ControllerIOList(state.getCamCache().getCameras());
		dt_list = new ControllerIOList(
			state.getDetCache().getDetectors());
		dms_list = new ControllerIOList(state.getDmsCache().getDMSs());
		flow_list = new ControllerIOList(
			state.getCamCache().getFlowStreams());
		gate_list = new ControllerIOList(state.getGateArms());
		gps_list = new ControllerIOList<Gps>(state.getGpses());
		lcs_list = new ControllerIOList(state.getLcsCache().getLcss());
		lcs_state_list = new ControllerIOList(
			state.getLcsCache().getLcsStates());
		m_list = new ControllerIOList(state.getRampMeters());
		v_list = new ControllerIOList(
			state.getCamCache().getVideoMonitors());
		b_list = new ControllerIOList(state.getBeacons());
		wsensor_list = new ControllerIOList(state.getWeatherSensors());
		tr_list = new ControllerIOList(state.getTagReaders());
	}

	/** Controller IO list model */
	private class ControllerIOList<T extends ControllerIO>
		extends ProxyListModel<T>
	{
		private final IComboBoxModel<T> model;
		private final CellEditorComboBoxModel<T> editor_mdl;
		@SuppressWarnings("unchecked")
		private ControllerIOList(TypeCache c) {
			super(c);
			model = new IComboBoxModel<T>(this);
			editor_mdl = new CellEditorComboBoxModel<T>(
				cell_editor, model);
		}
		@Override
		protected boolean check(T p) {
			addIO(p);
			return p.getController() == null;
		}
		@Override
		protected void checkRemove(T p) {
			removeIO(p);
		}
	}

	/** Initialize the model */
	public void initialize() {
		a_list.initialize();
		c_list.initialize();
		dt_list.initialize();
		dms_list.initialize();
		flow_list.initialize();
		gate_list.initialize();
		gps_list.initialize();
		lcs_list.initialize();
		lcs_state_list.initialize();
		m_list.initialize();
		v_list.initialize();
		b_list.initialize();
		wsensor_list.initialize();
		tr_list.initialize();
	}

	/** Dispose of the model */
	public void dispose() {
		a_list.dispose();
		c_list.dispose();
		dt_list.dispose();
		dms_list.dispose();
		flow_list.dispose();
		gate_list.dispose();
		gps_list.dispose();
		lcs_list.dispose();
		lcs_state_list.dispose();
		m_list.dispose();
		v_list.dispose();
		b_list.dispose();
		wsensor_list.dispose();
		tr_list.dispose();
	}

	/** Get the count of columns in the table */
	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the count of rows in the table */
	@Override
	public int getRowCount() {
		return io.length - 1;
	}

	/** Get the value at the specified cell */
	@Override
	public Object getValueAt(int row, int column) {
		int pin = row + 1;
		switch (column) {
		case COL_PIN:
			return pin;
		case COL_TYPE:
			return types[pin];
		case COL_DEVICE:
			return io[pin];
		default:
			return null;
		}
	}

	/** Is the specified cell editable? */
	@Override
	public boolean isCellEditable(int row, int col) {
		return col != COL_PIN && canWriteIO();
	}

	/** Check if the user can write device IO */
	private boolean canWriteIO() {
		return canWriteIO(Alarm.SONAR_TYPE) &&
		       canWriteIO(Camera.SONAR_TYPE) &&
		       canWriteIO(Detector.SONAR_TYPE) &&
		       canWriteIO(DMS.SONAR_TYPE) &&
		       canWriteIO(GateArm.SONAR_TYPE) &&
		       canWriteIO(Lcs.SONAR_TYPE) &&
		       canWriteIO(LcsState.SONAR_TYPE) &&
		       canWriteIO(RampMeter.SONAR_TYPE) &&
		       canWriteIO(Beacon.SONAR_TYPE) &&
		       canWriteIO(WeatherSensor.SONAR_TYPE) &&
		       canWriteIO(TagReader.SONAR_TYPE);
	}

	/** Check if the user can write one device IO */
	private boolean canWriteIO(String tname) {
		return session.canWrite(tname, "pin") &&
		       session.canWrite(tname, "controller");
	}

	/** Set the value of one cell in the table */
	@Override
	public void setValueAt(Object value, int row, int column) {
		int pin = row + 1;
		switch (column) {
		case COL_TYPE:
			setDeviceType(pin, (DeviceType) value);
			break;
		case COL_DEVICE:
			setDevice(pin, value);
			break;
		}
	}

	/** Set the device type */
	private void setDeviceType(int pin, DeviceType io_type) {
		if (io_type != types[pin]) {
			clearDevice(pin);
			types[pin] = io_type;
			io[pin] = null;
		}
	}

	/** Set the device */
	private void setDevice(int pin, Object value) {
		DeviceType io_type = types[pin];
		clearDevice(pin);
		if (value instanceof ControllerIO)
			setDeviceIO(pin, (ControllerIO) value);
	}

	/** Set the device IO */
	private void setDeviceIO(int pin, ControllerIO cio) {
		cio.setPin(pin);
		cio.setController(controller);
	}

	/** Clear the device at the specified pin */
	private void clearDevice(int pin) {
		ControllerIO cio = io[pin];
		DeviceType io_type = types[pin];
		if (cio != null)
			cio.setController(null);
	}

	/** Create the pin column */
	private TableColumn createPinColumn() {
		TableColumn c = new TableColumn(COL_PIN, 44);
		c.setHeaderValue(I18N.get("controller.pin"));
		return c;
	}

	/** Create the type column */
	private TableColumn createTypeColumn() {
		TableColumn c = new TableColumn(COL_TYPE, 100);
		c.setHeaderValue(I18N.get("device.type"));
		JComboBox<DeviceType> cbx = new JComboBox<DeviceType>(
			IO_TYPE.toArray(new DeviceType[0]));
		c.setCellEditor(new DefaultCellEditor(cbx));
		return c;
	}

	/** Create the device column */
	private TableColumn createDeviceColumn() {
		TableColumn c = new TableColumn(COL_DEVICE, 140);
		c.setHeaderValue(I18N.get("device"));
		c.setCellEditor(cell_editor);
		c.setCellRenderer(new DeviceCellRenderer());
		return c;
	}

	/** Inner class for editing cells in the device column */
	protected class DeviceCellEditor extends AbstractCellEditor
		implements TableCellEditor
	{
		@SuppressWarnings("unchecked")
		public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column)
		{
			int pin = row + 1;
			ControllerIOList io_list = getIOList(types[pin]);
			io_list.model.setSelectedItem(value);
			d_combo.setModel(io_list.editor_mdl);
			return d_combo;
		}
		public Object getCellEditorValue() {
			return d_combo.getSelectedItem();
		}
	}

	/** Lookup a controller IO list for a given device type */
	private ControllerIOList getIOList(DeviceType d) {
		if (d == null)
			return null_list;
		switch (d) {
		case Alarm:
			return a_list;
		case Camera:
			return c_list;
		case Detector:
			return dt_list;
		case DMS:
			return dms_list;
		case Flow_Stream:
			return flow_list;
		case Gate_Arm:
			return gate_list;
		case Gps:
			return gps_list;
		case Lcs:
			return lcs_list;
		case Lcs_State:
			return lcs_state_list;
		case Ramp_Meter:
			return m_list;
		case Video_Monitor:
			return v_list;
		case Beacon:
			return b_list;
		case Weather_Sensor:
			return wsensor_list;
		case Tag_Reader:
			return tr_list;
		default:
			return null_list;
		}
	}

	/** Inner class for rendering cells in the device column */
	protected class DeviceCellRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus,
			int row, int column)
		{
			return super.getTableCellRendererComponent(table,
				getDeviceLabel(value), isSelected, hasFocus,
				row, column);
		}
	}

	/** Get a device label (normally name) */
	private Object getDeviceLabel(Object value) {
		if (value instanceof LcsState) {
			LcsState ls = (LcsState) value;
			Lcs lcs = ls.getLcs();
			LcsIndication li = LcsIndication.fromOrdinal(
				ls.getIndication());
			return lcs.getName() + ' ' + ls.getLane() + ' ' +
				li.description;
		} else
			return value;
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createPinColumn());
		m.addColumn(createTypeColumn());
		m.addColumn(createDeviceColumn());
		return m;
	}

	/** Update one pin */
	private void updatePin(int pin, ControllerIO p, DeviceType io_type) {
		if (pin > 0 && pin < io.length) {
			io[pin] = p;
			types[pin] = io_type;
			int row = pin - 1;
			fireTableRowsUpdated(row, row);
		}
	}

	/** Add an IO to a pin on the controller */
	private void addIO(ControllerIO p) {
		if (p.getController() == controller)
			updatePin(p.getPin(), p, getType(p));
	}

	/** Remove an IO from a pin on the controller */
	private void removeIO(ControllerIO p) {
		for (int pin = 1; pin < io.length; pin++) {
			if (io[pin] == p)
				updatePin(pin, null, null);
		}
	}
}
