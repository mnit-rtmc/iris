/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2004-2008  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.warning;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.tms.WarningSign;
import us.mn.state.dot.tms.client.sonar.ProxySelectionModel;;

/**
 * Action to deploy a warning sign.
 *
 * @author Douglas Lau
 */
public class DeployAction extends AbstractAction {

	/** Proxy selection model */
	protected final ProxySelectionModel<WarningSign> s_model;

	/** Flag to deploy/clear sign */
	protected final boolean deploy;

	/** Create a new deploy action */
	public DeployAction(ProxySelectionModel<WarningSign> s, boolean d) {
		s_model = s;
		deploy = d;
		if(deploy) {
			putValue(Action.NAME, "Deploy");
			putValue(Action.SHORT_DESCRIPTION, "Turn on");
			putValue(Action.LONG_DESCRIPTION,
				"Turn on warning sign flashers");
		} else {
			putValue(Action.NAME, "Clear");
			putValue(Action.SHORT_DESCRIPTION, "Turn off");
			putValue(Action.LONG_DESCRIPTION,
				"Turn off warning sign flashers");
		}
	}

	/** Schedule the action to be performed */
	public void actionPerformed(ActionEvent e) {
		new AbstractJob() {
			public void perform() {
				do_perform();
			}
		}.addToScheduler();
	}

	/** Actually perform the action */
	protected void do_perform() {
		for(WarningSign s: s_model.getSelected())
			s.setDeployed(deploy);
		s_model.clearSelection();
	}
}
