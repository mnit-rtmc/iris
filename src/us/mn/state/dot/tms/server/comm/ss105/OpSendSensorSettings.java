/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2016  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server.comm.ss105;

import java.io.IOException;
import java.net.SocketTimeoutException;
import us.mn.state.dot.tms.server.ControllerImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Controller operation to send settings to an SS105.
 *
 * @author Douglas Lau
 */
public class OpSendSensorSettings extends OpSS105 {

	/** Time interval for data binning */
	static private final int BINNING_INTERVAL = 30;

	/** Flag to perform a controller restart */
	private final boolean restart;

	/** Create a new operation to send settings to a sensor */
	public OpSendSensorSettings(PriorityLevel p, ControllerImpl c,
		boolean r)
	{
		super(p, c);
		restart = r;
	}

	/** Create a new operation to send settings to a sensor */
	public OpSendSensorSettings(ControllerImpl c, boolean r) {
		this(PriorityLevel.DOWNLOAD, c, r);
	}

	/** Create the first phase of the operation */
	protected Phase<SS105Property> phaseOne() {
		if (restart)
			return new GetTimeInterval();
		else
			return new QueryVersion();
	}

	/** Phase to get the time interval (for binning) */
	protected class GetTimeInterval extends Phase<SS105Property> {

		/** Get the time interval (for binning) */
		protected Phase<SS105Property> poll(
			CommMessage<SS105Property> mess) throws IOException
		{
			TimeIntervalProperty ti = new TimeIntervalProperty();
			mess.add(ti);
			mess.queryProps();
			if (ti.value == BINNING_INTERVAL)
				return new GetClassification();
			else
				return new SetTimeInterval();
		}
	}

	/** Phase to set the time interval (for binning) */
	protected class SetTimeInterval extends Phase<SS105Property> {

		/** Set the time interval (for binning) */
		protected Phase<SS105Property> poll(
			CommMessage<SS105Property> mess) throws IOException
		{
			TimeIntervalProperty ti = new TimeIntervalProperty(
				BINNING_INTERVAL);
			mess.add(ti);
			mess.storeProps();
			return new GetClassification();
		}
	}

	/** Phase to get the classification lengths */
	protected class GetClassification extends Phase<SS105Property> {

		/** Get the classification lengths */
		protected Phase<SS105Property> poll(
			CommMessage<SS105Property> mess) throws IOException
		{
			ClassificationProperty c = new ClassificationProperty();
			mess.add(c);
			mess.queryProps();
			if (c.isDefault())
				return new QueryVersion();
			else
				return new SetClassification();
		}
	}

	/** Phase to set the classification lengths */
	protected class SetClassification extends Phase<SS105Property> {

		/** Set the classification lengths */
		protected Phase<SS105Property> poll(
			CommMessage<SS105Property> mess) throws IOException
		{
			ClassificationProperty c = new ClassificationProperty();
			mess.add(c);
			mess.storeProps();
			return new QueryVersion();
		}
	}

	/** Phase to query the firmware version */
	protected class QueryVersion extends Phase<SS105Property> {

		/** Query the firmware version */
		protected Phase<SS105Property> poll(
			CommMessage<SS105Property> mess) throws IOException
		{
			VersionProperty vr = new VersionProperty();
			mess.add(vr);
			try {
				mess.queryProps();
				controller.setVersion(vr.getVersion());
			}
			catch (SocketTimeoutException e) {
				controller.setVersion("unknown (HD?)");
			}
			return new SynchronizeClock();
		}
	}

	/** Phase to synchronize the clock */
	protected class SynchronizeClock extends Phase<SS105Property> {

		/** Synchronize the clock */
		protected Phase<SS105Property> poll(
			CommMessage<SS105Property> mess) throws IOException
		{
			TimeProperty tr = new TimeProperty();
			mess.add(tr);
			mess.storeProps();
			return null;
		}
	}
}
