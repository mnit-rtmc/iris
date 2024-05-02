/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2024  SRF Consulting Group
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.border.BevelBorder;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.SystemAttrEnum;
import us.mn.state.dot.tms.client.Session;
import us.mn.state.dot.tms.client.camera.VideoRequest.Size;
import us.mn.state.dot.tms.utils.I18N;

/** JPanel that shows video.
 *
 * This class handles:
 *   Switching between cameras.
 *   Switching between available streams for the current camera.
 *   Manages camera name label at top of panel.
 *   Manages status/timer/error label at bottom of panel.
 *   Manages MousePTZ link for panel.
 *   Optional: Streaming timeouts, if enabled.
 *
 * @author John L. Stanley - SRF Consulting
 */
@SuppressWarnings("serial")
public class VidPanel extends JPanel implements FocusListener {

	/** Current camera */
	private Camera camera;

	/** List of available StreamReq(s) for current camera.
	 * (Only includes those that "should" work in current context.) */
	private List<VidStreamReq> streamReqList = new ArrayList<VidStreamReq>();

	/** Current stream request number */
	private int streamReqNum = 0;

	/** Current stream manager */
	private VidStreamMgr streamMgr;

	/** placeholder gray panel used while stopped */
	protected final JComponent placeholderComponent;

	/** dimension of video */
	private Dimension videoDimension;

	/** Camera PTZ control */
	private CameraPTZ cam_ptz;

	/** Mouse PTZ control */
	private MousePTZ mouse_ptz;

	/** Panel that holds the video component */
	private final JPanel videoHolder;

	/** Automatically start streaming? */
	static private boolean autostart = true;
	
	/** If a connection attempt fails, do we want to
	 *  automatically try the next available stream? */
	static private boolean failover = true;
	
	/** Wait this long before skipping to next available
	 *  stream (or failing the connection attempt). */
	static private int     maxConnectSec = 10;

	/** Wait this long for video to restart until trying to reconnect */
	static private int     lostVideoSec = 10;
	
	/** Do we want to automatically reconnect? */
	static private boolean autoReconnect = true;

	/** Wait this long for a reconnect until retrying. */
	static private int     maxReconnectSec = 10;

	/** Expire a stream after this many seconds */
	static private int     maxDurationSec = 0;

	static private final Color LIGHT_BLUE = new Color(128, 128, 255);
	static private final Color MILD_GREEN = new Color(20, 138, 20);

	static private final int MINUTE_SEC = 60;
	static private final int HOUR_SEC   = 60 * 60;
	static private final int DAY_SEC    = 60 * 60 * 24;

	// Panel status monitor

	static private enum PanelStatus {
		STOPPED,   // blank panel when first created and when stopped
		SCANNING,  // scanning for a viable stream
		VIEWING,   // watching a stream
		FAILED,    // initial scan of streams all failed
		RECONNECT, // auto-reconnecting after a lost stream
		EXPIRED,   // duration timer has expired
	}
	// STOPPED, FAILED, and EXPIRED are similar, but they
	// put different messages in the status line.

	PanelStatus panelStatus = PanelStatus.STOPPED;

	private boolean pausePanel = false;

	private boolean streamError = false;

	/** How long has it been since we received a video frame. */
	private int videoGapSec = 0;

	/** How long has current VIEWING-state stream been running? */
	private int videoDurationSec = 0;

	private String pauseMsg = null;

	/** Set new panel status.
	 *  (May be same as current status.) */
	private synchronized boolean setStatus(PanelStatus ps, String pm) {
		boolean ret = true;
		// start new panel status
		pauseMsg = null;
		panelStatus = ps;
		switch (panelStatus) {
			case STOPPED:
			case FAILED:
			case EXPIRED:
				releaseStreamMgr();
				stopStatusMonitor();
				break;
			case SCANNING:
			case RECONNECT:
				readSystemAttributes();
				startStatusMonitor();
				ret = startCurrentStream();
				break;
			case VIEWING:
				if (mouse_ptz == null)
					startMousePTZ();  // turn mouse PTZ on
				break;
			default:
				// do nothing
				break;
		}
		pauseMsg = pm;
		videoGapSec = 0;
		queueUpdatePanel();
		return ret;
	}

	private boolean setStatus(PanelStatus ps) {
		return setStatus(ps, null);
	}
	
