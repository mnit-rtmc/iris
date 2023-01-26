/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2023  Minnesota Department of Transportation
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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgPatternHelper;
import us.mn.state.dot.tms.MsgLine;
import us.mn.state.dot.tms.RasterBuilder;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignConfigHelper;
import us.mn.state.dot.tms.client.EditModeListener;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * A panel for editing the properties of a message pattern.
 *
 * @author Douglas Lau
 */
public class MsgPatternPanel extends JPanel {

	/** MULTI label */
	private final ILabel multi_lbl = new ILabel("msg.pattern.multi");

	/** MULTI text area */
	private final JTextArea multi_txt = new JTextArea(5, 40);

	/** Sign config list */
	private final JList<SignConfig> config_lst = new JList<SignConfig>();

	/** Sign config scroll pane */
	private final JScrollPane config_pnl;

	/** Sign pixel panel */
	private final SignPixelPanel pixel_pnl = new SignPixelPanel(200, 100);

	/** Message preview panel */
	private final JPanel preview_pnl;

	/** Pager for sign pixel panel */
	private DMSPanelPager pager;

	/** Msg line panel */
	private final ProxyTablePanel<MsgLine> msg_line_pnl;

	/** User session */
	private final Session session;

	/** Proxy watcher */
	private final ProxyWatcher<MsgPattern> watcher;

	/** Proxy view */
	private final ProxyView<MsgPattern> view = new ProxyView<MsgPattern>() {
		@Override public void enumerationComplete() { }

		@Override public void update(MsgPattern pat, String a) {
			if (null == a) {
				msg_pattern = pat;
				msg_line_pnl.setModel(new MsgLineTableModel(
					session, pat));
				updateEditMode();
			}
			if (null == a || a.equals("multi"))
				multi_txt.setText(pat.getMulti());
			updatePixelPanel(pat);
		}

		@Override public void clear() {
			msg_pattern = null;
			multi_txt.setEnabled(false);
			multi_txt.setText("");
			pixel_pnl.setPhysicalDimensions(0, 0, 0, 0, 0, 0);
			pixel_pnl.setLogicalDimensions(0, 0, 0, 0);
			pixel_pnl.setGraphic(null);
			setPager(null);
		}
	};

	/** Edit mode listener */
	private final EditModeListener edit_lsnr = new EditModeListener() {
		public void editModeChanged() {
			updateEditMode();
		}
	};

	/** Update the edit mode */
	private void updateEditMode() {
		MsgPattern pat = msg_pattern;
		multi_txt.setEnabled(session.canWrite(pat, "multi"));
	}

	/** Message pattern being edited */
	private MsgPattern msg_pattern;

	/** Set the message pattern */
	public void setMsgPattern(MsgPattern pat) {
		watcher.setProxy(pat);
	}

	/** Create the message pattern panel */
	public MsgPatternPanel(Session s) {
		session = s;
		TypeCache<MsgPattern> cache =
			s.getSonarState().getDmsCache().getMsgPatterns();
		watcher = new ProxyWatcher<MsgPattern>(cache, view, false);
		config_pnl = new JScrollPane(config_lst);
		config_pnl.setMaximumSize(UI.dimension(112, 118));
		preview_pnl = createPreviewPanel();
		msg_line_pnl = new ProxyTablePanel<MsgLine>(
			new MsgLineTableModel(s, null)
		);
	}

	/** Create message preview panel */
	private JPanel createPreviewPanel() {
		JPanel pnl = new JPanel(new BorderLayout());
		pnl.setBorder(BorderFactory.createTitledBorder(
			I18N.get("dms.message.preview")));
		pnl.add(pixel_pnl, BorderLayout.CENTER);
		return pnl;
	}

