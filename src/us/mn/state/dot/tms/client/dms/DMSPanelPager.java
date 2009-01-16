/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import us.mn.state.dot.tms.SystemAttributeHelper;

/**
 * Pager for a DMSPanel.  This allows multiple page messages
 * to be displayed on a DMSPanel.  The SystemAttribute objects
 * DMS message on-time and off-time (blank time) are monitored.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSPanelPager {

	/** Time counter for amount of time message has been displayed */
	static protected final int TIMER_TICK_MS = 100;

	/** Get the system DMS page on time (ms) */
	static protected int readSystemOnTime() {
		return Math.round(1000 *
			SystemAttributeHelper.getDmsPageOnSecs());
	}

	/** Get the system DMS page off time (ms) */
	static protected int readSystemOffTime() {
		return Math.round(1000 *
			SystemAttributeHelper.getDmsPageOffSecs());
	}

	/** DMS panel being controlled */
	protected final DMSPanel panel;

	/** Multipage message on time, read from system attributes */
	protected final int onTimeMS;

	/** Multipage message off time, read from system attributes */
	protected final int offTimeMS;

	/** Swing timer */
	protected final Timer timer;

	/** Time counter for amount of time message has been displayed */
	protected int phaseTimeMS = 0;

	/** Blanking state -- true during blank time between pages */
	protected boolean isBlanking = false;

	/** Create a new DMS panel pager */
	public DMSPanelPager(DMSPanel p) {
		panel = p;
		onTimeMS = readSystemOnTime();
		offTimeMS = readSystemOffTime();
		timer = new Timer(TIMER_TICK_MS, new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				pageTimerTick();
			} 
		});
		timer.start();
	}

	/** Dispose of the pager */
	public void dispose() {
		timer.stop();
	}

	/** 
	 * Page timer tick. Called periodically to change the sign contents
	 * for multipage signs.
	 */
	protected void pageTimerTick() {
		if(doTick()) {
			if(isBlanking)
				panel.makeBlank();
			else
				panel.nextPage();
		}
	}

	/** Update the timer for one tick.
	 * @return True if panel needs repainting */
	protected boolean doTick() {
		phaseTimeMS += TIMER_TICK_MS;
		if(isBlanking) {
			if(phaseTimeMS >= offTimeMS) {
				isBlanking = false;
				phaseTimeMS = 0;
				return true;
			}
		} else {
			if(phaseTimeMS >= onTimeMS) {
				if(offTimeMS > 0)
					isBlanking = true;
				phaseTimeMS = 0;
				return true;
			}
		}
		return false;
	}
}
