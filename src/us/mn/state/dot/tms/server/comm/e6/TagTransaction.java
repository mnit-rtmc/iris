/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015-2018  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.server.comm.CRC;
import us.mn.state.dot.tms.server.comm.ParsingException;

/**
 * Tag transaction.
 *
 * @author Douglas Lau
 */
public class TagTransaction extends E6Property {

	/** SeGo read CRC-16 */
	static private final CRC SEGO_CRC = new CRC(16, 0x1021, 0xFFFF, false,
		0xFFFF);

	/** SeGo Page 0 CRC-12 */
	static private final CRC PAGE0_CRC = new CRC(12, 0x80F, 0x000, false);

	/** IAG CRC-16 */
	static private final CRC IAG_CRC = new CRC(16, 0x1021, 0x0000, false);

	/** SeGo authority code for FDOT.
	 * Also used for interoperability, with 16 million range IDs */
	static private final int AUTHORITY_FDOT = 0x0A01;

	/** SeGo authority code for NCTA */
	static private final int AUTHORITY_NCTA = 0x0A07;

	/** SeGo authority code for MnPASS */
	static private final int AUTHORITY_MNPASS = 0x0A09;

	/* FIXME: change to 4-digit authority code */

	/** Tag transaction types */
	public enum TransactionType {
		sego_read_streamlined_page_4 (0x3021, 21+7),
		sego_read_verify_page        (0x3022, 20),
		sego_read_regular            (0x3023, 6),
		seen_frame_count             (0x3043, 6),
		sego_read_streamlined_page_9 (0x3070, 21+7),
		astm_read                    (0x5014, 10+7),
		iag_read                     (0x5026, 35+7),
		iag_read_authenticated       (0x5030, 35+7);
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
		return isSeGoReadValid()
		    || isIAGReadValid()
		    || isASTMReadValid();
	}

	/** Get the transaction type code */
	private Integer getTypeCode() {
		return (data.length >= 2) ? parse16(data, 0) : null;
	}

	/** Get the transaction type */
	private TransactionType getTransactionType() {
		Integer c = getTypeCode();
		return (c != null) ? TransactionType.fromCode(c) : null;
	}

	/** Check if the data length is valid for the transaction type */
	private boolean isLengthValid() {
		TransactionType tt = getTransactionType();
		return (tt != null) && (data.length == tt.len);
	}

	/** Get the date/time stamp */
	public Long getStamp() {
		if (isSeGoReadValid())
			return parseStamp(21);
		if (isIAGReadValid())
			return parseStamp(35);
		if (isASTMReadValid())
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
		if (isSeGoReadValid())
			return TagType.SeGo;
		if (isIAGReadValid())
			return TagType.IAG;
		if (isASTMReadValid())
			return TagType.ASTM;
		return null;
	}

	/** Get the agency ID */
	public Integer getAgency() {
		if (isSeGoReadValid())
			return parseSeGoAgency();
		if (isIAGReadValid())
			return parseIAGAgency();
		else
			return null;
	}

	/** Get the transponder ID */
	public Integer getId() {
		if (isSeGoReadValid())
			return parseSeGoId();
		if (isIAGReadValid())
			return parseIAGId();
		if (isASTMReadValid())
			return parseASTMId();
		return null;
	}

	/** Check if transaction is a valid SeGo streamlined read */
	private boolean isSeGoReadValid() {
		TransactionType tt = getTransactionType();
		return (TransactionType.sego_read_streamlined_page_4 == tt
		     || TransactionType.sego_read_streamlined_page_9 == tt)
		     && isLengthValid()
		     && isSeGoReadCRCValid()
		     && isSeGoMnPass()
		     && isSeGoPage0CRCValid();
	}

	/** Check if SeGo read CRC is valid.  NOTE: length must be valid */
	private boolean isSeGoReadCRCValid() {
		byte[] payload = new byte[16];
		System.arraycopy(data, 2, payload, 0, 16);
		return SEGO_CRC.calculate(payload) == getSeGoCRC16();
	}

	/** Get SeGo CRC-16 from data packet */
	private int getSeGoCRC16() {
		return parse16(data, 18);
	}

	/** Is it a SeGo MnPass tag? */
	private boolean isSeGoMnPass() {
		/* Non-MnPass tags have E022 at start of page 0 */
		return data[2] != 0xE0;
	}

	/** Check if SeGo page 0 CRC is valid.  NOTE: length must be valid */
	private boolean isSeGoPage0CRCValid() {
		byte[] page0 = new byte[7];
		page0[0] = getShiftedData(2);
		page0[1] = getShiftedData(3);
		page0[2] = getShiftedData(4);
		page0[3] = getShiftedData(5);
		page0[4] = getShiftedData(6);
		page0[5] = getShiftedData(7);
		page0[6] = (byte) (data[8] << 4);
		return PAGE0_CRC.calculate(page0) == getSeGoCRC12();
	}

