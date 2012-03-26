/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2010-2012  Minnesota Department of Transportation
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
import java.io.RandomAccessFile;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.ConcurrentSkipListSet;
import us.mn.state.dot.sched.TimeSteward;
import us.mn.state.dot.tms.Constants;
import us.mn.state.dot.tms.Interval;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * A cache for periodic sample data.  This is needed so that threads which are
 * collecting sample data do not have to wait for file I/O to store the data.
 * Another dedicated thread can call the flush method at regular intervals to
 * force the cached data to be written to files.
 *
 * Sample files are binary with a fixed number of bytes per sample.
 * Each file contains one day of sample data.  For example, a volume file with
 * a 30-second period would have 2880 bytes.
 *
 * @author Douglas Lau
 */
abstract public class PeriodicSampleCache {

	/** Minimum sample period (seconds) */
	static protected final int MIN_PERIOD = 20;

	/** Maximum sample bytes */
	static protected final int MAX_SAMPLE_BYTES = 2;

	/** Get the number of samples per day */
	static private int samplesPerDay(int period) {
		return Interval.DAY / period;
	}

	/** Byte buffer for flushing samples to file */
	static protected final ByteBuffer buffer = ByteBuffer.allocate(
		samplesPerDay(MIN_PERIOD) * MAX_SAMPLE_BYTES);

	/** Sample archive factory */
	private final SampleArchiveFactory factory;

	/** Sample period in seconds */
	private final int period;

	/** Sample cache */
	private final ConcurrentSkipListSet<PeriodicSample> samples =
		new ConcurrentSkipListSet<PeriodicSample>();

	/** Create a new periodic sample cache.
	 * @param f Sample archive factory.
	 * @param p Sample period in seconds. */
	protected PeriodicSampleCache(SampleArchiveFactory f, int p) {
		assert p >= MIN_PERIOD;
		factory = f;
		period = p;
	}

	/** Get the number of bytes per sample */
	abstract protected int sampleBytes();

	/** Get the maximum sample value allowed */
	abstract protected int maxValue();

	/** Put a sample value into the buffer.
	 * @param value Sample value. */
	abstract protected void putValue(int value);

	/** Get a sample value from the buffer.
	 * @return Sample value. */
	abstract protected int getValue();

	/** Get the number of samples per day */
	private int samplesPerDay() {
		return samplesPerDay(period);
	}

	/** Add a periodic sample to the cache.
	 * @param ps Sample to add to the cache. */
	public void addSample(PeriodicSample ps) {
		if(isArchiveEnabled() && shouldArchive(ps))
			samples.add(ps);
	}

	/** Is archiving enabled? */
	private boolean isArchiveEnabled() {
		return SystemAttrEnum.SAMPLE_ARCHIVE_ENABLE.getBoolean();
	}

	/** Check if a periodic sample should be archived */
	private boolean shouldArchive(PeriodicSample ps) {
		return ps.period % period == 0 &&
		       ps.value > Constants.MISSING_DATA &&
		       ps.value <= maxValue();
	}

	/** Flush all buffered samples to archive files.  This method locks on
	 * a static buffer, so it cannot be called by multiple threads even
	 * on different cache objects.  There should be one thread which calls
	 * this method on each cache serially. */
	public void flush() throws IOException {
		synchronized(buffer) {
			flush_unlocked();
		}
	}

	/** Flush all buffered samples to archive files. */
	private void flush_unlocked() throws IOException {
		FileChannel channel = null;
		buffer.clear();
		try {
			File file = null;
			PeriodicSample ps = samples.pollFirst();
			// NOTE: an I/O exception here will lose sample data
			while(ps != null) {
				File f = factory.createFile(ps.start());
				if(!f.equals(file)) {
					if(channel != null)
						writeBuffer(channel);
					channel = readBuffer(f);
					file = f;
				}
				if(ps.period == period) {
					buffer.position(samplePosition(ps));
					putValue(ps.value);
				} else if(ps.period > period)
					interpolate(ps);
				ps = samples.pollFirst();
			}
			if(channel != null) {
				writeBuffer(channel);
				channel = null;
			}
		}
		finally {
			if(channel != null)
				channel.close();
		}
	}

