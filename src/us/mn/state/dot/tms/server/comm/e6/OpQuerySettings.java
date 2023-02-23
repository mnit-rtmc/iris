/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2023  Minnesota Department of Transportation
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
import org.json.JSONException;
import org.json.JSONObject;
import us.mn.state.dot.tms.server.TagReaderImpl;
import us.mn.state.dot.tms.server.comm.CommMessage;
import us.mn.state.dot.tms.server.comm.ControllerException;
import us.mn.state.dot.tms.server.comm.PriorityLevel;

/**
 * Operation to query settings from an E6.
 *
 * @author Douglas Lau
 */
public class OpQuerySettings extends OpE6 {

	/** Settings for one RF protocol */
	private class ProtocolSettings {
		final RFProtocol protocol;
		final JSONObject settings = new JSONObject();
		ProtocolSettings(RFProtocol p) {
			protocol = p;
		}
		void put(String key, Object value) {
			try {
				settings.put(key, value);
			}
			catch (JSONException e) {
				logError("put: " + e.getMessage() + ", " +
					key);
			}
		}
	}

	/** Tag reader settings */
	private final JSONObject settings = new JSONObject();

	/** Put an object into settings */
	private void putSetting(String key, Object value) {
		try {
			settings.put(key, value);
		}
		catch (JSONException e) {
			logError("putSetting: " + e.getMessage() + ", " + key);
		}
	}

	/** Create a new "query settings" operation */
	public OpQuerySettings(TagReaderImpl tr) {
		super(PriorityLevel.SETTINGS, tr);
	}

	/** Create the second phase of the operation */
	@Override
	protected Phase<E6Property> phaseTwo() {
		return new QueryFirmware();
	}

	/** Phase to query the firmware versions */
	private class QueryFirmware extends Phase<E6Property> {

