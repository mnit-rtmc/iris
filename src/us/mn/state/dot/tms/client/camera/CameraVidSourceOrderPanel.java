/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2017-2018  Minnesota Department of Transportation
 * Copyright (C) 2020 SRF Consulting Group
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
package us.mn.state.dot.tms.client.camera;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CameraTemplate;
import us.mn.state.dot.tms.CameraVidSourceOrder;
import us.mn.state.dot.tms.CameraVidSourceOrderHelper;
import us.mn.state.dot.tms.VidSourceTemplate;
import us.mn.state.dot.tms.VidSourceTemplateHelper;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyView;
import us.mn.state.dot.tms.client.proxy.ProxyWatcher;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.IListCellRenderer;
import us.mn.state.dot.tms.client.widget.IListSelectionAdapter;
import us.mn.state.dot.tms.client.widget.Icons;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;
import us.mn.state.dot.tms.utils.I18N;

/**
 * CameraVidSourceOrder is a UI for editing the video source templates
 * assigned to camera templates and their respective priority
 *
 * @author Douglas Lau
 * @author Michael Janson
 */
@SuppressWarnings("serial")
public class CameraVidSourceOrderPanel extends JPanel
		implements ProxyView<CameraVidSourceOrder>{

	/** Parse an integer */
	static private Integer parseInt(String t) {
		try {
			return Integer.parseInt(t);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	/** User Session */
	private final Session session;
	
	/** Cache of CameraVidSourceOrder objects */
	private final TypeCache<CameraVidSourceOrder> cache;

	/** Proxy watcher */
	private final ProxyWatcher<CameraVidSourceOrder> watcher;

	/** Camera template */
	private final CameraTemplate camera_template;
	
	private List<CameraVidSourceOrder> cam_vid_src;
	
	/** Camera template video source list model */
	private final DefaultListModel<VidSourceTemplate> cam_vid_src_mdl =
		new DefaultListModel<VidSourceTemplate>();

	/** Camera template video source list */
	private final JList<VidSourceTemplate> cam_vid_src_lst =
			new JList<VidSourceTemplate>(cam_vid_src_mdl);

	/** Camera template video source scroll pane */
	private final JScrollPane cam_vid_src_scrl =
			new JScrollPane(cam_vid_src_lst);
	
	/** Available video source list model */
	private final DefaultListModel<VidSourceTemplate> vid_src_mdl =
		new DefaultListModel<VidSourceTemplate>();

	/** Available video source list */
	private final JList<VidSourceTemplate> vid_src_lst =
			new JList<VidSourceTemplate>(vid_src_mdl);

	/** Available template video source scroll pane */
	private final JScrollPane vid_src_scrl = new JScrollPane(vid_src_lst);

	/** Insert video source button */
	private final JButton insert_btn = new JButton(
			new IAction("camera.template.source.add")
	{
		protected void doActionPerformed(ActionEvent e) {
			insertVideoSource();
		}
	});
	
	/** Video source info */
	private final JPanel vid_src_info;
	
	/** Camera video source label */
	private final ILabel cam_vid_src_lbl =
			new ILabel("camera.template.sources");
	
	/** Available video source label */
	private final ILabel avail_vid_src_lbl =
			new ILabel("camera.template.available_sources"); 
	
	/** Video source info */
	private final ILabel vid_src_info_lbl =
			new ILabel("camera.template.source.info");

	/** Insert a video source */
	private void insertVideoSource() {
		VidSourceTemplate vst = vid_src_lst.getSelectedValue();
		if (vst != null) {
			cam_vid_src_mdl.addElement(vst);
			cam_vid_src_lst.setSelectedIndex(cam_vid_src_mdl.size() - 1);
			String src_name = vst.getName();
			int src_order = cam_vid_src_mdl.size() - 1;
			String n = CameraVidSourceOrderHelper.getFirstAvailableName(
					camera_template.getName());
			cache.createObject(n);
			CameraVidSourceOrder cvo = cache.lookupObjectWait(n);
			
			// if we don't get the object it's probably because of a DB error
			// which will pop up by itself (so doing nothing is fine)
			if (cvo != null) {
				cvo.setCameraTemplate(camera_template.getName());
				cvo.setVidSourceTemplate(src_name);
				cvo.setSourceOrder(src_order);
				cam_vid_src.add(cvo);
			}
		}
	}
	
	/** Remove video source button */
	private final JButton remove_btn = new JButton(
			new IAction("camera.template.source.remove")
	{
		protected void doActionPerformed(ActionEvent e) {
			int s = cam_vid_src_lst.getSelectedIndex();
			VidSourceTemplate vst = cam_vid_src_lst.getSelectedValue();
			if (s >= 0) {
				cam_vid_src_mdl.remove(s);
				CameraVidSourceOrder cvo = cam_vid_src.remove(s);
				cvo.destroy();
				
				int i = s < cam_vid_src.size() ? s : cam_vid_src_mdl.size()-1;
				cam_vid_src_lst.setSelectedIndex(i);
			}
		}
	});
	
	/** Up button */
	private final JButton up_btn = new JButton(
			new IAction("camera.template.source.up")
	{
		protected void doActionPerformed(ActionEvent e) {
			moveVidSrcUp();
		}
	});

	/** Move selected camera up */
	private void moveVidSrcUp() {
		int s = cam_vid_src_lst.getSelectedIndex();
		if (s > 0) {
			VidSourceTemplate vst0 = cam_vid_src_mdl.get(s - 1);
			VidSourceTemplate vst1 = cam_vid_src_mdl.get(s);
			cam_vid_src_mdl.set(s - 1, vst1);
			cam_vid_src_mdl.set(s, vst0);
			cam_vid_src_lst.setSelectedIndex(s - 1);
			CameraVidSourceOrder cmvo1 = cam_vid_src.get(s-1);
			CameraVidSourceOrder cmvo2 = cam_vid_src.get(s);
			cmvo1.setSourceOrder(s);
			cmvo2.setSourceOrder(s-1);
			cam_vid_src.set(s, cmvo1);
			cam_vid_src.set(s-1, cmvo2);
		}
	}

	/** Down action */
	private final JButton down_btn = new JButton(
			new IAction("camera.template.source.down")
	{
		protected void doActionPerformed(ActionEvent e) {
			moveCameraDown();
		}
	});

	/** Move selected camera down */
	private void moveCameraDown() {
		int s = cam_vid_src_lst.getSelectedIndex();
		if (s >= 0 && s < cam_vid_src_mdl.size() - 1) {
			VidSourceTemplate vst0 = cam_vid_src_mdl.get(s);
			VidSourceTemplate vst1 = cam_vid_src_mdl.get(s + 1);
			cam_vid_src_mdl.set(s, vst1);
			cam_vid_src_mdl.set(s + 1, vst0);
			cam_vid_src_lst.setSelectedIndex(s + 1);
			CameraVidSourceOrder cmvo1 = cam_vid_src.get(s);
			CameraVidSourceOrder cmvo2 = cam_vid_src.get(s+1);
			cmvo1.setSourceOrder(s+1);
			cmvo2.setSourceOrder(s);
			cam_vid_src.set(s+1, cmvo1);
			cam_vid_src.set(s, cmvo2);
		}
	}
	
	/** Video Source Fields */
	
	/** Name (Label) Label and Field */
	private final JLabel vsNameLbl = new JLabel(I18N.get(
			"camera.video_source.template.name") + ":");
	private final JLabel vsNameField = new JLabel();
	
	/** Codec Label and Field */
	private final JLabel vsCodecLbl = new JLabel(I18N.get(
			"camera.video_source.template.codec") + ":");
	private final JLabel vsCodecField = new JLabel();

	/** Encoder (Type) Label and Field */
	private final JLabel vsEncoderLbl = new JLabel(I18N.get(
			"camera.video_source.template.encoder") + ":");
	private final JLabel vsEncoderField = new JLabel();

	/** Scheme Label and Field */
	private final JLabel vsSchemeLbl = new JLabel(I18N.get(
			"camera.video_source.template.scheme") + ":");
	private final JLabel vsSchemeField = new JLabel();

	/** Default Port Label and Field */
	private final JLabel vsLatencyLbl = new JLabel(I18N.get(
			"camera.video_source.template.latency") + ":");
	private final JLabel vsLatencyField = new JLabel();

	/** Default Port Label and Field */
	private final JLabel vsDefPortLbl = new JLabel(I18N.get(
			"camera.video_source.template.default_port") + ":");
	private final JLabel vsDefPortField = new JLabel();

	/** Resolution Width Label and Field */
	private final JLabel vsRezWidthLbl = new JLabel(I18N.get(
			"camera.video_source.template.rez_width") + ":");
	private final JLabel vsRezWidthField = new JLabel();

	/** Resolution Height Label and Field */
	private final JLabel vsRezHeightLbl = new JLabel(I18N.get(
			"camera.video_source.template.rez_height") + ":");
	private final JLabel vsRezHeightField = new JLabel();

	/** Subnets Label and Field */
	private final JLabel vsSubnetsLbl = new JLabel(I18N.get(
			"camera.video_source.template.subnets") + ":");
	private final JTextArea vsSubnetsField = new JTextArea(1, 52);

	/** (GStreamer) Configuration Label and Field */
	private final JLabel vsConfigLbl = new JLabel("<html>" + I18N.get(
			"camera.video_source.template.config") + ":<br>&nbsp;</html>");
	private final JTextArea vsConfigField = new JTextArea(2, 50);

	/** Notes Label and Field */
	private final JLabel vsNotesLbl = new JLabel("<html>" + I18N.get(
			"camera.video_source.template.notes") + ":<br><br>&nbsp;</html>");
	private final JTextArea vsNotesField = new JTextArea(3, 54);

	/** Separator size between fields on the same line */
	private final static int hGap = 10;
	
	
	/** Display information for video source */
	private void displayVidSrcInfo(VidSourceTemplate vst) {
		if (vst != null) {
			vsNameField.setText(checkNull(vst.getLabel()));
			vsCodecField.setText(checkNull(vst.getCodec()));
			vsEncoderField.setText(checkNull(vst.getEncoder()));
			vsSchemeField.setText(checkNull(vst.getScheme()));
			vsLatencyField.setText(checkNull(vst.getLatency()));
			vsDefPortField.setText(checkNull(vst.getDefaultPort()));
			vsRezWidthField.setText(checkNull(vst.getRezHeight()));
			vsRezHeightField.setText(checkNull(vst.getRezWidth()));
			vsSubnetsField.setText(checkNull(vst.getSubnets()));
			vsConfigField.setText(checkNull(vst.getConfig()));
			vsNotesField.setText(checkNull(vst.getNotes()));
		} else {
			vsNameField.setText("");
			vsCodecField.setText("");
			vsEncoderField.setText("");
			vsSchemeField.setText("");
			vsLatencyField.setText("");
			vsDefPortField.setText("");
			vsRezWidthField.setText("");
			vsRezHeightField.setText("");
			vsSubnetsField.setText("");
			vsConfigField.setText("");
			vsNotesField.setText("");
		}
	}
	
	/** Return blank string if field is null */
	private static String checkNull(String s) {
		return s != null ? s : "";
	}
	
	/** Return blank string if field is null */
	private static String checkNull(Integer i) {
		return i != null ? i.toString() : "";
	}
	
	/** Create a new play list panel */
	public CameraVidSourceOrderPanel(Session s, CameraTemplate ct) {
		session = s;
		camera_template = ct;
		cache = s.getSonarState().getCamVidSrcOrder();
		watcher = new ProxyWatcher<CameraVidSourceOrder>(cache, this, false);
		cam_vid_src = VidStreamReq.getCamVidSrcOrder(camera_template);
		vid_src_info = new JPanel();
	}

	/** Initialize the widgets */
	public void initialize() {
		setBorder(UI.border);
		cam_vid_src_lst.setVisibleRowCount(12);
		vid_src_lst.setVisibleRowCount(12);
		cam_vid_src_lst.setCellRenderer(new LabelRenderer());
		vid_src_lst.setCellRenderer(new LabelRenderer());
		cam_vid_src_scrl.setPreferredSize(new Dimension(300,200));
		vid_src_scrl.setPreferredSize(new Dimension(300,200));
		vid_src_info.setLayout(new BoxLayout(vid_src_info, BoxLayout.Y_AXIS));
		vid_src_info.setPreferredSize(new Dimension(650,200));
		initVstFields();
		ImageIcon insert_icon = Icons.getIconByPropName(
				"camera.template.source.add");
		insert_btn.setIcon(insert_icon);
		insert_btn.setHideActionText(true);
		insert_btn.setToolTipText(I18N.get("camera.template.source.add"));
		remove_btn.setText("X");
		remove_btn.setFont(
				new java.awt.Font("Arial", java.awt.Font.PLAIN, 18));
		remove_btn.setToolTipText(I18N.get("camera.template.source.remove"));
		
		ImageIcon up_icon = Icons.getIconByPropName(
				"camera.template.source.up");
		up_btn.setIcon(up_icon);
		up_btn.setHideActionText(true);
		ImageIcon down_icon = Icons.getIconByPropName(
				"camera.template.source.down");
		down_btn.setIcon(down_icon);
		down_btn.setHideActionText(true);
		
		layoutPanel();
		initializeVidSrc();
		createJobs();
		watcher.initialize();
	}
	
	private class LabelRenderer extends IListCellRenderer<VidSourceTemplate> {
		/** Return the label of a VidSourceTemplate */
		@Override
		protected String valueToString(VidSourceTemplate vst) {
			return vst.getLabel();
		}
	}

	/** Initialize the video source template fields (sets various options). */
	private void initVstFields() {
		// we're using text areas for the long fields - disable editing, change
		// color, and allow wrapping to make them useful
		vsSubnetsField.setEditable(false);
		vsSubnetsField.setBackground(UIManager.getColor("Panel.background"));
		vsSubnetsField.setLineWrap(true);
		vsSubnetsField.setWrapStyleWord(true);
		
		vsConfigField.setEditable(false);
		vsConfigField.setBackground(UIManager.getColor("Panel.background"));
		vsConfigField.setLineWrap(true);
		vsConfigField.setWrapStyleWord(true);
		
		vsNotesField.setEditable(false);
		vsNotesField.setBackground(UIManager.getColor("Panel.background"));
		vsNotesField.setLineWrap(true);
		vsNotesField.setWrapStyleWord(true);
		
		// change all other fields to use non-bold text
		Font f = vsNameField.getFont();
		Font fnb = f.deriveFont(f.getStyle() & ~Font.BOLD);
		vsNameField.setFont(fnb);
		vsCodecField.setFont(fnb);
		vsEncoderField.setFont(fnb);
		vsSchemeField.setFont(fnb);
		vsLatencyField.setFont(fnb);
		vsDefPortField.setFont(fnb);
		vsRezWidthField.setFont(fnb);
		vsRezHeightField.setFont(fnb);
		
		// set size for the first 2 rows of fields
		vsNameField.setText(" ");
		Dimension d = vsNameField.getPreferredSize();
		vsNameField.setPreferredSize(new Dimension(120, d.height));
		vsCodecField.setPreferredSize(new Dimension(80, d.height));
		vsEncoderField.setPreferredSize(new Dimension(80, d.height));
		vsSchemeField.setPreferredSize(new Dimension(60, d.height));
		vsLatencyField.setPreferredSize(new Dimension(60, d.height));
		vsDefPortField.setPreferredSize(new Dimension(40, d.height));
		vsRezWidthField.setPreferredSize(new Dimension(40, d.height));
		vsRezHeightField.setPreferredSize(new Dimension(40, d.height));
	}
	
	/** Layout the video source template fields on the panel. */
	private void layoutVstFields() {
		// add fields to the panel (in rows of panels)
		JPanel fRow1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fRow1.setAlignmentY(TOP_ALIGNMENT);
		fRow1.add(vsNameLbl);
		fRow1.add(vsNameField);
		fRow1.add(Box.createHorizontalStrut(hGap));
		
		fRow1.add(vsCodecLbl);
		fRow1.add(vsCodecField);
		fRow1.add(Box.createHorizontalStrut(hGap));
		
		fRow1.add(vsEncoderLbl);
		fRow1.add(vsEncoderField);
		fRow1.add(Box.createHorizontalStrut(3*hGap));
		
		fRow1.add(vsSchemeLbl);
		fRow1.add(vsSchemeField);
		
		JPanel fRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fRow2.setAlignmentY(TOP_ALIGNMENT);
		fRow2.add(vsLatencyLbl);
		fRow2.add(vsLatencyField);
		fRow2.add(Box.createHorizontalStrut(hGap));
		
		fRow2.add(vsDefPortLbl);
		fRow2.add(vsDefPortField);
		fRow2.add(Box.createHorizontalStrut(hGap));
		
		fRow2.add(vsRezWidthLbl);
		fRow2.add(vsRezWidthField);
		fRow2.add(Box.createHorizontalStrut(hGap));
		
		fRow2.add(vsRezHeightLbl);
		fRow2.add(vsRezHeightField);
		
		JPanel fRow3 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fRow3.setAlignmentY(TOP_ALIGNMENT);
		fRow3.add(vsSubnetsLbl);
		fRow3.add(vsSubnetsField);
		
		JPanel fRow4 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fRow4.setAlignmentY(TOP_ALIGNMENT);
		fRow4.add(vsConfigLbl);
		fRow4.add(vsConfigField);
		
		JPanel fRow5 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fRow5.setAlignmentY(TOP_ALIGNMENT);
		fRow5.add(vsNotesLbl);
		fRow5.add(vsNotesField);
		
		// add the rows of fields to the panel
		vid_src_info.add(fRow1);
		vid_src_info.add(fRow2);
		vid_src_info.add(fRow3);
		vid_src_info.add(fRow4);
		vid_src_info.add(fRow5);
	}
	
	/** Layout the panel */
	private void layoutPanel() {
		layoutVstFields();
		GroupLayout gl = new GroupLayout(this);
		gl.setHonorsVisibility(false);
		gl.setAutoCreateGaps(false);
		gl.setAutoCreateContainerGaps(false);
		gl.setHorizontalGroup(createHorizontalGroup(gl));
		gl.setVerticalGroup(createVerticalGroup(gl));
		gl.linkSize(insert_btn, remove_btn, up_btn, down_btn);
		setLayout(gl);
	}

	/** Create the horizontal group */
	private GroupLayout.Group createHorizontalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup hg = gl.createSequentialGroup();
		
		GroupLayout.ParallelGroup p1 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		p1.addComponent(cam_vid_src_lbl);
		p1.addComponent(cam_vid_src_scrl);
		gl.linkSize(SwingConstants.HORIZONTAL, cam_vid_src_scrl);
		hg.addGroup(p1);
		
		hg.addGap(UI.hgap);

		GroupLayout.ParallelGroup p2 = gl.createParallelGroup(
				GroupLayout.Alignment.TRAILING);
		p2.addComponent(up_btn);
		p2.addComponent(insert_btn);
		p2.addComponent(remove_btn);
		p2.addComponent(down_btn);
		hg.addGroup(p2);
		
		hg.addGap(UI.hgap);

		GroupLayout.ParallelGroup p3 = gl.createParallelGroup(
				GroupLayout.Alignment.CENTER);
		p3.addComponent(avail_vid_src_lbl);
		p3.addComponent(vid_src_scrl);
		gl.linkSize(SwingConstants.HORIZONTAL, vid_src_scrl);
		hg.addGroup(p3);
		
		GroupLayout.ParallelGroup pg = gl.createParallelGroup(
				GroupLayout.Alignment.LEADING);
		pg.addComponent(vid_src_info_lbl);
		pg.addComponent(vid_src_info);
		pg.addGroup(hg);
		
		return pg;
	}

	/** Create the vertical group */
	private GroupLayout.Group createVerticalGroup(GroupLayout gl) {
		GroupLayout.SequentialGroup vg = gl.createSequentialGroup();
		
		GroupLayout.ParallelGroup p0 = gl.createParallelGroup(
				GroupLayout.Alignment.CENTER);
		
		p0.addComponent(cam_vid_src_lbl);
		p0.addComponent(avail_vid_src_lbl);
		
		GroupLayout.ParallelGroup p1 = gl.createParallelGroup(
			GroupLayout.Alignment.CENTER);
		
		GroupLayout.SequentialGroup v0 = gl.createSequentialGroup();
		v0.addComponent(up_btn);
		v0.addGap(10*UI.vgap);
		v0.addComponent(insert_btn);
		v0.addGap(UI.vgap);
		v0.addComponent(remove_btn);
		v0.addGap(10*UI.vgap);
		v0.addComponent(down_btn);
		p1.addGroup(v0);
		
		p1.addComponent(cam_vid_src_scrl);
		p1.addComponent(vid_src_scrl);
		
		GroupLayout.ParallelGroup p2 = gl.createParallelGroup(
				GroupLayout.Alignment.CENTER);
		
		p2.addComponent(vid_src_info_lbl);
		
		GroupLayout.ParallelGroup p3 = gl.createParallelGroup(
				GroupLayout.Alignment.CENTER);
		
		p3.addComponent(vid_src_info);
		
		vg.addGroup(p0);
		vg.addGroup(p1);
		vg.addGroup(p2);
		vg.addGroup(p3);
		return vg;
	}

	/** Create jobs */
	private void createJobs() {
		ListSelectionModel cvs = cam_vid_src_lst.getSelectionModel();
		cvs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		cvs.addListSelectionListener(new IListSelectionAdapter() {
			@Override
			public void valueChanged() {
				if (isCamSrcSelected()) {
					vid_src_lst.clearSelection();
					displayVidSrcInfo(cam_vid_src_lst.getSelectedValue());
				} else if (!isVidSrcSelected())
					displayVidSrcInfo(null);
				updateButtons();
			}
		});
		
		ListSelectionModel vs = vid_src_lst.getSelectionModel();
		vs.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		vs.addListSelectionListener(new IListSelectionAdapter() {
			@Override
			public void valueChanged() {
				if (isVidSrcSelected()) {
					cam_vid_src_lst.clearSelection();
					displayVidSrcInfo(vid_src_lst.getSelectedValue());
				} else if (!isCamSrcSelected())
					displayVidSrcInfo(null);
				updateButtons();
			}
		});
	}

	/** Update the edit mode */
	public void updateEditMode() {
		boolean ud = canWrite("camera_template");
		updateButtons();
	}

	/** Update buttons */
	private void updateButtons() {
		boolean ud = canWrite("camera_template");
		insert_btn.setEnabled(ud && isVidSrcSelected());
		remove_btn.setEnabled(ud && isCamSrcSelected());
		up_btn.setEnabled(ud && isCamSrcSelected()&& canMoveUp());
		down_btn.setEnabled(ud && isCamSrcSelected() && canMoveDown());
	}
	
	//Method to check is already exists

	/** Check if the user can write an attribute */
	private boolean canWrite(String attr) {
		return session.canWrite(camera_template, attr);
	}

	/** Check if a camera video source is selected */
	private boolean isCamSrcSelected() {
		return cam_vid_src_lst.getSelectedIndex() >= 0;
	}
	
	/** Check if a video source is selected */
	private boolean isVidSrcSelected() {
		return vid_src_lst.getSelectedIndex() >= 0;
	}

	/** Check if selected video source can be moved up */
	private boolean canMoveUp() {
		return cam_vid_src_lst.getSelectedIndex() > 0;
	}

	/** Check if selected video source can be moved down */
	private boolean canMoveDown() {
		int s = cam_vid_src_lst.getSelectedIndex();
		return (s >= 0 && s < cam_vid_src_mdl.size() - 1);
	}

	/** Update one attribute on the form */
	public void updateAttribute(String a) {
		if (null == a || a.equals("camera_template"))
			updateCamVidSrc();
	}

	/** Update the camera video source list */
	private void updateCamVidSrc() {
		cam_vid_src = VidStreamReq.getCamVidSrcOrder(camera_template);
		if (cam_vid_src != null) {
			for (int i = 0; i < cam_vid_src.size(); i++) {
				VidSourceTemplate vst = lookupVidSource(cam_vid_src.get(i));
				if (i < cam_vid_src_mdl.size())
					cam_vid_src_mdl.set(i, vst);
				else
					cam_vid_src_mdl.addElement(vst);
			}
			for (int i = cam_vid_src.size(); i < cam_vid_src_mdl.size(); i++)
				cam_vid_src_mdl.remove(i);
		}
	}
	
	/** Initialize video source list */
	private void initializeVidSrc() {
		Iterator<VidSourceTemplate> it = VidSourceTemplateHelper.iterator();
		while (it.hasNext())
			vid_src_mdl.addElement(it.next());
	}

	
	private static VidSourceTemplate lookupVidSource(CameraVidSourceOrder cvso) {
		return VidSourceTemplateHelper.lookup(cvso.getVidSourceTemplate());
	}

	@Override
	public void enumerationComplete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(CameraVidSourceOrder p, String a) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		
	}
}
