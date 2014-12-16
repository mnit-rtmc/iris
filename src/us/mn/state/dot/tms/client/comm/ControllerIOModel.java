/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2014  Minnesota Department of Transportation
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
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
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
import us.mn.state.dot.tms.GateArm;
import us.mn.state.dot.tms.LaneMarking;
import us.mn.state.dot.tms.LaneUseIndication;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSIndication;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.TagReader;
import us.mn.state.dot.tms.WeatherSensor;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.proxy.ProxyListModel;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Special table model for Controller I/O pins.
 *
 * @author Douglas Lau
 */
public class ControllerIOModel extends AbstractTableModel {

	/** Count of columns in table model */
	static protected final int COLUMN_COUNT = 3;

	/** Pin column number */
	static protected final int COL_PIN = 0;

	/** Device type column number */
	static protected final int COL_TYPE = 1;

	/** Device column number */
	static protected final int COL_DEVICE = 2;

	/** Device types which can be associated with controller IO */
	protected enum DeviceType {
		Alarm, Camera, Detector, DMS, Gate_Arm, Lane_Marking,
		LCSIndication, Ramp_Meter, Beacon, Weather_Sensor, Tag_Reader
	}

	/** Types of IO devices */
	static protected final LinkedList<DeviceType> IO_TYPE =
		new LinkedList<DeviceType>();
	static {
		IO_TYPE.add(null);
		IO_TYPE.add(DeviceType.Alarm);
		IO_TYPE.add(DeviceType.Camera);
		IO_TYPE.add(DeviceType.Detector);
		IO_TYPE.add(DeviceType.DMS);
		IO_TYPE.add(DeviceType.Gate_Arm);
		IO_TYPE.add(DeviceType.Lane_Marking);
		IO_TYPE.add(DeviceType.LCSIndication);
		IO_TYPE.add(DeviceType.Ramp_Meter);
		IO_TYPE.add(DeviceType.Beacon);
		IO_TYPE.add(DeviceType.Weather_Sensor);
		IO_TYPE.add(DeviceType.Tag_Reader);
	}

	/** Get the type of the specified ControllerIO device */
	static protected DeviceType getType(ControllerIO cio) {
		if(cio instanceof Alarm)
			return DeviceType.Alarm;
		else if(cio instanceof Camera)
			return DeviceType.Camera;
		else if(cio instanceof Detector)
			return DeviceType.Detector;
		else if(cio instanceof DMS)
			return DeviceType.DMS;
		else if(cio instanceof GateArm)
			return DeviceType.Gate_Arm;
		else if(cio instanceof LaneMarking)
			return DeviceType.Lane_Marking;
		else if(cio instanceof LCSIndication)
			return DeviceType.LCSIndication;
		else if(cio instanceof RampMeter)
			return DeviceType.Ramp_Meter;
		else if(cio instanceof Beacon)
			return DeviceType.Beacon;
		else if(cio instanceof WeatherSensor)
			return DeviceType.Weather_Sensor;
		else if (cio instanceof TagReader)
			return DeviceType.Tag_Reader;
		else
			return null;
	}

	/** Compare two device types for equality */
	static protected boolean compareTypes(DeviceType t0, DeviceType t1) {
		if(t0 == null)
			return t1 == null;
		else
			return t0 == t1;
	}

	/** Controller object */
	protected final Controller controller;

	/** Login session */
	protected final Session session;

	/** SONAR state */
	protected final SonarState state;

	/** Array of ControllerIO assignments */
	protected final ControllerIO[] io;

	/** Array of ControllerIO device types */
	protected final DeviceType[] types;

	/** Controller IO list for alarms */
	private final ControllerIOList<Alarm> a_list;

	/** Controller IO list for cameras */
	private final ControllerIOList<Camera> c_list;

	/** Controller IO list for detectors */
	private final ControllerIOList<Detector> dt_list;

	/** Controller IO list for DMSs */
	private final ControllerIOList<DMS> dms_list;

	/** Controller IO list for gate arms */
	private final ControllerIOList<GateArm> gate_list;

	/** Controller IO list for lane markings */
	private final ControllerIOList<LaneMarking> lmark_list;

	/** Controller IO list for LCS indications */
	private final ControllerIOList<LCSIndication> lcsi_list;

	/** Controller IO list for ramp meters */
	private final ControllerIOList<RampMeter> m_list;

	/** Controller IO list for beacons */
	private final ControllerIOList<Beacon> b_list;

