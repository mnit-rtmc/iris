/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2009-2016  Minnesota Department of Transportation
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
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.RasterBuilder;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.SignGroup;
import us.mn.state.dot.tms.SignText;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.IPanel;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.units.Distance;
import static us.mn.state.dot.tms.units.Distance.Units.INCHES;
import static us.mn.state.dot.tms.units.Distance.Units.MILLIMETERS;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * PropMessages is a GUI panel for displaying and editing sign messages on a
 * DMS properties form.
 *
 * @author Douglas Lau
 */
public class PropMessages extends JPanel {

	/** Get tiny distance units to use for display */
	static private Distance.Units distUnitsTiny() {
		return SystemAttrEnum.CLIENT_UNITS_SI.getBoolean()
		     ? Distance.Units.CENTIMETERS : INCHES;
	}

	/** Sign grou panel */
	private final ProxyTablePanel<SignGroup> sign_group_pnl;

	/** Sign text panel */
	private final ProxyTablePanel<SignText> sign_text_pnl;

	/** Preview panel */
	private final JPanel preview_pnl;

	/** Sign pixel panel */
	private final SignPixelPanel pixel_pnl = new SignPixelPanel(40, 400,
		true);

	/** Default font label */
	private final ILabel font_lbl = new ILabel("dms.font.default");

	/** Default font combo box */
	private final JComboBox<Font> font_cbx = new JComboBox<Font>();

	/** Font height label */
	private final ILabel font_height_ilbl = new ILabel("dms.font.height");

	/** Font height label */
	private final JLabel font_height_lbl = IPanel.createValueLabel();

	/** AWS allowed check box */
	private final JCheckBox aws_allowed_chk = new JCheckBox(
		new IAction("dms.aws.allowed")
	{
		protected void doActionPerformed(ActionEvent e) {
			proxy.setAwsAllowed(aws_allowed_chk.isSelected());
		}
	});

	/** AWS controlled check box */
	private final JCheckBox aws_control_chk = new JCheckBox(
		new IAction("item.style.aws.controlled")
	{
		protected void doActionPerformed(ActionEvent e) {
			proxy.setAwsControlled(aws_control_chk.isSelected());
		}
	});

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
		font_cbx.setAction(new IAction("font") {
			protected void doActionPerformed(ActionEvent e) {
				proxy.setDefaultFont(
					(Font) font_cbx.getSelectedItem());
			}
		});
		font_cbx.setModel(new IComboBoxModel<Font>(
			session.getSonarState().getDmsCache().getFontModel()));
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
		gl.linkSize(font_lbl, font_height_ilbl);
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
		GroupLayout.SequentialGroup g1 = gl.createSequentialGroup();
		g1.addComponent(font_lbl);
		g1.addGap(UI.hgap);
		g1.addComponent(font_cbx);
		hg.addGroup(g1);
		GroupLayout.SequentialGroup g2 = gl.createSequentialGroup();
		g2.addComponent(font_height_ilbl);
		g2.addGap(UI.hgap);
		g2.addComponent(font_height_lbl);
		hg.addGroup(g2);
		if (SystemAttrEnum.DMS_AWS_ENABLE.getBoolean()) {
			hg.addComponent(aws_allowed_chk);
			hg.addComponent(aws_control_chk);
		}
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
		GroupLayout.ParallelGroup g1 = gl.createBaselineGroup(false,
			false);
		g1.addComponent(font_lbl);
		g1.addComponent(font_cbx);
		vg.addGroup(g1);
		vg.addGap(UI.vgap);
		GroupLayout.ParallelGroup g2 = gl.createBaselineGroup(false,
			false);
		g2.addComponent(font_height_ilbl);
		g2.addComponent(font_height_lbl);
		vg.addGroup(g2);
		if (SystemAttrEnum.DMS_AWS_ENABLE.getBoolean()) {
			vg.addGap(UI.vgap);
			vg.addComponent(aws_allowed_chk);
			vg.addGap(UI.vgap);
			vg.addComponent(aws_control_chk);
		}
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
		Integer w = proxy.getFaceWidth();
		Integer lh = getLineHeightPixels();
		Integer hp = proxy.getHorizontalPitch();
		Integer vp = proxy.getVerticalPitch();
		Integer hb = proxy.getHorizontalBorder();
		if (w != null && lh != null && hp != null && vp != null &&
		    hb != null)
		{
			int h = lh * vp;
			pixel_pnl.setPhysicalDimensions(w, h, hb, 0, hp, vp);
		}
		Integer wp = proxy.getWidthPixels();
		Integer cw = proxy.getCharWidthPixels();
		if (wp != null && lh != null && cw != null)
			pixel_pnl.setLogicalDimensions(wp, lh, cw, 0);
		pixel_pnl.repaint();
		SignText st = sign_text_pnl.getSelectedProxy();
		if (st != null)
			pixel_pnl.setGraphic(renderMessage(st));
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

	/** Render a message to a raster graphic */
	private RasterGraphic renderMessage(SignText st) {
		MultiString multi = new MultiString(st.getMulti());
		RasterGraphic[] pages = renderPages(multi);
		if (pages != null && pages.length > 0)
			return pages[0];
		else
			return null;
	}

	/** Render the pages of a text message */
	private RasterGraphic[] renderPages(MultiString ms) {
		Integer w = proxy.getWidthPixels();
		Integer h = getLineHeightPixels();
		Integer cw = proxy.getCharWidthPixels();
		Integer ch = proxy.getCharHeightPixels();
		if (w == null || h == null || cw == null || ch == null)
			return null;
		int df = DMSHelper.getDefaultFontNumber(proxy);
		RasterBuilder b = new RasterBuilder(w, h, cw, ch, df);
		try {
			return b.createPixmaps(ms);
		}
		catch (InvalidMsgException e) {
			return null;
		}
	}

	/** Update the edit mode */
	public void updateEditMode() {
		font_cbx.setEnabled(canUpdate("defaultFont"));
		aws_allowed_chk.setEnabled(canUpdate("awsAllowed"));
		aws_control_chk.setEnabled(canUpdate("awsControlled"));
	}

	/** Update one attribute on the form tab */
	public void updateAttribute(String a) {
		if (a == null || a.equals("defaultFont"))
			font_cbx.setSelectedItem(proxy.getDefaultFont());
		if (a == null || a.equals("defaultFont") ||
		    a.equals("verticalPitch"))
			font_height_lbl.setText(calculateFontHeight());
		if (a == null || a.equals("awsAllowed"))
			aws_allowed_chk.setSelected(proxy.getAwsAllowed());
		if (a == null || a.equals("awsControlled"))
			aws_control_chk.setSelected(proxy.getAwsControlled());
		// NOTE: messageCurrent attribute changes after all sign
		//       dimension attributes are updated.
		if (a == null || a.equals("messageCurrent"))
			selectSignText();
	}

	/** Check if the user can update an attribute */
	private boolean canUpdate(String aname) {
		return session.canUpdate(proxy, aname);
	}

	/** Calculate the height of the default font on the sign */
	private String calculateFontHeight() {
		Font f = proxy.getDefaultFont();
		Integer vp = proxy.getVerticalPitch();
		if (f != null && vp != null) {
			int h = f.getHeight();
			if (h > 0 && vp > 0) {
				float mm = (h - 0.5f) * vp;
				Distance fh = new Distance(mm, MILLIMETERS);
				return formatFontHeight(fh);
			}
		}
		return "???";
	}

	/** Format the font height for display */
	private String formatFontHeight(Distance fh) {
		Distance.Formatter df = new Distance.Formatter(1);
		return df.format(fh.convert(distUnitsTiny()));
	}
}