	/** Read the contents of the given file into the buffer.
	 * @param f File to read. */
	private FileChannel readBuffer(File f) throws IOException {
		FileChannel channel = new RandomAccessFile(f,"rw").getChannel();
		buffer.clear();
		while(channel.read(buffer) >= 0 && buffer.hasRemaining());
		while(buffer.position() < samplesPerDay() * sampleBytes())
			putValue(Constants.MISSING_DATA);
		buffer.flip();
		return channel;
	}

	/** Write the buffer to the file channel and close the file. */
	private void writeBuffer(FileChannel channel) throws IOException {
		channel.position(0);
		buffer.position(0);
		while(buffer.hasRemaining())
			channel.write(buffer);
		if(channel.size() > buffer.position())
			channel.truncate(buffer.position());
		channel.close();
	}

	/** Compute the position of a sample in the file.
	 * @param ps Periodic sample.
	 * @return File position of sample (0 is first sample). */
	private int samplePosition(PeriodicSample ps) {
		return TimeSteward.secondOfDayInt(ps.start()) *
		       sampleBytes() / period;
	}

	/** Interpolate sample data from a sample with a larger period.
	 * @param ps Periodic sample (with a larger period). */
	private void interpolate(PeriodicSample ps) {
		assert ps.period > period;
		buffer.position(samplePosition(ps));
		int[] values = getValues(ps.period / period);
		interpolate(values, ps.value);
		buffer.position(samplePosition(ps));
		putValues(values);
	}

	/** Interpolate sample data into an array of values.
	 * @param values Array of existing sample values.
	 * @param total Total of all sample values. */
	static private void interpolate(int[] values, int total) {
		int e_total = 0;	// existing values total
		int n_missing = 0;
		for(int value: values) {
			if(value < 0)
				n_missing++;
			else
				e_total += value;
		}
		if(n_missing > 0) {
			int v_miss = total - e_total;
			if(v_miss > 0) {
				int t_miss = v_miss / n_missing;
				int m_miss = v_miss % n_missing;
				for(int i = 0; i < values.length; i++) {
					if(values[i] < 0) {
						values[i] = t_miss;
						if(m_miss > 0) {
							values[i]++;
							m_miss--;
						}
					}
				}
			}
		}
	}

	/** Get an array of sample values from the buffer.
	 * @param n_samples Number of sample values.
	 * @return Array of samples values. */
	private int[] getValues(int n_samples) {
		int[] values = new int[n_samples];
		for(int i = 0; i < values.length; i++)
			values[i] = getValue();
		return values;
	}

	/** Put an array of sample values into the buffer.
	 * @param values Sample values to put. */
	private void putValues(int[] values) {
		for(int v: values)
			putValue(v);
	}

	/** Periodic sample cache for eight-bit samples */
	static public final class EightBit extends PeriodicSampleCache {

		/** Create a new eight bit periodic sample cache */
		public EightBit(SampleArchiveFactory f, int p) {
			super(f, p);
		}

		/** Get the number of bytes per sample */
		protected int sampleBytes() {
			return 1;
		}

		/** Get the maximum sample value allowed */
		protected int maxValue() {
			return Byte.MAX_VALUE;
		}

		/** Put a sample value into the buffer.
		 * @param value Sample value. */
		protected void putValue(int value) {
			buffer.put((byte)value);
		}

		/** Get a sample value from the buffer.
		 * @return Sample value. */
		protected int getValue() {
			return buffer.get();
		}
	}

	/** Periodic sample cache for sixteen-bit samples */
	static public final class SixteenBit extends PeriodicSampleCache {

		/** Create a new sixteen bit periodic sample cache */
		public SixteenBit(SampleArchiveFactory f, int p) {
			super(f, p);
		}

		/** Get the number of bytes per sample */
		protected int sampleBytes() {
			return 2;
		}

		/** Get the maximum sample value allowed */
		protected int maxValue() {
			return Short.MAX_VALUE;
		}

		/** Put a sample value into the buffer.
		 * @param value Sample value. */
		protected void putValue(int value) {
			buffer.putShort((short)value);
		}

		/** Get a sample value from the buffer.
		 * @return Sample value. */
		protected int getValue() {
			return buffer.getShort();
		}
	}
}
