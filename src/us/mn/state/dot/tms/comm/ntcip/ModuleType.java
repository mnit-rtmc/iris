/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005  Minnesota Department of Transportation
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
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package us.mn.state.dot.tms.comm.ntcip;

/**
 * Ntcip ModuleType object
 *
 * @author Douglas Lau
 */
public class ModuleType extends GlobalModuleTable implements ASN1Integer {

	/** Undefined module type */
	static public final int UNDEFINED = 0;

	/** Other module type (manufacturer specific)
	 * Note: Ledstar version 1.9 has ModuleType.OTHER for its software */
	static public final int OTHER = 1;

	/** Hardware module type */
	static public final int HARDWARE = 2;

	/** Software module type */
	static public final int SOFTWARE = 3;

	/** String descriptions of module types */
	static public final String[] DESCRIPTION = {
		"???", "other", "hardware", "software"
	};

	/** Create a new ModuleType object */
	public ModuleType(int i) {
		super(i);
	}

	/** Get the object name */
	protected String getName() { return "moduleType"; }

	/** Get the module table item (for moduleType objects) */
	protected int getTableItem() { return 6; }

	/** Actual module type */
	protected int m_type;

	/** Set the integer value */
	public void setInteger(int value) {
		m_type = value;
		if(m_type < 0 || m_type >= DESCRIPTION.length)
			m_type = UNDEFINED;
	}

	/** Get the integer value */
	public int getInteger() { return m_type; }

	/** Get the object value */
	public String getValue() { return DESCRIPTION[m_type]; }
}
