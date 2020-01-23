/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Iteris Inc.
 * Copyright (C) 2020  Minnesota Department of Transportation
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

import javax.swing.JTabbedPane;
import us.mn.state.dot.tms.Word;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.utils.I18N;

/**
 * A form for displaying ane editing allowed/banned DMS words.
 *
 * @author Michael Darter
 */
public class WordForm extends AbstractForm {

	/** Check if the user is permitted to use the form */
	static public boolean isPermitted(Session s) {
		return s.isWritePermitted(Word.SONAR_TYPE);
	}

	/** Allowed words panel */
	private final ProxyTablePanel a_panel;

	/** Banned words panel */
	private final ProxyTablePanel b_panel;

	/** Tabbed pane */
	private final JTabbedPane tab = new JTabbedPane(JTabbedPane.TOP);

	/** Create form */
	public WordForm(Session s) {
		super(I18N.get("word.plural"));
		setHelpPageName("help.word.form");
		a_panel = new ProxyTablePanel<Word>(
			new WordTableModel(s, true));
		b_panel = new ProxyTablePanel<Word>(
			new WordTableModel(s, false));
	}

	/** Initialize form widgets */
	@Override
	protected void initialize() {
		super.initialize();
		a_panel.initialize();
		b_panel.initialize();
		tab.add(I18N.get("word.allowed"), a_panel);
		tab.add(I18N.get("word.banned"), b_panel);
		add(tab);
	}

	/** Dispose of the form */
	@Override
	protected void dispose() {
		a_panel.dispose();
		b_panel.dispose();
		super.dispose();
	}
}
