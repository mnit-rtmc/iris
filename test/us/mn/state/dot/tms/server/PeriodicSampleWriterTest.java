/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2012-2013  Minnesota Department of Transportation
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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import junit.framework.TestCase;

/** 
 * Periodic Sample Writer test cases
 * @author Doug Lau
 */
public class PeriodicSampleWriterTest extends TestCase {

	public PeriodicSampleWriterTest(String name) {
		super(name);
	}

	public void testWriter() {
		PeriodicSampleCache cache = new PeriodicSampleCache(
			PeriodicSampleType.VOLUME);
		Calendar cal = Calendar.getInstance();
		cal.set(2012, Calendar.JANUARY, 1, 0, 0, 30);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 1));
		cal.set(2012, Calendar.JANUARY, 1, 0, 1, 0);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 2));
		cal.set(2012, Calendar.JANUARY, 1, 0, 1, 30);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 3));
		cal.set(2012, Calendar.JANUARY, 1, 0, 2, 0);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 4));
		// Missing sample @ 2012-01-01 00:02:30
		cal.set(2012, Calendar.JANUARY, 1, 0, 3, 0);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 6));
		cal.set(2012, Calendar.JANUARY, 1, 0, 3, 30);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 7));
		cal.set(2012, Calendar.JANUARY, 1, 0, 4, 0);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 8));
		cal.set(2012, Calendar.JANUARY, 1, 0, 4, 30);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 9));
		cal.set(2012, Calendar.JANUARY, 1, 0, 5, 0);
		cache.add(new PeriodicSample(cal.getTimeInMillis(), 30, 10));
		SampleArchiveFactory factory = new TestFactory();
		PeriodicSampleWriter writer = new PeriodicSampleWriter(factory);
		try {
			File file = new File("/tmp/TEST.v30");
			file.delete();
			writer.flush(cache, "TEST");
			FileChannel channel = new RandomAccessFile(file,
				"rw").getChannel();
			ByteBuffer buf = ByteBuffer.allocate(2880);
			while(channel.read(buf) >= 0 && buf.hasRemaining());
			channel.close();
			buf.flip();
			assertTrue(buf.get() == 1);
			assertTrue(buf.get() == 2);
			assertTrue(buf.get() == 3);
			assertTrue(buf.get() == 4);
			assertTrue(buf.get() == -1);
			assertTrue(buf.get() == 6);
			assertTrue(buf.get() == 7);
			assertTrue(buf.get() == 8);
			assertTrue(buf.get() == 9);
			assertTrue(buf.get() == 10);
			assertTrue(buf.get() == -1);
			assertTrue(buf.get() == -1);
			assertTrue(file.length() == 2880);
		}
		catch(IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	static class TestFactory implements SampleArchiveFactory {
		public File createFile(String sensor_id, String ext, long stamp)
			throws IOException
		{
			return new File("/tmp", sensor_id + "." + ext);
		}
		public File createFile(String sensor_id,
			PeriodicSampleType s_type, PeriodicSample ps)
			throws IOException
		{
			return new File("/tmp", sensor_id + "." +
				s_type.extension + ps.period);
		}
		public boolean hasKnownExtension(String name) {
			return true;
		}
	}
}
