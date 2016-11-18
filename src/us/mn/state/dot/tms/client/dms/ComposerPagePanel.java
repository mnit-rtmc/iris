/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2016  Minnesota Department of Transportation
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
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import us.mn.state.dot.tms.RasterBuilder;
import us.mn.state.dot.tms.client.widget.ILabel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.MultiBuilder;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * The ComposerPagePanel is a GUI panel for tabbed pages related to the sign
 * composer panel.
 *
 * @author Douglas Lau
 */
public class ComposerPagePanel extends JPanel {

	/** Sign message composer */
	private final SignMessageComposer composer;

	/** Maximum number of lines on a sign */
	private final int max_lines;

	/** Page number (0-relative) */
	private final int n_page;

	/** Panels to wrap message line combo boxes */
	private final JPanel[] line_pnl;

	/** Message combo box widgets */
	private final MsgComboBox[] line_cbx;

	/** Font combo box widget */
	private final FontComboBox font_cbx;

	/** Number of lines on selected sign */
	private int n_lines;

	/** Create a new page panel */
	public ComposerPagePanel(SignMessageComposer smc, int ml, int p) {
		composer = smc;
		n_page = p;
		max_lines = ml;
		n_lines = ml;
		line_cbx = new MsgComboBox[max_lines];
		line_pnl = new JPanel[max_lines];
		font_cbx = new FontComboBox(composer);
		for (int i = 0; i < max_lines; i++) {
			line_cbx[i] = new MsgComboBox(composer,
				getLineNumber(i));
			line_cbx[i].initialize();
		}
		layoutPanel();
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
		if (FontComboBox.getIEnabled()) {
			ILabel label = new ILabel("font");
			label.setLabelFor(font_cbx);
			GroupLayout.SequentialGroup sfg =
				gl.createSequentialGroup();
			sfg.addComponent(label);
			sfg.addGap(UI.hgap);
			sfg.addComponent(font_cbx);
			hg.addGroup(sfg);
			GroupLayout.ParallelGroup vfg =
				gl.createBaselineGroup(true, false);
			vfg.addComponent(label);
			vfg.addComponent(font_cbx);
			vg.addGroup(vfg);
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
		for (MsgComboBox cbox: line_cbx)
			cbox.setSelectedIndex(-1);
		font_cbx.setSelectedFontNumber(composer.getDefaultFont());
	}

	/** Dispose of page panel */
	public void dispose() {
		removeAll();
		for (MsgComboBox cbox: line_cbx)
			cbox.dispose();
		font_cbx.dispose();
	}

	/** Enable or Disable the page panel */
	@Override
	public void setEnabled(boolean b) {
		for (MsgComboBox cbx: line_cbx)
			cbx.setEnabled(b);
		font_cbx.setEnabled(b);
		super.setEnabled(b);
	}

	/** Set the edit mode */
	public void setEditMode(boolean cam) {
		for (MsgComboBox cbox: line_cbx)
			cbox.setEditMode(cam);
	}

	/** Set the number of lines on the page */
	public void setLines(int nl) {
		n_lines = nl;
		for (int n = 0; n < max_lines; n++)
			line_pnl[n].setVisible(n < nl);
	}

	/** Set the message combo box models */
	public void setModels(SignTextModel stm) {
		for (int n = 0; n < max_lines; n++)
			line_cbx[n].setModel(getLineModel(stm, n));
	}

	/** Get line model for a combo box */
	private SignTextComboBoxModel getLineModel(SignTextModel stm, int n) {
		short line = getLineNumber(n);
		return (stm != null && n < n_lines)
		     ? stm.getLineModel(line)
		     : new SignTextComboBoxModel(line);
	}

	/** Get line model number */
	private short getLineNumber(int n) {
		return (short)(n_page * n_lines + n + 1);
	}

	/** Set the raster builder */
	public void setBuilder(RasterBuilder rb) {
		font_cbx.setBuilder(rb);
	}

	/** Set the selected lines */
	public void setSelected(String[] lines) {
		for (int n = 0; n < max_lines; n++) {
			MsgComboBox cl = line_cbx[n];
			int i = getLineNumber(n) - 1;
			if (i < lines.length)
				cl.getModel().setSelectedItem(lines[i]);
			else if (cl.getItemCount() > 0)
				cl.setSelectedIndex(0);
		}
	}

	/** Get a MULTI string for the page.
	 * @param n_font Current font number.
	 * @return MULTI string for the page. */
	public MultiString getMulti(int n_font) {
		MultiBuilder mb = new MultiBuilder();
		String[] mess = new String[n_lines];
		int m = 0;
		for (int i = 0; i < mess.length; i++) {
			mess[i] = line_cbx[i].getMessage();
			if (mess[i].length() > 0)
				m = i + 1;
		}
		for (int i = 0; i < m; i++) {
			if (i == 0) {
				int fn = getFontNumber();
				if (fn != n_font)
					mb.setFont(fn, null);
			} else
				mb.addLine(null);
			new MultiString(mess[i]).parse(mb);
		}
		return mb.toMultiString();
	}

	/** Get the font number for the page */
	public int getFontNumber() {
		Integer fn = font_cbx.getFontNumber();
		return (fn != null) ? fn : composer.getDefaultFont();
	}

	/** Set the font number for the page */
	public void setFontNumber(int fn) {
		font_cbx.setSelectedFontNumber(fn);
	}
}
