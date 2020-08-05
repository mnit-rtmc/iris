/*
 * IRIS -- Intelligent Roadway Information System
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import us.mn.state.dot.sonar.client.TypeCache;
import us.mn.state.dot.tms.CameraTemplate;
import us.mn.state.dot.tms.CameraTemplateHelper;
import us.mn.state.dot.tms.CameraVidSourceOrder;
import us.mn.state.dot.tms.CameraVidSourceOrderHelper;
import us.mn.state.dot.tms.EncoderType;
import us.mn.state.dot.tms.VidSourceTemplate;
import us.mn.state.dot.tms.VidSourceTemplateHelper;
import us.mn.state.dot.tms.client.EditModeListener;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.proxy.ProxyTablePanel;
import us.mn.state.dot.tms.client.proxy.SwingProxyAdapter;
import us.mn.state.dot.tms.client.widget.AbstractForm;
import us.mn.state.dot.tms.client.widget.DisabledSelectionModel;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.IComboBoxModel;
import us.mn.state.dot.tms.client.widget.ILabel;
import us.mn.state.dot.tms.client.widget.Widgets;
import us.mn.state.dot.tms.utils.I18N;

/**
 * User interface for creating and editing video source templates
 * assigned to camera templates and their respective priority
 *
 * @author Gordon Parikh
 */
@SuppressWarnings("serial")
public class VidSourceTemplateEditor extends AbstractForm {

	/** Client Session */
	private Session session;
	
	/** Handle of the frame that contains this form */
	JInternalFrame frame;
	
	/** Table of Video Source Templates */
	private ProxyTablePanel<VidSourceTemplate> vidSrcTemplates;
	
	/** Cache of VidSourceTemplates */
	private final TypeCache<VidSourceTemplate> cache;
	
	/** Video Source Template Model */
	private VidSrcTemplateModel vidSrcModel;
	
	/** Currently selected video source template */
	private VidSourceTemplate selectedVidSource;
	
	/** Model for Camera Templates assigned to the selected video source */
	private final DefaultListModel<String> camTemplateModel =
		new DefaultListModel<String>();

	/** List of Camera Templates linking to the selected video source */
	private final JList<String> camTemplateList =
			new JList<String>(camTemplateModel);
	
	/** ArrayList of camera templates linking to the selected video source */
	private final ArrayList<CameraTemplate> camTemplates =
			new ArrayList<CameraTemplate>();
	
	/** Edit mode listener */
	private final EditModeListener edit_lsnr = new EditModeListener() {
		public void editModeChanged() {
			updateButtons();
			updateEditFields();
		}
	};
	
	private VidSourceTemplate waitingForVst;
	
	/** Proxy listener for SONAR updates */
	private final SwingProxyAdapter<VidSourceTemplate> listener =
			new SwingProxyAdapter<VidSourceTemplate>() {
		@Override
		protected void proxyAddedSwing(VidSourceTemplate vst) {
			// check if we are waiting for a proxy to show up
			if (waitingForVst != null &&
					waitingForVst.getName() == vst.getName()) {
				// if we are, it's because we wanted to select it
				vidSrcTemplates.disableSelectionHandling();
				vidSrcTemplates.selectProxy(vst);
				setSelectedVideoSource(vst, false);
				vidSrcTemplates.enableSelectionHandling();
				
				// and now we're done with this
				waitingForVst = null;
			}
		}
		@Override
		protected void proxyChangedSwing(VidSourceTemplate vst, String attr) {
			loadVidSrcTemplateFields(vst);
		}
	};
	
	/** Warning color for fields with invalid or missing contents. */
	public static final Color WARNING_COLOR =
			new Color(255, 204, 203, 255);
	
	public static final Color OK_COLOR = Color.WHITE;
	
	/** Video Source Edit Fields Label */
	private JLabel vidSrcEditFieldLbl;
	private String vidSrcEditFieldLblPrfx;
	
	/** Video Source Edit Fields Panel */
	private JPanel vidSrcEditFieldPnl;
	
	/** Video Source Edit Fields */
	
	/** Name (Label) Label and Field */
	private JLabel vsNameLbl;
	private JTextField vsNameField;
	
	/** Codec Label and Field */
	private JLabel vsCodecLbl;
	private JTextField vsCodecField;
	
	/** Encoder (Type) Label and Field */
	private JLabel vsEncoderLbl;
	private JComboBox<EncoderType> vsEncoderField;
	private IComboBoxModel<EncoderType> vsEncoderModel;
	
	/** Scheme Label and Field */
	private JLabel vsSchemeLbl;
	private JTextField vsSchemeField;
	
	/** Default Port Label and Field */
	private JLabel vsLatencyLbl;
	private JTextField vsLatencyField;
	
