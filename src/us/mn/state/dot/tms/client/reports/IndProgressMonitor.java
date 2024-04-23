/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2018  SRF Consulting Group
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

package us.mn.state.dot.tms.client.reports;

import java.awt.Component;
import java.lang.reflect.Field;

import javax.swing.JProgressBar;
import javax.swing.ProgressMonitor;

/** Similar to a standard Swing ProgressMonitor, but
 * this shows a bouncing "indeterminate" progress bar.
 * 
 * Note:  The ability to directly access the internal
 * progress bar object via reflection was removed by
 * a Java security update, so this now has a "planB"
 * fallback where it loops a linear progress bar
 * moving from 5 to 95 until the report finishes.
 *   
 * @see javax.swing.ProgressMonitor
 * 
 * @author John L. Stanley - SRF Consulting
 */
public class IndProgressMonitor extends ProgressMonitor {

	// set if we can access the internal progress bar
	private boolean bIndeterminateWasSet = false;
	
	// fallback option for more recent versions of Java
	private boolean bPlanB = false;

	// planB loop value (range 5-95)
	private int value = 5;
	
	/** Constructs a graphic ProgressMonitor style object
	 *  that shows a bouncing "indeterminate" progress bar.
	 *
	 * @see javax.swing.ProgressMonitor#ProgressMonitor(Component,
	 *  Object, String, int, int) Parameter description
	 */
	public IndProgressMonitor(Component parentComponent,
			Object message, String note) {
		super(parentComponent, message, note, 0, 100);
	}

	/** Update the progress monitor.
	 * (Retained for compatibility with ProgressMonitor.
	 *  When using IndProgressMonitor, I recommend calling
	 *  the update() method instead.)
	 * 
	 * This or update() should be called occasionally
	 * (every 1/10 sec is a good interval) while the
	 * process is running to display the progress
	 * monitor animation.
	 *  
	 * @param nv an int specifying the current value,
	 *  between the maximum and minimum specified for
	 *  this component.  The nv value is mostly ignored,
	 *  but, if the specified value is &gt;= 100, the
	 *  progress monitor will be closed.
	 */
	@Override
	public void setProgress(int nv) {
		if (bPlanB) {
			value += 5;
			if (value >= 99) {
				value = 5;
			}
			super.setProgress(value);
			return;
		}
		if (!bIndeterminateWasSet) {
			// Note: Once indeterminate mode is set, we
			// don't need to keep updating the monitor
			// as the progress bar has its own thread
			// that handles the "bounce".
			super.setProgress(nv);
			if (hack_setIndeterminateMode())
				bIndeterminateWasSet = true;
		}
	}

	/** Slightly more intuitive method to
	 *  use in place of setProgress(0). */
	public void update() {
		setProgress(0);
	}

	/** <b>Hack</b>: Uses reflection to set indeterminate mode on a
	 *  ProgressMonitor object.  (I don't recommend this but,
	 *  thanks to the 10+ year backlog in adding this specific
	 *  enhancement to the ProgressMonitor component, there's
	 *  no "good way" to do this...)<p>
	 *  
	 *  Note: The progress bar isn't actually created before
	 *  the initial ProgressMonitor delay has expired, so we
	 *  need to keep calling super.setProgress(...) until
	 *  the dialog is created before we can hack the progress
	 *  bar.<p>
	 *  
	 * @return Did we manage to hack the progress bar into
	 *  indeterminate mode?  true if we did.  false if we
	 *  did not.
	 */
	private boolean hack_setIndeterminateMode() {
		Field f = null;
		try {
			f = ProgressMonitor.class.getDeclaredField("myBar");
			try {
				f.setAccessible(true);
			} catch (Exception e) {
				bPlanB  = true;
				return false;
			}
			JProgressBar bar = (JProgressBar)f.get(this);
			if (bar != null) {
				bar.setIndeterminate(true);
				return true;
			}
		} catch (Exception ignore) {
			ignore.printStackTrace();
		}
		return false;
	}
}
