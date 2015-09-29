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

import java.util.Date;
import us.mn.state.dot.tms.server.TagReaderImpl;
import us.mn.state.dot.tms.server.TagType;
import us.mn.state.dot.tms.server.comm.ControllerProperty;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Tag transaction.
 *
 * @author Douglas Lau
 */
public class TagTransaction extends E6Property {

	/** SeGo region code for MN */
	static private final int REGION_MN = 0x0A;

	/** SeGo agency code for MnDOT */
	static private final int AGENCY_MNDOT = 0x09;

	/** Tag transaction types */
	public enum TransactionType {
		SeGo_streamlined_read	(0x3021, 28),
		read_verify_page	(0x3022, 20),
		seen_frame_count	(0x3043, 13),
		ASTM_read		(0x5014, 17);
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

	/** Get the command */
	public Command command() {
		// FIXME: hacky -- not a real property
		return null;
	}

	/** Create a new tag transaction */
	public TagTransaction(byte[] d, int off, int len) {
		data = new byte[len];
		System.arraycopy(d, off, data, 0, len);
	}

	/** Check if tag transaction is a valid read */
	public boolean isValidRead() {
		return isValidSeGoRead() || isValidASTMRead();
	}

	/** Get the transaction type */
	private TransactionType getTransactionType() {
		if (data.length >= 2) {
			int c = parse16(data, 0);
			return TransactionType.fromCode(c);
		}
		return null;
	}

	/** Get the date/time stamp */
	public Long getStamp() {
		if (isValidSeGoRead())
			return parseStamp(21);
		if (isValidASTMRead())
			return parseStamp(10);
		return null;
	}

	/** Parse a time / date stamp */
	private Long parseStamp(int off) {
		try {
			return parseTimeDate(data, off);
		}
		catch (ParsingException e) {
			return null;
		}
	}

	/** Parse an ASTM stamp */
	private Long parseASTMStamp() {
		return null;
	}

	/** Get the tag type */
	public TagType getTagType() {
		if (isValidSeGoRead())
			return TagType.SeGo;
		if (isValidASTMRead())
			return TagType.ASTM;
		return null;
	}

	/** Get the transponder ID */
	public Integer getId() {
		if (isValidSeGoRead())
			return parseSeGoId();
		if (isValidASTMRead())
			return parseASTMId();
		return null;
	}

	/** Check if transaction is a valid SeGo streamlined read */
	private boolean isValidSeGoRead() {
		TransactionType tt = getTransactionType();
		if (tt == TransactionType.SeGo_streamlined_read) {
			// FIXME: check CRC
			return (data.length == tt.len);
		}
		return false;
	}

	/** Parse a SeGo ID */
	private Integer parseSeGoId() {
		if (parse8(data, 4) != REGION_MN)
			return null;
		if (parse8(data, 5) != AGENCY_MNDOT)
			return null;
		return parse32(data, 5) & 0xFFFFFF;
	}

	/** Check if transaction is a valid ASTM read */
	private boolean isValidASTMRead() {
		TransactionType tt = getTransactionType();
		if (tt == TransactionType.ASTM_read) {
			// FIXME: check CRC
			return (data.length == tt.len);
		}
		return false;
	}

	/** Parse an ASTM ID */
	private Integer parseASTMId() {
		return parse32(data, 3);
	}

	/** Get HOV flag */
	public Boolean getHOV() {
		if (isValidSeGoRead())
			return parseSeGoHOV();
		if (isValidASTMRead())
			return false;
		return null;
	}

	/** Parse a SeGo HOV flag */
	private boolean parseSeGoHOV() {
		return (parse8(data, 2) & 0x03) != 0;
	}

	/** Log one tag read */
	public void logRead(TagReaderImpl tr) {
		if (isValidRead()) {
			Long stamp = getStamp();
			TagType typ = getTagType();
			Integer tid = getId();
			Boolean hov = getHOV();
			if (stamp != null && typ != null && tid != null)
				tr.logRead(stamp, typ, tid, hov);
		}
	}

	/** Get a string representation */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("tag transaction: ");
		sb.append(getTransactionType());
		Long stamp = getStamp();
		if (stamp != null) {
			sb.append(' ');
			sb.append(new Date(stamp));
		}
		TagType typ = getTagType();
		if (typ != null) {
			sb.append(' ');
			sb.append(typ);
		}
		Integer tid = getId();
		if (tid != null) {
			sb.append(' ');
			sb.append(tid);
		}
		Boolean hov = getHOV();
		if (hov != null) {
			sb.append(" HOV: ");
			sb.append(hov);
		}
		return sb.toString();
	}
}
