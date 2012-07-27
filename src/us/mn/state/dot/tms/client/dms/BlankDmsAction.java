/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2012  Minnesota Department of Transportation
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

import java.util.List;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.client.proxy.ProxySelectionModel;
import us.mn.state.dot.tms.client.widget.IAction;

/**
 * Action to blank all selected DMS.
 *
 * @author Douglas Lau
 */
public class BlankDmsAction extends IAction {

	/** Selection model */
	private final ProxySelectionModel<DMS> selectionModel;

	/** DMS dispatcher */
	private final DMSDispatcher dispatcher;

	/** User who is sending message */
	private final User owner;

	/** Create a new action to blank the selected DMS */
	public BlankDmsAction(ProxySelectionModel<DMS> s, DMSDispatcher d,
		User o)
	{
		super("dms.blank");
		selectionModel = s;
		dispatcher = d;
		owner = o;
	}

	/** Actually perform the action */
	protected void do_perform() {
		List<DMS> sel = selectionModel.getSelected();
		if(sel.size() > 0) {
			SignMessage m = dispatcher.createBlankMessage();
			if(m != null) {
				for(DMS dms: sel) {
					dms.setOwnerNext(owner);
					dms.setMessageNext(m);
				}
			}
		}
	}
}
