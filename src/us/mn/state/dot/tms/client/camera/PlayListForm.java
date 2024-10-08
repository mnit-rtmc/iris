/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2024  Minnesota Department of Transportation
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

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import us.mn.state.dot.tms.PlayList;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTableForm;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying and editing play lists.
 *
 * @author Douglas Lau
 */
public class PlayListForm extends ProxyTableForm<PlayList> {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.canRead(PlayList.SONAR_TYPE);
	}

	/** Panel for play lists */
	static private class PlayListPanel extends ProxyTablePanel<PlayList> {
		private final JCheckBox meta_chk =
			new JCheckBox(I18N.get("play.list.meta"));
		private final PlayListModel mdl;

		private PlayListPanel(Session s) {
			super(new PlayListModel(s));
			mdl = (PlayListModel) model;
		}

		@Override
		protected void addCreateDeleteWidgets(
			GroupLayout.SequentialGroup hg,
			GroupLayout.ParallelGroup vg
		) {
			hg.addComponent(meta_chk);
			vg.addComponent(meta_chk);
			hg.addGap(UI.hgap);
			super.addCreateDeleteWidgets(hg, vg);
		}

		@Override
		protected void createObject() {
			mdl.createObject(meta_chk.isSelected());
		}
	}

	/** Create a new play list form */
	public PlayListForm(Session s) {
		super(I18N.get("play.list.title"), new PlayListPanel(s));
	}
}
