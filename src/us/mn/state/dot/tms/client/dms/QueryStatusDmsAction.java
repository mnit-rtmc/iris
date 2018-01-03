/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Iteris Inc.
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

import java.awt.event.ActionEvent;
import us.mn.state.dot.tms.client.widget.IAction;

/**
 * Action to pixel test all selected DMS.
 * @author Michael Darter
 */
public class QueryStatusDmsAction extends IAction {

	/** DMS dispatcher */
	private final DMSDispatcher dispatcher;

	/** Create a new action to pixel test the selected DMS */
	public QueryStatusDmsAction(DMSDispatcher d) {
		super("dms.query.status");
		dispatcher = d;
	}

	/** Perform the action */
	protected void doActionPerformed(ActionEvent e) {
		dispatcher.queryStatusDms();
	}
}
