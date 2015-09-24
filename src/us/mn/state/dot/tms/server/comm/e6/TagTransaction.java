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

import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Tag transaction.
 *
 * @author Douglas Lau
 */
public class TagTransaction extends ControllerProperty {

	/** Tag transaction types */
	public enum TransactionType {
		SeGo_streamlined_read	(0x3021, 27),
		read_verify_page	(0x3022, 19),
		SeGo_seen_frame_count	(0x3043, 12),
		ASTM_read		(0x5014, 12);
		private TransactionType(int c, int l) {
			code = c;
			len = l;
		}
		public final int code;
		public final int len;
		static public TransactionType fromCode(int c) {
			for (TransactionType tt: values()) {
				if (tt.code == c)
					return tt;
			}
			return null;
		}
	}

	/** Transaction data */
	private final byte[] data;

	/** Create a new tag transaction */
	public TagTransaction(byte[] d, int off, int len) {
		data = new byte[len];
		System.arraycopy(d, off, data, 0, len);
	}

	/** Get the transaction type */
	public TransactionType getTransactionType() {
		int c = parse16(data, 0);
		return TransactionType.fromCode(c);
	}

	/** Get the transponder ID */
	public Integer getId() {
		TransactionType tt = getTransactionType();
		switch (tt) {
		case SeGo_streamlined_read:
			if (data.length == tt.len)
				return parse32(data, 6) & 0xFFFFFF;
			break;
		default:
			break;
		}
		return null;
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "tag transaction: " + getTransactionType() + ' ' +
			getId();
	}
}
