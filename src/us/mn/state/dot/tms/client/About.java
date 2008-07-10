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
import us.mn.state.dot.tms.utils.I18NMessages;

/**
 * Provides an about form for the IRIS client.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author Michael Darter
 */
public class About extends AbstractForm {

	/** 1st title line */
	final static String m_title1="IRIS -- Intelligent Roadway Information System v. @@VERSION@@";

	/** 2nd title line (optional) */
	final static String m_title2=I18NMessages.get("AboutForm.Title2");

	/** 3rd title line (optional) */
	final static String m_title3=I18NMessages.get("AboutForm.Title3");

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

		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel,
			BoxLayout.Y_AXIS));
		centerPanel.add(Box.createHorizontalStrut(10));

		// title 1
		JLabel label1 = new JLabel(m_title1);
		label1.setHorizontalTextPosition(JLabel.CENTER);
		label1.setFont(new Font("Dialog", 1, 18));
		label1.setAlignmentX(centerPanel.CENTER_ALIGNMENT);
		centerPanel.add(label1);

		// optional title 2
		if (useTitle(m_title2)) {
			JLabel label = new JLabel(m_title2);
			label.setAlignmentX(centerPanel.CENTER_ALIGNMENT);
			label.setFont(new Font("Dialog", 1, 16));
			centerPanel.add(label);
		}

		// optional title 3
		if (useTitle(m_title3)) {
			JLabel label = new JLabel(m_title3);
			label.setAlignmentX(centerPanel.CENTER_ALIGNMENT);
			label.setFont(new Font("Dialog", 1, 16));
			centerPanel.add(label);
		}

		centerPanel.add(Box.createHorizontalStrut(10));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(topPanel);
		add(centerPanel);
	}

	/** return true to use an optional title else false */
	private boolean useTitle(String t) {
		return t!=null && t.length()>0;
	}
}

