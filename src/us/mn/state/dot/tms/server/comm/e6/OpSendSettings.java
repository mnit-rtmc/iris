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
			return new QueryDownlink();
		}
	}

	/** Phase to query the downlink frequency */
	private class QueryDownlink extends Phase<E6Property> {

		/** Query the downlink frequency */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			FrequencyProp freq = new FrequencyProp(
				FrequencyProp.Source.downlink);
			poller.sendQuery(freq);
			poller.waitResponse(freq);
			mess.logQuery(freq);
			return new QueryUplink();
		}
	}

	/** Phase to query the uplink frequency */
	private class QueryUplink extends Phase<E6Property> {

		/** Query the uplink frequency */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			FrequencyProp freq = new FrequencyProp(
				FrequencyProp.Source.uplink);
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
			return new QuerySeGoAtten();
		}
	}

	/** Phase to query the SeGo RF attenuation */
	private class QuerySeGoAtten extends Phase<E6Property> {

		/** Query the SeGo RF attenuation */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			RFAttenProp atten = new RFAttenProp(RFProtocol.SeGo);
			poller.sendQuery(atten);
			poller.waitResponse(atten);
			mess.logQuery(atten);
			return new QueryASTMv6Atten();
		}
	}

	/** Phase to query the ASTMv6 RF attenuation */
	private class QueryASTMv6Atten extends Phase<E6Property> {

		/** Query the ASTMv6 RF attenuation */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			RFAttenProp atten = new RFAttenProp(RFProtocol.ASTMv6);
			poller.sendQuery(atten);
			poller.waitResponse(atten);
			mess.logQuery(atten);
			return new QuerySeGoSeen();
		}
	}

	/** Phase to query the SeGo seen count */
	private class QuerySeGoSeen extends Phase<E6Property> {

		/** Query the SeGo seen count */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			SeenCountProp seen = new SeenCountProp(RFProtocol.SeGo);
			poller.sendQuery(seen);
			poller.waitResponse(seen);
			mess.logQuery(seen);
			return new QueryASTMv6Seen();
		}
	}

	/** Phase to query the ASTMv6 seen count */
	private class QueryASTMv6Seen extends Phase<E6Property> {

		/** Query the ASTMv6 seen count */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			SeenCountProp seen = new SeenCountProp(
				RFProtocol.ASTMv6);
			poller.sendQuery(seen);
			poller.waitResponse(seen);
			mess.logQuery(seen);
			return new QuerySeGoDataDetect();
		}
	}

	/** Phase to query the SeGo data detect */
	private class QuerySeGoDataDetect extends Phase<E6Property> {

		/** Query the SeGo data detect */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			DataDetectProp det = new DataDetectProp(RFProtocol.SeGo);
			poller.sendQuery(det);
			poller.waitResponse(det);
			mess.logQuery(det);
			return new QueryASTMv6DataDetect();
		}
	}

	/** Phase to query the ASTMv6 data detect */
	private class QueryASTMv6DataDetect extends Phase<E6Property> {

		/** Query the ASTMv6 data detect */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			DataDetectProp det = new DataDetectProp(
				RFProtocol.ASTMv6);
			poller.sendQuery(det);
			poller.waitResponse(det);
			mess.logQuery(det);
			return new QueryLineLoss();
		}
	}

	/** Phase to query the line loss */
	private class QueryLineLoss extends Phase<E6Property> {

		/** Query the line loss */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			LineLossProp loss = new LineLossProp();
			poller.sendQuery(loss);
			poller.waitResponse(loss);
			mess.logQuery(loss);
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