	/** Controller IO list for weather sensors */
	private final ControllerIOList<WeatherSensor> wsensor_list;

	/** Controller IO list for toll readers */
	private final ControllerIOList<TagReader> tr_list;

	/** Model for null device type */
	private final ComboBoxModel no_model = new DefaultComboBoxModel();

	/** Device combo box */
	protected final JComboBox d_combo = new JComboBox();

	/** Create a new controller IO model */
	public ControllerIOModel(Session s, Controller c) {
		session = s;
		state = s.getSonarState();;
		controller = c;
		io = new ControllerIO[Controller.ALL_PINS];
		types = new DeviceType[Controller.ALL_PINS];
		d_combo.setRenderer(new DeviceComboRenderer());
		a_list = new ControllerIOList<Alarm>(state.getAlarms());
		c_list = new ControllerIOList<Camera>(
			state.getCamCache().getCameras());
		dt_list = new ControllerIOList<Detector>(
			state.getDetCache().getDetectors());
		dms_list = new ControllerIOList<DMS>(
			state.getDmsCache().getDMSs());
		gate_list = new ControllerIOList<GateArm>(
			state.getGateArms());
		lmark_list = new ControllerIOList<LaneMarking>(
			state.getLaneMarkings());
		lcsi_list = new ControllerIOList<LCSIndication>(
			state.getLcsCache().getLCSIndications());
		m_list = new ControllerIOList<RampMeter>(
			state.getRampMeters());
		b_list = new ControllerIOList<Beacon>(
			state.getBeacons());
		wsensor_list = new ControllerIOList<WeatherSensor>(
			state.getWeatherSensors());
		tr_list = new ControllerIOList<TagReader>(
			state.getTagReaders());
	}

	/** Controller IO list model */
	private class ControllerIOList<T extends ControllerIO>
		extends ProxyListModel<T>
	{
		private final IComboBoxModel<T> model;
		private ControllerIOList(TypeCache<T> c) {
			super(c);
			model = new IComboBoxModel<T>(this);
		}
		@Override
		protected boolean check(T p) {
			addIO(p);
			return p.getController() == null;
		}
		@Override
		protected int doProxyRemoved(T p) {
			removeIO(p);
			return super.doProxyRemoved(p);
		}
	}

	/** Initialize the model */
	public void initialize() {
		a_list.initialize();
		c_list.initialize();
		dt_list.initialize();
		dms_list.initialize();
		gate_list.initialize();
		lmark_list.initialize();
		lcsi_list.initialize();
		m_list.initialize();
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
		gate_list.dispose();
		lmark_list.dispose();
		lcsi_list.dispose();
		m_list.dispose();
		b_list.dispose();
		wsensor_list.dispose();
		tr_list.dispose();
	}

	/** Get the count of columns in the table */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/** Get the count of rows in the table */
	public int getRowCount() {
		return io.length - 1;
	}

