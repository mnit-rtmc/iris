/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.widget;

import javax.swing.Action;
import javax.swing.JButton;
import us.mn.state.dot.sched.ActionJob;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.utils.I18N;

/** 
 *  An IRIS button, which provides:
 *	-Tooltips via I18N text.
 *	-Mnemonics via I18N text, with the mnemonic appended to the tooltip.
 *	-Handling of "IRIS enabled" state via boolean system attribute.
 *
 *  @author Michael Darter
 */
public class IButton extends JButton
{
	/** Attribute that controls if buttin is IRIS enabled */
	final private SystemAttrEnum m_sa;

	/** I18N id that identifies the button's text */
	private String m_textid;

	/** Constructor for a standardized IRIS button.
	 *  @param textid I18N id for button text. If the I18N string contains
	 *	   an underline, the mnemonic will be set. A tooltip will be 
	 *	   set if an I18N string with id textId + ".tooltip" exists in
	 *	   the message bundle. The key mnemonic is appended to the 
	 *	   tooltip.
	 */
	public IButton(String textid) {
		super();
		m_sa = null;
		m_textid = textid;
		init();
	}

	/** Constructor for a standardized IRIS button.
	 *  @param textid I18N id for button text. If the I18N string contains
	 *	   an underline, the mnemonic will be set. A tooltip will be 
	 *	   set if an I18N string with id textId + ".tooltip" exists in
	 *	   the message bundle. The key mnemonic is appended to the 
	 *	   tooltip.
	 *  @param sa Attribute that specifies boolean true if button is used.
	 */
	public IButton(String textid, SystemAttrEnum sa) {
		super();
		m_textid = textid;
		m_sa = sa;
		init();
	}

	/** Init, called by constructors. */
	private void init() {
		if(m_textid == null)
			return;
		setText(I18N.get(m_textid));
		// set mnemonic
		int m = I18N.getKeyEvent(m_textid);
		if(m != 0)
			setMnemonic(m);
		// set tooltip
		String tt = I18N.getSilent(m_textid + ".tooltip");
		if(tt != null) {
			if(m != 0) {
				char c = I18N.getKeyEventChar(m_textid);
				tt += " (alt-" + c + ")";
			}
			setToolTipText(tt);
		}
	}

	/** IRIS enabled? */
	public boolean getIEnabled() {
		if(m_sa == null)
			return true;
		return m_sa.getBoolean();
	}

	/** Set the button action */
	public void setAction(Action a) {
		super.setAction(a);
		// setting the action clears existing button 
		// props, so re-add mnemonic, tooltip, etc.
		init();
	}
}
