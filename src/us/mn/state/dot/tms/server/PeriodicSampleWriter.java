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
import us.mn.state.dot.tms.units.Interval;
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
	static private final Interval MIN_PERIOD = new Interval(5);

	/** Get the number of samples per day */
	static private int samplesPerDay(Interval period) {
		return (int)period.per(Interval.DAY);
	}

	/** Sample archive factory */
	private final SampleArchiveFactory factory;

	/** Byte buffer for flushing samples to file */
	private final ByteBuffer buffer = ByteBuffer.allocate(
		samplesPerDay(MIN_PERIOD) * PeriodicSampleType.MAX_BYTES);

	/** Sample period for current cache */
	private transient Interval period;

	/** Current file */
	private transient File file;

	/** Current file channel */
	private transient FileChannel channel;

	/** Create a new periodic sample writer */
	public PeriodicSampleWriter(SampleArchiveFactory f) {
		factory = f;
	}

	/** Flush samples from a cache to files */
	public void flush(PeriodicSampleCache cache, String sensor_id)
		throws IOException
	{
		period = new Interval(0);
		file = null;
		channel = null;
		buffer.clear();
		flush(cache.iterator(), sensor_id, cache.sample_type);
	}

	/** Flush an iterator of samples to files */
	private void flush(Iterator<PeriodicSample> it, String sensor_id,
		PeriodicSampleType s_type) throws IOException
	{
		try {
			while(it.hasNext()) {
				PeriodicSample ps = it.next();
				period = new Interval(ps.period);
				File f = factory.createFile(sensor_id, s_type,
					ps);
				if(!f.equals(file)) {
					file = f;
					readNextFile(s_type);
				}
				putSample(ps, s_type);
			}
			writeBuffer(s_type.sample_bytes);
		}
		finally {
			if(channel != null)
				channel.close();
		}
	}

	/** Read next file (after writing current file buffer). */
	private void readNextFile(PeriodicSampleType s_type) throws IOException{
		writeBuffer(s_type.sample_bytes);
		readBuffer(s_type);
	}

	/** Read the contents of the given file into the buffer.
	 * @param s_type Sample type. */
	private void readBuffer(PeriodicSampleType s_type) throws IOException {
		int n_size = bufferBytes(s_type.sample_bytes);
		channel = new RandomAccessFile(file, "rw").getChannel();
		buffer.clear();
		readBuffer();
		// Buffer should contain no more than one day of samples
		if(buffer.position() > n_size)
			buffer.position(n_size);
		padBuffer(s_type);
		buffer.flip();
	}

	/** Get the number of bytes in buffer for one day.
	 * @param s_bytes Bytes per sample.
	 * @return Size of buffer in bytes. */
	private int bufferBytes(int s_bytes) {
		return samplesPerDay(period) * s_bytes;
	}

	/** Read all existing sample data from file */
	private void readBuffer() throws IOException {
		while(channel.read(buffer) >= 0 && buffer.hasRemaining());
	}

	/** Pad the buffer with MISSING_DATA for full day.
	 * @param s_type Sample type. */
	private void padBuffer(PeriodicSampleType s_type) {
		int n_size = bufferBytes(s_type.sample_bytes);
		int n_sam = (n_size - buffer.position()) / s_type.sample_bytes;
		for(int i = 0; i < n_sam; i++)
			s_type.putValue(buffer, MISSING_DATA);
	}

	/** Write the buffer to the file channel and close the file. */
	private void writeBuffer(int s_bytes) throws IOException {
		if(channel != null) {
			int n_size = bufferBytes(s_bytes);
			channel.position(0);
			buffer.position(0);
			// Write sample data buffer to file
			while(buffer.hasRemaining())
				channel.write(buffer);
			// Truncate file if it's larger than buffer
			if(channel.size() > n_size)
				channel.truncate(n_size);
			channel.close();
			channel = null;
		}
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
