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
 * DMS message activation priority specific to Caltrans D10.
 * @author Michael Darter
 * @see MsgActPriority,MsgActPriorityProc,SignMessage,MsgActPriorityCallBackBlank
 */
public class MsgActPriorityD10 extends MsgActPriority implements Serializable {

	/** priority values specific to D10 */
	public static final int VAL_D10_CAWS_MSG=VAL_OPER_MSG+1;   // oper msgs, alerts, travel time don't overwrite caws msgs
	public static final int VAL_D10_CAWS_BLANK=VAL_OPER_BLANK;   // uses procedural activation priority
	public static final int VAL_D10_OPER_MSG=VAL_OPER_MSG;
	public static final int VAL_D10_CAWS_TRAVELTIME=110;

	/** canned priorities specific to D10 */
	// note: a CAWS blank is a procedural activation priority: it only blanks existing CAWS messages
	public static final MsgActPriority PRI_D10_CAWS_MSG=new MsgActPriority(VAL_D10_CAWS_MSG);
	public static final MsgActPriority PRI_D10_OPER_MSG=new MsgActPriority(VAL_D10_OPER_MSG);
	public static final MsgActPriority PRI_D10_CAWS_TRAVELTIME=new MsgActPriority(VAL_D10_CAWS_TRAVELTIME);
}

