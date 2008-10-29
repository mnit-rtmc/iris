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
import us.mn.state.dot.tms.DMSImpl;
import us.mn.state.dot.tms.comm.caws.CawsPoller;

/**
 * DMS message procedural activation priority. This class extends the
 * simple Ntcip activation priority class by adding a callback, which
 * can be used for more complex comparisons between priority levels
 * to determine if one priority level supersedes another. Note that
 * the integer priority level from the superclass is still used, for
 * example, when a potential new SignMessage with a MsgActPriority 
 * compares itself with a MsgActPriorityProc that is already on a sign.
 *
 * @author Michael Darter
 * @see MsgActPriority,SignMessage,MsgActPriorityCallBackBlank
 */
public class MsgActPriorityProc extends MsgActPriority implements Serializable {

	/** callback, for procedural priority determination */
	protected CallbackSupersede m_cb=null;

	/** Create a priority using a callback to determine priority */
	public MsgActPriorityProc(final int priority,final CallbackSupersede cb) {
		setValue(priority);
		m_cb=cb;
	}

	/** 
	 * Determine if one message would supersede (replace) another on the 
	 * DMS. Note that we are asking the potential new priority (this) to 
	 * evaluate whether it supersedes the priority of the message on the 
	 * sign (the argument).
	 * @param priOnSign Typically the priority level of the message on the sign.
	 * @return true if the priority of this supersedes the argument priority value.
	 */
	public boolean supersede(final SignMessage msgOnSign) {
		System.err.println("MsgActPriorityProc.supersede("+msgOnSign.toStringDebug()+" called.");
		if (m_cb==null || msgOnSign==null)
			return false;
		return m_cb.supersede(msgOnSign);
	}

	/** callback interface, implemented for procedural message priority */
	public interface CallbackSupersede {

		/** return true if this message would supersede the arg */
		public boolean supersede(SignMessage existingMsg);
	}

	/** procedural activation level for alerts */
	public class MsgActPriorityCallBackAlert implements MsgActPriorityProc.CallbackSupersede,Serializable
	{
		/**
		 * Blank existing sign msg if owned by CAWS. 
		 * @return True to supersede an existing sign message else false.
		 */
		public boolean supersede(SignMessage existingMsg) {
			if(existingMsg==null)
				return true;
			return (existingMsg.isBlank() ||
				existingMsg instanceof SignAlert ||
				existingMsg instanceof SignTravelTime);
		}
	}
}

