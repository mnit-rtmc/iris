/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
import java.util.Calendar;
import us.mn.state.dot.tms.Constants;

/**
 * A cache which holds recent traffic data for one detector
 *
 * @author Douglas Lau
 */
public class DataCache {

	/** Volume traffic data buffer */
	protected final TrafficDataBuffer.Volume vol_buf;

	/** Scan traffic data buffer */
	protected final TrafficDataBuffer.Scan scan_buf;

	/** Speed traffic data buffer */
	protected final TrafficDataBuffer.Speed speed_buf;

	/** Create a new data cache */
	public DataCache(String det) {
		vol_buf = new TrafficDataBuffer.Volume(det);
		scan_buf = new TrafficDataBuffer.Scan(det);
		speed_buf = new TrafficDataBuffer.Speed(det);
	}

	/** Write one 30-second record of volume/scan data to the cache */
	public synchronized void write(Calendar cal, int volume, int scans) {
		long stamp = cal.getTime().getTime();
		vol_buf.write(stamp, volume);
		scan_buf.write(stamp, scans);
	}

	/** Write one 30-second record of speed data to the cache */
	public synchronized void writeSpeed(Calendar cal, int speed) {
		long stamp = cal.getTime().getTime();
		speed_buf.write(stamp, speed);
	}

	/** Merge one 5-minute record of volume/scan data with the cache */
	public synchronized void merge(Calendar cal, int vol5, int scan5)
		throws IOException
	{
		long stamp = cal.getTime().getTime();
		try {
			int[] volume = vol_buf.read(stamp, 10);
			int[] scans = scan_buf.read(stamp, 10);
			if(!mergeData5(volume, vol5, scans, scan5))
				return;
			if(vol5 > Constants.MISSING_DATA)
				vol_buf.merge(stamp, volume);
			if(scan5 > Constants.MISSING_DATA)
				scan_buf.merge(stamp, scans);
		}
		catch(IndexOutOfBoundsException e) {
			mergeSlow(cal, vol5, scan5);
		}
	}

	/** Flush data before the given stamp from the cache */
	public synchronized void flush(Calendar cal) throws IOException {
		long stamp = cal.getTime().getTime();
		vol_buf.flush(stamp);
		scan_buf.flush(stamp);
		speed_buf.flush(stamp);
	}

	/** Merge 5-minute data with existing 30-second data */
	static protected boolean mergeData5(int[] volume, int vol5,
		int[] scans, int scan5)
	{
		int vol30 = 0;
		int scan30 = 0;
		int missing = 0;
		boolean smerge = true;
		for(int i = 0; i < 10; i++) {
			if(volume[i] < 0) {
				missing++;
				continue;
			}
			vol30 += volume[i];
			if(scans[i] < 0) {
				smerge = false;
				continue;
			}
			scan30 += scans[i];
		}
		if(missing < 1)
			return false;
		int vmis5 = vol5 - vol30;
		if(vmis5 <= 0)
			return false;
		int vmis30 = vmis5 / missing;
		int vmod30 = vmis5 % missing;
		int smis5 = scan5 - scan30;
		if(smis5 < 0)
			smis5 = 0;
		int smis30 = smis5 / missing;
		int smod30 = smis5 % missing;
		for(int i = 0; i < 10; i++) {
			if(volume[i] < 0) {
				volume[i] = vmis30;
				if(vmod30-- > 0) {
					volume[i]++;
					if(smerge && vmis30 == 0) {
						scans[i] = smis5;
						smis5 = 0;
					}
				}
				if(smerge && vmis30 > 0) {
					scans[i] = smis30;
					if(smod30-- > 0)
						scans[i]++;
				}
				else smis5 = 0;
			}
		}
		return true;
	}

	/** Merge 5-minute data for one detector the slow way (no buffer) */
	protected void mergeSlow(Calendar cal, int vol5, int scan5)
		throws IOException
	{
		long stamp = cal.getTime().getTime();
		int r = cal.get(Calendar.HOUR_OF_DAY) * 12 +
			cal.get(Calendar.MINUTE) / 5;
		File v30 = vol_buf.file(stamp);
		File c30 = scan_buf.file(stamp);
		RandomAccessFile vFile = new RandomAccessFile(v30, "rw");
		RandomAccessFile cFile = new RandomAccessFile(c30, "rw");
		try {
			int[] volume = readVolume(vFile, r);
			int[] scans = readScans(cFile, r);
			if(!mergeData5(volume, vol5, scans, scan5))
				return;
			if(vol5 > Constants.MISSING_DATA)
				mergeVolume(vFile, r, volume);
			if(scan5 > Constants.MISSING_DATA)
				mergeScans(cFile, r, scans);
		}
		finally {
			vFile.close();
			cFile.close();
		}
	}

	/** Read one 5-minute volume record from a file */
	protected int[] readVolume(RandomAccessFile vFile, int r)
		throws IOException
	{
		int[] volume = new int[10];
		for(int i = 0; i < 10; i++)
			volume[i] = Constants.MISSING_DATA;
		if(vFile.length() < (r + 1) * 10)
			return volume;
		byte[] vol = new byte[10];
		vFile.seek(r * 10);
		vFile.readFully(vol);
		for(int i = 0; i < 10; i++)
			volume[i] = vol[i];
		return volume;
	}

	/** Merge one 5-minute volume record with a file */
	protected void mergeVolume(RandomAccessFile vFile, int r, int[] volume)
		throws IOException
	{
		int fsiz = (r + 1) * 10;
		if(vFile.length() < fsiz)	
			vFile.setLength(fsiz);
		byte[] vol = new byte[10];
		for(int i = 0; i < 10; i++)
			vol[i] = (byte)volume[i];
		vFile.seek(r * 10);
		vFile.write(vol);
	}

	/** Read one 5-minute scan record from a file */
	protected int[] readScans(RandomAccessFile cFile, int r)
		throws IOException
	{
		int[] scans = new int[10];
		for(int i = 0; i < 10; i++)
			scans[i] = Constants.MISSING_DATA;
		if(cFile.length() < (r + 1) * 20)
			return scans;
		byte[] scn = new byte[20];
		cFile.seek(r * 20);
		cFile.readFully(scn);
		for(int i = 0; i < 10; i++) {
			int j = i * 2;
			scans[i] = scn[j] << 8 + scn[j + 1] << 0;
		}
		return scans;
	}

	/** Merge one 5-minute scan record with a file */
	protected void mergeScans(RandomAccessFile cFile, int r, int[] scans)
		throws IOException
	{
		int fsiz = (r + 1) * 20;
		if(cFile.length() < fsiz)
			cFile.setLength(fsiz);
		byte[] scn = new byte[20];
		for(int i = 0; i < 10; i++) {
			int j = i * 2;
			scn[j] = (byte)((scans[i] >>> 8) & 0xFF);
			scn[j+1] = (byte)((scans[i] >>> 0) & 0xFF);
		}
		cFile.seek(r * 20);
		cFile.write(scn);
	}
}
