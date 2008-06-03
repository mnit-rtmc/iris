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

package us.mn.state.dot.tms.utils;

import java.util.Properties;

/**
 * Convenience class to handle property files.
 *
 *
 * @version    Initial release, 06/03/08
 * @author     Michael Darter, AHMCT
 */
public class PropertyFile
{
	/** class can't be instantiated */
	private PropertyFile() {}

	/** 
	  * Read a property from the property file
	  * @param pf Property file.
	  * @param id Name of property to return.
	  */
	static public String get(Properties pf,String id) {
		return get(pf,id,"");
	}

	/** 
	  * Read a property from the property file
	  * @param pf Property file.
	  * @param id Name of property to return.
	  * @param def if id doesn't exist, the default string is returned. 
	  */
	static public String get(Properties pf,String id,String def) {

		// eliminate nulls
		id=(id==null ? "" : id);
		def=(def==null ? "" : def);

		if (pf==null) {
			System.err.println("Error: the property file is null");
			return "";
		}

		String p=pf.getProperty(id);

		// use default
		if (p==null) {
			p=def;
			if (def.length()==0)
				System.err.println("Warning: a property ("+id+
					") was not found in the property file.");
			else
				System.err.println("Warning: a property ("+id+
					") was not found in the property file, "+
					"assigned the default value("+def+")");
		}
		return p;
	}
}

