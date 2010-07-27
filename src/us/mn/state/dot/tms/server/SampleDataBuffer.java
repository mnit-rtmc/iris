/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2010  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.RandomAccessFile;
import java.io.IOException;
import java.io.DataOutputStream;
import java.util.Date;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.Constants;

/**
 * A buffer for periodic sample data.  Sample files are binary with a fixed
 * number of bytes per sample.  Each file contains one day of sample data.  For
 * example, a volume file with a 30-second period would have 2880 bytes.
 *
 * @author Douglas Lau
 */
abstract public class SampleDataBuffer {

	/** Sample data debug log */
	static protected final IDebugLog TRAFFIC_LOG = new IDebugLog("traffic");

	/** Path where sample data files are stored */
	static protected final String DATA_PATH = "/var/lib/iris/traffic";

	/** Number of samples to buffer */
	static protected final int BUFFERED_SAMPLES = 20;

	/** Get a valid directory for a given date stamp.
	 * @param stamp Time stamp
	 * @return Directory to store sample data.
	 * @throws IOException If directory cannot be created. */
	static protected File directory(long stamp) throws IOException {
		String d = TimeSteward.dateShortString(stamp);
		File year = new File(DATA_PATH + File.separator +
			d.substring(0, 4));
		if(!year.exists() && !year.mkdir())
			throw new IOException("mkdir failed: " + year);
		File dir = new File(year.getPath() + File.separator + d);
		if(!dir.exists() && !dir.mkdir())
			throw new IOException("mkdir failed: " + dir);
		return dir;
	}

	/** Compute the file sample number for a given time stamp.
	 * @param stamp Time stamp of sample.
	 * @return Sample number in file (0 is first sample). */
	protected int sampleNumber(long stamp) {
		return TimeSteward.secondOfDayInt(stamp) / period;
	}

	/** Create a file (path) for the given time stamp.
	 * @param stamp Time stamp of sample.
	 * @return File to store sample data. */
	protected File sampleFile(long stamp) throws IOException {
		return new File(directory(stamp).getCanonicalPath() +
			File.separator + sensor + extension());
	}

	/** Get the sampling period in milliseconds. */
	protected int periodMillis() {
		return period * 1000;
	}

	/** Sensor ID */
	protected final String sensor;

	/** Sample period in seconds */
	protected final int period;

	/** Data buffer */
	protected final short[] buf = new short[BUFFERED_SAMPLES];

	/** Time stamp of first sample in the buffer */
	protected long start;

	/** Count of valid samples in the buffer */
	protected int count;

	/** Create a new sample data buffer.
	 * @param s Sensor ID */
	protected SampleDataBuffer(String s, int p) {
		sensor = s;
		period = p;
	}

