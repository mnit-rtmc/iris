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

/**
 * File convienence methods.
 * @author Michael Darter
 */
public class SFile {

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
}
