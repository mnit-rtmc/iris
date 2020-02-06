/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  SRF Consulting Group
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
import javax.swing.JTabbedPane;

import us.mn.state.dot.tms.client.dms.SignFacePanel;
import us.mn.state.dot.tms.client.dms.SignPixelPanel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * WYSIWYG DMS Message Editor Pane
 *
 * @author Gordon Parikh, John L. Stanley - SRF Consulting
 */
@SuppressWarnings("serial")


public class WMsgEditorPanel extends JPanel {
	/** WController for updating renderings */
	private WController controller;
	
	/** Tabs for switching between WYSIWYG/MULTI mode and viewing sign config,
	 * warnings, and errors */
	private JTabbedPane tab_pane;
	private WMsgWysiwygPanel wysiwyg_pnl;
	private JPanel multi_pnl;
	private JPanel config_pnl;
	
	// TODO - warnings and errors (should be dynamic)
	
	public WMsgEditorPanel(WController c) {
		controller = c;
		
		// create the tab pane and the tabs
		tab_pane = new JTabbedPane(JTabbedPane.TOP);
		
		// generate each tab
		wysiwyg_pnl = new WMsgWysiwygPanel(controller);
		multi_pnl = new WMsgMultiPanel(controller);
		config_pnl = new JPanel();					// TODO
		
		// add the tabs to the pane
		tab_pane.add(I18N.get("wysiwyg.epanel.wysiwyg_tab"), wysiwyg_pnl);
		tab_pane.add(I18N.get("wysiwyg.epanel.multi_tab"), multi_pnl);
		tab_pane.add(I18N.get("wysiwyg.epanel.config_tab"), config_pnl);
		
		// add the tab pane to the panel
		add(tab_pane);
	}
	
	/** Update the main WYSIWYG panel with the currently selected page (called
	 *  from the main form). */
	public void updateWysiwygSignPage(WMsgSignPage sp) {
		// use the currently selected page to update the main WYSIWYG panel
		wysiwyg_pnl.setPage(sp);
	}

	/** Get the pixel panel from the WYSIWYG editor panel */
	public SignPixelPanel getEditorPixelPanel() {
		return wysiwyg_pnl.getEditorPixelPanel(); 
	}
}

