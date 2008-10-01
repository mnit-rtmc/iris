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

import java.io.Serializable;

/**
 * DMS message activation priority. This class is intended to mimic the 
 * functionality provided by the NTCIP activation priority. The priority
 * is numeric and ranges from 0 to 255, with higher numbers for higher 
 * priority levels. In general, a message with a priority of X will 
 * supersede a message with a priority of Y if X >= Y. For procedural 
 * activation priorities see the subclass. This class defines static
 * priority levels for use by all agencies. If an agency desires customized
 * priority levels, create a subclass.
 *
 * @author Michael Darter
 * @see MsgActPriorityD10, MsgActPriorityProc, SignMessage,
 * MsgActPriorityCallBackBlank
 */
public class MsgActPriority implements Serializable {

	/** priority values used for validation */
	public static final int VAL_LOWEST = 0;
	public static final int VAL_HIGHEST = 255;

	/** priority values common to all agencies */
	public static final int VAL_OPER_BLANK = 130;
	public static final int VAL_OPER_MSG = VAL_OPER_BLANK;
	public static final int VAL_OPER_ALERT = VAL_OPER_MSG;
	public static final int VAL_IRIS_TRAVELTIME = 110;

	/** canned priorities common to all agencies */
	static public final MsgActPriority PRI_LOWEST =
		new MsgActPriority(VAL_LOWEST);
	static public final MsgActPriority PRI_HIGHEST =
		new MsgActPriority(VAL_HIGHEST);
	static public final MsgActPriority PRI_OPER_BLANK =
		new MsgActPriority(VAL_OPER_BLANK);
	static public final MsgActPriority PRI_OPER_MSG =
		new MsgActPriority(VAL_OPER_MSG);
	static public final MsgActPriority PRI_OPER_ALERT =
		new MsgActPriority(VAL_OPER_ALERT);
	static public final MsgActPriority PRI_IRIS_TRAVELTIME =
		new MsgActPriority(VAL_IRIS_TRAVELTIME);

	/** priority, which follows NTCIP activation priority, ranges 0-255 */
	protected int m_priority = VAL_OPER_MSG;

	/** Create a priority */
	public MsgActPriority() {
	}

	/** Create a priority using an int */
	public MsgActPriority(final int priority) {
		this.setValue(priority);
	}

	/** get String representation */
	public String toString() {
		return "Priority=" + m_priority;
	}

	/** 
	 * Determine if one message would supersede (replace) another on the 
	 * DMS. Note that we are asking the potential new priority (this) to 
	 * evaluate whether it supercedes the priority of the message on the 
	 * sign (the argument).
	 * @param priOnSign Typically the priority level of the message on the
	 * sign.
	 * @return true if the priority of this supersedes the argument priority
	 * value.
	 */
	public boolean supersede(final MsgActPriority priOnSign) {
		if(priOnSign == null)
			return false;
		return this.valueOf() >= priOnSign.valueOf();
	}

	/** set priority using int */
	public void setValue(final int priority) {
		assert (priority >= VAL_LOWEST && priority <= VAL_HIGHEST) :
			"Bogus priority=" + priority;
		m_priority = priority;
		m_priority=(m_priority < VAL_LOWEST ? VAL_LOWEST : m_priority);
		m_priority=(m_priority > VAL_HIGHEST ? VAL_HIGHEST :m_priority);
	}

	/** get priority as int */
	public int valueOf() {
		return m_priority;
	}

	/** Test for equality */
	public boolean equals(Object o) {
		if(o instanceof MsgActPriority)
			return this.valueOf() == ((MsgActPriority)o).valueOf();
		return false;
	}

	/** Calculate a hash code */
	public int hashCode() {
		return this.hashCode();
	}
}
