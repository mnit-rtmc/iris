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
		SeGo_streamlined_read	(0x3021),
		read_verify_page	(0x3022),
		SeGo_seen_frame_count	(0x3043),
		ASTM_read		(0x5014);
		private TransactionType(int c) {
			code = c;
		}
		public final int code;
		static public TransactionType fromCode(int c) {
			for (TransactionType tt: values()) {
				if (tt.code == c)
					return tt;
			}
			return null;
		}
	}

	/** Tag transaction type */
	public final TransactionType t_type;

	/** Create a new tag transaction */
	public TagTransaction(byte[] data, int off) throws ParsingException {
		int c = parse16(data, off);
		t_type = TransactionType.fromCode(c);
		if (t_type == null)
			throw new ParsingException("TRANSACTION TYPE: " + c);
	}

	/** Get a string representation */
	@Override
	public String toString() {
		return "tag transaction: " + t_type;
	}
}
