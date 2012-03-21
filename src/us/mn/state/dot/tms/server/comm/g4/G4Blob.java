/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012  Iteris Inc.
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
package us.mn.state.dot.tms.server.comm.g4;

import java.util.Date;
import java.util.LinkedList;
import us.mn.state.dot.tms.server.comm.ChecksumException;
import us.mn.state.dot.tms.server.comm.ParsingException;
import us.mn.state.dot.tms.utils.ByteBlob;

/**
 * This is an ordered list of bytes sent to, or received from a G4 
 * controller. Methods help interpret this data for parsing and
 * constructing G4 records.
 * 
 * @author Michael Darter
 */
public class G4Blob extends ByteBlob {

	/** G4 record leading bytes */
	private static final int[] LEADER = {0xFF, 0xAA};

	/** G4 header size */
	private static final int HEADER_SIZE = 4;

	/** G4 field indexes */
	private static final int QUAL_IDX = 2;
	private static final int DATALEN_IDX = 3;
	private static final int SID_IDX = 4;
	private static final int MSGNUM_IDX = 6;
	private static final int STATUS_IDX = 9;
	private static final int DATA_IDX = 6;

	/** Qualifiers */
	public static final int QUAL_DATAREQ = 0x53;
	public static final int QUAL_STATSHEADER = 0x80;
	public static final int QUAL_VOL = 0x10;
	public static final int QUAL_OCC = 0x11;
	public static final int QUAL_SPEED = 0x12;
	public static final int QUAL_C1 = 0x14;
	public static final int QUAL_C2 = 0x15;
	public static final int QUAL_C3 = 0x16;
	public static final int QUAL_C4 = 0x17;
	public static final int QUAL_C5 = 0x18;
	public static final int QUAL_STATSEND = 0x81;

	/** Data lengths for requests */
	public static final int DATALEN_DATAREQ = 3;

	/** Minimum length of record (stat end message) */
	private static final int STAT_END_LEN = 9;
	private static final int MIN_REC_LEN = STAT_END_LEN;

	/** Minimum data length */
	private static final int MIN_DATA_LEN = 3;

	/** Constructor */
	public G4Blob() {
		super();
	}

	/** Constructor with initial capacity */
	public G4Blob(int ic) {
		super(ic);
	}

	/** Constructor with array */
	public G4Blob(int[] ba) {
		super(ba);
	}

	/** Constructor with blob */
	public G4Blob(ByteBlob bb) {
		super(bb);
	}

	/** Is the record the minimum length or longer? */
	public boolean minRecLength() {
		return size() >= MIN_REC_LEN;
	}

	/** Is the leader valid? The leader is the first 2 bytes.
	 * @param i Index of first byte of leader.
	 * @return True if the leader is valid else false. */
	protected boolean validLeader(int i) {
		if(i < 0 || i >= size() || size() < LEADER.length)
			return false;
		return getInt(i) == LEADER[0] && getInt(i + 1) == LEADER[1];
	}

	/** Get the qualifier.
	 * @return The qualifier.
	 * @throws ParsingException if not enough bytes */
	protected int getQualifier() throws ParsingException {
		if(size() < 3)
			throw new ParsingException("invalid length");
		return getInt(QUAL_IDX);
	}

	/** Get the sensor id.
	 * @return The sensor id */
	protected int getSensorId() throws ParsingException {
		if(size() < 6)
			throw new ParsingException("invalid length");
		int sid = getTwoByteValue(SID_IDX);
		if(sid < 0 || sid > 65536 - 1)
			throw new ParsingException("msg_num invalid");
		return sid;
	}

	/** Get the message number.
	 * @return A valid message number */
	protected int getMsgNum() throws ParsingException {
		if(size() < 7)
			throw new ParsingException("invalid length");
		int mn = getInt(MSGNUM_IDX);
		if(mn < 0 || mn > 255)
			throw new ParsingException("msg_num invalid");
		return mn;
	}

	/** Get the data length field value */
	protected int getDataLength() throws ParsingException {
		if(size() > DATALEN_IDX)
			return getInt(DATALEN_IDX);
		else
			throw new ParsingException("record too small");
	}

	/** Get the number of zones */
	protected int getNumZones() throws ParsingException {
		if(size() < 18)
			throw new ParsingException("invalid length");
		int n = 0xF & getInt(17);
		if(n >=0 && n <= LaneSample.MAX_NUM_LANES)
			return n;
		throw new ParsingException("invalid #lanes=" + n);
	}

	/** Get the controller time */
	protected long getControllerTime() throws ParsingException {
		//TODO: protocol doc is unclear on time value
		//long v = getFourByteValue(10);
		return new Date().getTime();
	}

	/** Get the message period in secs */
	protected int getMsgPeriod() throws ParsingException {
		if(size() < 21)
			throw new ParsingException("invalid length");
		return getTwoByteValue(19);
	}

	/** Get the voltage */
	protected double getVoltage() throws ParsingException {
		if(size() < 22)
			throw new ParsingException("invalid length");
		return getInt(21) * .1;
	}

	/** Get units
	 * @return True for SI units else false for Imperial. */
	protected boolean getUnits() throws ParsingException {
		if(size() > STATUS_IDX)
			return (0x01 & getInt(STATUS_IDX)) == 0 ? true : false;
		else
			throw new ParsingException("invalid length");
	}

