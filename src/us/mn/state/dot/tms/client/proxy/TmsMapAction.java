/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2005-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.proxy;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import us.mn.state.dot.tms.utils.AbstractJob;

/**
 * TmsMapAction is an abstract action which is associated with one type
 * of TMS map object. It also passes off calls to actionPerformed to a
 * worker thread so RMI calls never happen on the Swing thread.
 *
 * @author Douglas Lau
 */
abstract public class TmsMapAction extends AbstractAction {

	/** TMS map proxy */
	protected final TmsMapProxy proxy;

	/** Create a new TMS map action */
	public TmsMapAction(TmsMapProxy p) {
		proxy = p;
	}

	/** Schedule the action to be performed */
	public void actionPerformed(ActionEvent e) {
		if(proxy != null) {
			new AbstractJob() {
				public void perform() throws Exception {
					do_perform();
				}
			}.addToScheduler();
		}
	}

	/** Do the actual job (on the AbstractJob thread) */
	abstract protected void do_perform() throws Exception;
}