	/** Get a string representation of the sample data buffer */
	public String toString() {
		StringBuilder b = new StringBuilder();
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

	/** Get the number of bytes per sample */
	abstract protected int sampleSize();

	/** Get the count of samples in a file.
	 * @param f File to check.
	 * @return Count of samples in the file. */
	protected int sampleCount(File f) {
		return (int)(f.length() / sampleSize());
	}

	/** Get the sample offset from the start of the buffer.
	 * @param stamp Time stamp of sample.
	 * @return Number of samples from start of buffer. */
	protected int sampleOffset(long stamp) {
		// NOTE: this cannot use sampleNumber because the samples might
		//       be in different days
		int p = periodMillis();
		return (int)((stamp / p) - (start / p));
	}

	/** Write a data sample to the buffer.
	 * @param stamp Time stamp of sample.
	 * @param value Sample value. */
	public void write(long stamp, int value) {
		if(count == 0)
			start = stamp;
		int offset = sampleOffset(stamp) - count;
		if(offset >= 0) {
			while(offset-- > 0)
				write(Constants.MISSING_DATA);
			write(value);
		} else {
			/* Timestamps duplicated or out of order */
			TRAFFIC_LOG.log("out of order: " + start + " vs " +
				stamp + " value: " + value);
		}
	}

	/** Write a single sample to the end of the buffer.
	 * @param value Sample value. */
	protected void write(int value) {
		buf[count] = (short)value;
		count++;
	}

	/** Write a value to a data output stream.
	 * @param dos DataOutputStream to write to.
	 * @param value Sample value. */
	abstract protected void writeTo(DataOutputStream dos, int value)
		throws IOException;

	/** Read a group of samples (for merging 5-minute data).
	 * @param stamp Time stamp of first sample.
	 * @param len Number of samples to read.
	 * @return Array of sample data. */
	public int[] read(long stamp, int len) {
		int offset = sampleOffset(stamp);
		int[] values = new int[len];
		for(int i = 0; i < len; i++)
			values[i] = read(offset + i);
		return values;
	}

	/** Read a single sample out of the buffer.
	 * @param offset Sample offset.
	 * @return Sample value. */
	protected int read(int offset) {
		if(offset < 0 || offset >= count)
			return Constants.MISSING_DATA;
		else
			return buf[offset];
	}

	/** Merge a group of samples (for 5-minute data).
	 * @param stamp Time stamp of first sample.
	 * @param values Array of sample data. */
	public void merge(long stamp, int[] values) {
		if(count == 0)
			start = stamp;
		long time = start;
		int offset = sampleOffset(stamp);
		int len = Math.max(count, offset + values.length);
		if(offset < 0) {
			if(offset + values.length <= 0)
				throw new IndexOutOfBoundsException();
			time = stamp;
			len = Math.max(count - offset, values.length);
			offset = 0;
		}
		if(len > BUFFERED_SAMPLES)
			throw new IndexOutOfBoundsException();
		int[] vals = read(time, len);
		for(int i = 0; i < values.length; i++)
			vals[i + offset] = values[i];
		start = time;
		count = 0;
		for(int i = 0; i < len; i++)
			write(vals[i]);
	}

	/** Flush all buffered data from before the given time.
	 * @param stamp Time stamp. */
	public void flush(long stamp) throws IOException {
		while(count > 0) {
			int offset = sampleOffset(stamp);
			if(offset <= 0)
				return;
			int n = sampleNumber(start);
			int mark = Math.min(n + offset,
				Constants.SAMPLES_PER_DAY);
			int n_samples = flush(mark - n);
			count -= n_samples;
			if(count > 0) {
				start += periodMillis() * n_samples;
				System.arraycopy(buf, n_samples, buf, 0, count);
			}
		}
	}

	/** Flush the given number of samples to the file system.
	 * @param n_sample Number of samples to flush.
	 * @return Number of samples actually flushed. */
	protected int flush(int n_samples) throws IOException {
		if(n_samples > count)
			n_samples = count;
		boolean missing = true;
		for(int i = 0; i < n_samples; i++) {
			if(read(i) != Constants.MISSING_DATA)
				missing = false;
		}
		if(missing)
			return n_samples;
		File f = sampleFile(start);
		int n = sampleNumber(start);
		int offset = n - sampleCount(f);
		if(offset < 0) {
			TRAFFIC_LOG.log("Truncating " + f + " @ sample: " + n +
				" offset: " + offset);
			truncateFile(f, n);
		}
		FileOutputStream fos = new FileOutputStream(f.getPath(), true);
		try {
			BufferedOutputStream bos =
				new BufferedOutputStream(fos);
			DataOutputStream dos = new DataOutputStream(bos);
			while(offset-- > 0)
				writeTo(dos, Constants.MISSING_DATA);
			for(int i = 0; i < n_samples; i++)
				writeTo(dos, read(i));
			dos.flush();
		}
		finally {
			fos.close();
		}
		return n_samples;
	}

	/** Truncate the file before the specified sample stamp.
	 * @param f File to truncate.
	 * @param n Sample number to truncate to. */
	protected void truncateFile(File f, int n) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(f, "rw");
		try {
			raf.setLength(n * sampleSize());
		}
		finally {
			raf.close();
		}
	}

	/** Detector volume data buffer */
	static public final class Volume extends SampleDataBuffer {

		/** Volume data sample size */
		static protected final int SAMPLE_SIZE = 1;

		/** Create a new volume data buffer */
		public Volume(String s) {
			super(s, 30);
		}

		/** Get the file extension for a volume data file */
		protected String extension() { return ".v30"; }

		/** Get the number of bytes per sample */
		protected int sampleSize() { return SAMPLE_SIZE; }

		/** Write a value to a data output stream */
		protected void writeTo(DataOutputStream dos, int vol)
			throws IOException
		{
			if(vol < Constants.MISSING_DATA || vol > Byte.MAX_VALUE)
				vol = Constants.MISSING_DATA;
			dos.writeByte(vol);
		}
	}

	/** Detector scan data buffer */
	static public final class Scan extends SampleDataBuffer {

		/** Scan data sample size */
		static protected final int SAMPLE_SIZE = 2;

		/** Create a new scan data buffer */
		public Scan(String s) {
			super(s, 30);
		}

		/** Get the file extension for a scan data file */
		protected String extension() { return ".c30"; }

		/** Get the number of bytes per sample */
		protected int sampleSize() { return SAMPLE_SIZE; }

		/** Write a value to a data output stream */
		protected void writeTo(DataOutputStream dos, int scan)
			throws IOException
		{
			if(scan < Constants.MISSING_DATA)
				scan = Constants.MISSING_DATA;
			dos.writeShort(scan);
		}
	}

	/** Detector speed data buffer */
	static public final class Speed extends SampleDataBuffer {

		/** Speed data sample size */
		static protected final int SAMPLE_SIZE = 1;

		/** Create a new speed data buffer */
		public Speed(String s) {
			super(s, 30);
		}

		/** Get the file extension for a speed data file */
		protected String extension() { return ".s30"; }

		/** Get the number of bytes per sample */
		protected int sampleSize() { return SAMPLE_SIZE; }

		/** Write a value to a data output stream */
		protected void writeTo(DataOutputStream dos, int speed)
			throws IOException
		{
			if(speed < Constants.MISSING_DATA ||
			   speed > Byte.MAX_VALUE)
				speed = Constants.MISSING_DATA;
			dos.writeByte(speed);
		}
	}
}
