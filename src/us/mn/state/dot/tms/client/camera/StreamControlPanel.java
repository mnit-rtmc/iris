/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2002-2020  Minnesota Department of Transportation
 * Copyright (C) 2014-2015  AHMCT, University of California
 * Copyright (C) 2022-2024  SRF Consulting Group
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

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import us.mn.state.dot.tms.client.EditModeListener;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.UserProperty;
import us.mn.state.dot.tms.client.widget.IAction;
import us.mn.state.dot.tms.client.widget.Icons;
import us.mn.state.dot.tms.client.widget.SmartDesktop;
import us.mn.state.dot.tms.utils.I18N;
import static us.mn.state.dot.tms.client.widget.Widgets.UI;

/**
 * A panel to contain video stream control widgets.
 *
 * @author Timothy Johnson
 * @author Douglas Lau
 * @author Travis Swanston
 * @author Michael Janson
 * @author Gordon Parikh
 * @author John L. Stanley
 */
public class StreamControlPanel extends JPanel {

	/** Stream control commands */
	static private enum StreamCommand {
		STOP("camera.stream.stop"),
		PLAY("camera.stream.play"),
		PLAY_EXTERNAL("camera.stream.playext"),
		RESTORE_LAYOUT("camera.layout.restore"),
		CLOSE_ALL_LAYOUTS("camera.layout.close"),
		SAVE_LAYOUT("camera.layout.save"),
		DELETE_LAYOUT("camera.layout.delete");

		/** Command I18n text */
		private final String text_id;

		/** Create a stream command */
		private StreamCommand(String tid) {
			text_id = tid;
		}

		/** Create a stream command button */
		private JButton createButton(final StreamControlPanel pnl) {
			IAction ia = new IAction(text_id) {
				@Override
				protected void doActionPerformed(ActionEvent ev) {
					handleButton(pnl, ev);
				}
			};
			final JButton btn = new JButton(ia);
			ImageIcon icon = Icons.getIconByPropName(text_id);
			if (icon != null) {
				btn.setMargin(UI.insets());
				btn.setIcon(icon);
				btn.setHideActionText(true);
			}
			btn.setFocusPainted(false);
			return btn;
		}

		/** Handle button press */
		private void handleButton(StreamControlPanel pnl, ActionEvent ev) {
			boolean bCtrl = (ev.getModifiers() & ActionEvent.CTRL_MASK) != 0;
			switch (this) {
			case STOP:
				pnl.stopStream();
				break;
			case PLAY:
				pnl.playStream();
				break;
			case PLAY_EXTERNAL:
				pnl.playExternal();
				break;
			case RESTORE_LAYOUT:
				pnl.restoreLayout(bCtrl);
				break;
			case CLOSE_ALL_LAYOUTS:
				pnl.closeAllLayouts();
				break;
			case SAVE_LAYOUT:
				pnl.saveLayout();
				break;
			case DELETE_LAYOUT:
				pnl.deleteLayout();
				break;
			}
		}
	}

	/** User session */
	private final Session session;

	/** User properties */
	private final Properties props;

	/** Smart desktop */
	private final SmartDesktop desktop;

	/** Stream panel */
	private final StreamPanel stream_pnl;

	/** Edit mode listener */
	private final EditModeListener edit_lsnr = new EditModeListener() {
		public void editModeChanged() {
			updateEditMode();
		}
	};

	/** Stop button */
	private final JButton stop_btn;

	/** Play button */
	private final JButton play_btn;

	/** Play external button */
	private final JButton playext_btn;

	/** Layout ComboBox */
	private final JComboBox<String> layout_cbx;

	/** Layout ComboBox model */
	private final DefaultComboBoxModel<String> layout_mdl;

	/** Save layout button */
	private final JButton save_layout_btn;

	/** Delete layout button */
	private final JButton delete_layout_btn;

	/** Restore layout button */
	private final JButton restore_layout_btn;

	/** Close all layouts button */
	private final JButton close_all_layouts_btn;

	/** Create a stream control panel.
	 * @param s User session. */
	public StreamControlPanel(Session s, StreamPanel pnl) {
		super(new FlowLayout(FlowLayout.CENTER, UI.hgap, UI.vgap));
		session = s;
		desktop = session.getDesktop();
		session.addEditModeListener(edit_lsnr);
		props = session.getProperties();
		stream_pnl = pnl;
		stop_btn = StreamCommand.STOP.createButton(this);
		play_btn = StreamCommand.PLAY.createButton(this);
		playext_btn = StreamCommand.PLAY_EXTERNAL.createButton(this);
		layout_mdl = new DefaultComboBoxModel<String>();
		layout_cbx = new JComboBox<String>(layout_mdl);
		layout_cbx.setToolTipText(I18N.get("camera.layout"));
		layout_cbx.setPrototypeDisplayValue("layout_MMM");
		restore_layout_btn = StreamCommand.RESTORE_LAYOUT
				.createButton(this);
		close_all_layouts_btn = StreamCommand.CLOSE_ALL_LAYOUTS
				.createButton(this);
		save_layout_btn = StreamCommand.SAVE_LAYOUT.createButton(this);
		delete_layout_btn = StreamCommand.DELETE_LAYOUT
				.createButton(this);
		
		add(stop_btn);
		add(play_btn);
		add(playext_btn);
		add(Box.createHorizontalStrut(UI.hgap*2));
		add(layout_cbx);
		add(restore_layout_btn);
		add(close_all_layouts_btn);
		add(Box.createHorizontalStrut(UI.hgap*2));
		add(save_layout_btn);
		add(delete_layout_btn);

		updateButtonState(false, false);
		updateLayoutList();
	}

	/** Dispose of the stream control panel */
	public final void dispose() {
		session.removeEditModeListener(edit_lsnr);
	}