	/** Default Port Label and Field */
	private JLabel vsDefPortLbl;
	private JTextField vsDefPortField;
	
	/** Resolution Width Label and Field */
	private JLabel vsRezWidthLbl;
	private JTextField vsRezWidthField;
	
	/** Resolution Height Label and Field */
	private JLabel vsRezHeightLbl;
	private JTextField vsRezHeightField;
	
	/** Subnets Label and Field */
	private JLabel vsSubnetsLbl;
	private JTextArea vsSubnetsField;
	
	/** (GStreamer) Configuration Label and Field */
	private JLabel vsConfigLbl;
	private JTextArea vsConfigField;
	
	/** Notes Label and Field */
	private JLabel vsNotesLbl;
	private JTextArea vsNotesField;
	
	/** Separator size between fields on the same line */
	private final static int hGap = 10;
	
	/** Button panel */
	private JPanel buttonPnl;
	
	/** Delete Button */
	private JButton deleteBtn;
	
	/** Delete Button */
	private JButton clearBtn;
	
	/** Delete Button */
	private JButton helpBtn;
	
	/** Delete Button */
	private JButton cancelBtn;
	
	/** Delete Button */
	private JButton saveBtn;
	
	/** Delete Button */
	private JButton cloneBtn;
	
	protected VidSourceTemplateEditor(Session s) {
		super(I18N.get("camera.video_source.template_editor"), true);
		session = s;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setPreferredSize(new Dimension(800, 700));
		vidSrcModel = new VidSrcTemplateModel(session);
		vidSrcTemplates = new ProxyTablePanel<VidSourceTemplate>(
				vidSrcModel) {
			/** Set the selected video source template when clicked on. */
			@Override
			protected void selectProxy() {
				VidSourceTemplate vst = getSelectedProxy();
				setSelectedVideoSource(vst);
			}
		};
		
		// disable selection on the list of camera templates associated with
		// the selected video source
		camTemplateList.setSelectionModel(new DisabledSelectionModel());
		
		// get the camera cache for encoder types
		CamCache cc = session.getSonarState().getCamCache();
		
		// initialize the cache for saving VidSourceTemplates (the watcher too)
		cache = session.getSonarState().getVidSrcTemplates();
		
		// instantiate the video source edit fields/labels
		vsNameLbl = new JLabel(I18N.get(
				"camera.video_source.template.name") + ":");
		vsNameField = new JTextField(10);
		
		vsCodecLbl = new JLabel(I18N.get(
				"camera.video_source.template.codec") + ":");
		vsCodecField = new JTextField(10);
		
		vsEncoderLbl = new JLabel(I18N.get(
				"camera.video_source.template.encoder") + ":");
		vsEncoderModel = new IComboBoxModel<EncoderType>(
				cc.getEncoderTypeModel());
		vsEncoderField = new JComboBox<EncoderType>(vsEncoderModel);
		vsEncoderField.setRenderer(new EncoderTypeRenderer());
		
		vsSchemeLbl = new JLabel(I18N.get(
				"camera.video_source.template.scheme") + ":");
		vsSchemeField = new JTextField(10);
		
		vsLatencyLbl = new JLabel(I18N.get(
				"camera.video_source.template.latency") + ":");
		vsLatencyField = new JTextField(10);

		vsDefPortLbl = new JLabel(I18N.get(
				"camera.video_source.template.default_port") + ":");
		vsDefPortField = new JTextField(6);
		
		vsRezWidthLbl = new JLabel(I18N.get(
				"camera.video_source.template.rez_width") + ":");
		vsRezWidthField = new JTextField(6);
		
		vsRezHeightLbl = new JLabel(I18N.get(
				"camera.video_source.template.rez_height") + ":");
		vsRezHeightField = new JTextField(6);
		
		vsSubnetsLbl = new JLabel(I18N.get(
				"camera.video_source.template.subnets") + ":");
		vsSubnetsField = new JTextArea(1, 60);
		vsSubnetsField.setLineWrap(true);
		vsSubnetsField.setWrapStyleWord(true);
		
		vsConfigLbl = new JLabel("<html>" + I18N.get(
			"camera.video_source.template.config") + ":<br>&nbsp;</html>");
		vsConfigField = new JTextArea(2, 58);
		vsConfigField.setLineWrap(true);
		vsConfigField.setWrapStyleWord(true);
		
		vsNotesLbl = new JLabel("<html>" +I18N.get(
			"camera.video_source.template.notes") + ":<br><br>&nbsp;</html>");
		vsNotesField = new JTextArea(3, 62);
		vsNotesField.setLineWrap(true);
		vsNotesField.setWrapStyleWord(true);
		
		// instantiate the buttons (TODO ACTIONS)
		deleteBtn = new JButton(deleteConfirm);
		clearBtn = new JButton(clear);
		helpBtn = new JButton(help);
		cancelBtn = new JButton(cancel);
		saveBtn = new JButton(save);
		cloneBtn = new JButton(clone);
	}
	
