/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2007  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client;

import java.awt.Color;
import java.awt.Font;
import java.net.URL;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.mn.state.dot.tms.client.toast.AbstractForm;

/**
 * Provides an about form for the IRIS client.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 */
public class About extends AbstractForm {

	/** Create a new About form */
	public About() {
		super("About IRIS");
	}

	/** Initialize the About form */
	protected void initialize() {
		JLabel mndotLogo = new JLabel();
		URL url = getClass().getResource("/images/dot.gif");
		mndotLogo.setIcon(new ImageIcon(url));
		JLabel tmcLogo = new JLabel();
		url = getClass().getResource("/images/tmc.gif");
		tmcLogo.setIcon(new ImageIcon(url));
		JLabel irisLogo = new JLabel();
		url = getClass().getResource("/images/iris.gif");
		irisLogo.setIcon(new ImageIcon(url));
		JPanel topPanel = new JPanel();
		topPanel.setBackground(Color.WHITE);
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		topPanel.add(Box.createHorizontalStrut(10));
		topPanel.add(mndotLogo);
		topPanel.add(Box.createHorizontalGlue());
		topPanel.add(irisLogo);
		topPanel.add(Box.createHorizontalGlue());
		topPanel.add(tmcLogo);
		topPanel.add(Box.createHorizontalStrut(10));
		JLabel label = new JLabel(
			"IRIS -- Intelligent Roadway Information System v. @@VERSION@@");
		label.setFont(new Font("Dialog", 1, 18));
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel,
			BoxLayout.X_AXIS));
		centerPanel.add(Box.createHorizontalStrut(10));
		centerPanel.add(label);
		centerPanel.add(Box.createHorizontalStrut(10));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(topPanel);
		add(centerPanel);
	}
}
