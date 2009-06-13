/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2009  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.utils.I18N;

/**
 * Provides a Support form for the IRIS client, which consists of html text
 * and a single image. The text is read from the I18N message bundle.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author Michael Darter
 */
public class Support extends AbstractForm {

	/** text */
	final static String m_text1 = I18N.get("SupportForm.Text1");;

	/** Create a new form */
	public Support() {
		super("IRIS Support");
	}

	/** Initialize form */
	protected void initialize() {

		// center panel, contains text
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel,
			BoxLayout.Y_AXIS));
		centerPanel.add(Box.createHorizontalStrut(10));
		JLabel label1 = new JLabel(m_text1);
		//label1.setHorizontalTextPosition(JLabel.CENTER);
		centerPanel.add(label1);
		centerPanel.add(Box.createHorizontalStrut(10));
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		// bottom panel, contains logo
		JLabel logo = new JLabel();
		URL url = getClass().getResource("/images/tmc.gif");
		if(url != null)
			logo.setIcon(new ImageIcon(url));
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		bottomPanel.add(Box.createHorizontalStrut(10));
		bottomPanel.add(logo);
		bottomPanel.add(Box.createHorizontalGlue());

		add(centerPanel);
		add(bottomPanel);
	}
}

