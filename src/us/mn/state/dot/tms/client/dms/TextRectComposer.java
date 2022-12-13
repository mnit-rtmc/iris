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
import us.mn.state.dot.tms.utils.MultiBuilder;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * The TextRectComposer is a panel for text rectangles on a message composer.
 *
 * @author Douglas Lau
 */
public class TextRectComposer extends JPanel {

	/** Message composer */
	private final MessageComposer composer;

	/** Maximum number of lines on a sign */
	private final int max_lines;

	/** Text rectangle number (0-relative) */
	private final int n_rect;

	/** Panels to wrap message line combo boxes */
	private final JPanel[] line_pnl;

	/** Sign text combo box widgets */
	private final SignTextCBox[] line_cbx;

	/** Number of lines on selected sign */
	private int n_lines;

	/** Create a new text rect composer */
	public TextRectComposer(MessageComposer mc, int ml, int nr) {
		composer = mc;
		n_rect = nr;
		max_lines = ml;
		n_lines = ml;
		line_cbx = new SignTextCBox[max_lines];
		line_pnl = new JPanel[max_lines];
		for (int i = 0; i < max_lines; i++) {
			line_cbx[i] = buildLineComboBox(i);
		}
		layoutPanel();
	}

	/** Build a sign text combo box */
	private SignTextCBox buildLineComboBox(int n) {
		SignTextCBox cbx = new SignTextCBox();
		// Unlink incident if the first line (what) is changed
		final boolean unlink = (n == 0);
		cbx.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				composer.updateMessage(unlink);
			}
		});
		return cbx;
	}

	/** Layout the panel */
	private void layoutPanel() {
		for (int i = 0; i < max_lines; i++)
			initLine(i);
		GroupLayout gl = new GroupLayout(this);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		gl.setHonorsVisibility(false);
		setLayout(gl);
		GroupLayout.ParallelGroup hg = gl.createParallelGroup();
		GroupLayout.SequentialGroup vg = gl.createSequentialGroup();
		for (int i = 0; i < max_lines; i++) {
			hg.addComponent(line_pnl[i]);
			vg.addComponent(line_pnl[i]);
		}
		gl.setHorizontalGroup(hg);
		gl.setVerticalGroup(vg);
	}

	/** Initialize one line panel */
	private void initLine(int i) {
		JPanel pnl = new JPanel(new BorderLayout());
		pnl.setBackground(Color.BLACK);
		if (i == 0)
			pnl.setBorder(UI.panelBorder());
		else
			pnl.setBorder(UI.noTopBorder());
		pnl.add(line_cbx[i]);
		line_pnl[i] = pnl;
	}

	/** Clear the widgets */
	public void clearWidgets() {
		for (SignTextCBox cbox: line_cbx)
			cbox.setSelectedIndex(-1);
	}

	/** Dispose of page panel */
	public void dispose() {
		removeAll();
		for (SignTextCBox cbox: line_cbx)
			cbox.dispose();
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
		for (SignTextCBox cbox: line_cbx)
			cbox.setEditMode();
	}

	/** Set the number of lines on the page */
	public void setLines(int nl) {
		n_lines = nl;
		for (int n = 0; n < max_lines; n++)
			line_pnl[n].setVisible(n < nl);
	}

	/** Set the message combo box models */
	public void setModels(SignTextFinder stf) {
		for (int n = 0; n < max_lines; n++)
			line_cbx[n].setModel(getLineModel(stf, n));
	}

	/** Get line model for a combo box */
	private SignTextCBoxModel getLineModel(SignTextFinder stf, int n) {
		short line = getLineNumber(n);
		return (n < n_lines)
		     ? stf.getLineModel(line)
		     : new SignTextCBoxModel(line);
	}

	/** Get line model number */
	private short getLineNumber(int n) {
		return (short) (n_rect * n_lines + n + 1);
	}

	/** Set the selected lines */
	public void setSelectedLines(String[] lines) {
		for (int n = 0; n < max_lines; n++) {
			SignTextCBox cl = line_cbx[n];
			int i = getLineNumber(n) - 1;
			if (i < lines.length) {
				String ms = new MultiString(lines[i])
					.normalizeLine().stripFonts().toString();
				cl.getModel().setSelectedItem(ms);
			} else if (cl.getItemCount() > 0)
				cl.setSelectedIndex(0);
		}
	}

	/** Get a MULTI string for the page.
	 * @return MULTI string for the page. */
	public MultiString getMulti() {
		MultiBuilder mb = new MultiBuilder();
		String[] mess = new String[n_lines];
		int m = 0;
		for (int i = 0; i < mess.length; i++) {
			mess[i] = line_cbx[i].getMessage();
			if (mess[i].length() > 0)
				m = i + 1;
		}
		for (int i = 0; i < m; i++) {
			if (i > 0)
				mb.addLine(null);
			new MultiString(mess[i]).parse(mb);
		}
		return mb.toMultiString();
	}
}