	/** Current status monitor job */
	private StatusMonitor statusMonitor = null;

	/** Status monitor job, called once per second */
	private class StatusMonitor extends Job {
		StatusMonitor() {
			super(Calendar.SECOND, 1);
		}

		@SuppressWarnings("incomplete-switch")
		public void perform2() {
			try {
				if (videoDurationSec < Integer.MAX_VALUE)
					++videoDurationSec;
				if ((panelStatus == PanelStatus.VIEWING)
				 && (mouse_ptz == null))
					startMousePTZ();  // turn mouse PTZ on
				if (mouse_ptz == null) {
					// if mouse_ptz isn't controlling the mouse cursor...
					Cursor c1 = videoHolder.getCursor();
					Cursor c2 = c1;
					switch (panelStatus) {
						case SCANNING:
						case RECONNECT:
							c2 = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
							break;
						case VIEWING:
							c2 = c1;
							break;
						case FAILED:
						case EXPIRED:
						case STOPPED:
							c2 = null;
					}
					if (c1 != c2)
						videoHolder.setCursor(c2);
				}
				switch (panelStatus) {
					case VIEWING:
					case RECONNECT:
					case SCANNING:
						// Check for gap in receiving video frames
						if (getReceivedFrameCount() > 0) {
							videoGapSec = 0;
							if (panelStatus != PanelStatus.VIEWING)
								setStatus(PanelStatus.VIEWING);
						}
						else
							++videoGapSec;
				}
				// Process status-specific tests...
				switch (panelStatus) {
					case SCANNING:
						// Did the video stream start soon enough?
						if (videoGapSec >= maxConnectSec) {
							if (failover) {
								videoGapSec = 0;
								if (startNextStream()) {
									// If stream-timeout is enabled and
									// all streams failed to connect, stop.
									if ((maxDurationSec != 0) && (streamReqNum == 0))
										setStatus(PanelStatus.FAILED, "All Video Sources Failed");
									return;
								}
							}
							setStatus(PanelStatus.FAILED);
						}
						break;
					case VIEWING:
						// Did the video stream fail?
						if (videoGapSec >= lostVideoSec) {
							if (autoReconnect)
								setStatus(PanelStatus.RECONNECT);
							else
								setStatus(PanelStatus.FAILED);
							break;
						}
						// Has the video stream been running too long?
						if (maxDurationSec != 0) { // Infinite max-duration == 0
							if (videoDurationSec >= maxDurationSec)
								setStatus(PanelStatus.EXPIRED, "Stream Expired");
							else
								updateBottomLabel();
						}
						break;
					case RECONNECT:
						// Did the video stream reconnect soon enough?
						if (videoGapSec >= maxReconnectSec) {
							if (autoReconnect)
								setStatus(PanelStatus.RECONNECT);
							else
								setStatus(PanelStatus.FAILED);
						}
				}
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		@Override
		public void perform() {
			try {
				if (statusMonitor == this)
					perform2();
				else
					PANEL_UPDATE.removeJob(this);
			}
			catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		/** Check if this is a repeating job */
		@Override
		public boolean isRepeating() {
			return statusMonitor == this;
		}
	};

	/** Start status monitor */
	private void startStatusMonitor() {
		statusMonitor = new StatusMonitor();
		PANEL_UPDATE.addJob(statusMonitor);
	}

	/** Stop status monitor */
	void stopStatusMonitor() {
		StatusMonitor old = statusMonitor;
		statusMonitor = null;
		PANEL_UPDATE.removeJob(old);
		videoGapSec = 0;
	}

	/** Create fixed-size video panel */
	public VidPanel(Size sz) {
		this(sz.width, sz.height);
	}

	/** Create fixed-size video panel */
	public VidPanel(Dimension dim) {
		this(dim.width, dim.height);
	}

	/** Create fixed-size video panel with specified stream */
	public VidPanel(Dimension dim, int strm_num) {
		this(dim.width, dim.height);
		streamReqNum = strm_num;
	}

	/** Create resizeable video panel */
	public VidPanel(int width, int height) {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		addFocusListener(this);

		videoDimension = new Dimension(width, height);
		placeholderComponent = new JPanel(new BorderLayout());
		placeholderComponent.setPreferredSize(videoDimension);
		placeholderComponent.setMinimumSize(videoDimension);
		placeholderComponent.setBackground(Color.LIGHT_GRAY);

		videoHolder = new JPanel(new BorderLayout());
		videoHolder.setPreferredSize(videoDimension);
		videoHolder.setMinimumSize(videoDimension);
		videoHolder.setBackground(Color.LIGHT_GRAY);
		videoHolder.add(placeholderComponent, BorderLayout.CENTER);

		addTopLabel(" ");
		add(videoHolder, BorderLayout.CENTER);
		addBottomLabel(" ");

		// Catch when panel using this is closed and
		// shut down the stream if it's running.
		addAncestorListener(new AncestorListener() {
			@Override
			public void ancestorAdded(AncestorEvent event) {
				if (panelStatus == PanelStatus.VIEWING)
					startCurrentStream();
			}
			@Override
			public void ancestorMoved(AncestorEvent event) {}
			@Override
			public void ancestorRemoved(AncestorEvent event) {
				releaseStreamMgr();
			}
		});

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent arg0) {
				if (mouse_ptz != null) {
					Dimension sz = videoHolder.getSize();
					mouse_ptz.resize(sz.width, sz.height);
				}
			}
			@Override
			public void componentShown(ComponentEvent e) {
			}
			@Override
			public void componentHidden(ComponentEvent e) {
			}
		});

