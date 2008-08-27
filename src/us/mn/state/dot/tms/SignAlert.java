/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2003-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms;

import java.util.Map;
import java.io.Serializable;

/**
 * SignAlert is a class which encapsulates all the properties of a single
 * alert on a dynamic message sign (DMS).
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SignAlert extends SignMessage {

	/** overwrite existing sign messages */
	protected boolean m_overwrite=false;

	/** Create a new sign alert */
	public SignAlert(String o, MultiString m, Map<Integer, BitmapGraphic> b,
		int d, boolean overwrite)
	{
		super(o, m, b, d);
		m_overwrite=overwrite;
		MsgActPriority ap=new MsgActPriorityProc(MsgActPriority.VAL_OPER_ALERT,
			new callBackAlert());
		this.setActivationPriority(ap);
	}

	/**
	 * This is an activation priority callback used for alerts.
	 * @see MsgActPriority,MsgActPriorityProc,SignMessage
	 */
	public class callBackAlert implements MsgActPriorityProc.CallbackSupersede,Serializable
	{
		/** return true for a new alert to supersede the existing sign message */
		public boolean supersede(SignMessage existingMsg) {
			if(existingMsg==null)
				return true;
			boolean s=false;
			// overwrite existing messages on the sign?
			if (m_overwrite)
				s=supersedeNumeric(existingMsg);
			else
				s=existingMsg.isBlank() ||
					existingMsg instanceof SignAlert ||
					existingMsg instanceof SignTravelTime;
			System.err.println("SignAlert.callBackAlert.supersede(): will alert supersede? "+s);
			return s;
		}
	}
}
