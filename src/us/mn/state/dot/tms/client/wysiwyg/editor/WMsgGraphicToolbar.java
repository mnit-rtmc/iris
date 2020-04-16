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

import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import us.mn.state.dot.tms.Graphic;
import us.mn.state.dot.tms.client.dms.GraphicListCellRenderer;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.utils.I18N;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;

/**
 * WYSIWYG DMS Message Editor Graphic Option Panel containing buttons with
 * various options for graphic insert mode.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WMsgGraphicToolbar extends WToolbar {
	
	/** Graphics list */
	private DefaultComboBoxModel<Graphic> supportedGraphics;
	private JComboBox<Graphic> graphicList;
	
	/** "Add" button */
	private JButton addBtn;
	
	public WMsgGraphicToolbar(WController c) {
		super(c);

		// use a FlowLayout with no margins to give more control of separation
		setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		// get the list of graphics from the controller
		supportedGraphics = controller.getGraphicModel();
		
		// make the ComboBox and add it
		graphicList = new JComboBox<Graphic>(supportedGraphics);
		graphicList.setRenderer(new WGraphicListCellRenderer());
		graphicList.addPopupMenuListener(graphicMenuListener);
		add(graphicList);
		
		// add the "add" button
		addBtn = new JButton(addGraphic);
		addBtn.setToolTipText(I18N.get("wysiwyg.epanel.add_graphic_tooltip"));
		add(Box.createHorizontalStrut(10));
		add(addBtn);
		
		// TODO add a button to open graphic menu
		// TODO also need to somehow update the graphic list (proxy watcher?)
		
		addMoveRegionForwardButton();
		addMoveRegionBackwardButton();
	}
	
	/** Action to add the graphic */
	private final IAction addGraphic = new IAction(
			"wysiwyg.epanel.add_graphic_button") {
		protected void doActionPerformed(ActionEvent e)
				throws Exception {
			// get the selected graphic and add it using the controller
			Graphic g = (Graphic) graphicList.getSelectedItem();
			WController.println("Adding graphic %d", g.getGNumber());
			controller.addGraphic(g);
		}
	};
	
	/** Listener for updating graphic list when popup menu is opened */
	private PopupMenuListener graphicMenuListener = new PopupMenuListener() {
		public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			// update the list of supported graphics - this should carry to
			// the combobox
			controller.updateGraphicModel();
		}
		
		public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) { }
		public void popupMenuCanceled(PopupMenuEvent arg0) { }
	};
	
	private class WGraphicListCellRenderer extends GraphicListCellRenderer {
		public WGraphicListCellRenderer() {
			cell.setPreferredSize(new Dimension(200, 30));
			cell.setHorizontalTextPosition(SwingConstants.LEFT);
		}
		
		/** Modified cell renderer to scale images. */
		@Override
		public Component getListCellRendererComponent(
			JList<? extends Graphic> list, Graphic g, int index,
			boolean isSelected, boolean hasFocus)
		{
			String v = (g != null) ? Integer.toString(g.getGNumber()) : "";
			cell.getListCellRendererComponent(list, v, index, isSelected,
				hasFocus);
			Image im = createImage(g).getScaledInstance(
					-1, 25, BufferedImage.SCALE_DEFAULT);
			cell.setIcon((im != null) ? new ImageIcon(im) : null);
			return cell;
		}
	}

	/** Does nothing in graphic toolbar. */
	@Override
	public void setColor(Color c, String mode) { }
}