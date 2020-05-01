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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiConfig;
import us.mn.state.dot.tms.utils.wysiwyg.WEditorErrorManager;
import us.mn.state.dot.tms.utils.wysiwyg.WPage;

/**
 * WYSIWYG DMS Message Editor Pane
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")


public class WMsgEditorPanel extends JPanel {
	/** WController for updating renderings */
	private WController controller;
	
	/** Tabs for switching between WYSIWYG/MULTI mode and viewing sign config,
	 * warnings, and errors */
	private JTabbedPane tabPane;
	private WMsgWysiwygPanel wysiwygPanel;
	private WMsgMultiPanel multiPanel;
	private WMsgConfigPanel configPanel;
	private WMsgErrorPanel errorPanel;
	
	private final static int WYSIWYG_PANEL_TAB = 0;
	private final static int MULTI_PANEL_TAB = 1;
	private final static int CONFIG_PANEL_TAB = 2;
	private final static int ERROR_PANEL_TAB = 3;
	
	// TODO - warnings and errors (should be dynamic)
	
	public WMsgEditorPanel(WController c) {
		controller = c;
		
		// left-aligned FlowLayout
		setLayout(new BorderLayout());
		
		// create the tab pane and the tabs
		tabPane = new JTabbedPane(JTabbedPane.TOP);
		
		// generate each tab
		wysiwygPanel = new WMsgWysiwygPanel(controller);
		multiPanel = new WMsgMultiPanel(controller);
		configPanel = new WMsgConfigPanel(controller);
		
		// add the tabs to the pane
		tabPane.add(I18N.get("wysiwyg.epanel.wysiwyg_tab"), wysiwygPanel);
		tabPane.add(I18N.get("wysiwyg.epanel.multi_tab"), multiPanel);
		tabPane.add(I18N.get("wysiwyg.epanel.config_tab"), configPanel);
		
		// set the active MultiConfig 
		setActiveMultiConfig(controller.getMultiConfig());
		
		// add the tab pane to the panel
		add(tabPane, BorderLayout.CENTER);
	}
	
	/** Toggle active tab between WYSIWYG and MULTI */
	public Action toggleActiveTab = new AbstractAction() {
		public void actionPerformed(ActionEvent e) {
			if (controller.multiConfigUseable()) {
				if (tabPane.getSelectedComponent() == wysiwygPanel)
					tabPane.setSelectedComponent(multiPanel);
				else if (tabPane.getSelectedComponent() == multiPanel)
					tabPane.setSelectedComponent(wysiwygPanel);
			}
		}
	};
	
	/** Enable or disable functions that correspond to any tags not supported
	 *  by the MultiConfig provided.
	 */
	public void setActiveMultiConfig(MultiConfig mc) {
		wysiwygPanel.checkSupportedFuncs(mc);
		configPanel.setActiveMultiConfig(mc);
		if (configPanel.hasErrors())
			tabPane.setForegroundAt(CONFIG_PANEL_TAB, Color.RED);
		else if (configPanel.hasWarnings())
			tabPane.setForegroundAt(CONFIG_PANEL_TAB, Color.ORANGE);
		else
			tabPane.setForegroundAt(CONFIG_PANEL_TAB, Color.BLACK);
		
		// disable the wysiwyg panel if the MultiConfig isn't usable
		if (mc != null && mc.isUseable()) {
			tabPane.setEnabledAt(WYSIWYG_PANEL_TAB, true);
			tabPane.setSelectedIndex(WYSIWYG_PANEL_TAB);
		} else {
			tabPane.setEnabledAt(WYSIWYG_PANEL_TAB, false);
			tabPane.setSelectedIndex(MULTI_PANEL_TAB);
		}
	}
	
	/** Update the main WYSIWYG panel with the currently selected page (called
	 *  from the main form). */
	public void setPage(WPage sp) {
		// use the currently selected page to update the main WYSIWYG panel
		wysiwygPanel.setPage(sp);
	}

	/** Get the pixel panel from the WYSIWYG editor panel */
	public WImagePanel getWImagePanel() {
		return wysiwygPanel.getWImagePanel(); 
	}
	
	/** Add the dynamic error panel for displaying current MULTI/renderer
	 *  errors. This tab only appears when there are errors.
	 */
	public void addErrorPanel(WEditorErrorManager errMan) {
		// create a new error panel then add it to the tab pane
		errorPanel = new WMsgErrorPanel(errMan);
		tabPane.add(I18N.get("wysiwyg.epanel.error_tab"), errorPanel);
		
		// make it red to catch attention
		tabPane.setForegroundAt(ERROR_PANEL_TAB, Color.RED);
		
		// enable the restore button
		wysiwygPanel.enableRestoreButton();
	}
	
	/** Return whether or not the error panel is currently showing. */
	public boolean hasErrorPanel() {
		return errorPanel != null;
	}
	
	/** Update the error panel with the current MULTI/renderer errors from the
	 *  error manager. The error panel must have been initialized first.
	 */
	public void updateErrorPanel() {
		if (hasErrorPanel())
			errorPanel.updateErrorList();
	}
	
	/** Remove the dynamic error panel. Performed when errors are addressed
	 *  by the user.
	 */
	public void removeErrorPanel() {
		if (hasErrorPanel()) {
			tabPane.remove(errorPanel);
			errorPanel = null;
			wysiwygPanel.disableRestoreButton();
		}
	}
	
	/** Update the text toolbar */
	public void updateTextToolbar() {
		wysiwygPanel.updateTextToolbar();
	}
	
	/** Enable or disable tag edit button */
	public void updateTagEditButton(boolean state) {
		wysiwygPanel.updateTagEditButton(state);
	}
	
	/** Update the non-text tag button state to */
	public void updateNonTextTagButton(boolean state) {
		wysiwygPanel.updateNonTextTagButton(state);
	}
	
	/** Update the non-text tag info label, optionally including a color icon
	 *  (if c is null, no color is shown).
	 */
	public void updateNonTextTagInfo(String s, Color c) {
		wysiwygPanel.updateNonTextTagInfo(s, c);
	}
	
	public String getMultiPanelContents() {
		return multiPanel.getMulti();
	}
}

