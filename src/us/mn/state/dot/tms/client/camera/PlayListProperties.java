/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017  Minnesota Department of Transportation
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

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.PlayList;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.SonarObjectForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * PlayListProperties is a dialog for entering and editing play lists.
 *
 * @author Douglas Lau
 */
public class PlayListProperties extends SonarObjectForm<PlayList> {

	/** Play list panel */
	private final PlayListPanel play_pnl;

	/** Create a new tag reader properties form */
	public PlayListProperties(Session s, PlayList pl) {
		super(I18N.get("play.list") + ": ", s, pl);
		play_pnl = new PlayListPanel(s, pl);
	}

	/** Get the SONAR type cache */
	@Override
	protected TypeCache<PlayList> getTypeCache() {
		return state.getCamCache().getPlayLists();
	}

	/** Initialize the widgets on the form */
	@Override
	protected void initialize() {
		play_pnl.initialize();
		add(play_pnl);
		super.initialize();
	}

	/** Update the edit mode */
	@Override
	protected void updateEditMode() {
		play_pnl.updateEditMode();
	}

	/** Update one attribute on the form */
	@Override
	protected void doUpdateAttribute(String a) {
		play_pnl.updateAttribute(a);
	}
}
