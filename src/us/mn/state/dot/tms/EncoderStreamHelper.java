/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import us.mn.state.dot.tms.utils.URIUtil;

/**
 * Helper class for encoder streams.
 *
 * @author Douglas Lau
 */
public class EncoderStreamHelper extends BaseHelper {

	/** Don't allow instances to be created */
	private EncoderStreamHelper() {
		assert false;
	}

	/** Lookup the encoder stream with the specified name */
	static public EncoderStream lookup(String name) {
		return (EncoderStream) namespace.lookupObject(
			EncoderStream.SONAR_TYPE, name);
	}

	/** Get an encoder stream iterator */
	static public Iterator<EncoderStream> iterator() {
		return new IteratorWrapper<EncoderStream>(namespace.iterator(
			EncoderStream.SONAR_TYPE));
	}

	/** Check if an encoder stream is multicast */
	static public boolean isMcast(EncoderStream es) {
		return es.getMcastPort() != null;
	}

	/** Find the best matching encoder stream.
	 * @param et Encoder type.
	 * @param eq Allowed encoding quality (null for any).
	 * @param mcast Allow multicast.
	 * @param flow_stream Flow stream (null for any). */
	static EncoderStream find(EncoderType et, EncodingQuality eq,
		boolean mcast, Boolean flow_stream)
	{
		EncoderStream best = null;
		int best_val = 0;
		Iterator<EncoderStream> it = iterator();
		while (it.hasNext()) {
			EncoderStream es = it.next();
			if (!objectEquals(et, es.getEncoderType()))
				continue;
			if (es.getViewNum() != null)
				continue;
			if (eq != null &&
			    eq != EncodingQuality.fromOrdinal(es.getQuality()))
				continue;
			if (isMcast(es) && !mcast)
				continue;
			if (flow_stream != null &&
		            flow_stream != es.getFlowStream())
				continue;
			int v = value(es);
			if (v > best_val) {
				best = es;
				best_val = v;
			}
		}
		return best;
	}

	/** Get encoder stream value */
	static private int value(EncoderStream es) {
		int qual_val = es.getQuality();
		int mcast_val = isMcast(es)
			? EncodingQuality.VALUES.length + 2
			: 0;
		int flow_val = es.getFlowStream() ? 1 : 0;
		return qual_val + mcast_val + flow_val;
	}
}
