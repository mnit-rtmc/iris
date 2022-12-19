/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2022  Minnesota Department of Transportation
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
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.RasterBuilder;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.ILabel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * PropMessages is a GUI panel for displaying and editing sign messages on a
 * DMS properties form.
 *
 * @author Douglas Lau
 */
public class PropMessages extends JPanel {

	/** Sign grou panel */
	private final ProxyTablePanel<SignGroup> sign_group_pnl;

	/** Sign text panel */
	private final ProxyTablePanel<SignText> sign_text_pnl;

	/** Preview panel */
	private final JPanel preview_pnl;

	/** Sign pixel panel */
	private final SignPixelPanel pixel_pnl = new SignPixelPanel(40, 400);

	/** User session */
	private final Session session;

	/** DMS proxy */
	private final DMS proxy;

	/** Create a new DMS properties messages panel */
	public PropMessages(Session s, DMS sign) {
		session = s;
		proxy = sign;
		sign_group_pnl = new ProxyTablePanel<SignGroup>(
			new SignGroupTableModel(s, sign))
		{
			protected void selectProxy() {
				super.selectProxy();
				selectGroup();
			}
		};
		sign_text_pnl = new ProxyTablePanel<SignText>(
			new SignTextTableModel(s, null))
		{
			protected void selectProxy() {
				super.selectProxy();
				selectSignText();
			}
		};
		preview_pnl = createPreviewPanel();
	}

	/** Initialize the messages panel */
	public void initialize() {
		setBorder(UI.border);
		sign_group_pnl.initialize();
		sign_text_pnl.initialize();
		layoutPanel();
	}

	/** Dispose of the form */
	public void dispose() {
		sign_group_pnl.dispose();
		sign_text_pnl.dispose();
		removeAll();
	}

	/** Layout the panel */
	private void layoutPanel() {
		GroupLayout gl = new GroupLayout(this);
		gl.setHonorsVisibility(false);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		gl.setHorizontalGroup(createHorizontalGroup(gl));
		gl.setVerticalGroup(createVerticalGroup(gl));
		setLayout(gl);
	}

	/** Create the horizontal group */
	private GroupLayout.Group createHorizontalGroup(GroupLayout gl) {
		GroupLayout.ParallelGroup hg = gl.createParallelGroup();
		GroupLayout.SequentialGroup g0 = gl.createSequentialGroup();
		g0.addComponent(sign_group_pnl);
		g0.addGap(UI.hgap);
		g0.addComponent(sign_text_pnl);
		hg.addGroup(g0);
		hg.addComponent(preview_pnl);
		return hg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup vg = gl.createSequentialGroup();
		GroupLayout.ParallelGroup g0 = gl.createParallelGroup();
		g0.addComponent(sign_group_pnl);
		g0.addComponent(sign_text_pnl);
		vg.addGroup(g0);
		vg.addGap(UI.vgap);
		vg.addComponent(preview_pnl);
		vg.addGap(UI.vgap);
		return vg;
	}

	/** Create a message preview panel */
	private JPanel createPreviewPanel() {
		pixel_pnl.setFilterColor(new Color(0, 0, 255, 48));
		JPanel pnl = new JPanel(new BorderLayout());
		pnl.setBorder(BorderFactory.createTitledBorder(
			I18N.get("dms.message.preview")));
		pnl.add(pixel_pnl, BorderLayout.CENTER);
		return pnl;
	}

	/** Select a new sign group */
	private void selectGroup() {
		sign_text_pnl.setModel(new SignTextTableModel(session,
			sign_group_pnl.getSelectedProxy()));
	}

	/** Select a new sign text message */
	private void selectSignText() {
		SignConfig sc = proxy.getSignConfig();
		if (sc != null) {
			int w = sc.getFaceWidth();
			Integer lh = getLineHeightPixels();
			int ph = sc.getPitchHoriz();
			int pv = sc.getPitchVert();
			int bh = sc.getBorderHoriz();
			int pw = sc.getPixelWidth();
			int cw = sc.getCharWidth();
			if (lh != null) {
				int h = lh * pv;
				pixel_pnl.setPhysicalDimensions(w, h, bh, 0,
				                                ph, pv);
				pixel_pnl.setLogicalDimensions(pw, lh, cw, 0);
				pixel_pnl.repaint();
			}
		}
		SignText st = sign_text_pnl.getSelectedProxy();
		if (st != null)
			pixel_pnl.setGraphic(renderSignText(st));
		else
			pixel_pnl.setGraphic(null);
	}

	/** Get the line height of the sign */
	private Integer getLineHeightPixels() {
		RasterBuilder b = DMSHelper.createRasterBuilder(proxy);
		if (b != null)
			return b.getLineHeightPixels();
		else
			return null;
	}

	/** Render a sign text to a raster graphic */
	private RasterGraphic renderSignText(SignText st) {
		MultiString multi = new MultiString(st.getMulti());
		RasterGraphic[] pages = renderSignText(multi);
		if (pages != null && pages.length > 0)
			return pages[0];
		else
			return null;
	}

	/** Render a sign text (single-line) */
	private RasterGraphic[] renderSignText(MultiString ms) {
		SignConfig sc = proxy.getSignConfig();
		if (null == sc)
			return null;
		int w = sc.getPixelWidth();
		int h = getLineHeightPixels();
		int cw = sc.getCharWidth();
		int ch = sc.getCharHeight();
		int df = DMSHelper.getDefaultFontNum(proxy);
		ColorScheme cs = ColorScheme.fromOrdinal(sc.getColorScheme());
		RasterBuilder rb = new RasterBuilder(w, h, cw, ch, df, cs);
		try {
			return rb.createPixmaps(ms);
		}
		catch (InvalidMsgException e) {
			return null;
		}
	}

	/** Update one attribute on the form tab */
	public void updateAttribute(String a) {
		// NOTE: msgCurrent attribute changes after all sign
		//       dimension attributes are updated.
		if (a == null || a.equals("msgCurrent"))
			selectSignText();
	}

	/** Check if the user can write an attribute */
	private boolean canWrite(String aname) {
		return session.canWrite(proxy, aname);
	}
}
