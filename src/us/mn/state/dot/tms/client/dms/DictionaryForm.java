/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2015  Iteris Inc.
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
 * A form for displaying ane editing dictionary words
 * @author Michael Darter
 */
public class DictionaryForm extends AbstractForm {

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
	public DictionaryForm(Session s) {
		super(I18N.get("dictionary"));
		setHelpPageName("help.dictionaryform");
		a_panel = new ProxyTablePanel<Word>(
			new DictTableModel(s, true));
		b_panel = new ProxyTablePanel<Word>(
			new DictTableModel(s, false));
	}

	/** Initialize form widgets */
	@Override
	protected void initialize() {
		super.initialize();
		a_panel.initialize();
		b_panel.initialize();
		tab.add(I18N.get("dictionary.allowed_words"), a_panel);
		tab.add(I18N.get("dictionary.banned_words"), b_panel);
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