	/** Check if the user is permitted to use the form. */
	static public boolean isPermitted(Session s) {
		return s.canRead(VidSourceTemplate.SONAR_TYPE);
	}
	
	/** Initialize the form */
	@Override
	protected void initialize() {
		// initialize layout
		GridBagLayout gbl = new GridBagLayout();
		JPanel gbPanel = new JPanel(gbl);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.BASELINE_LEADING;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.insets = Widgets.UI.insets();
		gbc.ipadx = 10;
		gbc.ipady = 0;
		gbc.weightx = 1;
		gbc.weighty = 1;
		
		/* Video Source Template Label */
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbPanel.add(new ILabel("camera.video_source.templates"), gbc);
		
		/* Camera Templates Using Source Label */
		gbc.gridx = 1;
		gbPanel.add(new ILabel("camera.video_source.camera_templates"), gbc);
		
		/* Video Source Template Table (ProxyTableForm) */
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.BOTH;
		vidSrcTemplates.initialize();
		cache.addProxyListener(listener);
		JScrollPane vstPn = new JScrollPane(vidSrcTemplates,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		vstPn.setMinimumSize(new Dimension(600, 200));
		gbPanel.add(vstPn, gbc);
		
		/* Camera Template Table */
		gbc.gridx = 1;
		JScrollPane ctPn = new JScrollPane(camTemplateList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		ctPn.setMinimumSize(new Dimension(150, 150));
		gbPanel.add(ctPn, gbc);
		
		/* Video Source Edit Fields Label */
		vidSrcEditFieldLblPrfx = I18N.get("camera.video_source.edit_fields");
		vidSrcEditFieldLbl = new JLabel(vidSrcEditFieldLblPrfx);
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbPanel.add(vidSrcEditFieldLbl, gbc);
		
		/* Video source Edit Fields Panel */
		vidSrcEditFieldPnl = new JPanel();
		vidSrcEditFieldPnl.setLayout(new BoxLayout(
				vidSrcEditFieldPnl, BoxLayout.Y_AXIS));
		vidSrcEditFieldPnl.setPreferredSize(new Dimension(550,350));
		
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
		vidSrcEditFieldPnl.add(fRow1);
		vidSrcEditFieldPnl.add(fRow2);
		vidSrcEditFieldPnl.add(fRow3);
		vidSrcEditFieldPnl.add(fRow4);
		vidSrcEditFieldPnl.add(fRow5);
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 3;
		gbPanel.add(vidSrcEditFieldPnl, gbc);
		
		/* Button panel */
		buttonPnl = new JPanel();
		
		// add buttons to the panel with some gaps
		buttonPnl.add(deleteBtn);
		buttonPnl.add(Box.createHorizontalStrut(40));
		
		buttonPnl.add(clearBtn);
		buttonPnl.add(Box.createHorizontalStrut(40));
		
		buttonPnl.add(helpBtn);
		buttonPnl.add(Box.createHorizontalStrut(40));
		
		buttonPnl.add(cancelBtn);
		buttonPnl.add(Box.createHorizontalStrut(10));
		
		buttonPnl.add(saveBtn);
		buttonPnl.add(Box.createHorizontalStrut(10));
		
		buttonPnl.add(cloneBtn);
		
		gbc.gridy = 4;
		gbPanel.add(buttonPnl, gbc);
		add(gbPanel);
		session.addEditModeListener(edit_lsnr);
		updateButtons();
		updateEditFields();
	}
	
	public void setFrame(JInternalFrame f) {
		frame = f;
		
		// add a window listener to capture the close event for confirmation
		frame.addInternalFrameListener(new InternalFrameAdapter() {
		    @Override
		    public void internalFrameClosing(InternalFrameEvent e) {
		    	cancel.actionPerformed(null);
		    }
		});
		frame.setDefaultCloseOperation(JInternalFrame.DO_NOTHING_ON_CLOSE);
	}
	
	/** Dispose of the form */
	@Override
	public void dispose() {
		vidSrcTemplates.dispose();
		super.dispose();
	}
	
	/** Handle the selection of a video source template from the table. */
	private void setSelectedVideoSource(
			VidSourceTemplate vst, boolean loadFields) {
		// first check if the user has changed any of the data in the fields
		// if they have and it hasn't been saved, show a confirmation dialog
		boolean changed = fieldsChanged();
		boolean confirmed = false;
		if (changed && loadFields)
			confirmed = confirmDiscard();
		if (!changed || !loadFields ||
				(changed && loadFields && confirmed)) {
			selectedVidSource = vst;
			
			// update various things
			updateVidSrcLabel();
			updateCamTemplates();
			updateButtons();
			
			// only load fields if requested - this deals with some of the
			// consequences of SONAR's asynchronous nature
			if (loadFields)
				loadVidSrcTemplateFields();
		} else if (changed && loadFields && !confirmed) {
			// if fields have been changed but the user pressed "go back",
			// re-select the original video source in the table (disabling
			// selection handling first)
			selectVideoSourceInTable(selectedVidSource, false);
		}
	}
	
	private void setSelectedVideoSource(VidSourceTemplate vst) {
		setSelectedVideoSource(vst, true);
	}
	
	/** Select the given video source template in the table. If fireHandlers
	 *  is true, the selection change will trigger the handler defined in
	 *  the table's selectProxy() method. If false, only the selection in the
	 *  table will change and no other events will be triggered.
	 */
	private void selectVideoSourceInTable(
			VidSourceTemplate vst, boolean fireHandlers) {
		// disable selection handling first if fireHandlers is false
		if (!fireHandlers)
			vidSrcTemplates.disableSelectionHandling();
		
		vidSrcTemplates.selectProxy(vst);
		
		// make sure to turn it back on
		if (!fireHandlers)
			vidSrcTemplates.enableSelectionHandling();
	}
	
	public VidSourceTemplate getSelectedVideoSource() {
		return selectedVidSource;
	}
	
	/** Update the label above the video source template edit fields to include
	 *  the label of the selected video source (if one is selected).
	 */
	private void updateVidSrcLabel() {
		if (selectedVidSource != null) {
			vidSrcEditFieldLbl.setText(vidSrcEditFieldLblPrfx +
					" " + selectedVidSource.getLabel());
		} else
			vidSrcEditFieldLbl.setText(vidSrcEditFieldLblPrfx);
	}
	
	/** Update buttons based on the selected video source template. */
	private void updateButtons() {
		// enable delete, save/clear, and clone buttons if the user has
		// permission
		boolean sel = selectedVidSource != null;
		boolean perm = session.canWrite(VidSourceTemplate.SONAR_TYPE);
		deleteBtn.setEnabled(sel && perm);
		cloneBtn.setEnabled(sel && perm);
		saveBtn.setEnabled(perm);
		
		// change the text on the save button depending on the selection state
		if (sel)
			saveBtn.setText(I18N.get("camera.video_source.template.save"));
		else
			saveBtn.setText(I18N.get("camera.video_source.template.create"));
	}
	
	/** Update edit fields based on edit mode on or off. */
	private void updateEditFields() {
		boolean perm = session.canWrite(VidSourceTemplate.SONAR_TYPE);
		vsNameField.setEnabled(perm);
		vsCodecField.setEnabled(perm);
		vsEncoderField.setEnabled(perm);
		vsSchemeField.setEnabled(perm);
		vsLatencyField.setEnabled(perm);
		vsDefPortField.setEnabled(perm);
		vsRezWidthField.setEnabled(perm);
		vsRezHeightField.setEnabled(perm);
		vsSubnetsField.setEnabled(perm);
		vsConfigField.setEnabled(perm);
		vsNotesField.setEnabled(perm);
		
		// use the same border as the other fields (text areas are weird)		
		Border border = vsNameField.getBorder();
		vsSubnetsField.setBorder(BorderFactory.createCompoundBorder(
				border, BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		vsConfigField.setBorder(BorderFactory.createCompoundBorder(
				border, BorderFactory.createEmptyBorder(1, 1, 1, 1)));
		vsNotesField.setBorder(BorderFactory.createCompoundBorder(
				border, BorderFactory.createEmptyBorder(1, 1, 1, 1)));
	}
	
	/** Update the list of camera templates associated with the currently
	 *  selected video source.
	 */
	private void updateCamTemplates() {
		// clear the model, then go through all camera templates to find ones
		// associated with the selected video source (or do nothing if it's
		// null)
		camTemplateModel.clear();
		camTemplates.clear();
		if (selectedVidSource != null) {
			Iterator<CameraVidSourceOrder> it =
					CameraVidSourceOrderHelper.iterator();
			while (it.hasNext()) {
				CameraVidSourceOrder cvso = it.next();
				String vstName = cvso.getVidSourceTemplate();
				if (vstName.equals(selectedVidSource.getName())) {
					CameraTemplate ct = CameraTemplateHelper.lookup(
							cvso.getCameraTemplate());
					camTemplateModel.addElement(ct.getLabel());
					camTemplates.add(ct);
				}
			}
		}
	}
	
	/** Return the list of camera templates that link to the selected video
	 *  source template.
	 */
	public ArrayList<CameraTemplate> getCamTemplatesForVidSource() {
		return camTemplates;
	}
	
	/** Load values from the selected video source template into the form
	 *  fields. If the selected video source is null, the form is cleared.
	 */
	private void loadVidSrcTemplateFields() {
		if (selectedVidSource != null) {
			loadVidSrcTemplateFields(selectedVidSource);
		} else
			clearFormFields();
	}
	
	/** Load values from the provided video source template into the form
	 *  fields. If the selected video source is null, nothing happens.
	 */
	private void loadVidSrcTemplateFields(VidSourceTemplate vst) {
		if (vst != null) {
			vsNameField.setText(vst.getLabel());
			vsCodecField.setText(vst.getCodec());
			
			// pick out the right encoder type from the model
			String en = vst.getEncoder();
			int ei = -1;
			for (int i = 0; i < vsEncoderModel.getSize(); ++i) {
				EncoderType e = vsEncoderModel.getElementAt(i);
				if (e != null && e.getName().equals(en)) {
					ei = i;
					break;
				}
			}
			// set if found, otherwise no selection (-1)
			vsEncoderField.setSelectedIndex(ei);
			
			vsSchemeField.setText(vst.getScheme());
			
			// convert int fields to strings
			vsLatencyField.setText(getString(vst.getLatency()));
			vsDefPortField.setText(getString(
					vst.getDefaultPort()));
			vsRezWidthField.setText(getString(
					vst.getRezWidth()));
			vsRezHeightField.setText(getString(
					vst.getRezHeight()));
			
			vsSubnetsField.setText(vst.getSubnets());
			vsConfigField.setText(vst.getConfig());
			vsNotesField.setText(vst.getNotes());
		}
	}
	
	/** Convert an Integer value to a string. If the value is null, an empty
	 *  string is returned (instead of "null"
	 */
	private static String getString(Integer i) {
		if (i != null)
			return String.valueOf(i);
		return "";
	}
	
	private VidSourceTemplateEditor form = this;
	
	/** Action triggered when pressing the delete button. */
	private IAction deleteConfirm = new IAction(
			"camera.video_source.template.delete") {
		@Override
		protected void doActionPerformed(ActionEvent ev) throws Exception {
			// update camera templates just in case something changed since
			// last selection
			updateCamTemplates();
			
			// open a dialog to confirm deleting
			String t = String.format(I18N.get(
					"camera.video_source.template.confirm_delete_title"),
					selectedVidSource.getLabel());
			
			session.getDesktop().show(
					new VidSrcTemplConfirmDeleteForm(t, form));
		}
	};
	
	/** Delete the currently selected video source template object. If there
	 *  are camera templates associated with it, this removes those links and
	 *  reorders the CameraVidSourceOrder objects as needed.
	 * @throws InterruptedException 
	 */
	public void deleteSelectedVidSrc() throws InterruptedException {
		if (selectedVidSource == null)
			return;
		
		// first get any associated CameraVidSourceOrder objects - we will
		// eventually delete them all so we can delete the VidSourceTemplate
		ArrayList<CameraVidSourceOrder> vidSrcCVOs =
				CameraVidSourceOrderHelper.listForVidSource(
						selectedVidSource.getName());
		
		// go through the list, with each element representing a link between
		// the selected VidSourceTemplate and a CameraTemplate
		for (CameraVidSourceOrder cvoVidSrc: vidSrcCVOs) {
			// find all CVOs with the same CameraTemplate
			ArrayList<CameraVidSourceOrder> camTemplCVOs =
					CameraVidSourceOrderHelper.listForCameraTemplate(
							cvoVidSrc.getCameraTemplate());
			
			// for any with a source order above the one we're going to delete,
			// decrement it by 1
			for (CameraVidSourceOrder cvoCamTmpl: camTemplCVOs) {
				if (cvoCamTmpl.getSourceOrder() > cvoVidSrc.getSourceOrder())
					cvoCamTmpl.setSourceOrder(cvoCamTmpl.getSourceOrder()-1);
			}
			
			// finally we can delete the CVO linking to the selected VidSrc
			cvoVidSrc.destroy();
		}
		
		// finally delete the video source itself and clear the selection
		selectedVidSource.destroy();
		
		selectedVidSource = null;
		updateVidSrcLabel();
		updateCamTemplates();
		updateButtons();
		clearFormFields();
	}
	
	/** Action triggered when pressing the clear button. */
	private IAction clear = new IAction(
			"camera.video_source.template.clear") {
		@Override
		protected void doActionPerformed(ActionEvent ev) throws Exception {
			// set the video source to null - it will do the rest
			setSelectedVideoSource(null);
		}
	};

	/** Action triggered when pressing the config field help button. */
	private IAction help = new IAction(
			"camera.video_source.template.help") {
		@Override
		protected void doActionPerformed(ActionEvent ev) throws Exception {
			// open the dialog with config substitution field info
			session.getDesktop().show(new VidSrcTemplateConfigHelp());
		}
	};

	/** Clone the selected video source. */
	private IAction clone = new IAction(
			"camera.video_source.template.clone") {
		@Override
		protected void doActionPerformed(ActionEvent ev) throws Exception {
			if (selectedVidSource != null) {
				// generate a new, unique name for the video source
				String n = VidSourceTemplateHelper.createUniqueName();
				
				// generate a new, unique label for the source based on the
				// selected one
				String oldLbl = selectedVidSource.getLabel();
				String l = oldLbl + "_1";
				for (int i = 2; i < 999; ++i) {
					if (VidSourceTemplateHelper.lookupLabel(l) == null)
						break;
					l = oldLbl + "_" + String.valueOf(i);
				}
				
				// save the non-nullable attributes besides the name in a map
				HashMap<String,Object> amap = new HashMap<String,Object>();
				amap.put("label", l);
				amap.put("config", selectedVidSource.getConfig());
				
				// create a new object and set the remaining attributes (to
				// allow for null values)
				cache.createObject(n, amap);
				VidSourceTemplate vst = cache.lookupObjectWait(n);
				vst.setDefaultPort(selectedVidSource.getDefaultPort());
				vst.setSubnets(selectedVidSource.getSubnets());
				vst.setLatency(selectedVidSource.getLatency());
				vst.setEncoder(selectedVidSource.getEncoder());
				vst.setScheme(selectedVidSource.getScheme());
				vst.setCodec(selectedVidSource.getCodec());
				vst.setRezWidth(selectedVidSource.getRezWidth());
				vst.setRezHeight(selectedVidSource.getRezHeight());
				vst.setMulticast(selectedVidSource.getMulticast());
				vst.setNotes(selectedVidSource.getNotes());
				
				// make a note that we are waiting for this proxy to show up
				// in the table
				waitingForVst = vst;
				setSelectedVideoSource(vst);
			}
		}
	};

	/** Save the selected video source or create a new one. */
	private Action save = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent ev){
			if (selectedVidSource != null)
				saveSelectedVidSource();
			else
				createNewVidSource();
		}
	};
	
	private void saveSelectedVidSource() {
		// validate all the fields, then save if they are valid
		if (selectedVidSource != null && validateFormFields()) {
			selectedVidSource.setLabel(vsNameField.getText());
			selectedVidSource.setCodec(vsCodecField.getText());
			selectedVidSource.setScheme(vsSchemeField.getText());
			EncoderType e = (EncoderType) vsEncoderField.getSelectedItem();
			String etn = e != null ? e.getName() : null;
			selectedVidSource.setEncoder(etn);
			
			if (!vsLatencyField.getText().isEmpty()) {
				selectedVidSource.setLatency(Integer.valueOf(
						vsLatencyField.getText()));
			}
			if (!vsDefPortField.getText().isEmpty()) {
				selectedVidSource.setDefaultPort(Integer.valueOf(
						vsDefPortField.getText()));
			}
			if (!vsRezWidthField.getText().isEmpty()) {
				selectedVidSource.setRezWidth(Integer.valueOf(
						vsRezWidthField.getText()));
			}
			if (!vsRezHeightField.getText().isEmpty()) {
				selectedVidSource.setRezHeight(Integer.valueOf(
						vsRezHeightField.getText()));
			}
			selectedVidSource.setSubnets(vsSubnetsField.getText());
			selectedVidSource.setConfig(vsConfigField.getText());
			selectedVidSource.setNotes(vsNotesField.getText());
		}
	}
	
	/** Create a new video source with no fields. */
	private void createNewVidSource() {
		// get a new, unique name for the video source, then create the object
		// and set it as the selected one
		if (selectedVidSource == null && validateFormFields()) {
			String n = VidSourceTemplateHelper.createUniqueName();
			cache.createObject(n);
			selectedVidSource = cache.lookupObjectWait(n);
			
			waitingForVst = selectedVidSource;
			
			// now save the rest of the fields
			saveSelectedVidSource();
		}
	}
	
	/** Validate values in the form fields. Not all fields are actually 
	 *  validated. If any fields are invalid, false is returned
	 */
	private boolean validateFormFields() {
		boolean formOK = true;
		
		// name (label) cannot be empty and must be unique
		if (vsNameField.getText().isEmpty()) {
			formOK = false;
			vsNameField.setBackground(WARNING_COLOR);
			showWarningRequired(I18N.get("camera.video_source.template.name"));
		} else {
			String newLabel = vsNameField.getText();
			
			// if the new name is different, make sure there isn't another
			// VidSourceTemplate with this label
			if (selectedVidSource == null ||
					(selectedVidSource != null
					 && !newLabel.equals(selectedVidSource.getLabel()))) {
				Iterator<VidSourceTemplate> it =
						VidSourceTemplateHelper.iterator();
				boolean labelConflict = false;
				while (it.hasNext()) {
					VidSourceTemplate vst = it.next();
					
					// if it's not the same exact source, cross-check the labels
					if (selectedVidSource == null ||
							(selectedVidSource != null
							 && vst.getName() != selectedVidSource.getName())) {
						if (newLabel.equals(vst.getLabel())) {
							// TODO dialog???
							formOK = false;
							labelConflict = true;
							vsNameField.setBackground(WARNING_COLOR);
						}
					}
				}
				if (!labelConflict)
					vsNameField.setBackground(OK_COLOR);
				else {
					// if there is a label conflict, give the user a hint
					JOptionPane.showConfirmDialog(
					  Session.getCurrent().getDesktop(),
					  I18N.get("camera.video_source.template.conflict_msg"),
					  I18N.get("camera.video_source.template.conflict"),
					  JOptionPane.DEFAULT_OPTION,
					  JOptionPane.ERROR_MESSAGE);
				}
			} else
				vsNameField.setBackground(OK_COLOR);
		}
		
		// config field must also not be empty
		if (vsConfigField.getText().isEmpty()) {
			formOK = false;
			vsConfigField.setBackground(WARNING_COLOR);
			showWarningRequired(I18N.get("camera.video_source.template.config"));
		} else
			vsConfigField.setBackground(OK_COLOR);
		
		
		// integer fields must be integers (or blank)
		if (!vsLatencyField.getText().isEmpty()) {
			try {
				Integer.valueOf(vsLatencyField.getText());
				vsLatencyField.setBackground(OK_COLOR);
			} catch (NumberFormatException e) {
				formOK = false;
				vsLatencyField.setBackground(WARNING_COLOR);
				showWarningInteger(I18N.get(
						"camera.video_source.template.latency"));
			}
		} else
			vsLatencyField.setBackground(OK_COLOR);
		
		if (!vsDefPortField.getText().isEmpty()) {
			try {
				Integer.valueOf(vsDefPortField.getText());
				vsDefPortField.setBackground(OK_COLOR);
			} catch (NumberFormatException e) {
				formOK = false;
				vsDefPortField.setBackground(WARNING_COLOR);
				showWarningInteger(I18N.get(
						"camera.video_source.template.default_port"));
			}
		} else
			vsDefPortField.setBackground(OK_COLOR);
		
		if (!vsRezWidthField.getText().isEmpty()) {
			try {
				Integer.valueOf(vsRezWidthField.getText());
				vsRezWidthField.setBackground(OK_COLOR);
			} catch (NumberFormatException e) {
				formOK = false;
				vsRezWidthField.setBackground(WARNING_COLOR);
				showWarningInteger(I18N.get(
						"camera.video_source.template.rez_width"));
			}
		} else
			vsRezWidthField.setBackground(OK_COLOR);
		
		if (!vsRezHeightField.getText().isEmpty()) {
			try {
				Integer.valueOf(vsRezHeightField.getText());
				vsRezHeightField.setBackground(OK_COLOR);
			} catch (NumberFormatException e) {
				formOK = false;
				vsRezHeightField.setBackground(WARNING_COLOR);
				showWarningInteger(I18N.get(
						"camera.video_source.template.rez_height"));
			}
		} else
			vsRezHeightField.setBackground(OK_COLOR);
		
		return formOK;
	}
	
	/** Show a warning message indicating that the specified field is required. */
	private static void showWarningRequired(String fieldName) {
		// if there is a label conflict, give the user a hint
		String msg = String.format(I18N.get(
				"camera.video_source.template.field_required_msg"), fieldName);
		JOptionPane.showConfirmDialog(Session.getCurrent().getDesktop(), msg,
		  I18N.get("camera.video_source.template.field_required"),
		  JOptionPane.DEFAULT_OPTION,
		  JOptionPane.ERROR_MESSAGE);
	}
	
	/** Show a warning message indicating that the specified field must be an
	 *  integer.
	 */
	private static void showWarningInteger(String fieldName) {
		// if there is a label conflict, give the user a hint
		String msg = String.format(I18N.get(
				"camera.video_source.template.field_int_msg"), fieldName);
		JOptionPane.showConfirmDialog(Session.getCurrent().getDesktop(), msg,
				I18N.get("camera.video_source.template.field_int"),
				JOptionPane.DEFAULT_OPTION,
				JOptionPane.ERROR_MESSAGE);
	}
	
	/** Show a dialog to ask the user if they want to discard changes or go
	 *  back to their previous step. Returns true if the user pressed discard
	 *  changes, and false if they pressed go back (or the form's X button);
	 */
	private static boolean confirmDiscard() {
		String goBack = I18N.get(
				"camera.video_source.template.unsaved_changes_goback");
		String discardChanges = I18N.get(
				"camera.video_source.template.unsaved_changes_discard");
		Object[] choices = {goBack, discardChanges};
		Object defaultChoice = choices[0];
		int ret = JOptionPane.showOptionDialog(
				Session.getCurrent().getDesktop(),
				I18N.get("camera.video_source.template.unsaved_changes_msg"),
				I18N.get("camera.video_source.template.unsaved_changes_title"),
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
				null, choices, defaultChoice);
		return ret == 1;
	}
	
	/** Check if the form fields have been changed compared to the selected
	 *  template (or if they have any contents if the selected template is
	 *  null). Returns true if any fields have changed, and false otherwise.
	 */
	private boolean fieldsChanged() {
		// if no selected source, check if any changes
		if (selectedVidSource == null) {
			return !vsNameField.getText().isEmpty()
				|| !vsCodecField.getText().isEmpty()
				|| vsEncoderField.getSelectedIndex() > 0
				|| !vsSchemeField.getText().isEmpty()
				|| !vsLatencyField.getText().isEmpty()
				|| !vsDefPortField.getText().isEmpty()
				|| !vsRezWidthField.getText().isEmpty()
				|| !vsRezHeightField.getText().isEmpty()
				|| !vsSubnetsField.getText().isEmpty()
				|| !vsConfigField.getText().isEmpty()
				|| !vsNotesField.getText().isEmpty();
		}
		
		// if there is a selected source, cross-check each field
		EncoderType et = (EncoderType) vsEncoderField.getSelectedItem();
		String etn = et != null ? et.getName() : null;
		boolean etEq = etn != null
					   ? etn.equals(selectedVidSource.getEncoder())
					   : etn == selectedVidSource.getEncoder();
		
		String latency = getString(selectedVidSource.getLatency());
		String defPort = getString(selectedVidSource.getDefaultPort());
		String rezWidth = getString(selectedVidSource.getRezWidth());
		String rezHeight = getString(selectedVidSource.getRezHeight());
		return !vsNameField.getText().equals(selectedVidSource.getLabel())
			|| !vsCodecField.getText().equals(selectedVidSource.getCodec())
			|| !etEq
			|| !vsSchemeField.getText().equals(selectedVidSource.getScheme())
			|| !vsLatencyField.getText().equals(latency)
			|| !vsDefPortField.getText().equals(defPort)
			|| !vsRezWidthField.getText().equals(rezWidth)
			|| !vsRezHeightField.getText().equals(rezHeight)
			|| !vsSubnetsField.getText().equals(selectedVidSource.getSubnets())
			|| !vsConfigField.getText().equals(selectedVidSource.getConfig())
			|| !vsNotesField.getText().equals(selectedVidSource.getNotes());
	}
	
	/** Clear all fields in the form and deselect any selected template. */
	private void clearFormFields() {
		// deselect the source in the table
		// disable the table's list selection listener before deselecting
		vidSrcTemplates.disableSelectionHandling();
		vidSrcTemplates.selectProxy(null);
		vidSrcTemplates.enableSelectionHandling();
		
		// now clear the fields
		vsNameField.setText("");
		vsCodecField.setText("");
		vsEncoderField.setSelectedIndex(0);
		vsSchemeField.setText("");
		vsLatencyField.setText("");
		vsDefPortField.setText("");
		vsRezWidthField.setText("");
		vsRezHeightField.setText("");
		vsSubnetsField.setText("");
		vsConfigField.setText("");
		vsNotesField.setText("");
		
		// update buttons since save should say clear now
		updateButtons();
	}
	
	/** Action triggered when pressing the cancel button. */
	private IAction cancel = new IAction(
			"camera.video_source.template.cancel") {
		@Override
		protected void doActionPerformed(ActionEvent ev) throws Exception {
			// check if the fields have unsaved changes
			if (fieldsChanged()) {
				// if they do, open a dialog and only close if they confirm
				if (confirmDiscard())
					closeForm();
			} else
				// otherwise just close
				closeForm();
		}
	};
	
	/** Close the form without doing anything else. */
	public void closeForm() {
		close(session.getDesktop());
	}
}



















