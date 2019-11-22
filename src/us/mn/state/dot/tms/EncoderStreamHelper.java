/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019  Minnesota Department of Transportation
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

	/** Find the best matching encoder stream */
	static public EncoderStream find(EncoderType et, EncodingQuality q,
		boolean mcast)
	{
		EncoderStream best = null;
		int best_val = 0;
		Iterator<EncoderStream> it = iterator();
		while (it.hasNext()) {
			EncoderStream es = it.next();
			int v = value(et, q, mcast, es);
			if (v > best_val) {
				best = es;
				best_val = v;
			}
		}
		return best;
	}

	/** Get encoder stream value */
	static private int value(EncoderType et, EncodingQuality q,
		boolean mcast, EncoderStream es)
	{
		return objectEquals(et, es.getEncoderType())
		      ? qualityValue(es, q) + mcastValue(es, mcast)
		      : 0;
	}

	/** Get quality value */
	static private int qualityValue(EncoderStream es, EncodingQuality q) {
		return 3 - Math.abs(q.ordinal() - es.getQuality());
	}

	/** Get multicast value */
	static private int mcastValue(EncoderStream es, boolean mcast) {
		if (mcast)
			return isMcast(es) ? 5 : 0;
		else
			return isMcast(es) ? -5 : 0;
	}
}
