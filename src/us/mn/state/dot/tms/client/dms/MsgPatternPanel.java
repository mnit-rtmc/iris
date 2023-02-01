/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2022  Minnesota Department of Transportation
 * Copyright (C) 2023       SRF Consulting Group
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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.ColorScheme;
import us.mn.state.dot.tms.Font;
import us.mn.state.dot.tms.FontHelper;
import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.RasterBuilder;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.client.EditModeListener;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IPanel;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * A panel for editing the properties of a message pattern.
 *
 * @author Douglas Lau
 * @author John L. Stanley - SRF Consulting
 */
public class MsgPatternPanel extends IPanel
	implements ProxyView<MsgPattern>
{
	/** MULTI text area */
	private final JTextArea multi_txt = new JTextArea();

	/** Sign pixel panel */
	private final SignPixelPanel pixel_pnl = new SignPixelPanel(100, 180);

	/** Pager for sign pixel panel */
	private DMSPanelPager pager;

	/** User session */
	private final Session session;

	/** Proxy watcher */
	private final ProxyWatcher<MsgPattern> watcher;

	/** Edit mode listener */
	private final EditModeListener edit_lsnr = new EditModeListener() {
		public void editModeChanged() {
			updateEditMode();
		}
	};

	/** Message pattern being edited */
	private MsgPattern msg_pattern;

	/** Set the message pattern */
	public void setMsgPattern(MsgPattern pat) {
		watcher.setProxy(pat);
		updatePixelPanel(pat);
	}

	/** Create the detector panel */
	public MsgPatternPanel(Session s, boolean r) {
		session = s;
		TypeCache<MsgPattern> cache =
			s.getSonarState().getDmsCache().getMsgPatterns();
		watcher = new ProxyWatcher<MsgPattern>(cache, this, false);
	}

	/** Initialize the panel */
	@Override
	public void initialize() {
		super.initialize();
		add("msg.pattern.multi");
		add(multi_txt, Stretch.FULL);
		multi_txt.setWrapStyleWord(false);
		add(createPreviewPanel(), Stretch.FULL);
		createJobs();
		watcher.initialize();
		clear();
		session.addEditModeListener(edit_lsnr);
	}

	/** Create message preview panel */
	private JPanel createPreviewPanel() {
		JPanel pnl = new JPanel(new BorderLayout());
		pnl.setBorder(BorderFactory.createTitledBorder(
			I18N.get("dms.message.preview")));
		pnl.add(pixel_pnl, BorderLayout.CENTER);
		return pnl;
	}

	/** Create the jobs */
	private void createJobs() {
		multi_txt.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				setMulti(multi_txt.getText());
			}
		});
	}

	/** Set the MULTI string */
	private void setMulti(String m) {
		MsgPattern pat = msg_pattern;
		if (pat != null) {
			MultiString ms = new MultiString(m).normalize();
			pat.setMulti(ms.toString());
		}
		updatePixelPanel(pat);
	}

	/** Update pixel panel preview */
	private void updatePixelPanel(MsgPattern pat) {
		SignConfig sc = DMSDispatcher.getSelectedSignConfig(pat);
		if (sc != null) {
			updatePixelPanel(sc, new MultiString(
				pat.getMulti()));
			return;
		}
		pixel_pnl.setPhysicalDimensions(0, 0, 0, 0, 0, 0);
		pixel_pnl.setLogicalDimensions(0, 0, 0, 0);
		pixel_pnl.setGraphic(null);
		pixel_pnl.repaint();
	}

	/** Update pixel panel preview */
	private void updatePixelPanel(SignConfig sc, MultiString multi) {
		pixel_pnl.setDimensions(sc);
		int pw = sc.getPixelWidth();
		int ph = sc.getPixelHeight();
		int cw = sc.getCharWidth();
		int ch = sc.getCharHeight();
		Font f = sc.getDefaultFont();
		int df = (f != null)
		       ? f.getNumber()
		       : FontHelper.DEFAULT_FONT_NUM;
		ColorScheme cs = ColorScheme.fromOrdinal(sc.getColorScheme());
		RasterBuilder rb = new RasterBuilder(pw, ph, cw, ch, df, cs);
		try {
			RasterGraphic[] rg = rb.createPixmaps(multi);
			if (rg != null) {
				String ms = multi.toString();
				setPager(new DMSPanelPager(pixel_pnl, rg, ms));
				return;
			}
		}
		catch (InvalidMsgException e) { /* fall through */ }
		setPager(null);
		pixel_pnl.setGraphic(null);
	}

	/** Set the DMS panel pager */
	private void setPager(DMSPanelPager p) {
		DMSPanelPager op = pager;
		if (op != null)
			op.dispose();
		pager = p;
	}

	/** Dispose of the panel */
	@Override
	public void dispose() {
		clear();
		session.removeEditModeListener(edit_lsnr);
		watcher.dispose();
		super.dispose();
	}

	/** Update the edit mode */
	public void updateEditMode() {
		MsgPattern pat = msg_pattern;
		multi_txt.setEnabled(session.canWrite(pat, "multi"));
	}

	/** Called when all proxies have been enumerated (from ProxyView). */
	@Override
	public void enumerationComplete() { }

	/** Update one attribute (from ProxyView). */
	@Override
	public void update(MsgPattern pat, String a) {
		if (null == a) {
			msg_pattern = pat;
			updateEditMode();
		}
		if (null == a || a.equals("multi"))
			multi_txt.setText(pat.getMulti());
	}

	/** Clear all attributes (from ProxyView). */
	@Override
	public void clear() {
		setPager(null);
		msg_pattern = null;
		multi_txt.setEnabled(false);
		multi_txt.setText("");
	}
}
