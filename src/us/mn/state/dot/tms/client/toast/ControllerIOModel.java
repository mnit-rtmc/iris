/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.toast;

import java.awt.Component;
import java.util.LinkedList;
import javax.swing.AbstractCellEditor;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.tms.Alarm;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.Controller;
import us.mn.state.dot.tms.ControllerIO;
import us.mn.state.dot.tms.ControllerIO_SONAR;
import us.mn.state.dot.tms.Detector;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.LCS;
import us.mn.state.dot.tms.LCSIndication;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.SonarState;
import us.mn.state.dot.tms.client.camera.CameraManager;
import us.mn.state.dot.tms.client.dms.DMSManager;
import us.mn.state.dot.tms.client.lcs.LCSManager;
import us.mn.state.dot.tms.client.meter.MeterManager;
import us.mn.state.dot.tms.client.warning.WarningSignManager;

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
		Alarm, Camera, Detector, DMS, LCS, LCSIndication, Ramp_Meter,
		Warning_Sign
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
		IO_TYPE.add(DeviceType.LCS);
		IO_TYPE.add(DeviceType.LCSIndication);
		IO_TYPE.add(DeviceType.Ramp_Meter);
		IO_TYPE.add(DeviceType.Warning_Sign);
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
		else if(cio instanceof LCS)
			return DeviceType.LCS;
		else if(cio instanceof LCSIndication)
			return DeviceType.LCSIndication;
		else if(cio instanceof RampMeter)
			return DeviceType.Ramp_Meter;
		else if(cio instanceof WarningSign)
			return DeviceType.Warning_Sign;
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

	/** Array of ControllerIO assignments */
	protected ControllerIO[] io;

	/** Array of ControllerIO device types */
	protected DeviceType[] types;

	/** Available alarm model */
	protected final WrapperComboBoxModel a_model;

	/** Available camera model */
	protected final WrapperComboBoxModel c_model;

	/** Available detector model */
	protected final WrapperComboBoxModel dt_model;

	/** Available DMS model */
	protected final WrapperComboBoxModel dms_model;

	/** Available LCS model */
	protected final WrapperComboBoxModel lcs_model;

	/** Available LCS indication model */
	protected final WrapperComboBoxModel lcsi_model;

	/** Available ramp meter model */
	protected final WrapperComboBoxModel m_model;

	/** Available warning sign model */
	protected final WrapperComboBoxModel w_model;

	/** Model for null device type */
	protected final ComboBoxModel no_model = new DefaultComboBoxModel();

	/** Device combo box */
	protected final JComboBox d_combo = new JComboBox();

	/** Create a new controller IO model */
	public ControllerIOModel(Controller c, SonarState state) {
		controller = c;
		io = new ControllerIO[0];
		types = new DeviceType[0];
		a_model = new WrapperComboBoxModel(state.getAvailableAlarms(),
			 true);
		c_model = new WrapperComboBoxModel(
			Session.cam_manager_singleton.getStyleModel(
			CameraManager.STYLE_NO_CONTROLLER), true);
		dt_model = new WrapperComboBoxModel(
			Session.det_manager_singleton.getStyleModel(
			DetectorManager.STYLE_NO_CONTROLLER), true);
		dms_model = new WrapperComboBoxModel(
			Session.dms_manager_singleton.getStyleModel(
			DMSHelper.STYLE_NO_CONTROLLER), true);
		lcs_model = new WrapperComboBoxModel(
			Session.lcs_manager_singleton.getStyleModel(
			LCSManager.STYLE_NO_CONTROLLER), true);
		lcsi_model = new WrapperComboBoxModel(
			Session.lcsi_manager_singleton.getStyleModel(
			LCSIManager.STYLE_NO_CONTROLLER), true);
		m_model = new WrapperComboBoxModel(
			Session.meter_manager_singleton.getStyleModel(
			MeterManager.STYLE_NO_CONTROLLER), true);
		w_model = new WrapperComboBoxModel(
			Session.warn_manager_singleton.getStyleModel(
			WarningSignManager.STYLE_NO_CONTROLLER), true);
	}

	/** Set the array of controller IO assignments */
	public void setCio(ControllerIO[] cio) {
		io = cio;
		types = new DeviceType[cio.length];
		for(int i = 0; i < cio.length; i++)
			types[i] = getType(cio[i]);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				fireTableDataChanged();
			}
		});
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
	public boolean isCellEditable(int row, int column) {
		return column != COL_PIN;
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
			if(cio instanceof ControllerIO_SONAR) {
				ControllerIO_SONAR cio_s =
					(ControllerIO_SONAR)cio;
				cio_s.setController(null);
			}
			types[pin] = io_type;
			io[pin] = null;
		}
	}

	/** Set the device */
	protected void setDevice(int pin, Object value) {
		clearDevice(pin);
		ControllerIO cio = lookupControllerIO(types[pin], value);
		if(cio instanceof ControllerIO_SONAR) {
			ControllerIO_SONAR cio_s = (ControllerIO_SONAR)cio;
			cio_s.setPin(pin);
			cio_s.setController(controller);
		}
	}

	/** Clear the device at the specified pin */
	protected void clearDevice(int pin) {
		ControllerIO cio = io[pin];
		if(cio instanceof ControllerIO_SONAR) {
			ControllerIO_SONAR cio_s = (ControllerIO_SONAR)cio;
			cio_s.setController(null);
		}
	}

	/** Lookup the ControllerIO for the given value */
	protected ControllerIO lookupControllerIO(DeviceType d, Object value) {
		if(d == null || value == null)
			return null;
		switch(d) {
			case Alarm:
				return (Alarm)value;
			case Camera:
				return (Camera)value;
			case Detector:
				return (Detector)value;
			case DMS:
				return (DMS)value;
			case LCS:
				return (LCS)value;
			case LCSIndication:
				return (LCSIndication)value;
			case Ramp_Meter:
				return (RampMeter)value;
			case Warning_Sign:
				return (WarningSign)value;
			default:
				return null;
		}
	}

	/** Create the pin column */
	protected TableColumn createPinColumn() {
		TableColumn c = new TableColumn(COL_PIN, 44);
		c.setHeaderValue("Pin");
		return c;
	}

	/** Create the type column */
	protected TableColumn createTypeColumn() {
		TableColumn c = new TableColumn(COL_TYPE, 100);
		c.setHeaderValue("Type");
		JComboBox combo = new JComboBox(IO_TYPE.toArray());
		c.setCellEditor(new DefaultCellEditor(combo));
		return c;
	}

	/** Create the device column */
	protected TableColumn createDeviceColumn() {
		TableColumn c = new TableColumn(COL_DEVICE, 140);
		c.setHeaderValue("Device");
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
				return a_model;
			case Camera:
				return c_model;
			case Detector:
				return dt_model;
			case DMS:
				return dms_model;
			case LCS:
				return lcs_model;
			case LCSIndication:
				return lcsi_model;
			case Ramp_Meter:
				return m_model;
			case Warning_Sign:
				return w_model;
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
			d_combo.setModel(model);
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
				value, isSelected, hasFocus, row, column);
		}
	}

	/** Create the table column model */
	public TableColumnModel createColumnModel() {
		TableColumnModel m = new DefaultTableColumnModel();
		m.addColumn(createPinColumn());
		m.addColumn(createTypeColumn());
		m.addColumn(createDeviceColumn());
		return m;
	}
}