		setFocusable(true);
		setupKeyBindings();
	}

	/** Setup key bindings on the panel */
	private void setupKeyBindings() {
		//FIXME:  Right-ALT key doesn't catch ALT_DOWN_MASK keystrokes.
		// So RightAlt + (F5, F6, left-arrow, and right-arrow) don't work.
		InputMap im = getInputMap(
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap am = getActionMap();

		/* Alt+RightArrow - Start next stream */
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT,
				KeyEvent.ALT_DOWN_MASK), "startNextStream");
		am.put("startNextStream", startNextStreamAction);

		/* Alt+LeftArrow - Start previous stream */
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT,
				KeyEvent.ALT_DOWN_MASK), "startPreviousStream");
		am.put("startPreviousStream", startPreviousStreamAction);

		/* F5 or Alt+F5 - Restart stream */
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5,
				KeyEvent.ALT_DOWN_MASK), "restartStream");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0),
				"restartStream");
		am.put("restartStream", restartStreamAction);

		/* F6 or Alt+F6 - Stop stream */
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6,
				KeyEvent.ALT_DOWN_MASK), "stopStream");
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0),
				"stopStream");
		am.put("stopStream", stopStreamAction);

		/* Shift+F5 - Restart all open layout streams */
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5,
				KeyEvent.SHIFT_DOWN_MASK), "restartOpenStreams");
		am.put("restartOpenStreams", restartOpenStreamsAction);

		/* Shift+F6 - Stop all open layout streams */
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6,
				KeyEvent.SHIFT_DOWN_MASK), "stopOpenStreams");
		am.put("stopOpenStreams", stopOpenStreamsAction);

