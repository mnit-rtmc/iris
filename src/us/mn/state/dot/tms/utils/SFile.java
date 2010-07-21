/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009 - 2010  AHMCT, University of California
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
package us.mn.state.dot.tms.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.SecurityException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * File convienence methods.
 * @author Michael Darter
 */
public class SFile 
{
	/** Write a string to a file, terminated w/ \r\n. */
	public static boolean writeLineToFile(String fname, String s,
		boolean append)
	{
		return(SFile.writeStringToFile(fname, s + "\r\n", append));
	}

	/** Write a string to a file.
	 * @param fname File name, may be null.
	 * @param s String to write, may be null.
	 * @param append False to overwrite existing file else true. */
	public static boolean writeStringToFile(String fname, 
		String s, boolean append)
	{
		s = (s == null ? "" : s);
		FileWriter f = null;
		boolean ok = true;
		try {
			f = new FileWriter(fname, append);
			f.write(s);
		} catch(Exception ex) {
			Log.warning(
				"Warning: writeStringToFile(): fname=" + 
					fname + ", ex=" + ex);
			ok = false;
		} finally {
			try {
				f.close();
			} catch(Exception ex) {}
		}
		return ok;
	}

	/** Read the specified URL and return a byte array or null on error.
	 * @param surl URL which may be null */
	public static byte[] readUrl(String surl) {
		if(surl == null)
			return null;
		URL url = null;
		try {
			url = new URL(surl);
		} catch(MalformedURLException ex) {
			Log.warning("SFile.readUrl(), malformed URL: "+ex);
			return null;
		}
		InputStream in = null;
		byte[] ret = new byte[0];
		try {
			// open
			URLConnection c = url.openConnection();
			int pl = c.getContentLength();

			// Log.finest("SFile.readUrl(), content len="+pl);
			long fdate = c.getDate();
			// Log.finest("SFile.readUrl(), date="+d);

			// read until eof
			in = c.getInputStream();
			ArrayList<Byte> al = new ArrayList(pl);
			while(true) {
				int b = in.read(); // throws IOException on error
				if(b < 0)
					break; // eof
				al.add(new Byte((byte)b));
			}

			// create byte[]
			ret = new byte[al.size()];
			for(int i = 0; i < ret.length; ++i)
				ret[i] = (byte)(al.get(i));

			// for(int i = 0; i < len; ++i ) 
			//	System.err.print(ret[i]+" "+(char)ret[i]+","); 
			//	Log.finest(" ");
			//Log.finest("SFile.readUrl(), read "
			//	+ al.size() + " bytes, file date=" + fdate + ".");
		} catch(UnknownHostException e) {
			//Log.finest(
			//	"SFile.readUrl(): ignoring bogus url: "+e);
		} catch(IOException e) {
			Log.warning(
				"SFile.readUrl(), caught exception: " + e);
		} finally {
			try {
				if(in != null)
					in.close();
			} catch(IOException ex) {}
		}
		return ret;
	}

	/** Return an absolute file path.
	 * @param fn File name. Can be null. If "", then "" is returned.
	 * @throws SecurityException */
	public static String getAbsolutePath(String fn) {
		// on Windows, getAbsolutePath("") returns "C:\"
		if(fn == null || fn.isEmpty())
			return "";
		File fh = new File(fn);
		return fh.getAbsolutePath();
	}

	/** Return true if specified file exists else false.
	 * @param fn File name, may be null. */
	public static boolean fileExists(String fn) {
		if(fn == null)
			return false;
		boolean exists = true;
		try {
			exists = new File(fn).exists();
		} catch(Exception ex) {
			exists = false;
		}
		return exists;
	}

	/** Delete file */
	public static boolean delete(String fn) {
		if(!fileExists(fn))
			return true;
		boolean ok = false;
		try {
			File fd = new File(fn);
			ok = fd.delete();
		} catch(Exception ex) {
			ok = false;
			Log.fine("SFile.delete() ex=" + ex);
		}
		return ok;
	}
}
