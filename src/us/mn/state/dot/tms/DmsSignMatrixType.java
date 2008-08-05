/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

/**
 * DMS sign matrix type enumeration.
 *
 * @author Michael Darter
 */
public enum DmsSignMatrixType {

	/** possible enum values */
	FULL("Full-matrix",0),
	CHARACTER("Character-matrix",1),
	LINE("Line-matrix",2);

	/** matrix type string */
	protected final String m_type;

	/** matrix type as a unique id */
	protected final int m_id;

	/** Create a new dms sign matrix type */
	private DmsSignMatrixType(String type,int id) {
		m_type = type;
		m_id=id;
	}

	/** get String version of enum */
	public String toString() {
		return m_type;
	}

	/** get int version of enum */
	public int toInt() {
		return m_id;
	}
}

