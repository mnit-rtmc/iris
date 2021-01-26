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
import us.mn.state.dot.tms.CapUrgencyField;
import us.mn.state.dot.tms.CapUrgencyFieldHelper;
import us.mn.state.dot.tms.client.proxy.ProxyTableModel;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;


/**
 * A panel for displaying and editing CAP urgency substitution values.
 *
 * @author Gordon Parikh
 */
@SuppressWarnings("serial")
public class CapUrgencyFieldPanel extends ProxyTablePanel<CapUrgencyField> {

	/** TypeCache for looking up objects after creation */
	private final TypeCache<CapUrgencyField> cache;

	public CapUrgencyFieldPanel(ProxyTableModel<CapUrgencyField> m) {
		super(m);
		cache = m.getSession().getSonarState().getCapUrgencyFieldCache();
	}

	/** Create a new urgency substitution value. Uses the text in the
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
			ev = CapUrgencyField.DEFAULT_EVENT;

		// generate a new unique name
		String name = CapUrgencyFieldHelper.createUniqueName();

		// create the object with the unique name then set the label
		cache.createObject(name);
		CapUrgencyField cu = cache.lookupObjectWait(name);
		if (cu != null)
			cu.setEvent(ev);
		// TODO do something if null
	}
}
