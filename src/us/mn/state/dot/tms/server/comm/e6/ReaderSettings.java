/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2023  Minnesota Department of Transportation
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

import us.mn.state.dot.tms.server.TagReaderImpl;
import us.mn.state.dot.tms.utils.Json;

/**
 * A collection of tag reader settings which can be converted to JSON.
 * Only values which have been successfully read will be included.
 *
 * @author Douglas Lau
 */
public class ReaderSettings {

	/** Settings for one RF protocol */
	static public class ProtocolSettings {
		public final RFAttenProp rf_atten;
		public final DataDetectProp data_detect;
		public final SeenCountProp seen_count;
		public ProtocolSettings(RFProtocol p) {
			rf_atten = new RFAttenProp(p);
			data_detect = new DataDetectProp(p);
			seen_count = new SeenCountProp(p);
		}
		/** Get JSON representation */
		public String toJson() {
			StringBuilder sb = new StringBuilder();
			sb.append('{');
			sb.append(Json.num("rf_atten_downlink_db",
				rf_atten.getDownlinkDb()));
			sb.append(Json.num("rf_atten_uplink_db",
				rf_atten.getUplinkDb()));
			sb.append(Json.num("data_detect_db",
				data_detect.getValue()));
			sb.append(Json.num("seen_count", seen_count.getSeen()));
			sb.append(Json.num("unique_count",
				seen_count.getUnique()));
			if (sb.length() < 2)
				return null;
			// remove trailing comma
			if (sb.charAt(sb.length() - 1) == ',')
				sb.setLength(sb.length() - 1);
			sb.append('}');
			return sb.toString();
		}
	}

	/** ACK timeout */
	public final AckTimeoutProp ack_timeout = new AckTimeoutProp(
		AckTimeoutProp.Protocol.udp_ip);

	/** Downlink frequency */
	public final FrequencyProp downlink_freq = new FrequencyProp(
		FrequencyProp.Source.downlink);

	/** Uplink frequency */
	public final FrequencyProp uplink_freq = new FrequencyProp(
		FrequencyProp.Source.uplink);

	/** Settings for SeGo protocol */
	public final ProtocolSettings sego =
		new ProtocolSettings(RFProtocol.SeGo);

	/** Settings for IAG protocol */
	public final ProtocolSettings iag =
		new ProtocolSettings(RFProtocol.IAG);

	/** Settings for 6C protocol */
	public final ProtocolSettings _6c =
		new ProtocolSettings(RFProtocol._6C);

	/** Line loss */
	public final LineLossProp line_loss = new LineLossProp();

	/** Mux mode */
	public final MuxModeProp mux_mode = new MuxModeProp();

	/** Antenna channel */
	public final AntennaChannelProp antenna_channel =
		new AntennaChannelProp();

	/** Master / slave */
	public final MasterSlaveProp master_slave = new MasterSlaveProp();

	/** Create new reader settings */
	public ReaderSettings() { }

	/** Get RF atten property */
	public RFAttenProp getRfAtten(RFProtocol p) {
		switch (p) {
			case SeGo: return sego.rf_atten;
			case IAG: return iag.rf_atten;
			case _6C: return _6c.rf_atten;
			default: return null;
		}
	}

	/** Store RF attenuation for one protocol */
	public void storeRfAtten(TagReaderImpl tr, RFProtocol p) {
		switch (p) {
			case SeGo:
				tr.setSeGoAttenDownlinkDbNotify(
					sego.rf_atten.getDownlinkDb());
				tr.setSeGoAttenUplinkDbNotify(
					sego.rf_atten.getUplinkDb());
				break;
			case IAG:
				tr.setIAGAttenDownlinkDbNotify(
					iag.rf_atten.getDownlinkDb());
				tr.setIAGAttenUplinkDbNotify(
					iag.rf_atten.getUplinkDb());
				break;
		}
	}

	/** Get data detect property */
	public DataDetectProp getDataDetect(RFProtocol p) {
		switch (p) {
			case SeGo: return sego.data_detect;
			case IAG: return iag.data_detect;
			case _6C: return _6c.data_detect;
			default: return null;
		}
	}

	/** Set data detect for one protocol */
	public void storeDataDetect(TagReaderImpl tr, RFProtocol p) {
		switch (p) {
			case SeGo:
				tr.setSeGoDataDetectDbNotify(
					sego.data_detect.getValue());
				break;
			case IAG:
				tr.setIAGDataDetectDbNotify(
					iag.data_detect.getValue());
				break;
		}
	}

	/** Get seen count property */
	public SeenCountProp getSeenCount(RFProtocol p) {
		switch (p) {
			case SeGo: return sego.seen_count;
			case IAG: return iag.seen_count;
			case _6C: return _6c.seen_count;
			default: return null;
		}
	}

	/** Store seen/unique for one protocol */
	public void storeSeenUnique(TagReaderImpl tr, RFProtocol p) {
		switch (p) {
			case SeGo:
				tr.setSeGoSeenCountNotify(
					sego.seen_count.getSeen());
				tr.setSeGoUniqueCountNotify(
					sego.seen_count.getUnique());
				break;
			case IAG:
				tr.setIAGSeenCountNotify(
					iag.seen_count.getSeen());
				tr.setIAGUniqueCountNotify(
					iag.seen_count.getUnique());
				break;
		}
	}

	/** Get JSON representation */
	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		sb.append(Json.num("ack_timeout", ack_timeout.getValue()));
		sb.append(Json.num("downlink_freq_khz",
			downlink_freq.getFreqKhz()));
		sb.append(Json.num("uplink_freq_khz",
			uplink_freq.getFreqKhz()));
		sb.append(Json.sub("sego", sego.toJson()));
		sb.append(Json.sub("iag", iag.toJson()));
		sb.append(Json.sub("_6c", _6c.toJson()));
		sb.append(Json.num("line_loss_db", line_loss.getValue()));
		sb.append(Json.num("mux_mode", mux_mode.getValue()));
		sb.append(Json.num("antenna_channel",
			antenna_channel.getValue()));
		sb.append(Json.str("sync_mode", master_slave.getMode()));
		sb.append(Json.str("slave_select_count",
			master_slave.getSlaveSelectCount()));
		// remove trailing comma
		if (sb.charAt(sb.length() - 1) == ',')
			sb.setLength(sb.length() - 1);
		sb.append('}');
		return sb.toString();
	}
}
