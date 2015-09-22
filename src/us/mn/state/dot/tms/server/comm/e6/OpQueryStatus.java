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
 * Operation to query status of an E6.
 *
 * @author Douglas Lau
 */
public class OpQueryStatus extends OpE6 {

	/** Create a new "query status" operation */
	public OpQueryStatus(TagReaderImpl tr, E6Poller ep) {
		super(PriorityLevel.DEVICE_DATA, tr, ep);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<E6Property> phaseTwo() {
		return new QueryDiagStatus();
	}

	/** Phase to query the diagnostic status */
	private class QueryDiagStatus extends Phase<E6Property> {

		/** Query diag status */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			DiagStatusProp stat = new DiagStatusProp();
			poller.sendQuery(stat);
			mess.logQuery(stat);
			return new QueryBufferedTransactions();
		}
	}

	/** Phase to query the buffered tag transaction count */
	private class QueryBufferedTransactions extends Phase<E6Property> {

		/** Query the buffered tag transaction count */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			BufferedTransactionProp count =
				new BufferedTransactionProp();
			poller.sendQuery(count);
			mess.logQuery(count);
			return null;
		}
	}
}
