/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2018  SRF Consulting Group
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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.dms.SignFacePanel;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.Widgets;
import us.mn.state.dot.tms.utils.I18N;

/**
 * WYSIWYG DMS Message Editor Pane
 *
 * @author Gordon Parikh, John L. Stanley, and Michael Janson - SRF Consulting
 */
@SuppressWarnings("serial")


public class WMsgEditorPanel extends JPanel {

	// TODO TODO TODO
	
	/** Tabbed sign/sign group listing */
	private JTabbedPane tab_pane;
	
	/** TODO Temporary */
	private SignFacePanel sfp = new SignFacePanel();
	
	public WMsgEditorPanel() {
		tab_pane = new JTabbedPane(JTabbedPane.TOP);
//		tab_pane.add(I18N.get("wysiwyg.epanel.wysiwyg_tab"), dms_pn);
//		tab_pane.add(I18N.get("wysiwyg.epanel.multi_tab"), sgrp_pn);
//		tab_pane.add(I18N.get("wysiwyg.epanel.config_tab"), sgrp_pn);
		
	}
	
	
}

