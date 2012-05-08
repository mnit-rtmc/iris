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
import java.util.Iterator;
import us.mn.state.dot.sched.TimeSteward;
import static us.mn.state.dot.tms.Interval.DAY;
import us.mn.state.dot.tms.SystemAttrEnum;
import static us.mn.state.dot.tms.server.Constants.MISSING_DATA;

/**
 * A writer for periodic sample data.  This is needed so that threads which are
 * collecting sample data do not have to wait for file I/O to store the data.
 * A dedicated FLUSH thread uses a writer to force the cached data to be
 * written to files at regular intervals.
 *
 * Sample files are binary with a fixed number of bytes per sample.
 * Each file contains one day of sample data.  For example, a volume file with
 * a 30-second period would have 2880 bytes.
 *
 * @author Douglas Lau
 */
public class PeriodicSampleWriter {

	/** Minimum sample period (seconds) */
	static private final int MIN_PERIOD = 5;

	/** Get the number of samples per day */
	static private int samplesPerDay(int period) {
		return DAY / period;
	}

	/** Is archiving enabled? */
	static private boolean isArchiveEnabled() {
		return SystemAttrEnum.SAMPLE_ARCHIVE_ENABLE.getBoolean();
	}

	/** Sample archive factory */
	private final SampleArchiveFactory factory;

	/** Byte buffer for flushing samples to file */
	private final ByteBuffer buffer = ByteBuffer.allocate(
		samplesPerDay(MIN_PERIOD) * PeriodicSampleType.MAX_BYTES);

	/** Create a new periodic sample writer */
	public PeriodicSampleWriter() {
		factory = new SampleArchiveFactory();
	}

	/** Flush samples from a cache to files */
	public void flush(PeriodicSampleCache cache) throws IOException {
		if(isArchiveEnabled()) {
			buffer.clear();
			flush(cache.iterator(), cache.sensor_id,
				cache.sample_type);
		}
	}

	/** Flush an iterator of samples to files */
	private void flush(Iterator<PeriodicSample> it, String sensor_id,
		PeriodicSampleType s_type) throws IOException
	{
		int period = 0;
		FileChannel channel = null;
		try {
			File file = null;
			while(it.hasNext()) {
				PeriodicSample ps = it.next();
				period = ps.period;
				File f = factory.createFile(sensor_id, s_type,
					ps);
				if(!f.equals(file)) {
					if(channel != null) {
						writeBuffer(channel, period,
							s_type.sample_bytes);
					}
					channel = readBuffer(f, period, s_type);
					file = f;
				}
				putSample(ps, s_type);
			}
			if(channel != null) {
				writeBuffer(channel, period,
					s_type.sample_bytes);
				channel = null;
			}
		}
		finally {
			if(channel != null)
				channel.close();
		}
	}

	/** Read the contents of the given file into the buffer.
	 * @param f File to read.
	 * @param period Sampling period (seconds).
	 * @param s_type Sample type. */
	private FileChannel readBuffer(File f, int period,
		PeriodicSampleType s_type) throws IOException
	{
		int n_size = bufferBytes(period, s_type.sample_bytes);
		FileChannel channel = new RandomAccessFile(f,"rw").getChannel();
		buffer.clear();
		readBuffer(channel);
		// Buffer should contain no more than one day of samples
		if(buffer.position() > n_size)
			buffer.position(n_size);
		padBuffer(period, s_type);
		buffer.flip();
		return channel;
	}

	/** Get the number of bytes in buffer for one day.
	 * @param period Sampling period (seconds).
	 * @param s_bytes Bytes per sample.
	 * @return Size of buffer in bytes. */
	private int bufferBytes(int period, int s_bytes) {
		return samplesPerDay(period) * s_bytes;
	}

	/** Read all existing sample data from file */
	private void readBuffer(FileChannel channel) throws IOException {
		while(channel.read(buffer) >= 0 && buffer.hasRemaining());
	}

	/** Pad the buffer with MISSING_DATA for full day.
	 * @param period Sampling period (seconds).
	 * @param s_type Sample type. */
	private void padBuffer(int period, PeriodicSampleType s_type) {
		int n_size = bufferBytes(period, s_type.sample_bytes);
		int n_sam = (n_size - buffer.position()) / s_type.sample_bytes;
		for(int i = 0; i < n_sam; i++)
			s_type.putValue(buffer, MISSING_DATA);
	}

	/** Write the buffer to the file channel and close the file. */
	private void writeBuffer(FileChannel channel, int period, int s_bytes)
		throws IOException
	{
		int n_size = bufferBytes(period, s_bytes);
		channel.position(0);
		buffer.position(0);
		// Write sample data buffer to file
		while(buffer.hasRemaining())
			channel.write(buffer);
		// Truncate file if it's larger than buffer
		if(channel.size() > n_size)
			channel.truncate(n_size);
		channel.close();
	}

	/** Put one sample into the buffer.
	 * @param ps Periodic sample. */
	private void putSample(PeriodicSample ps, PeriodicSampleType s_type) {
		buffer.position(samplePosition(ps, s_type.sample_bytes));
		s_type.putValue(buffer, ps.value);
	}

	/** Compute the position of a sample in the file.
	 * @param ps Periodic sample.
	 * @return File position of sample (0 is first sample). */
	private int samplePosition(PeriodicSample ps, int s_bytes) {
		return TimeSteward.secondOfDayInt(ps.start()) *
		       s_bytes / ps.period;
	}
}