//		/* Alt + forward-slash - Pause stream */
//		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SLASH,
//				KeyEvent.ALT_DOWN_MASK), "pauseStream");
//		am.put("pauseStream", pauseStreamAction);
	}

	private Action startNextStreamAction = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			startNextStream();
		}
	};

	private Action startPreviousStreamAction = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			startPreviousStream();
		}
	};

	/** Restart the stream that is currently playing. */
	private Action restartStreamAction = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			restartStream();
		}
	};

	/** Stop the stream that is currently playing. */
	private Action stopStreamAction = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			setStatus(PanelStatus.STOPPED);
		}
	};

	/** Restart all open layout streams. */
	private Action restartOpenStreamsAction = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			StreamControlPanel.restartOpenLayouts();
		}
	};

	/** Stop all open layout streams. */
	private Action stopOpenStreamsAction = new AbstractAction() {
		@Override
		public void actionPerformed(ActionEvent e) {
			StreamControlPanel.stopOpenLayouts();
		}
	};

	Dimension getVideoDimension() {
		return videoDimension;
	}

	VidStreamMgr getStreamMgr() {
		return streamMgr;
	}

	//-------------------------------------------
	// The updatePanelJob job is run 0.1 seconds
	// after queueUpdatePanel() is called.  If
	// it's called more than once, the 0.1 sec
	// delay is reset, allowing several things
	// to be changed with only 1 updatePanelJob.

	static private boolean isNothing(String str) {
		return ((str == null) || str.isEmpty());
	}

	/** Shared VideoPanel update thread */
	static protected final Scheduler
		PANEL_UPDATE = new Scheduler("VideoPanels");

	/** current job to rebuild the panel */
	private UpdatePanelJob updatePanelJob = 
			new UpdatePanelJob();

	private JLabel bottomLabel;

	private String bottomLableText;

	/** job class that rebuilds the panel */
	class UpdatePanelJob extends Job {

		public UpdatePanelJob() {
			super(100);
		}

		public void perform() {
			Camera     cam;
			VidStreamMgr smgr;
			removeAll();
			String bottomMsg = "";
			synchronized (this) {
				cam  = camera;
				smgr = streamMgr;
			}
			if (cam == null) {
				// no camera selected
				addTopLabel("");
				addVideo(placeholderComponent);
			}
			else {
				// camera selected
				addTopLabel(cam.getName());
				if (smgr == null) {
					// no stream manager
					addVideo(placeholderComponent);
				}
				else {
					// stream manager available
					JComponent vc = smgr.getComponent();
					addVideo(vc);
					String lbl = smgr.getLabel();
					String msg = smgr.getErrorMsg();
					streamError = !isNothing(msg);
					if (streamError)
						bottomMsg = lbl+": "+msg;
					else if (panelStatus != PanelStatus.VIEWING) {
						msg = smgr.getStatus();
						if (isNothing(msg))
							bottomMsg = lbl;
						else
							bottomMsg = lbl+": "+msg;
					}
					else
						bottomMsg = lbl;
				}
				if (!isNothing(pauseMsg)) {
					streamError = false;
					bottomMsg = pauseMsg;
				}
			}
			boolean bHardFail = (panelStatus == PanelStatus.FAILED) && (pauseMsg != null);
			if (bHardFail)
				addBottomLabel(bottomMsg, Color.ORANGE, Color.GRAY);
			else if (streamError)
				addBottomLabel(bottomMsg, Color.BLACK, Color.ORANGE);
			else
				addBottomLabel(bottomMsg);
			revalidate();
			repaint();
			queueFireChangeListeners();
		}

		@Override
		public boolean isRepeating() {
			return false;
		}
	};

	/** Substitute camera-name label to show PtzInfo tooltip */
	private class CameraNameLabel extends JLabel {
		@Override
		public String getToolTipText(MouseEvent evt) {
			Camera cam = camera;
			if (cam == null)
				return null;
			String txt = CameraHelper.getPtzInfo(camera);
			setToolTipText(txt);
			return txt;
		}
	}

	/** Add top label (camera-name) line */
	private void addTopLabel(String txt) {
		Color nameColorBG;
		Color nameColorFG = Color.BLACK;
		if (VidPanel.this.isFocusOwner())
			nameColorBG = pausePanel
					? Color.BLUE
					: Color.WHITE;
		else
			nameColorBG = pausePanel
					? LIGHT_BLUE
					: Color.LIGHT_GRAY;
		if (pausePanel)
			nameColorFG = Color.WHITE;
		JLabel lbl = new CameraNameLabel();
		configureLabel(lbl, txt, nameColorFG, nameColorBG, nameColorFG);
		add(lbl, BorderLayout.NORTH);
	}

	private void addVideo(JComponent vidcomp) {
		videoHolder.removeAll();
		videoHolder.add(vidcomp, BorderLayout.CENTER);
		add(videoHolder);
	}

	/** Add bottom label line */
	private void addBottomLabel(String txt) {
		addBottomLabel(txt, null, null);
	}

	/** Add bottom label line, with optional colors */
	private void addBottomLabel(String txt, Color fgColor, Color bgColor) {
		JLabel lbl = new JLabel();
		bottomLabel = lbl;
		bottomLableText = txt;
		configureLabel(lbl, "", fgColor, bgColor, MILD_GREEN);
		add(lbl, BorderLayout.SOUTH);
		if (panelStatus != PanelStatus.VIEWING)
			bottomLabel.setText(bottomLableText);
		else
			updateBottomLabel();
	}
	
	/** Update text in bottom label including time-remaining count-down timer */
	private void updateBottomLabel() {
		if ((bottomLabel == null) || (bottomLableText == null))
			return;
		if (maxDurationSec == 0) {
			bottomLabel.setText(bottomLableText);
			return;
		}
		int sec = maxDurationSec - videoDurationSec;
		String str;
		float f;
		if (sec >= DAY_SEC) {
			f = (float)sec / DAY_SEC;
			str =  String.format("%.0f+ days remaining", f);
		}
		else if (sec >= HOUR_SEC) {
			f = (float)sec / HOUR_SEC;
			str =  String.format("%.1f+ hours remaining", f);
		}
		else if (sec >= MINUTE_SEC) {
			f = (float)sec / MINUTE_SEC;
			str =  String.format("%.1f+ minutes remaining", f);
		}
		else {
			str =  String.format("%d seconds remaining", sec);
		}
		bottomLabel.setText("<html>"+bottomLableText+"&nbsp;&nbsp;&nbsp;<small>"+str);
	}

	/** Configure a label.
	 * (Adds a tool-tip if the text is wider than the label */
	private JLabel configureLabel(
			JLabel lbl, String txt,
			Color fgColor, Color bgColor, Color expiredColor) {
		lbl.setText(txt);
		lbl.setHorizontalAlignment(JLabel.CENTER);
		FontMetrics lblFontMetrics = lbl.getFontMetrics(lbl.getFont());
		if (panelStatus == PanelStatus.EXPIRED)
			fgColor = expiredColor;
		if (fgColor != null)
			lbl.setForeground(fgColor);
		if (bgColor != null) {
			lbl.setOpaque(true);
			lbl.setBackground(bgColor);
		}
		lbl.setAlignmentX(CENTER_ALIGNMENT);
		int txtWidth = lblFontMetrics.stringWidth(txt) + 2;
		int lblWidth = lbl.getWidth();
		if (txtWidth > lblWidth)
			lbl.setToolTipText(txt);
		else
			lbl.setToolTipText(null);
		return lbl;
	}

	public synchronized void queueUpdatePanel() {
		UpdatePanelJob up = updatePanelJob;
		if (up != null)
			PANEL_UPDATE.removeJob(up);
		updatePanelJob = new UpdatePanelJob();
		PANEL_UPDATE.addJob(updatePanelJob);
	}

	public void focusGained(FocusEvent fe) {
		queueUpdatePanel();
	}

	public void focusLost(FocusEvent fe){
		queueUpdatePanel();
	}

	//-------------------------------------------
	// Methods to set camera and manage streaming

	/** Set camera, initialize sreqList,
	 *  and start playing first stream.
	 *
	 * @param c Camera
	 * @return true if stream available, false if none available.
	 */
	public boolean setCamera(Camera cam) {
		releaseStreamMgr();
		camera = cam;

		readSystemAttributes();

		Session s = Session.getCurrent();
		streamReqList = VidStreamReq.getVidStreamReqs(camera);
		streamReqNum = 0;
		cam_ptz = new CameraPTZ(s);
		cam_ptz.setCamera(cam);
		boolean ret = !streamReqList.isEmpty();
		panelStatus = PanelStatus.STOPPED;
		if (autostart) {
			videoDurationSec = 0;
			ret = setStatus(PanelStatus.SCANNING);
		}
		return ret;
	}

	static private void readSystemAttributes() {
		autostart       = SystemAttrEnum.VID_CONNECT_AUTOSTART.getBoolean();
		failover        = SystemAttrEnum.VID_CONNECT_FAIL_NEXT_SOURCE.getBoolean();
		maxConnectSec   = SystemAttrEnum.VID_CONNECT_FAIL_SEC.getInt();
		lostVideoSec    = SystemAttrEnum.VID_LOST_TIMEOUT_SEC.getInt();
		autoReconnect   = SystemAttrEnum.VID_RECONNECT_AUTO.getBoolean();
		maxReconnectSec = SystemAttrEnum.VID_RECONNECT_TIMEOUT_SEC.getInt();
		maxDurationSec  = SystemAttrEnum.VID_MAX_DURATION_SEC.getInt();
	}
	
	/** Create a mouse PTZ */
	static private MousePTZ createMousePTZ(CameraPTZ cam_ptz,
			Dimension sz,
			JPanel video_pnl)
	{
		return (cam_ptz != null)
		      ? new MousePTZ(cam_ptz, sz, video_pnl)
		      : null;
	}

	private void startMousePTZ() {
		if (mouse_ptz == null) {
			videoDimension = videoHolder.getSize();
			mouse_ptz = createMousePTZ(cam_ptz, videoDimension, videoHolder);
		}
	}
	
	private void stopMousePTZ() {
		if (mouse_ptz != null) {
			mouse_ptz.dispose();
			mouse_ptz = null;
		}
	}
	
	/** Start/restart playing stream number n.
	 * Automatically wraps at both ends of
	 * request list.  Returns false if no
	 * stream is available. */
	private boolean playStream(int snum) {
		releaseStreamMgr();
		List<VidStreamReq> srl = streamReqList;
		int len = (srl == null) ? 0 : srl.size();
		if (len == 0) {
			streamReqNum = 0;
			queueUpdatePanel();
			return false;
		}
		if (snum < 0)
			snum = len - 1;
		else if (snum >= len)
			snum = 0;
		streamReqNum = snum;
		startStreamMgr(srl.get(snum));
		queueUpdatePanel();
		return true;
	}

	/** Start playing previous stream */
	private boolean startPreviousStream() {
		return playStream(streamReqNum - 1);
	}

	/** Start or restart playing current stream */
	private boolean startCurrentStream() {
		return playStream(streamReqNum);
	}

	/** Start playing next stream */
	private boolean startNextStream() {
		return playStream(streamReqNum + 1);
	}

	/** Stop playing current stream.
	 * (Blanks the video portion of the panel.) */
	public void stopStream() {
		setStatus(PanelStatus.STOPPED);
	}

	/** Restart playing current stream. */
	public void restartStream() {
		videoDurationSec = 0;
		setStatus(PanelStatus.SCANNING);
	}

	/** Release the current stream manager
	 * (Blanks the video portion of the panel.) */
	void releaseStreamMgr() {
		stopMousePTZ();
		videoHolder.setCursor(null);
		VidStreamMgr vmOld = streamMgr;
		if (vmOld != null) {
			vmOld.queueStopStream();
			streamMgr = null;
		}
		queueUpdatePanel();
	}

	/** Start a StreamMgr from a StreamReq */
	private void startStreamMgr(VidStreamReq sreq) {
		releaseStreamMgr();
		if (sreq == null)
			return;
		if (sreq.isGst())
			streamMgr = new VidStreamMgrGst(this, sreq);
		else if (sreq.isMJPEG())
			streamMgr = new VidStreamMgrMJPEG(this, sreq);
		else
			return;  // Should never happen..
		streamMgr.queueStartStream();
		queueUpdatePanel();
	}

	/**
	 * @return
	 */
	public boolean isStreaming() {
		VidStreamMgr sm = streamMgr;
		if (sm == null)
			return false;
		return sm.isStreaming();
	}

	/**
	 * Gets number of frames received since this
	 * was last called.
	 */
	private int getReceivedFrameCount() {
		VidStreamMgr sm = streamMgr;
		if (sm == null)
			return 0;
		return sm.getReceivedFrameCnt();
	}

	//-------------------------------------------
	// Include a ChangeListener interface

	public void addChangeListener(ChangeListener listener) {
		listenerList.add(ChangeListener.class, listener);
	}

	public void removeChangeListener(ChangeListener listener) {
		listenerList.remove(ChangeListener.class, listener);
	}

	public ChangeListener[] getChangeListeners() {
		return listenerList.getListeners(ChangeListener.class);
	}

	/** Job to call any ChangeListeners */
	private final Job fireChangeListenersJob = new Job(100) {
		public void perform() {
			ChangeEvent event = new ChangeEvent(this);
			for (ChangeListener listener : getChangeListeners()) {
				listener.stateChanged(event);
			}
		}
	};

	/** Queue job to fire any ChangeListeners */
	private void queueFireChangeListeners() {
		PANEL_UPDATE.removeJob(fireChangeListenersJob);
		PANEL_UPDATE.addJob(fireChangeListenersJob);
	}

	/** Initialize popout-panel tooltip for blank no-video background */
	public void initPopoutTooltip() {
		String ttt = I18N.get("vid.blank.tooltip");
		placeholderComponent.setToolTipText(ttt);
	}
}
