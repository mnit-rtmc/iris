/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dr500;

import java.io.IOException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Controller operation to send settings to a DR-500.
 *
 * @author Douglas Lau
 */
public class OpSendSensorSettings extends OpDR500 {

	/** Sensitivity value */
	static private final int SENSITIVITY_VAL = 99;

	/** Low speed value */
	static private final int LO_SPEED_VAL = 1;

	/** Threshold speed value */
	static private final int THRESHOLD_SPEED_VAL = 121;

	/** High speed value */
	static private final int HI_SPEED_VAL = 120;

	/** Target value (0 = select strongest, 1 = select fastest) */
	static private final int TARGET_VAL = 0;

	/** Time average value (seconds) */
	static private final int TIME_AVG_VAL = 30;

	/** Requested mode flags */
	static private int MODE_FLAGS = ModeFlag.SLOW_FILTER.flag
	                              | ModeFlag.RAIN_FILTER.flag;

	/** Create a new operation to send settings to a sensor */
	public OpSendSensorSettings(PriorityLevel p, ControllerImpl c) {
		super(p, c);
	}

	/** Create a new operation to send settings to a sensor */
	public OpSendSensorSettings(ControllerImpl c) {
		this(PriorityLevel.DOWNLOAD, c);
	}

	/** Create the first phase of the operation */
	@Override
	protected Phase<DR500Property> phaseOne() {
		return new QuerySysInfo();
	}

	/** Phase to query the system information */
	protected class QuerySysInfo extends Phase<DR500Property> {

