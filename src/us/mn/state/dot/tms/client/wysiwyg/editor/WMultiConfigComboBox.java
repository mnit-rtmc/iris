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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.utils.MultiConfig;

/**
 * Custom ComboBox for displaying a list of MultiConfigs corresponding to a
 * sign group in the WYSIWYG DMS Message Editor.
 *
 * @author Gordon Parikh - SRF Consulting
 */

@SuppressWarnings("serial")
class WMultiConfigComboBox extends JComboBox<MultiConfig> {
	
	/** "Master" MultiConfig */
	private MultiConfig signGroupMultiConfig;
	
	/** Controller */
	WController controller;
	
	/** Model containing all MultiConfigs */
	private DefaultComboBoxModel<MultiConfig> allConfigs;
	
	/** Initialize the ComboBox from the sign group MultiConfig */
	public WMultiConfigComboBox(
			DefaultComboBoxModel<MultiConfig> model, MultiConfig sgmc,
			WController c) {
		super(model);
		signGroupMultiConfig = sgmc;
		controller = c;
		allConfigs = model;
		
		// use a custom renderer
		setRenderer(new WMultiConfigListCellRenderer());
		
		// and a custom ActionListener
		addActionListener(new WMultiConfigSelectionListener());
	}
	
	public static WMultiConfigComboBox fromSignGroupMultiConfig(
			MultiConfig sgmc, WController c) {
		// bail if the MultiConfig is the wrong type
		if (sgmc.getType() != MultiConfig.SIGNGROUP
				|| sgmc.getConfigList() == null)
			return null;
		
		// build the ComboBox model from the MultiConfig
		DefaultComboBoxModel<MultiConfig> model = makeModel(sgmc);
		
		// construct and return the ComboBox
		return new WMultiConfigComboBox(model, sgmc, c);
	}
	
	/** Generate the ComboBoxModel from a sign group's "master" MultiConfig */
	private static DefaultComboBoxModel<MultiConfig>
							makeModel(MultiConfig mc) {
		// initialize the model
		DefaultComboBoxModel<MultiConfig> configs =
				new DefaultComboBoxModel<MultiConfig>();
		
		// note that we won't add the "master" config to the model (at least
		// not here, since we will see it again in just a second)
		
		// loop through the tree and add to the model
		// first add all the "common-config" MultiConfigs
		for (MultiConfig cmc: mc.getConfigList())
			configs.addElement(cmc);
		
		// now add all the individual-sign configs
		for (MultiConfig smc: mc.getSignList())
			configs.addElement(smc);
		
		return configs;
	}
	
	private class WMultiConfigSelectionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// get the currently-selected MultiConfig
			MultiConfig mc = (MultiConfig) getSelectedItem();
			
			// make it the controller's "active" MultiConfig
			controller.setActiveMultiConfig(mc);
		}
	}
	
	/** Use a custom ListCellRenderer to display the MultiConfigs */
	private class WMultiConfigListCellRenderer
			implements ListCellRenderer<MultiConfig> {
		/** Cell renderer */
		private DefaultListCellRenderer cell = new DefaultListCellRenderer();
		
		/** ListCellRenderer that displays different types of MultiConfigs 
		 *  differently
		 */
		@Override
		public Component getListCellRendererComponent(
				JList<? extends MultiConfig> list, MultiConfig mc,
				int index, boolean isSelected, boolean hasFocus) {
			
			// format a string for the config based on the type
			// we will add indentation to show the "tree"
			String value;
			if (mc.getType() == MultiConfig.SIGNGROUP) {
				// we shouldn't get these, but just in case
				value = String.format(
						"<Sign Group> From %s: %d Configs, %d Signs",
						mc.getName(), mc.getConfigList().size(),
						mc.getSignList().size());
			} else if (mc.getType() == MultiConfig.CONFIG) {
				value = String.format("%s",
						mc.getName(), mc.getSignList().size());
			} else if (mc.getType() == MultiConfig.SIGN) {
				value = String.format("Sign %s", mc.getName());
			} else
				value = String.format("<UNKNOWN> %s", mc.getName());
			
			cell.getListCellRendererComponent(list, value, index,
					isSelected, hasFocus);
			return cell;
		}
	}

}















