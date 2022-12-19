/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2022  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.client.dms;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import static us.mn.state.dot.tms.SignMessage.MAX_LINES;
import us.mn.state.dot.tms.utils.MultiBuilder;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * TextRectComposer is a panel for text rectangles on a message composer.
 *
 * @author Douglas Lau
 */
public class TextRectComposer extends JPanel {

	/** Message composer */
	private final MessageComposer composer;

	/** Panels to wrap message line combo boxes */
	private final JPanel[] line_pnl;

	/** Sign text combo box widgets */
	private final SignTextCBox[] line_cbx;

	/** Create a new text rect composer */
	public TextRectComposer(MessageComposer mc) {
		composer = mc;
		line_cbx = new SignTextCBox[MAX_LINES];
		line_pnl = new JPanel[MAX_LINES];
		for (int i = 0; i < MAX_LINES; i++) {
			line_cbx[i] = buildLineComboBox(i);
			line_pnl[i] = buildLinePanel(i);
		}
		layoutPanel();
	}

	/** Build a sign text combo box */
	private SignTextCBox buildLineComboBox(int i) {
		SignTextCBox cbx = new SignTextCBox();
		// Unlink incident if the first line (what) is changed
		final boolean unlink = (i == 0);
		cbx.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				composer.updateMessage(unlink);
			}
		});
		return cbx;
	}

	/** Build panel for one line */
	private JPanel buildLinePanel(int i) {
		JPanel pnl = new JPanel(new BorderLayout());
		pnl.setVisible(false);
		pnl.setBackground(Color.BLACK);
		pnl.setBorder((i == 0)
			? UI.panelBorder()
			: UI.noTopBorder()
		);
		pnl.add(line_cbx[i]);
		return pnl;
	}

	/** Layout the panel */
	private void layoutPanel() {
		GroupLayout gl = new GroupLayout(this);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		gl.setHonorsVisibility(false);
		setLayout(gl);
		GroupLayout.ParallelGroup hg = gl.createParallelGroup();
		GroupLayout.SequentialGroup vg = gl.createSequentialGroup();
		for (int i = 0; i < MAX_LINES; i++) {
			hg.addComponent(line_pnl[i]);
			vg.addComponent(line_pnl[i]);
		}
		gl.setHorizontalGroup(hg);
		gl.setVerticalGroup(vg);
	}

	/** Clear the widgets */
	public void clearWidgets() {
		for (SignTextCBox cbx: line_cbx)
			cbx.setSelectedIndex(-1);
	}

	/** Dispose of page panel */
	public void dispose() {
		removeAll();
		for (SignTextCBox cbx: line_cbx)
			cbx.dispose();
	}

	/** Enable or Disable the page panel */
	@Override
	public void setEnabled(boolean b) {
		for (SignTextCBox cbx: line_cbx)
			cbx.setEnabled(b);
		super.setEnabled(b);
	}

	/** Set the edit mode */
	public void setEditMode() {
		for (SignTextCBox cbx: line_cbx)
			cbx.setEditMode();
	}

	/** Set the message combo box models */
	public void setModels(SignTextFinder stf, int first, int n_lines) {
		for (int i = 0; i < MAX_LINES; i++) {
			if (i < n_lines) {
				short ln = (short) (first + i);
				line_cbx[i].setModel(stf.getLineModel(ln));
				line_pnl[i].setVisible(true);
			} else {
				line_pnl[i].setVisible(false);
				line_cbx[i].setModel(new SignTextCBoxModel());
			}
		}
	}

	/** Set the selected lines */
	public void setSelectedLines(String[] lines) {
		for (int i = 0; i < MAX_LINES; i++) {
			if (i < lines.length) {
				String ms = new MultiString(lines[i])
					.normalizeLine().stripFonts().toString();
				line_cbx[i].getModel().setSelectedItem(ms);
			} else
				line_cbx[i].setSelectedIndex(-1);
		}
	}

	/** Get MULTI string of composed text */
	public String getMulti() {
		MultiBuilder mb = new MultiBuilder();
		for (int i = 0; i < MAX_LINES; i++) {
			if (i > 0)
				mb.addLine(null);
			new MultiString(line_cbx[i].getMessage()).parse(mb);
		}
		return mb.toMultiString().stripTrailingLines();
	}
}
