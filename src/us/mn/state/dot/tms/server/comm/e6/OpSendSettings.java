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
package us.mn.state.dot.tms.server.comm.e6;

import java.io.IOException;
import us.mn.state.dot.tms.server.TagReaderImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to send settings to an E6.
 *
 * @author Douglas Lau
 */
public class OpSendSettings extends OpE6 {

	/** Create a new "send settings" operation */
	public OpSendSettings(TagReaderImpl tr, E6Poller ep) {
		super(PriorityLevel.DOWNLOAD, tr, ep);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<E6Property> phaseTwo() {
		return new QueryTimeDate();
	}

	/** Phase to query the time / date */
	private class QueryTimeDate extends Phase<E6Property> {

		/** Query the time / date */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			TimeDateProp stamp = new TimeDateProp();
			poller.sendQuery(stamp);
			poller.waitResponse(stamp);
			mess.logQuery(stamp);
			return new QueryMode();
		}
	}

	/** Phase to query the mode */
	private class QueryMode extends Phase<E6Property> {

		/** Query the mode */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			ModeProp mode = new ModeProp();
			poller.sendQuery(mode);
			poller.waitResponse(mode);
			mess.logQuery(mode);
			return new QueryFreq();
		}
	}

	/** Phase to query the frequency */
	private class QueryFreq extends Phase<E6Property> {

		/** Query the frequency */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			FrequencyProp freq = new FrequencyProp(
				FrequencyProp.Source.downlink);
			poller.sendQuery(freq);
			poller.waitResponse(freq);
			mess.logQuery(freq);
			return new QueryMuxMode();
		}
	}

	/** Phase to query the mux mode */
	private class QueryMuxMode extends Phase<E6Property> {

		/** Query the mux mode */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			MuxModeProp mode = new MuxModeProp();
			poller.sendQuery(mode);
			poller.waitResponse(mode);
			mess.logQuery(mode);
			return new QueryRFControl();
		}
	}

	/** Phase to query the RF control */
	private class QueryRFControl extends Phase<E6Property> {

		/** Query the RF control */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			RFControlProp ctrl = new RFControlProp();
			poller.sendQuery(ctrl);
			poller.waitResponse(ctrl);
			mess.logQuery(ctrl);
			return new QueryMasterSlave();
		}
	}

	/** Phase to query the master/slave setting */
	private class QueryMasterSlave extends Phase<E6Property> {

		/** Query the master/slave setting */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			MasterSlaveProp mstr = new MasterSlaveProp();
			poller.sendQuery(mstr);
			poller.waitResponse(mstr);
			mess.logQuery(mstr);
			return new QueryAppendData();
		}
	}

	/** Phase to query the append data */
	private class QueryAppendData extends Phase<E6Property> {

		/** Query the append data */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			AppendDataProp append = new AppendDataProp();
			poller.sendQuery(append);
			poller.waitResponse(append);
			mess.logQuery(append);
			return new QueryDiagStatus();
		}
	}

	/** Phase to query the diagnostic status */
	private class QueryDiagStatus extends Phase<E6Property> {

		/** Query diag status */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			DiagStatusProp stat = new DiagStatusProp();
			poller.sendQuery(stat);
			poller.waitResponse(stat);
			mess.logQuery(stat);
			return null;
		}
	}
}
