/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Minnesota Department of Transportation
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

	/** Units variable */
	private final VarProperty un = new VarProperty(VarName.UNITS);

	/** Binning interval variable */
	private final VarProperty bn = new VarProperty(VarName.BIN_MINUTES);

	/** Sensitivity variable */
	private final VarProperty st = new VarProperty(VarName.SENSITIVITY);

	/** Low speed variable */
	private final VarProperty lo = new VarProperty(VarName.LO_SPEED);

	/** Threshold speed variable */
	private final VarProperty sp = new VarProperty(VarName.THRESHOLD_SPEED);

	/** High speed variable */
	private final VarProperty hi = new VarProperty(VarName.HI_SPEED);

	/** Target flag variable */
	private final VarProperty sf = new VarProperty(VarName.TARGET);

	/** Create a new operation to send settings to a sensor */
	public OpSendSensorSettings(ControllerImpl c) {
		super(PriorityLevel.DOWNLOAD, c);
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
			return new QueryUnits();
		}
	}

	/** Phase to query the units */
	protected class QueryUnits extends Phase<DR500Property> {

		/** Query the units */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			mess.add(un);
			mess.queryProps();
			return new QueryBinning();
		}
	}

	/** Phase to query the binning interval */
	protected class QueryBinning extends Phase<DR500Property> {

		/** Query the binning interval */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
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
			mess.add(st);
			mess.queryProps();
			return new QueryLowSpeed();
		}
	}

	/** Phase to query the low speed */
	protected class QueryLowSpeed extends Phase<DR500Property> {

		/** Query the low speed */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			mess.add(lo);
			mess.queryProps();
			return new QueryThresholdSpeed();
		}
	}

	/** Phase to query the threshold speed */
	protected class QueryThresholdSpeed extends Phase<DR500Property> {

		/** Query the threshold speed */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			mess.add(sp);
			mess.queryProps();
			return new QueryHighSpeed();
		}
	}

	/** Phase to query the high speed */
	protected class QueryHighSpeed extends Phase<DR500Property> {

		/** Query the high speed */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			mess.add(hi);
			mess.queryProps();
			return new QueryTarget();
		}
	}

	/** Phase to query the target flag */
	protected class QueryTarget extends Phase<DR500Property> {

		/** Query the target flag */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			mess.add(sf);
			mess.queryProps();
			return new QueryAll();
		}
	}

	/** Phase to query all variables */
	protected class QueryAll extends Phase<DR500Property> {

		/** Variable index */
		private byte index = 0;

		/** Query all variables */
		protected Phase<DR500Property> poll(
			CommMessage<DR500Property> mess) throws IOException
		{
			mess.add(new VarIndexProperty(index));
			mess.queryProps();
			index++;
			return this;
		}
	}
}
