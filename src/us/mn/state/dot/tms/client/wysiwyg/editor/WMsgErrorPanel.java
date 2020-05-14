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

package us.mn.state.dot.tms.client.wysiwyg.editor;

import javax.swing.JPanel;
import java.util.Iterator;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import us.mn.state.dot.tms.utils.wysiwyg.WEditorError;
import us.mn.state.dot.tms.utils.wysiwyg.WEditorErrorManager;

/**
 * WYSIWYG DMS Message Editor Error Tab for displaying errors encountered
 * while editing messages.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")


public class WMsgErrorPanel extends JPanel {
	/** Error manager that provides our list of errors */
	private WEditorErrorManager errMan;
	
	/** List for displaying error descriptions */
	private JList<String> errorList;
	DefaultListModel<String> errListModel;
	
	/** Scroll pane in case the error list gets long */
	private JScrollPane scrollPane;
	
	/** Create a new error panel containing any current errors with the MULTI
	 *  string/rendering. */
	public WMsgErrorPanel(WEditorErrorManager em) {
		errMan = em;
		
		// use a box layout to put components in a column
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		// create the error list with a scroll pane
		errListModel = new DefaultListModel<String>();
		errorList = new JList<String>(errListModel);
		scrollPane = new JScrollPane(errorList);
		updateErrorList();
		
		// add to the panel
		add(scrollPane);
	}
	
	/** Update the error list from the error manager. */
	public void updateErrorList() {
		// clear the list first
		errListModel.clear();
		
		// iterate over the errors in the manager and add them to the list
		Iterator<WEditorError> it = errMan.iterator();
		while (it.hasNext()) {
			WEditorError err = it.next();
			errListModel.addElement(err.getLongErrStr());
		}
	}
}
