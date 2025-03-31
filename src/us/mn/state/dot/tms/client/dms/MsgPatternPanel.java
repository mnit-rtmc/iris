/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2025  Minnesota Department of Transportation
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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.MsgPattern;
import us.mn.state.dot.tms.MsgPatternHelper;
import us.mn.state.dot.tms.MsgLine;
import us.mn.state.dot.tms.RasterBuilder;
import us.mn.state.dot.tms.SignConfig;
import us.mn.state.dot.tms.SignConfigHelper;
import us.mn.state.dot.tms.WordHelper;
import us.mn.state.dot.tms.client.EditModeListener;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiString;
import us.mn.state.dot.tms.utils.TextRect;

/**
 * A panel for editing the properties of a message pattern.
 *
 * @author Douglas Lau
 */
public class MsgPatternPanel extends JPanel {

	/** Preview filter color */
	static private final Color PREVIEW_CLR = new Color(0, 0, 255, 48);

	/** MULTI label */
	private final ILabel multi_lbl = new ILabel("msg.pattern.multi");

	/** MULTI text area */
	private final JTextArea multi_txt = new JTextArea(5, 40);

	/** Flash beacon label */
	private final ILabel beacon_lbl = new ILabel("dms.flash.beacon");

	/** Checkbox for flash beacon flag */
	private final JCheckBox beacon_chk = new JCheckBox(new IAction(null) {
		protected void doActionPerformed(ActionEvent e) {
			MsgPattern pat = msg_pattern;
			if (pat != null)
				pat.setFlashBeacon(beacon_chk.isSelected());
		}
	});

	/** Pixel service label */
	private final ILabel pix_srv_lbl = new ILabel("dms.pixel.service");

	/** Checkbox for pixel service flag */
	private final JCheckBox pix_srv_chk = new JCheckBox(new IAction(null) {
		protected void doActionPerformed(ActionEvent e) {
			MsgPattern pat = msg_pattern;
			if (pat != null)
				pat.setPixelService(pix_srv_chk.isSelected());
		}
	});

	/** Sign config list */
	private final JList<SignConfig> config_lst = new JList<SignConfig>();

	/** Sign config scroll pane */
	private final JScrollPane config_pnl;

	/** Sign pixel panel */
	private final SignPixelPanel pixel_pnl = new SignPixelPanel(200, 100);

	/** Message preview panel */
	private final JPanel preview_pnl;

	/** Pager for sign pixel panel */
	private SignPixelPager pager;

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
			if (null == a)
				updateMsgPattern(pat);
			if (null == a || a.equals("flashBeacon"))
				beacon_chk.setSelected(pat.getFlashBeacon());
			if (null == a || a.equals("pixelService"))
				pix_srv_chk.setSelected(pat.getPixelService());
			if (null == a || a.equals("multi")) {
				multi_txt.setText(pat.getMulti());
				updatePixelPnl();
			}
		}

		@Override public void clear() {
			msg_pattern = null;
			multi_txt.setEnabled(false);
			multi_txt.setText("");
			beacon_chk.setEnabled(false);
			beacon_chk.setSelected(false);
			pix_srv_chk.setEnabled(false);
			pix_srv_chk.setSelected(false);
			setPager(null);
			pixel_pnl.setPhysicalDimensions(0, 0, 0, 0, 0, 0);
			pixel_pnl.setLogicalDimensions(0, 0, 0, 0);
			pixel_pnl.setGraphic(null);
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
		beacon_chk.setEnabled(session.canWrite(pat, "flashBeacon"));
		pix_srv_chk.setEnabled(session.canWrite(pat, "pixelService"));
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
		) {
			@Override protected void selectProxy() {
				super.selectProxy();
				updatePixelPnl();
			}
		};
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
				updatePixelPnl();
			}
		});
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
		              .addComponent(beacon_lbl)
		              .addGap(UI.hgap)
		              .addComponent(beacon_chk))
		  .addGroup(gl.createSequentialGroup()
		              .addComponent(pix_srv_lbl)
		              .addGap(UI.hgap)
		              .addComponent(pix_srv_chk))
		  .addGroup(gl.createSequentialGroup()
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
		              .addComponent(beacon_lbl)
		              .addComponent(beacon_chk))
		  .addGap(UI.vgap)
		  .addGroup(gl.createParallelGroup()
		              .addComponent(pix_srv_lbl)
		              .addComponent(pix_srv_chk))
		  .addGap(UI.vgap)
		  .addGroup(gl.createParallelGroup()
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

	/** Update the message pattern */
	private void updateMsgPattern(MsgPattern pat) {
		msg_pattern = pat;
		msg_line_pnl.setModel(new MsgLineTableModel(session, pat) {
			@Override protected void proxyUpdate(MsgLine ml) {
				if (ml.getName().equals(selected)) {
					msg_line_pnl.selectProxy(ml);
					selected = null;
				}
			}
		});
		Set<SignConfig> cfgs = MsgPatternHelper.findSignConfigs(pat);
		DefaultListModel<SignConfig> mdl =
			new DefaultListModel<SignConfig>();
		for (SignConfig cfg: cfgs)
			mdl.addElement(cfg);
		config_lst.setModel(mdl);
		config_lst.setSelectedIndex(0);
		updateEditMode();
	}

	/** Get the selected sign configuration */
	public SignConfig getSelectedSignConfig() {
		return config_lst.getSelectedValue();
	}

	/** Update the pixel panel */
	private void updatePixelPnl() {
		setPager(null);
		SignConfig sc = config_lst.getSelectedValue();
		if (sc != null) {
			pixel_pnl.setDimensions(sc);
			String ms = getPreviewMulti(sc);
			setPager(createPager(sc, ms));
		} else {
			pixel_pnl.setPhysicalDimensions(0, 0, 0, 0, 0, 0);
			pixel_pnl.setLogicalDimensions(0, 0, 0, 0);
			pixel_pnl.setGraphic(null);
		}
	}

	/** Get preview MULTI string */
	private String getPreviewMulti(SignConfig sc) {
		assert sc != null;
		TextRect tr = SignConfigHelper.textRect(sc);
		MsgPattern pat = msg_pattern;
		String ms = (pat != null) ? pat.getMulti() : "";
		ArrayList<String> lines = new ArrayList<String>();
		MsgLine ml = msg_line_pnl.getSelectedProxy();
		if (ml != null) {
			int line = ml.getLine();
			// find the text rectangle containing the line
			int i = 0;
			TextRect rect = null;
			for (TextRect r : tr.find(ms)) {
				i += r.getLineCount();
				if (i >= line) {
					rect = r;
					break;
				}
			}
			String ln = ml.getMulti();
			if (rect != null) {
				for (int j = 0; j < 20 && ln != null; j++) {
					int w = rect.calculateWidth(ln);
					if (w <= rect.width)
						break;
					ln = WordHelper.abbreviate(ln);
				}
			}
			while (lines.size() + 1 < line)
				lines.add("");
			lines.add((ln != null) ? ln : ml.getMulti());
		}
		MultiString multi = new MultiString(tr.fill(ms, lines));
		return multi.stripTrailingWhitespaceTags().toString();
	}

	/** Create pixel panel pager */
	private SignPixelPager createPager(SignConfig sc, String ms) {
		RasterBuilder rb = SignConfigHelper.createRasterBuilder(sc);
		return (rb != null)
		      ? new SignPixelPager(pixel_pnl, rb, ms, PREVIEW_CLR)
		      : null;
	}

	/** Set the DMS panel pager */
	private void setPager(SignPixelPager p) {
		SignPixelPager op = pager;
		if (op != null)
			op.dispose();
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
