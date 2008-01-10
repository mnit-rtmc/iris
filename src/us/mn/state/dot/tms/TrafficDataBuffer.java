/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2008  Minnesota Department of Transportation
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.DataOutputStream;
import java.util.Calendar;
import java.util.Date;

/**
 * A buffer for traffic detector data. This is needed for performance, so that
 * each data file (10,000+) does not need to be updated every 30 seconds.
 *
 * @author Douglas Lau
 */
abstract public class TrafficDataBuffer implements Constants {

	/** Path where traffic data files are stored */
	static protected final String DATA_PATH = "/data/traffic";

	/** Buffer size is 10 minutes (one record every 30 seconds) */
	static protected final int BUFFERED_RECORDS = 20;

	/** Offset between records in milliseconds */
	static protected final int RECORD_OFFSET = 30000;

	/** Create a date string (eg YYYYMMDD) from the given date stamp */
	static public String date(long stamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(stamp));
		StringBuffer b = new StringBuffer(13);
		b.append(cal.get(Calendar.YEAR));
		while(b.length() < 4)
			b.insert(0, '0');
		b.append(cal.get(Calendar.MONTH) + 1);
		while(b.length() < 6)
			b.insert(4, '0');
		b.append(cal.get(Calendar.DAY_OF_MONTH));
		while(b.length() < 8)
			b.insert(6, '0');
		return b.toString();
	}

	/** Get a valid directory for a given date stamp */
	static protected File directory(long stamp) throws IOException {
		String d = date(stamp);
		File year = new File(DATA_PATH + File.separator +
			d.substring(0, 4));
		if(!year.exists()) {
			if(!year.mkdir())
				throw new IOException("mkdir failed: " + year);
		}
		File dir = new File(year.getPath() + File.separator + d);
		if(!dir.exists()) {
			if(!dir.mkdir())
				throw new IOException("mkdir failed: " + dir);
		}
		return dir;
	}

	/** Compute the file record number for a given time stamp */
	static protected int record(long stamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(stamp));
		return cal.get(Calendar.HOUR_OF_DAY) * 120 +
			cal.get(Calendar.MINUTE) * 2 +
			cal.get(Calendar.SECOND) / 30;
	}

	/** Print out a debugging message */
	static protected void debug(String m) {
		System.err.println("TDB " + m);
	}

	/** Create a file (path) for the given time stamp */
	public File file(long stamp) throws IOException {
		return new File(directory(stamp).getCanonicalPath() +
			File.separator + det_id + extension());
	}

	/** Time stamp of first record stored in the buffer */
	protected long start;

	/** Detector ID */
	protected final String det_id;

	/** Count of valid records in the buffer */
	protected int count;

	/** Data buffer */
	protected final short[] buf = new short[BUFFERED_RECORDS];

	/** Create a new traffic data buffer */
	protected TrafficDataBuffer(String det) {
		det_id = det;
	}

	/** Get a string representation of the buffer */
	public String toString() {
		StringBuffer b = new StringBuffer();
		b.append(new Date(start));
		b.append(" (");
		for(int i = 0; i < count; i++) {
			if(i > 0)
				b.append(',');
			b.append(read(i));
		}
		b.append(")");
		return b.toString();
	}

	/** Get the file extension */
	abstract protected String extension();

	/** Get the number of bytes per record */
	abstract protected int recordSize();

	/** Get the number of records in the file */
	protected int fileRecords(File f) {
		return (int)(f.length() / recordSize());
	}

	/** Get the record offset from the start of the buffer */
	protected int recordOffset(long stamp) {
		return (int)((stamp / RECORD_OFFSET) - (start / RECORD_OFFSET));
	}

	/** Write a data record to the buffer */
	public void write(long stamp, int value) {
		if(count == 0)
			start = stamp;
		int offset = recordOffset(stamp) - count;
		if(offset >= 0) {
			while(offset-- > 0)
				write(MISSING_DATA);
			write(value);
		} else {
			/* Timestamps duplicated or out of order */
			/* What can we do? */
		}
	}

	/** Write a single record to the end of the buffer */
	protected void write(int value) {
		buf[count] = (short)value;
		count++;
	}

	/** Write a value to a data output stream */
	abstract protected void writeTo(DataOutputStream dos, int value)
		throws IOException;

	/** Read a group of records (for merging 5-minute data) */
	public int[] read(long stamp, int len) {
		int offset = recordOffset(stamp);
		int[] values = new int[len];
		for(int i = 0; i < len; i++)
			values[i] = read(offset + i);
		return values;
	}

	/** Read a single record out of the buffer */
	protected int read(int offset) {
		if(offset < 0 || offset >= count)
			return MISSING_DATA;
		else
			return buf[offset];
	}

	/** Merge a group of records (for 5-minute data) */
	public void merge(long stamp, int[] values) {
		if(count == 0)
			start = stamp;
		long time = start;
		int offset = recordOffset(stamp);
		int len = Math.max(count, offset + values.length);
		if(offset < 0) {
			if(offset + values.length <= 0)
				throw new IndexOutOfBoundsException();
			time = stamp;
			len = Math.max(count - offset, values.length);
			offset = 0;
		}
		if(len > BUFFERED_RECORDS)
			throw new IndexOutOfBoundsException();
		int[] vals = read(time, len);
		for(int i = 0; i < values.length; i++)
			vals[i + offset] = values[i];
		start = time;
		count = 0;
		for(int i = 0; i < len; i++)
			write(vals[i]);
	}

	/** Flush all buffered data from before the given time */
	public void flush(long stamp) throws IOException {
		while(count > 0) {
			int offset = recordOffset(stamp);
			if(offset <= 0)
				return;
			int r = record(start);
			int mark = Math.min(r + offset, SAMPLES_PER_DAY);
			int records = flush(mark - r);
			count -= records;
			if(count > 0) {
				start += RECORD_OFFSET * records;
				System.arraycopy(buf, records, buf, 0, count);
			}
		}
	}

	/** Flush the given number of records to the file system */
	protected int flush(int records) throws IOException {
		if(records > count)
			records = count;
		boolean missing = true;
		for(int i = 0; i < records; i++) {
			if(read(i) != MISSING_DATA)
				missing = false;
		}
		if(missing)
			return records;
		File f = file(start);
		int r = record(start);
		int offset = r - fileRecords(f);
		if(offset < 0)
			truncateFile(f, r, offset);
		FileOutputStream fos = new FileOutputStream(f.getPath(), true);
		try {
			BufferedOutputStream bos =
				new BufferedOutputStream(fos);
			DataOutputStream dos = new DataOutputStream(bos);
			while(offset-- > 0)
				writeTo(dos, MISSING_DATA);
			for(int i = 0; i < records; i++)
				writeTo(dos, read(i));
			dos.flush();
		}
		finally {
			fos.close();
		}
		return records;
	}

	/** Truncate the file before the specified record stamp */
	protected void truncateFile(File f, int r, int o) throws IOException {
		debug("Truncating " + f + " @ record: " + r + " offset: " + o);
		RandomAccessFile raf = new RandomAccessFile(f, "rw");
		try { raf.setLength(r * recordSize()); }
		finally { raf.close(); }
	}

	/** Detector volume data buffer */
	static public final class Volume extends TrafficDataBuffer {

		/** Volume data record size */
		static protected final int RECORD_SIZE = 1;

		/** Create a new volume data buffer */
		public Volume(String det) {
			super(det);
		}

		/** Get the file extension for a volume data file */
		protected String extension() { return ".v30"; }

		/** Get the number of bytes per record */
		protected int recordSize() { return RECORD_SIZE; }

		/** Write a value to a data output stream */
		protected void writeTo(DataOutputStream dos, int vol)
			throws IOException
		{
			if(vol < MISSING_DATA || vol > Byte.MAX_VALUE)
				vol = MISSING_DATA;
			dos.writeByte(vol);
		}
	}

	/** Detector scan data buffer */
	static public final class Scan extends TrafficDataBuffer {

		/** Scan data record size */
		static protected final int RECORD_SIZE = 2;

		/** Create a new scan data buffer */
		public Scan(String det) {
			super(det);
		}

		/** Get the file extension for a scan data file */
		protected String extension() { return ".c30"; }

		/** Get the number of bytes per record */
		protected int recordSize() { return RECORD_SIZE; }

		/** Write a value to a data output stream */
		protected void writeTo(DataOutputStream dos, int scan)
			throws IOException
		{
			if(scan < MISSING_DATA)
				scan = MISSING_DATA;
			dos.writeShort(scan);
		}
	}

	/** Detector speed data buffer */
	static public final class Speed extends TrafficDataBuffer {

		/** Speed data record size */
		static protected final int RECORD_SIZE = 1;

		/** Create a new speed data buffer */
		public Speed(String det) {
			super(det);
		}

		/** Get the file extension for a speed data file */
		protected String extension() { return ".s30"; }

		/** Get the number of bytes per record */
		protected int recordSize() { return RECORD_SIZE; }

		/** Write a value to a data output stream */
		protected void writeTo(DataOutputStream dos, int speed)
			throws IOException
		{
			if(speed < MISSING_DATA || speed > Byte.MAX_VALUE)
				speed = MISSING_DATA;
			dos.writeByte(speed);
		}
	}
}