	/** Initialize the panel */
	public void initialize() {
		setBorder(UI.border);
		multi_txt.setLineWrap(true);
		multi_txt.setWrapStyleWord(false);
		config_lst.addListSelectionListener(new IListSelectionAdapter() {
			@Override public void valueChanged() {
				selectSignCfg();
			}
		});
		pixel_pnl.setFilterColor(new Color(0, 0, 255, 48));
		msg_line_pnl.initialize();
		layoutPanel();
		watcher.initialize();
		createJobs();
		session.addEditModeListener(edit_lsnr);
		view.clear();
	}

	/** Layout the panel */
	private void layoutPanel() {
		GroupLayout gl = new GroupLayout(this);
		gl.setHonorsVisibility(false);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		// horizontal layout
		GroupLayout.ParallelGroup hg = gl.createParallelGroup();
		hg.addGroup(gl.createSequentialGroup()
		              .addComponent(multi_lbl)
		              .addGap(UI.hgap)
		              .addComponent(multi_txt))
		  .addGroup(gl.createSequentialGroup()
		              .addComponent(config_pnl)
		              .addGap(UI.hgap)
		              .addComponent(preview_pnl))
		  .addComponent(msg_line_pnl);
		gl.setHorizontalGroup(hg);
		// vertical layout
		GroupLayout.SequentialGroup vg = gl.createSequentialGroup();
		vg.addGroup(gl.createParallelGroup()
		              .addComponent(multi_lbl)
		              .addComponent(multi_txt))
		  .addGap(UI.vgap)
		  .addGroup(gl.createParallelGroup()
		              .addComponent(config_pnl)
		              .addComponent(preview_pnl))
		  .addGap(UI.vgap)
		  .addComponent(msg_line_pnl);
		gl.setVerticalGroup(vg);
		setLayout(gl);
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
	private void setMulti(String ms) {
		MsgPattern pat = msg_pattern;
		if (pat != null) {
			MultiString multi = new MultiString(ms).normalize();
			pat.setMulti(multi.toString());
		}
	}

	/** Update pixel panel preview */
	private void updatePixelPanel(MsgPattern pat) {
		List<SignConfig> cfgs = MsgPatternHelper.findSignConfigs(pat);
		DefaultListModel<SignConfig> mdl =
			new DefaultListModel<SignConfig>();
		for (SignConfig cfg: cfgs)
			mdl.addElement(cfg);
		config_lst.setModel(mdl);
		config_lst.setSelectedIndex(0);
	}

	/** Select a sign config */
	private void selectSignCfg() {
		MsgPattern pat = msg_pattern;
		String ms = (pat != null) ? pat.getMulti() : "";
		SignConfig sc = config_lst.getSelectedValue();
		if (sc != null)
			pixel_pnl.setDimensions(sc);
		else {
			pixel_pnl.setPhysicalDimensions(0, 0, 0, 0, 0, 0);
			pixel_pnl.setLogicalDimensions(0, 0, 0, 0);
		}
		pixel_pnl.setGraphic(null);
		setPager(createPager(sc, ms));
	}

	/** Create pixel panel pager */
	private DMSPanelPager createPager(SignConfig sc, String ms) {
		RasterBuilder rb = SignConfigHelper.createRasterBuilder(sc);
		return (rb != null) ? createPager(rb, ms) : null;
	}

	/** Create pixel panel pager */
	private DMSPanelPager createPager(RasterBuilder rb, String ms) {
		RasterGraphic[] rg = rb.createRasters(ms);
		return (rg != null)
		      ? new DMSPanelPager(pixel_pnl, rg, ms)
		      : null;
	}

	/** Set the DMS panel pager */
	private void setPager(DMSPanelPager p) {
		DMSPanelPager op = pager;
		if (op != null)
			op.dispose();
		pixel_pnl.repaint();
		pager = p;
	}

	/** Dispose of the panel */
	public void dispose() {
		msg_line_pnl.dispose();
		watcher.dispose();
		session.removeEditModeListener(edit_lsnr);
		view.clear();
	}
}