	/** Get data shifted by 4 bits (for CRC) */
	private byte getShiftedData(int o) {
		return (byte) ((data[o] << 4) | ((data[o + 1] >> 4) & 0x0F));
	}

	/** Get SeGo CRC-12 from data packet */
	private int getSeGoCRC12() {
		return ((data[2] & 0xF0) << 4) | (data[9] & 0xFF);
	}

	/** Parse a SeGo agency */
	private Integer parseSeGoAgency() {
		return parse16(data, 4);
	}

	/** Parse a SeGo ID */
	private Integer parseSeGoId() {
		/* Note: byte 5 is agency, but we mask it off here */
		return parse32(data, 5) & 0xFFFFFF;
	}

	/** Check if transaction is a valid IAG read */
	private boolean isIAGReadValid() {
		TransactionType tt = getTransactionType();
		return (TransactionType.iag_read == tt
		     || TransactionType.iag_read_authenticated == tt)
		     && isLengthValid()
		     && isIAG_CRCValid();
	}

	/** Check if IAG CRC is valid.  NOTE: length must be valid */
	private boolean isIAG_CRCValid() {
		byte[] payload = new byte[30];
		System.arraycopy(data, 2, payload, 0, 30);
		return IAG_CRC.calculate(payload) == getIAG_CRC16();
	}

	/** Get IAG CRC-16 from data packet */
	private int getIAG_CRC16() {
		return parse16(data, 32);
	}

	/** Parse an IAG ID */
	private Integer parseIAGId() {
		return (parse32(data, 4) >> 1) & 0xFFFFFF;
	}

	/** Parse an IAG agency */
	private Integer parseIAGAgency() {
		return (parse8(data, 4) >> 1) & 0x7F;
	}

	/** Parse an IAG HOV flag */
	private boolean parseIAG_HOV() {
		return (parse8(data, 26) & 0x03) != 0;
	}

	/** Check if transaction is a valid ASTM read */
	private boolean isASTMReadValid() {
		TransactionType tt = getTransactionType();
		// FIXME: check CRC
		return (TransactionType.astm_read == tt)
		     && isLengthValid();
	}

	/** Parse an ASTM ID */
	private Integer parseASTMId() {
		return parse32(data, 3);
	}

	/** Get HOV flag */
	public Boolean getHOV() {
		if (isSeGoReadValid())
			return parseSeGoHOV();
		if (isIAGReadValid())
			return parseIAG_HOV();
		if (isASTMReadValid())
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
			Integer agency = getAgency();
			Integer tid = getId();
			Boolean hov = getHOV();
			if (stamp != null && typ != null && tid != null)
				tr.logRead(stamp, typ, agency, tid, hov);
		}
	}

	/** Parse a tag type in a seen frame count transaction */
	private TagType parseSeenTagType() {
		switch (data[2]) {
		case 1: return TagType.SeGo;
		case 2: return TagType.IAG;
		case 3: return TagType.ASTM;
		default: return null;
		}
	}

	/** Get a string representation */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		final TransactionType tt = getTransactionType();
		sb.append("tag transaction: ");
		if (null == tt) {
			sb.append("INVALID TYPE CODE: ");
			sb.append(getTypeCode());
			return sb.toString();
		}
		sb.append(tt);
		if (!isLengthValid()) {
			sb.append(" INVALID LENGTH: ");
			sb.append(data.length);
			return sb.toString();
		}
		if (tt == TransactionType.sego_read_streamlined_page_4
		 || tt == TransactionType.sego_read_streamlined_page_9)
		{
			if (!isSeGoReadCRCValid()) {
				sb.append(" INVALID CRC: ");
				sb.append(getSeGoCRC16());
			} else if (isSeGoMnPass() && !isSeGoPage0CRCValid()) {
				sb.append(" INVALID PAGE0 CRC: ");
				sb.append(getSeGoCRC12());
			}
		}
		if (tt == TransactionType.iag_read
		 || tt == TransactionType.iag_read_authenticated)
		{
			if (!isIAG_CRCValid()) {
				sb.append(" INVALID CRC: ");
				sb.append(getIAG_CRC16());
			}
		}
		if (tt == TransactionType.seen_frame_count) {
			TagType tag = parseSeenTagType();
			if (tag != null) {
				sb.append(' ');
				sb.append(tag);
				sb.append(": ");
				sb.append(parse16(data, 3));
			}
		}
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