	/** Get number of classes */
	protected int getNumClasses() throws ParsingException {
		if(size() < 19)
			throw new ParsingException("invalid length");
		int nc = 0x3f & getInt(18);
		if(nc == 0x08)
			return 2;
		else if(nc == 0x38)
			return 4;
		else if(nc == 0x3b)
			return 6;
		throw new ParsingException("unexpected bit pattern");
	}

	/** Get the expected record size, which is the size
	 * of the header plus data length plus the 2 checksum bytes. */
	private int expectedSize() throws ParsingException {
		return HEADER_SIZE + getDataLength() + 2;
	}

	/** Get the value of the checksum field */
	protected int getChecksum() throws ParsingException {
		int csidx = HEADER_SIZE + getDataLength();
		if(size() >= csidx + 2)
			return getTwoByteValue(csidx);
		else
			throw new ParsingException("invalid length");
	}

	/** Is the checksum valid? */
	protected boolean isChecksumValid() throws ParsingException {
		int csv = getChecksum();
		int dl = getDataLength();
		int ccs = calcIntChecksum(HEADER_SIZE, HEADER_SIZE + dl - 1);
		return ccs == csv;
	}

	/** Build a G4 data request.
	 * @param sid Sensor id.
	 * @return A G4 data request for the specified sensor id. */
	static protected G4Blob buildDataRequest(int sid) {
		G4Blob b = new G4Blob(9);
		b.add((byte)0xFF);
		b.add((byte)0xAA);
		b.add((byte)QUAL_DATAREQ);
		b.add((byte)DATALEN_DATAREQ);
		b.add((byte)(sid >> 8) & 0x0F); // MSB
		b.add((byte)(sid & 0x0F)); 		// LSB
		b.add((byte)0x00);
		byte[] cs = b.calcByteChecksum(HEADER_SIZE, 
			HEADER_SIZE + DATALEN_DATAREQ - 1);
		b.add(cs);
		return b;
	}

	/** Subdivide the blob into valid records.
 	 * @return a linked list containing valid records. */
	protected LinkedList<G4Blob> subdivide() throws ParsingException {
		LinkedList<G4Blob> l = new LinkedList<G4Blob>();
		int i = 0;
		while(i < size()) {
			G4Blob sr = validRec(i); // throws ParsingException
			if(sr == null) {
				++i;
				continue;
			}
			l.add(sr);
			i += sr.size();
		}
		return l;
	}

	/** Does the blob contain a single valid record? */
	protected boolean singleValidRec() throws ParsingException {
		if(validRec(0) == null)
			return false;
		return size() == expectedSize();
	}

	/** Is there is a valid record starting at the specified index?
	 * @return Null for invalid records else the record as a blob.
	 * @throws ChecksumException on checksum problems */
	protected G4Blob validRec(int i) throws ParsingException {
		if(i < 0 || i >= size())
			return null;
		if(!validLeader(i))
			return null;
		G4Blob bb = new G4Blob(getByteBlob(i, -1));
		if(bb.size() < MIN_REC_LEN)
			return null;
		if(!bb.validQualifier() || !bb.validDataLength())
			return null;
		// at this point, we know we have a record
		if(!bb.isChecksumValid())
			throw new ChecksumException("invalid checksum");
		return new G4Blob(bb.getByteBlob(0, bb.expectedSize()));
	}

	/* Is the record's qualifier valid? */
	private boolean validQualifier() throws ParsingException {
		int q = getQualifier();
		return q == QUAL_STATSHEADER || 
			q == QUAL_VOL || q == QUAL_OCC ||
			q == QUAL_SPEED || q == QUAL_C1 ||
			q == QUAL_C2 || q == QUAL_C3 ||
			q == QUAL_C4 || q == QUAL_C5 ||
			q == QUAL_STATSEND;
	}

	/** Is the data length stored in a record valid?
	 * @return True if the datalength is valid else false. */
	protected boolean validDataLength() throws ParsingException {
		int dl = getDataLength();
		return dl >= MIN_DATA_LEN && size() >= expectedSize();
	}

	/** Get a sample value for the specified lane.
	 * @param lane Lane number, zero based */
	protected int getSampleValue(int lane) throws ParsingException {
		assert(lane >= 0 && lane < LaneSample.MAX_NUM_LANES);
		int idx = DATA_IDX + 2 * lane;
		if(size() < idx + 2)
			throw new ParsingException("invalid length");
		return getTwoByteValue(idx);
	}

	/** Does the blob contain a header record */
	private boolean header_rec_read = false;
	private boolean statend_rec_read = false;

	/** Is the message read complete? This method is called during the 
	 * message read process to determine, on a byte-by-byte basis, if the 
	 * entire G4 message has been received.
	 * @return True when the complete message has been read else false. */
	protected boolean readComplete() {
		return containsStatEndRec();
	}

	/** Does the blob contain a STAT END record? This method is called
	 * each time a byte is read from the input stream so needs to be 
	 * fast. The record checksum is not verified.
	 * @return True if the last byte in the blob is the last checksum 
	 *	   byte of a STAT END message, else false. */
	protected boolean containsStatEndRec() {
		final int size = size();
		if(size < STAT_END_LEN)
			return false;
		final int ri = size - STAT_END_LEN;
		if(ri < 0)
			return false;
		if(!validLeader(ri))
			return false;
		if(getInt(ri + QUAL_IDX) != QUAL_STATSEND)
			return false;
		if(getInt(ri + DATALEN_IDX) != 3)
			return false;
		return true;
	}
}
