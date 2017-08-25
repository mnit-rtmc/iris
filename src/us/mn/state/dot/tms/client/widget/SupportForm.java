/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2017  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.widget;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.mn.state.dot.tms.utils.I18N;

/**
 * Provides a support form for the IRIS client, which consists of html text
 * and a single image. The text is read from the I18N message bundle.
 *
 * @author Erik Engstrom
 * @author Douglas Lau
 * @author Michael Darter
 */
public class SupportForm extends AbstractForm {

	/** Create a new support form */
	public SupportForm() {
		super(I18N.get("help.support.title"));
	}

	/** Initialize form */
	@Override
	protected void initialize() {
		super.initialize();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(createTextPanel());
		add(createLogoPanel());
	}

	/** Create support text panel */
	private JPanel createTextPanel() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
		pnl.add(Box.createHorizontalStrut(10));
		pnl.add(new ILabel("help.support.text1"));
		pnl.add(Box.createHorizontalStrut(10));
		return pnl;
	}

	/** Create panel containing logo */
	private JPanel createLogoPanel() {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.X_AXIS));
		pnl.add(Box.createHorizontalStrut(10));
		pnl.add(createLogo());
		pnl.add(Box.createHorizontalGlue());
		return pnl;
	}

	/** Create logo widget */
	private JLabel createLogo() {
		JLabel logo = new JLabel();
		logo.setIcon(Icons.getIcon("iris"));
		return logo;
	}
}