	/** Get the value at the specified cell */
	public Object getValueAt(int row, int column) {
		int pin = row + 1;
		switch(column) {
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
	public boolean isCellEditable(int row, int col) {
		return col != COL_PIN && canUpdateIO();
	}

	/** Check if the user can update device IO */
	protected boolean canUpdateIO() {
		return canUpdateIO(Alarm.SONAR_TYPE) &&
		       canUpdateIO(Camera.SONAR_TYPE) &&
		       canUpdateIO(Detector.SONAR_TYPE) &&
		       canUpdateIO(DMS.SONAR_TYPE) &&
		       canUpdateIO(GateArm.SONAR_TYPE) &&
		       canUpdateIO(LaneMarking.SONAR_TYPE) &&
		       canUpdateIO(LCSIndication.SONAR_TYPE) &&
		       canUpdateIO(RampMeter.SONAR_TYPE) &&
		       canUpdateIO(Beacon.SONAR_TYPE) &&
		       canUpdateIO(WeatherSensor.SONAR_TYPE) &&
		       canUpdateIO(TagReader.SONAR_TYPE);
	}

	/** Check if the user can update one device IO */
	protected boolean canUpdateIO(String tname) {
		return session.canUpdate(tname, "pin") &&
		       session.canUpdate(tname, "controller");
	}

	/** Set the value of one cell in the table */
	public void setValueAt(Object value, int row, int column) {
		int pin = row + 1;
		switch(column) {
		case COL_TYPE:
			setDeviceType(pin, (DeviceType)value);
			break;
		case COL_DEVICE:
			setDevice(pin, value);
			break;
		}
	}

	/** Set the device type */
	protected void setDeviceType(int pin, DeviceType io_type) {
		int row = pin - 1;
		if(io_type != types[pin]) {
			ControllerIO cio = io[pin];
			if(cio != null)
				cio.setController(null);
			types[pin] = io_type;
			io[pin] = null;
		}
	}

	/** Set the device */
	protected void setDevice(int pin, Object value) {
		clearDevice(pin);
		if(value instanceof ControllerIO) {
			ControllerIO cio = (ControllerIO)value;
			cio.setPin(pin);
			cio.setController(controller);
		}
	}

	/** Clear the device at the specified pin */
	protected void clearDevice(int pin) {
		ControllerIO cio = io[pin];
		if(cio != null)
			cio.setController(null);
	}

	/** Create the pin column */
	protected TableColumn createPinColumn() {
		TableColumn c = new TableColumn(COL_PIN, 44);
		c.setHeaderValue(I18N.get("controller.pin"));
		return c;
	}

	/** Create the type column */
	protected TableColumn createTypeColumn() {
		TableColumn c = new TableColumn(COL_TYPE, 100);
		c.setHeaderValue(I18N.get("device.type"));
		JComboBox combo = new JComboBox(IO_TYPE.toArray());
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Create the device column */
	protected TableColumn createDeviceColumn() {
		TableColumn c = new TableColumn(COL_DEVICE, 140);
		c.setHeaderValue(I18N.get("device"));
		c.setCellEditor(new DeviceCellEditor());
		c.setCellRenderer(new DeviceCellRenderer());
		return c;
	}

	/** Get the device model for the given device type */
	protected ComboBoxModel getDeviceModel(DeviceType d) {
		if(d == null)
			return no_model;
		switch(d) {
		case Alarm:
			return a_list.model;
		case Camera:
			return c_list.model;
		case Detector:
			return dt_list.model;
		case DMS:
			return dms_list.model;
		case Gate_Arm:
			return gate_list.model;
		case Lane_Marking:
			return lmark_list.model;
		case LCSIndication:
			return lcsi_list.model;
		case Ramp_Meter:
			return m_list.model;
		case Beacon:
			return b_list.model;
		case Weather_Sensor:
			return wsensor_list.model;
		case Tag_Reader:
			return tr_list.model;
		default:
			return no_model;
		}
	}

	/** Inner class for editing cells in the device column */
	protected class DeviceCellEditor extends AbstractCellEditor
		implements TableCellEditor
	{
		public Component getTableCellEditorComponent(JTable table,
			Object value, boolean isSelected, int row, int column)
		{
			int pin = row + 1;
			ComboBoxModel model = getDeviceModel(types[pin]);
			model.setSelectedItem(value);
			d_combo.setModel(new CellEditorComboBoxModel(this,
				model));
			return d_combo;
		}
		public Object getCellEditorValue() {
			return d_combo.getSelectedItem();
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

	/** Inner class for rendering combo editor in the device column */
	protected class DeviceComboRenderer extends DefaultListCellRenderer {
		public Component getListCellRendererComponent(JList list,
			Object value, int index, boolean isSelected,
			boolean cellHasFocus)
		{
			return super.getListCellRendererComponent(list,
				getDeviceLabel(value), index, isSelected,
				cellHasFocus);
		}
	}

	/** Get a device label (normally name) */
	protected Object getDeviceLabel(Object value) {
		if(value instanceof LCSIndication) {
			LCSIndication lcsi = (LCSIndication)value;
			LCS lcs = lcsi.getLcs();
			LaneUseIndication lui = LaneUseIndication.fromOrdinal(
				lcsi.getIndication());
			return lcs.getName() + " " + lui.description;
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

	/** Add an IO to a pin on the controller */
	private void addIO(ControllerIO p) {
		if (p.getController() == controller) {
			int pin = p.getPin();
			if (pin > 0 && pin < io.length) {
				io[pin] = p;
				types[pin] = getType(p);
				int row = pin - 1;
				fireTableRowsUpdated(row, row);
			}
		}
	}

	/** Remove an IO from a pin on the controller */
	protected void removeIO(ControllerIO p) {
		for(int pin = 0; pin < io.length; pin++) {
			if(io[pin] == p) {
				io[pin] = null;
				types[pin] = null;
				int row = pin - 1;
				fireTableRowsUpdated(row, row);
				return;
			}
		}
	}
}
