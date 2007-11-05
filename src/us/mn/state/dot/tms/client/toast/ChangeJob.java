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
package us.mn.state.dot.tms.client.toast;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import us.mn.state.dot.tms.utils.AbstractJob;

/**
 * ChangeJob is a simple extension/replacement for ChangeListener
 * which passes off a job to a scheduler.
 *
 * @author Douglas Lau
 */
abstract public class ChangeJob extends AbstractJob implements ChangeListener {

	/** Most recent change event */
	protected ChangeEvent event;

	/** State changed (from ChangeListener interface) */
	public void stateChanged(ChangeEvent e) {
		event = e;
		addToScheduler();
	}
}
