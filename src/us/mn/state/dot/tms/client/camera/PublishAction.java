/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2008-2009  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.camera;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import us.mn.state.dot.sched.AbstractJob;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;

/**
 * This is an action to publish a set of cameras.
 *
 * @author Douglas Lau
 */
public class PublishAction extends AbstractAction {

	/** Proxy selection model */
	protected final ProxySelectionModel<Camera> s_model;

	/** Create a new publish action */
	public PublishAction(ProxySelectionModel<Camera> s) {
		s_model = s;
		putValue(Action.NAME, "Publish");
		putValue(Action.SHORT_DESCRIPTION, "Publish selected cameras");
		putValue(Action.LONG_DESCRIPTION, "Publish the selected " +
			" cameras for unrestricted access");
	}

	/** Schedule the action to be performed */
	public void actionPerformed(ActionEvent e) {
		new AbstractJob() {
			public void perform() {
				do_perform();
			}
		}.addToScheduler();
	}

	/** Publish the selected cameras */
	protected void do_perform() {
		for(Camera c: s_model.getSelected())
			c.setPublish(true);
		s_model.clearSelection();
	}
}
