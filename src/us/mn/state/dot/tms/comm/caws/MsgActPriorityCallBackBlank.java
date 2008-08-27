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
package us.mn.state.dot.tms.comm.caws;

import us.mn.state.dot.tms.MsgActPriorityProc;
import us.mn.state.dot.tms.SignMessage;

import java.io.Serializable;

/**
 * This is an activation priority callback used for CAWS blank messages.
 * Note: it would be nice if this was an anonymous class, but it needs
 * implement Serializable, which can't be specified anonymously. Serializable
 * is necessary because RMI needs to marshal this class (via a field in 
 * SignMessage).
 * @author Michael Darter
 * @see MsgActPriority,MsgActPriorityProc,SignMessage
 */
public class MsgActPriorityCallBackBlank implements MsgActPriorityProc.CallbackSupersede,Serializable
{
	/**
	 * Blank existing sign msg if owned by CAWS. 
	 * @return True to supersede an existing sign message else false.
	 */
	public boolean supersede(SignMessage existingMsg) {
		if (existingMsg==null)
			return true;
		return existingMsg.isOwner(CawsPoller.CAWS);
	}
}