	/** Stop streaming */
	private void stopStream() {
		stream_pnl.schedStopStream();
	}

	/** Play streaming */
	private void playStream() {
		stream_pnl.schedPlayStream();
	}

	/** Play external streaming */
	private void playExternal() {
		stream_pnl.schedPlayExternal(desktop);
	}

	/** Save the current layout */
	private void saveLayout() {
		String layoutName = getLayoutName();
		Integer num = UserProperty.getLayoutNumber(props, layoutName);
		if (num != null) {
			String title = I18N.get("camera.layout.save.query.title");
			String msg   = I18N.get("camera.layout.save.query.msg");
			msg = String.format(msg, layoutName);
			int result = JOptionPane.showConfirmDialog(this,
					msg, title,
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE);
			if (result != JOptionPane.YES_OPTION)
				return;
		}
		UserProperty.saveStreamLayout(props, getLayoutName());
		updateLayoutList();
	}

	/** Restore the selected layout 
	 * @param bCtrl If true, don't auto-close previous open layout(s) */
	private void restoreLayout(boolean bCtrl) {
		StreamLayout layout = new StreamLayout(props, getLayoutName());
		if (!bCtrl)
			queueCloseOldLayouts(StreamLayout.getOpenFramesList());
		layout.restoreFrames(desktop);
		updateLayoutList();
		updateCloseAllLayoutsBtn();
	}

	/** Close all open video layouts */
	public void closeAllLayouts() {
		ArrayList<Frame> oldFrames = StreamLayout.getOpenFramesList();
		for (Frame f: oldFrames) {
			JFrame jf = (JFrame)f;
			VidWindow vw = (VidWindow)(jf.getContentPane());
			vw.stopStreaming();
			f.dispose();
		}
		updateCloseAllLayoutsBtn();
	}

	/** Restart streaming in all open video layouts */
	static public void restartOpenLayouts() {
		ArrayList<Frame> oldFrames = StreamLayout.getOpenFramesList();
		for (Frame f: oldFrames) {
			JFrame jf = (JFrame)f;
			VidWindow vw = (VidWindow)(jf.getContentPane());
			vw.restartStreaming();
		}
	}

	/** Stop streaming in all open video layouts */
	static public void stopOpenLayouts() {
		ArrayList<Frame> oldFrames = StreamLayout.getOpenFramesList();
		for (Frame f: oldFrames) {
			JFrame jf = (JFrame)f;
			VidWindow vw = (VidWindow)(jf.getContentPane());
			vw.stopStreaming();
		}
	}

	/** Queue a close old-frames to be done
	 *   -after- all new frames are drawn.
	 * Done this way to smooth the transition from old-
	 * layout video panels to new-layout video panels.
	 * (Reduces the odds that the video proxy will
	 *  totally drop and restart a video stream.)
	 * @param oldFrames Old layout video frames to be closed */ 
	public void queueCloseOldLayouts(ArrayList<Frame> oldFrames) {
		new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				// First, wait 2sec for the new video frames to start being drawn.
				Thread.sleep(2000);
				return null;
			}
			@Override
			protected void done() {
				// Then, wait for all new video frames to finish being drawn.
				SwingUtilities.invokeLater(new Runnable() 
				{
					public void run()
					{
						// Finally, close all old video frames.
						for (Frame f: oldFrames) {
							JFrame jf = (JFrame)f;
							VidWindow vw = (VidWindow)(jf.getContentPane());
							vw.stopStreaming();
							f.dispose();
						}
						updateCloseAllLayoutsBtn();
					}
				});
			}
		}.execute();
	}
	
	/** Delete the selected layout */
	private void deleteLayout() {
		Object[] options = {"Yes", "No"};
		String layoutName = getLayoutName();
		Integer num = UserProperty.getLayoutNumber(props, layoutName);
		if (num != null) {
			String title = I18N.get("camera.layout.delete.query.title");
			String msg   = I18N.get("camera.layout.delete.query.msg");
			msg = String.format(msg, layoutName);
			int result = JOptionPane.showOptionDialog(this,
					msg, title,
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE,
					null, options, options[1]);
			if (result != JOptionPane.YES_OPTION)
				return;
		}
		UserProperty.deleteStreamLayout(props, getLayoutName());
		updateLayoutList();
	}

	/** Get the selected layout name */
	private String getLayoutName() {
		String name = (String) layout_cbx.getSelectedItem();
		return (name != null && !name.isEmpty())
		      ?	name
		      : UserProperty.getNextStreamLayoutName(props);
	}

	/** Update the list of layouts based on the current properties */
	private void updateLayoutList() {
		String layoutName = (String) layout_cbx.getSelectedItem();
		layout_mdl.removeAllElements();
		ArrayList<String> layoutNames =
			UserProperty.getStreamLayoutNames(props);
		for (String ln: layoutNames)
			layout_mdl.addElement(ln);
		layout_cbx.setSelectedItem(layoutName);
		updateCloseAllLayoutsBtn();
	}

	/** Update the button state */
	public void updateButtonState(boolean has_camera, boolean is_streaming) {
		stop_btn.setEnabled(has_camera && is_streaming);
		play_btn.setEnabled(has_camera && !is_streaming);
		playext_btn.setEnabled(has_camera);
		updateCloseAllLayoutsBtn();
	}

	/** Update the edit mode */
	private void updateEditMode() {
		boolean editMode = session.getEditMode();
		save_layout_btn.setEnabled(editMode);
		delete_layout_btn.setEnabled(editMode);
		layout_cbx.setEditable(editMode);
	}

	/** Update the close all layouts button */
	private void updateCloseAllLayoutsBtn() {
		close_all_layouts_btn.setEnabled(StreamLayout.framesAreOpen());
	}
}
