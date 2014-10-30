/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2014  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.dinrelay;

import java.io.IOException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to command the outlet state of one DIN relay.
 *
 * @author Douglas Lau
 */
public class OpCommandOutlets extends OpDinRelay {

	/** Outlet command state */
	private final boolean[] outlets;

	/** Outlet property */
	private final OutletProperty property;

	/** Create a new operation to command the outlets */
	public OpCommandOutlets(ControllerImpl c, boolean[] out,
		OutletProperty op)
	{
		super(PriorityLevel.COMMAND, c);
		outlets = out;
		property = op;
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<DinRelayProperty> phaseOne() {
		return new QueryOutlets();
	}

	/** Phase to query the DIN relay outlet status */
	private class QueryOutlets extends Phase<DinRelayProperty> {

		/** Query the outlet status */
		protected Phase<DinRelayProperty> poll(
			CommMessage<DinRelayProperty> mess) throws IOException
		{
			mess.add(property);
			mess.queryProps();
			return new TurnOffOutlets();
		}
	}

	/** Turn off outlets which are commanded OFF */
	private class TurnOffOutlets extends Phase<DinRelayProperty> {

		/** Current outlet number */
		private int o_num = 0;

		/** Command next outlet OFF */
		protected Phase<DinRelayProperty> poll(
			CommMessage<DinRelayProperty> mess) throws IOException
		{
			CommandProperty prop;
			while (o_num < outlets.length) {
				if (shouldTurnOff(o_num)) {
					o_num++;
					prop = new CommandProperty(o_num,false);
					mess.add(prop);
					mess.storeProps();
					return this;
				} else
					o_num++;
			}
			return new TurnOnOutlets();
		}
	}

	/** Test if an outlet should be turned off */
	private boolean shouldTurnOff(int o_num) {
		boolean[] os = property.getOutletState();
		if (o_num >= 0 && o_num < os.length && o_num < outlets.length)
			return os[o_num] && !outlets[o_num];
		else
			return false;
	}

	/** Turn on outlets which are commanded ON */
	private class TurnOnOutlets extends Phase<DinRelayProperty> {

		/** Current outlet number */
		private int o_num = 0;

		/** Command next outlet ON */
		protected Phase<DinRelayProperty> poll(
			CommMessage<DinRelayProperty> mess) throws IOException
		{
			CommandProperty prop;
			while (o_num < outlets.length) {
				if (shouldTurnOn(o_num)) {
					o_num++;
					prop = new CommandProperty(o_num, true);
					mess.add(prop);
					mess.storeProps();
					return this;
				} else
					o_num++;
			}
			return null;
		}
	}

	/** Test if an outlet should be turned on */
	private boolean shouldTurnOn(int o_num) {
		boolean[] os = property.getOutletState();
		if(o_num >= 0 && o_num < os.length && o_num < outlets.length)
			return outlets[o_num] && !os[o_num];
		else
			return false;
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		property.complete(isSuccess());
		super.cleanup();
	}
}