		/** Query the system information */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			SysInfoProperty si = new SysInfoProperty();
			mess.add(si);
			mess.queryProps();
			controller.setVersion(si.getVersion());
			return new StoreDateTime();
		}
	}

	/** Phase to store the date/time */
	protected class StoreDateTime extends Phase<DR500Property> {

		/** Store the date/time */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			DateTimeProperty dt = new DateTimeProperty();
			mess.add(dt);
			mess.storeProps();
			return new QueryUnits();
		}
	}

	/** Phase to query the units */
	protected class QueryUnits extends Phase<DR500Property> {

		/** Query the units */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			VarProperty un = new VarProperty(VarName.UNITS);
			mess.add(un);
			mess.queryProps();
			if (un.getValue() != UnitsVar.MPH.ordinal())
				return new StoreUnits();
			else
				return new QueryBinning();
		}
	}

	/** Phase to store the units */
	protected class StoreUnits extends Phase<DR500Property> {

		/** Store the units */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			VarProperty un = new VarProperty(VarName.UNITS,
				UnitsVar.MPH.ordinal());
			mess.add(un);
			mess.storeProps();
			return new QueryBinning();
		}
	}

	/** Phase to query the binning interval */
	protected class QueryBinning extends Phase<DR500Property> {

		/** Query the binning interval */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			VarProperty bn = new VarProperty(VarName.BIN_MINUTES);
			mess.add(bn);
			mess.queryProps();
			return new QuerySensitivity();
		}
	}

	/** Phase to query the sensitivity */
	protected class QuerySensitivity extends Phase<DR500Property> {

		/** Query the sensitivity */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			VarProperty st = new VarProperty(VarName.SENSITIVITY);
			mess.add(st);
			mess.queryProps();
			if (st.getValue() != SENSITIVITY_VAL)
				return new StoreSensitivity();
			else
				return new QueryLowSpeed();
		}
	}

	/** Phase to store the sensitivity */
	protected class StoreSensitivity extends Phase<DR500Property> {

		/** Store the sensitivity */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			VarProperty st = new VarProperty(VarName.SENSITIVITY,
				SENSITIVITY_VAL);
			mess.add(st);
			mess.storeProps();
			return new QueryLowSpeed();
		}
	}

	/** Phase to query the low speed */
	protected class QueryLowSpeed extends Phase<DR500Property> {

		/** Query the low speed */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			VarProperty lo = new VarProperty(VarName.LO_SPEED);
			mess.add(lo);
			mess.queryProps();
			if (lo.getValue() != LO_SPEED_VAL)
				return new StoreLowSpeed();
			else
				return new QueryThresholdSpeed();
		}
	}

	/** Phase to store the low speed */
	protected class StoreLowSpeed extends Phase<DR500Property> {

		/** Store the low speed */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			VarProperty lo = new VarProperty(VarName.LO_SPEED,
				LO_SPEED_VAL);
			mess.add(lo);
			mess.storeProps();
			return new QueryThresholdSpeed();
		}
	}

	/** Phase to query the threshold speed */
	protected class QueryThresholdSpeed extends Phase<DR500Property> {

		/** Query the threshold speed */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			VarProperty sp = new VarProperty(
				VarName.THRESHOLD_SPEED);
			mess.add(sp);
			mess.queryProps();
			if (sp.getValue() != THRESHOLD_SPEED_VAL)
				return new StoreThresholdSpeed();
			else
				return new QueryHighSpeed();
		}
	}

	/** Phase to store the threshold speed */
	protected class StoreThresholdSpeed extends Phase<DR500Property> {

		/** Store the threshold speed */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			VarProperty sp = new VarProperty(
				VarName.THRESHOLD_SPEED, THRESHOLD_SPEED_VAL);
			mess.add(sp);
			mess.storeProps();
			return new QueryHighSpeed();
		}
	}

	/** Phase to query the high speed */
	protected class QueryHighSpeed extends Phase<DR500Property> {

		/** Query the high speed */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			VarProperty hi = new VarProperty(VarName.HI_SPEED);
			mess.add(hi);
			mess.queryProps();
			if (hi.getValue() != HI_SPEED_VAL)
				return new StoreHighSpeed();
			else
				return new QueryTarget();
		}
	}

	/** Phase to store the high speed */
	protected class StoreHighSpeed extends Phase<DR500Property> {

		/** Store the high speed */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			VarProperty hi = new VarProperty(VarName.HI_SPEED,
				HI_SPEED_VAL);
			mess.add(hi);
			mess.storeProps();
			return new QueryTarget();
		}
	}

	/** Phase to query the target flag */
	protected class QueryTarget extends Phase<DR500Property> {

		/** Query the target flag */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			VarProperty sf = new VarProperty(VarName.TARGET);
			mess.add(sf);
			mess.queryProps();
			if (sf.getValue() != TARGET_VAL)
				return new StoreTarget();
			else
				return new QueryTimeAvg();
		}
	}

	/** Phase to store the target flag */
	protected class StoreTarget extends Phase<DR500Property> {

		/** Store the target flag */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			VarProperty sf = new VarProperty(VarName.TARGET,
				TARGET_VAL);
			mess.add(sf);
			mess.storeProps();
			return new QueryTimeAvg();
		}
	}

	/** Phase to query time average */
	protected class QueryTimeAvg extends Phase<DR500Property> {

		/** Query time average */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			VarProperty ta = new VarProperty(VarName.TIME_AVG);
			mess.add(ta);
			mess.queryProps();
			if (ta.getValue() != TIME_AVG_VAL)
				return new StoreTimeAvg();
			else
				return new QueryMode();
		}
	}

	/** Phase to store time average */
	protected class StoreTimeAvg extends Phase<DR500Property> {

		/** Store time average */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			VarProperty ta = new VarProperty(VarName.TIME_AVG,
				TIME_AVG_VAL);
			mess.add(ta);
			mess.storeProps();
			return new QueryMode();
		}
	}

	/** Phase to query mode */
	protected class QueryMode extends Phase<DR500Property> {

		/** Query mode */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			VarProperty mode = new VarProperty(VarName.MODE);
			mess.add(mode);
			mess.queryProps();
			int f = mode.getValue() | MODE_FLAGS;
			if (f != mode.getValue())
				return new StoreMode(f);
			else
				return null;
		}
	}

	/** Phase to store mode */
	protected class StoreMode extends Phase<DR500Property> {
		private final int flags;
		private StoreMode(int f) {
			flags = f;
		}

		/** Store mode */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			VarProperty mode = new VarProperty(VarName.MODE, flags);
			mess.add(mode);
			mess.storeProps();
			return null;
		}
	}
}
