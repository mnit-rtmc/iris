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

import javax.swing.Action;
import us.mn.state.dot.sonar.User;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.SignMessage;
import us.mn.state.dot.tms.client.sonar.ProxyAction;
import us.mn.state.dot.tms.utils.I18NMessages;

/**
 * Action to clear the selected DMS.
 *
 * @author Douglas Lau
 */
public class ClearDmsAction extends ProxyAction<DMS> {

	/** DMS dispatcher */
	protected final DMSDispatcher dispatcher;

	/** User who is sending message */
	protected final User owner;

	/** Create a new action to clear the selected DMS */
	public ClearDmsAction(DMS p, DMSDispatcher d, User o) {
		super(p);
		dispatcher = d;
		owner = o;
		putValue(Action.NAME, I18NMessages.get("dms.clear"));
		putValue(Action.SHORT_DESCRIPTION,
			I18NMessages.get("dms.clear.tooltip"));
	}

	/** Actually perform the action */
	protected void do_perform() {
		SignMessage m = dispatcher.createBlankMessage();
		if(m != null) {
			proxy.setOwnerNext(owner);
			proxy.setMessageNext(m);
		}
	}
}