		/** Query the firmware versions */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			FirmwareVersionsProp v = new FirmwareVersionsProp();
			sendQuery(mess, v);
			mess.logQuery(v);
			controller.setVersionNotify(v.toString());
			return new QueryAckTimeout();
		}
	}

	/** Phase to query the data ack timeout */
	private class QueryAckTimeout extends Phase<E6Property> {

		/** Query the ack timeout */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			AckTimeoutProp ack_timeout = new AckTimeoutProp(
				AckTimeoutProp.Protocol.udp_ip);
			sendQuery(mess, ack_timeout);
			mess.logQuery(ack_timeout);
			putSetting("ack_timeout", ack_timeout.getValue());
			return new QueryRFControl();
		}
	}

	/** Phase to query the RF control setting */
	private class QueryRFControl extends Phase<E6Property> {

		/** Query the RF control setting */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			RFControlProp rf_control = new RFControlProp();
			sendQuery(mess, rf_control);
			mess.logQuery(rf_control);
			putSetting("rf_control", rf_control.getValue());
			return new QueryDownlinkFreq();
		}
	}

	/** Phase to query the downlink frequency */
	private class QueryDownlinkFreq extends Phase<E6Property> {

		/** Query the downlink frequency */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			FrequencyProp freq = new FrequencyProp(
				Source.downlink);
			sendQuery(mess, freq);
			mess.logQuery(freq);
			Integer fr = freq.getFreqKhz();
			tag_reader.setDownlinkFreqKhzNotify(fr);
			putSetting("downlink_freq_khz", fr);
			return new QueryUplinkFreq();
		}
	}

	/** Phase to query the uplink frequency */
	private class QueryUplinkFreq extends Phase<E6Property> {

		/** Query the uplink frequency */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			FrequencyProp freq = new FrequencyProp(Source.uplink);
			sendQuery(mess, freq);
			mess.logQuery(freq);
			Integer fr = freq.getFreqKhz();
			tag_reader.setUplinkFreqKhzNotify(fr);
			putSetting("uplink_freq_khz", fr);
			return new QueryLineLoss();
		}
	}

	/** Phase to query the line loss */
	private class QueryLineLoss extends Phase<E6Property> {

		/** Query the line loss */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			LineLossProp line_loss = new LineLossProp();
			sendQuery(mess, line_loss);
			mess.logQuery(line_loss);
			Integer ll = line_loss.getValue();
			tag_reader.setLineLossDbNotify(ll);
			putSetting("line_loss_db", ll);
			return new QueryMasterSlave();
		}
	}

	/** Phase to query the master/slave setting */
	private class QueryMasterSlave extends Phase<E6Property> {

		/** Query the master/slave setting */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			MasterSlaveProp master_slave = new MasterSlaveProp();
			sendQuery(mess, master_slave);
			mess.logQuery(master_slave);
			tag_reader.setSyncModeNotify(
				master_slave.getMode()
			);
			putSetting("sync_mode", master_slave.getMode());
			tag_reader.setSlaveSelectCountNotify(
				master_slave.getSlaveSelectCount()
			);
			putSetting("slave_select_count",
				master_slave.getSlaveSelectCount());
			return new QueryMuxMode();
		}
	}

	/** Phase to query the mux mode */
	private class QueryMuxMode extends Phase<E6Property> {

		/** Query the mux mode */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			MuxModeProp mux_mode = new MuxModeProp();
			sendQuery(mess, mux_mode);
			mess.logQuery(mux_mode);
			putSetting("mux_mode", mux_mode.getValue());
			return new QueryAntennaChannel();
		}
	}

	/** Phase to query the manual antenna channel control */
	private class QueryAntennaChannel extends Phase<E6Property> {

		/** Query the manual antenna channel */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			AntennaChannelProp antenna_channel =
				new AntennaChannelProp();
			sendQuery(mess, antenna_channel);
			mess.logQuery(antenna_channel);
			putSetting("antenna_channel",
				antenna_channel.getValue());
			return nextQueryPhase(null);
		}
	}

	/** Get the next query phase */
	private Phase<E6Property> nextQueryPhase(RFProtocol prot) {
		RFProtocol p = RFProtocol.next(prot);
		return (p != null)
		      ? new QueryAtten(new ProtocolSettings(p))
		      : null;
	}

	/** Phase to query the RF attenuation for one protocol */
	private class QueryAtten extends Phase<E6Property> {
		private final ProtocolSettings p_settings;
		private QueryAtten(ProtocolSettings ps) {
			p_settings = ps;
		}

		/** Query the RF attenuation */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			RFAttenProp atten = new RFAttenProp(
				p_settings.protocol);
			try {
				sendQuery(mess, atten);
			}
			catch (ControllerException e) {
				// SUB_COMMAND_ERROR => protocol not supported
				return nextQueryPhase(p_settings.protocol);
			}
			mess.logQuery(atten);
			storeRfAtten(tag_reader, atten);
			p_settings.put("rf_atten_downlink_db",
				atten.getDownlinkDb());
			p_settings.put("rf_atten_uplink_db",
				atten.getUplinkDb());
			return new QueryDataDetect(p_settings);
		}
	}

	/** Store RF attenuation for one protocol.
	 * FIXME: remove this after JSON settings is tested */
	private void storeRfAtten(TagReaderImpl tr, RFAttenProp atten) {
		switch (atten.protocol) {
			case SeGo:
				tr.setSeGoAttenDownlinkDbNotify(
					atten.getDownlinkDb());
				tr.setSeGoAttenUplinkDbNotify(
					atten.getUplinkDb());
				break;
			case IAG:
				tr.setIAGAttenDownlinkDbNotify(
					atten.getDownlinkDb());
				tr.setIAGAttenUplinkDbNotify(
					atten.getUplinkDb());
				break;
		}
	}

	/** Phase to query the data detect for one protocol */
	private class QueryDataDetect extends Phase<E6Property> {
		private final ProtocolSettings p_settings;
		private QueryDataDetect(ProtocolSettings ps) {
			p_settings = ps;
		}

		/** Query the data detect */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			DataDetectProp det =
				new DataDetectProp(p_settings.protocol);
			sendQuery(mess, det);
			mess.logQuery(det);
			storeDataDetect(tag_reader, det);
			p_settings.put("data_detect_db", det.getValue());
			return new QuerySeen(p_settings);
		}
	}

	/** Store data detect for one protocol.
	 * FIXME: remove this after JSON settings is tested */
	private void storeDataDetect(TagReaderImpl tr, DataDetectProp det) {
		switch (det.protocol) {
			case SeGo:
				tr.setSeGoDataDetectDbNotify(det.getValue());
				break;
			case IAG:
				tr.setIAGDataDetectDbNotify(det.getValue());
				break;
		}
	}

	/** Phase to query the seen count for one protocol */
	private class QuerySeen extends Phase<E6Property> {
		private final ProtocolSettings p_settings;
		private QuerySeen(ProtocolSettings ps) {
			p_settings = ps;
		}

		/** Query the seen count */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			SeenCountProp seen_count =
				new SeenCountProp(p_settings.protocol);
			sendQuery(mess, seen_count);
			mess.logQuery(seen_count);
			storeSeenUnique(tag_reader, seen_count);
			p_settings.put("seen_count", seen_count.getSeen());
			p_settings.put("unique_count", seen_count.getUnique());
			return new QueryUplinkSource(p_settings);
		}
	}

	/** Store seen/unique for one protocol.
	 * FIXME: remove this after JSON settings is tested */
	private void storeSeenUnique(TagReaderImpl tr, SeenCountProp seen) {
		switch (seen.protocol) {
			case SeGo:
				tr.setSeGoSeenCountNotify(seen.getSeen());
				tr.setSeGoUniqueCountNotify(seen.getUnique());
				break;
			case IAG:
				tr.setIAGSeenCountNotify(seen.getSeen());
				tr.setIAGUniqueCountNotify(seen.getUnique());
				break;
		}
	}

	/** Phase to query the uplink source control for one protocol */
	private class QueryUplinkSource extends Phase<E6Property> {
		private final ProtocolSettings p_settings;
		private QueryUplinkSource(ProtocolSettings ps) {
			p_settings = ps;
		}

		/** Query the uplink source */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			UplinkSourceProp src =
				new UplinkSourceProp(p_settings.protocol);
			sendQuery(mess, src);
			mess.logQuery(src);
			p_settings.put("uplink_source", src.getValue());
			return new QuerySlot(p_settings);
		}
	}

	/** Phase to query the slot for one protocol */
	private class QuerySlot extends Phase<E6Property> {
		private final ProtocolSettings p_settings;
		private QuerySlot(ProtocolSettings ps) {
			p_settings = ps;
		}

		/** Query the protocol slot */
		protected Phase<E6Property> poll(CommMessage<E6Property> mess)
			throws IOException
		{
			if (RFProtocol.IAG == p_settings.protocol) {
				ProtocolSlotProp slot =
					new ProtocolSlotProp(RFProtocol.IAG);
				sendQuery(mess, slot);
				mess.logQuery(slot);
				p_settings.put("slot", slot.getSlot());
			}
			putSetting(p_settings.protocol.toString().toLowerCase(),
				p_settings.settings);
			return nextQueryPhase(p_settings.protocol);
		}
	}

	/** Cleanup the operation */
	@Override
	public void cleanup() {
		if (isSuccess())
			tag_reader.setSettings(settings.toString());
		super.cleanup();
	}
}
