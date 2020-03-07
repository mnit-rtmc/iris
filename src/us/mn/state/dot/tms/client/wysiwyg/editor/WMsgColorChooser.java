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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.DmsColor;
import us.mn.state.dot.tms.DmsSignGroup;
import us.mn.state.dot.tms.DmsSignGroupHelper;
import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.QuickMessage;
import us.mn.state.dot.tms.RasterBuilder;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.dms.DMSPanelPager;
import us.mn.state.dot.tms.client.dms.SignFacePanel;
import us.mn.state.dot.tms.client.dms.SignPixelPanel;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.Icons;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.client.widget.Widgets;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * WYSIWYG DMS Message Editor Color Chooser
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")

public class WMsgColorChooser extends AbstractForm {
	
	/** SmartDesktop handle for closing the form */
	private SmartDesktop desktop;
	
	/** Handle to the text option toolbar that created us */
	private WMsgTextToolbar toolbar;
	
	/** Color scheme - determines what the color chooser looks like */
	private ColorScheme colorScheme;
	
	/** Selected color */
	private Color color;
	
	/** "Mode" of color chooser so we know whether we're editing the fore- or
	 *  background color */
	private String fgbgMode;
	
	/** Color chooser components - which one used changes depending on the
	 *  color scheme */
	private JColorChooser fullColorChooser;
	private JList<String> classicColorChooser;
	private DefaultListModel<String> classicColorModel;
	private Map<String, DmsColor> classicColorMap;
	private JSlider monochrome8BitSlider;

	/** Buttons */
	private JPanel btn_pnl;
	private JButton ok_btn;
	private JButton cancel_btn;
	
	public WMsgColorChooser(SmartDesktop d, WMsgTextToolbar tb,
			ColorScheme cs, String title, Color col, String fgbg) {
		super(title, true);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		desktop = d;
		toolbar = tb;
		colorScheme = cs;
		color = col;
		fgbgMode = fgbg;
		
		// create and add the color chooser panes
		initColorChooser();
		
		// now the buttons
		ok_btn = new JButton(chooseColor);
		cancel_btn = new JButton(cancel);
		btn_pnl = new JPanel();
		btn_pnl.add(ok_btn);
		btn_pnl.add(cancel_btn);
		add(btn_pnl);
	}
	
	/** Initialize the color chooser based on the provided color scheme. */
	private void initColorChooser() {
		// for full color, use Swing's JColorChooser which provides a full-
		// featured color chooser with all the bells and whistles
		if (colorScheme == ColorScheme.COLOR_24_BIT) {
			fullColorChooser = new JColorChooser(color);
			add(fullColorChooser);
		} else if (colorScheme == ColorScheme.COLOR_CLASSIC) {
			// for classic color scheme, use a JList filled with the classic
			// colors as options
			initClassicColorChooser();
			add(classicColorChooser);
		} else if (colorScheme == ColorScheme.MONOCHROME_8_BIT) {
			// for 8-bit monochrome, use a slider from 0 to 255
			monochrome8BitSlider = new JSlider(0, 255, 127);
			add(monochrome8BitSlider);
		}
	}
	
	private void initClassicColorChooser() {
		// use a hashmap to store classic colors (we use a linked hash map
		// to preserve order)
		classicColorMap = new LinkedHashMap<String, DmsColor>();
		classicColorMap.put("Black", DmsColor.BLACK);
		classicColorMap.put("Red", DmsColor.RED);
		classicColorMap.put("Yellow", DmsColor.YELLOW);
		classicColorMap.put("Green", DmsColor.GREEN);
		classicColorMap.put("Cyan", DmsColor.CYAN);
		classicColorMap.put("Blue", DmsColor.BLUE);
		classicColorMap.put("Magenta", DmsColor.MAGENTA);
		classicColorMap.put("White", DmsColor.WHITE);
		classicColorMap.put("Orange", DmsColor.ORANGE);
		classicColorMap.put("Amber", DmsColor.AMBER);
		
		// create the list model and fill it with colors
		classicColorModel = new DefaultListModel<String>();
		for (Map.Entry<String, DmsColor> el : classicColorMap.entrySet()) {
			classicColorModel.addElement(el.getKey());
		}
		
		// create the JList with the list model
		classicColorChooser = new JList<String>(classicColorModel);
		classicColorChooser.setSelectionMode(
				ListSelectionModel.SINGLE_SELECTION);
		
		// setup the list cell renderer to include color icons
		class ClassicColorListCellRenderer extends JLabel implements 
				ListCellRenderer<String> {
			public ClassicColorListCellRenderer() {
				setOpaque(true);
			}
			
			@Override
			public Component getListCellRendererComponent(
					JList<? extends String> list, String cName, int index,
					boolean isSelected, boolean hasFocus) {
				// get the DmsColor object from the hash map
				DmsColor c = classicColorMap.get(cName);
				
				// use the color icon and the name of the color in the list
				setIcon(createColorIcon(c.color, 16,16));
				setText(cName);
				
				// change the color if selected
				if (isSelected) {
					setBackground(list.getSelectionBackground());
					setForeground(list.getSelectionForeground());
				} else {
					setBackground(list.getBackground());
					setForeground(list.getForeground());
				}
				return this;
			}
		}
		classicColorChooser.setCellRenderer(new ClassicColorListCellRenderer());
	}
	
	/** Set the chosen color and close the form. */
	private final IAction chooseColor = new IAction("wysiwyg.color_chooser.ok_btn") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			color = null;
			if (colorScheme == ColorScheme.COLOR_24_BIT) {
				color = fullColorChooser.getColor();
			} else if (colorScheme == ColorScheme.COLOR_CLASSIC) {
				int i = classicColorChooser.getSelectedIndex();
				if (i != -1) {
					String cName = classicColorModel.get(i);
					color = classicColorMap.get(cName).color;
				}
			} else if (colorScheme == ColorScheme.MONOCHROME_8_BIT) {
				int mValue = monochrome8BitSlider.getValue();
				
				// TODO what do we actually do with this???
			}
			
			if (color != null)
				toolbar.setColor(color, fgbgMode);
			close(desktop);
		}
	};
	
	/** Close the form without doing anything. */
	private final IAction cancel = new IAction("wysiwyg.color_chooser.cancel") {
		@SuppressWarnings("synthetic-access")
		protected void doActionPerformed(ActionEvent e)
				throws Exception
		{
			close(desktop);
		}
	};
	
	public static  ImageIcon createColorIcon(Color c, int width, int height) {
		BufferedImage image = new BufferedImage(width, height,
				java.awt.image.BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(c);
        graphics.fillRect(0, 0, width, height);
        graphics.setXORMode(Color.DARK_GRAY);
        graphics.drawRect(0, 0, width-1, height-1);
        image.flush();
        ImageIcon icon = new ImageIcon(image);
        return icon;
    }
}