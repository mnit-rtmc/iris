/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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

package us.mn.state.dot.tms.client.alert;

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CapResponseType;
import us.mn.state.dot.tms.CapResponseTypeHelper;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;


/**
 * A panel for displaying and editing CAP response type substitution values.
 *
 * @author Gordon Parikh
 */
@SuppressWarnings("serial")
public class CapResponseTypePanel extends ProxyTablePanel<CapResponseType> {

	/** TypeCache for looking up objects after creation */
	private final TypeCache<CapResponseType> cache;
	
	public CapResponseTypePanel(ProxyTableModel<CapResponseType> m) {
		super(m);
		cache = m.getSession().getSonarState().getCapResponseTypeCache();
	}
	

	/** Create a new response type substitution value. Uses the text in the
	 *  field as the event type and creates a new unique name for the
	 *  substitution value.
	 */
	@Override
	protected void createObject() {
		// get the event from the text box and reset the text
		String ev = add_txt.getText().trim();
		add_txt.setText("");
		
		// if the event is empty, use default
		if (ev.isEmpty())
			ev = CapResponseType.DEFAULT_EVENT;
		
		// generate a new unique name
		String name = CapResponseTypeHelper.createUniqueName();
		
		// create the object with the unique name then set the label
		cache.createObject(name);
		CapResponseType crt = cache.lookupObjectWait(name);
		if (crt != null)
			crt.setEvent(ev);
		// TODO do something if null
	}
	
}
