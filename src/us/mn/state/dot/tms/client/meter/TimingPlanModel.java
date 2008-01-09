/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.meter;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.table.AbstractTableModel;
import us.mn.state.dot.tms.MeterPlan;
import us.mn.state.dot.tms.RampMeter;
import us.mn.state.dot.tms.utils.AbstractJob;
import us.mn.state.dot.tms.utils.ExceptionDialog;

/**
 * Special table model for a ramp meter's timing plans.
 *
 * @author Douglas Lau
 */
public class TimingPlanModel extends AbstractTableModel {

	/** Count of columns in timing plan table model */
	static protected final int COLUMN_COUNT = 7;

	/** Plan type column number */
	static protected final int COL_TYPE = 0;

	/** Start time column number */
	static protected final int COL_START = 1;

	/** Stop time column number */
	static protected final int COL_STOP = 2;

	/** Target rate column number */
	static protected final int COL_TARGET = 3;

	/** Cycle time column number */
	static protected final int COL_CYCLE = 4;

	/** Active column number */
	static protected final int COL_ACTIVE = 5;

	/** Testing column number */
	static protected final int COL_TESTING = 6;

	/** Variable target rate/cycle time string */
	static protected final String VARIABLE = "Variable";

	/** Calculate the cycle time for a given release rate */
	static BigDecimal calculateCycleTime(int target) {
		return new BigDecimal(3600.0f / target).setScale(1,
			BigDecimal.ROUND_HALF_EVEN);
	}

	/** Time parser */
	static protected final DateFormat[] TIME = {
		new SimpleDateFormat("h:mm a"),
		new SimpleDateFormat("H:mm"),
		new SimpleDateFormat("h a"),
		new SimpleDateFormat("H")
	};

	/** Format a minute as string */
	static protected String time_string(int minute) {
		StringBuffer min = new StringBuffer();
		min.append(minute % 60);
		while(min.length() < 2)
			min.insert(0, '0');
		return (minute / 60) + ":" + min;
	}

	/** Parse a time entry */
	static protected Date parseTime(String t) throws ParseException {
		ParseException ex = null;
		for(int i = 0; i < TIME.length; i++) {
			try {
				return TIME[i].parse(t);
			}
			catch(ParseException e) {
				ex = e;
			}
		}
		throw ex;
	}

	/** Parse a minute entry */
	static protected int parseMinute(String t) throws ParseException {
		Calendar c = Calendar.getInstance();
		c.setTime(parseTime(t));
		return c.get(Calendar.HOUR_OF_DAY) * 60 +
			c.get(Calendar.MINUTE);
	}

	/** Remote ramp meter object */
	protected final RampMeter meter;

	/** Array of timing plans */
	protected MeterPlan[] plans;

	protected final Object[][] values;

	/** Create a new ramp meter timing plan model */
	public TimingPlanModel(RampMeter meter) throws RemoteException {
		this.meter = meter;
		plans = meter.getTimingPlans();
		values = new Object[plans.length][COLUMN_COUNT];
		for(int i = 0; i < plans.length; i++) {
			MeterPlan p = plans[i];
			values[i][COL_TYPE] = p.getPlanType();
			values[i][COL_START] = time_string(p.getStartTime());
			values[i][COL_STOP] = time_string(p.getStopTime());
			int target = p.getTarget(meter);
			if(values[i][COL_TYPE].equals("Stratified")) {
				values[i][COL_TARGET] = VARIABLE;
				values[i][COL_CYCLE] = VARIABLE;
			} else {
				values[i][COL_TARGET] = new Integer(target);
				values[i][COL_CYCLE] = calculateCycleTime(
					target);
			}
			values[i][COL_ACTIVE] = Boolean.valueOf(p.isActive());
			values[i][COL_TESTING] = Boolean.valueOf(p.isTesting());
		}
	}

	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	public int getRowCount() {
		return values.length;
	}

	public Object getValueAt(int row, int column) {
		return values[row][column];
	}

	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	public boolean isCellEditable(int row, int column) {
		if(values[row][column].equals(VARIABLE))
			return false;
		return column > COL_TYPE && column != COL_CYCLE;
	}

	/** Set the value of one cell in the table */
	public void setValueAt(Object value, int row, int column) {
		try {
			if(column == COL_START)
				setStart(value, row);
			if(column == COL_STOP)
				setStop(value, row);
			if(column == COL_TARGET)
				setTarget(value, row);
			if(column == COL_ACTIVE)
				setActive(value, row);
			if(column == COL_TESTING)
				setTesting(value, row);
		}
		catch(ParseException e) {
			new ExceptionDialog(e).setVisible(true);
		}
	}

	/** Set the start time for the given timing plan */
	protected void setStart(Object value, int plan)
		throws ParseException
	{
		final int minute = parseMinute(value.toString());
		final MeterPlan p = plans[plan];
		values[plan][COL_START] = time_string(minute);
		new AbstractJob() {
			public void perform() throws Exception {
				p.setStartTime(minute);
				meter.notifyUpdate();
			}
		}.addToScheduler();
	}

	/** Set the stop time for the given timing plan */
	protected void setStop(Object value, int plan)
		throws ParseException
	{
		final int minute = parseMinute(value.toString());
		final MeterPlan p = plans[plan];
		values[plan][COL_STOP] = time_string(minute);
		new AbstractJob() {
			public void perform() throws Exception {
				p.setStopTime(minute);
				meter.notifyUpdate();
			}
		}.addToScheduler();
	}

	/** Set the target rate for the given timing plan */
	protected void setTarget(Object value, int plan)
		throws ParseException
	{
		final int target = Integer.parseInt(value.toString());
		final MeterPlan p = plans[plan];
		values[plan][COL_TARGET] = new Integer(target);
		values[plan][COL_CYCLE] = calculateCycleTime(target);
		new AbstractJob() {
			public void perform() throws Exception {
				p.setTarget(meter, target);
				meter.notifyUpdate();
			}
		}.addToScheduler();
	}

	/** Set the active flag for the given timing plan */
	protected void setActive(Object value, int plan)
		throws ParseException
	{
		final boolean active = ((Boolean)value).booleanValue();
		final MeterPlan p = plans[plan];
		values[plan][COL_ACTIVE] = value;
		new AbstractJob() {
			public void perform() throws Exception {
				p.setActive(active);
				meter.notifyUpdate();
			}
		}.addToScheduler();
	}

	/** Set the testing flag for the given timing plan */
	protected void setTesting(Object value, int plan)
		throws ParseException
	{
		final boolean testing = ((Boolean)value).booleanValue();
		final MeterPlan p = plans[plan];
		values[plan][COL_TESTING] = value;
		new AbstractJob() {
			public void perform() throws Exception {
				p.setTesting(testing);
				meter.notifyUpdate();
			}
		}.addToScheduler();
	}
}
