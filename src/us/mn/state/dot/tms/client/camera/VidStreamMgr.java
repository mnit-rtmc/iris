/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2019-2020  SRF Consulting Group
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
import javax.swing.JComponent;

import us.mn.state.dot.sched.Job;
import us.mn.state.dot.sched.Scheduler;
import us.mn.state.dot.tms.utils.I18N;

/** Parent class for objects that manage a video stream.
 * 
 * Child classes:
 * 		StreamMgrGst
 * 		StreamMgrMJPG
 * 
 * @author John L. Stanley - SRF Consulting
 */
@SuppressWarnings("serial")
public abstract class VidStreamMgr {

	/** Streamer job scheduler */
	protected static Scheduler STREAM_SCHED = new Scheduler("StreamMgr");

	/** Default timeout for direct URL Connections */
//	static protected final int TIMEOUT_DIRECT = 5 * 1000;
	static protected final int TIMEOUT_DIRECT = 500 * 1000;

	/** Milliseconds between updates to the status */
	static protected final int STATUS_DELAY = 1000;

//	static protected final Size baseSize = Size.SMALL;
//
//	static protected final Dimension baseDimension =
//			new Dimension(baseSize.width, baseSize.height);

	//-------------------------------------------
	// Data fields

	/** VideoPanel that holds the video component */
	protected VidPanel videoPanel;

	/** StreamReq used to initialize this video manager */
	protected final VidStreamReq vreq;

	/** Current video component */
	private JComponent vcomponent;

	/** Current status */
	private String sStatus = "";

	/** Current error (if any) */
	private String sCurErrMsg = null;
	
	/** Has streaming actually started? */
	private boolean bStreamingStarted = false;
	
	/** StreamTemplate label */
	protected final String templateLabel;

	//-------------------------------------------
	// constructor
	
	/** Parent stream-manager constructor
	 * This should only be called by StreamMgr child
	 * constructors (StreamMgrMJPEG or StreamMgrGst).
	 * 
	 * @param vp Video panel used to display stream
	 * @param vr Stream request used to define stream
	 */
	protected VidStreamMgr(VidPanel vp, VidStreamReq vr) {
		this.videoPanel = vp;
		this.vreq       = vr;
		vcomponent      = videoPanel.placeholderComponent;
		templateLabel   = vr.getVidSourceTemplate().getLabel();
	}

	//-------------------------------------------
	// Abstract methods defined by child classes

	/** Are we currently streaming? */
	public abstract boolean isStreaming();

	// The following two are Called from the STREAM_SCHED thread.
	protected abstract void doStartStream();
	protected abstract void doStopStream();

	//-------------------------------------------
	// Call doStartStream or doStopStream from
	// the STREAM_SCHED thread.

	/** Queue a start stream operation */
	protected void queueStartStream() {
//		System.out.println("VidStreamMgr.queueStartStream()");
		setStatus(I18N.get("camera.stream.opening"));
//		setStatus("Starting "+vreq.getVidSourceTemplate().getLabel());
		bStreamingStarted = false;
		STREAM_SCHED.addJob(jobStartStream);
	}
	
	/** Job that actually starts the stream
	  * (Called from the STREAM_SCHED thread.) */
	private final Job jobStartStream = new Job(300) {
		public void perform() {
//			System.out.println("VidStreamMgr.jobStartStream.perform()");
			clearErrorMsg();
			doStartStream();
		}
	};

	/** Tell VideoPanel that the video is streaming. */
	protected void streamingStarted() {
		if (!bStreamingStarted) {
			bStreamingStarted = true;
			setStatus("");
		}
	}


	/** Tell VideoPanel that the video has stopped. */
	protected void streamingStopped() {
		if (bStreamingStarted) {
			bStreamingStarted = false;
			videoPanel.queueUpdatePanel();
		}
	}

	/** Queue a stop stream operation */
	public void queueStopStream() {
		STREAM_SCHED.removeJob(jobStartStream);
		if (isStreaming()) {
			System.out.println("VidStreamMgr.queueStopStream()");
			STREAM_SCHED.addJob(jobStopStream);
		}
	}

	/** Job that actually stops the stream
	  * (Called from the STREAM_SCHED thread.) */
	private final Job jobStopStream = new Job() {
		public void perform() {
			System.out.println("VidStreamMgr.jobStopStream.perform()");
			doStopStream();
//			if (disposing)
//				STREAM_SCHED.dispose();
		}
	};

	//-------------------------------------------
	// Methods to pass info to the video panel
	// (The isXXX() and getXXX() methods in this
	//  section are called by the VideoPanel.)

	/** Get the template label */
	public String getLabel() {
		return templateLabel;
	}

	/** Set the status. */
	protected void setStatus(String stat) {
//		System.out.println("== VidStreamMgr.SetStatus(\""+stat+"\")");
		if (stat == null)
			stat = "";
		if (sStatus.equals(stat))
			return;
		sStatus = stat;
		videoPanel.queueUpdatePanel();
	}

	/** Get the status */
	public String getStatus() {
		return sStatus;
	}
	
	//-----
	
	private int receivedFrameCnt;

	public int getReceivedFrameCnt() {
		int cnt = receivedFrameCnt;
		receivedFrameCnt = 0;
		return cnt;
	}
	
	protected void incReceivedFrameCount() {
		++receivedFrameCnt;
	}

	//-----
	
	/** Clear the stream error message */
	protected void clearErrorMsg() {
		if (sCurErrMsg == null)
			return;
		System.out.println("== VidStreamMgr.clearErrorMsg()");
		sCurErrMsg = null;
		videoPanel.queueUpdatePanel();
	}

	/** Set a stream error message.
	 * (Latches first reported error.) */
	protected void setErrorMsg(String errMsg) {
		System.out.println("== VidStreamMgr.setErrorMsg(\""+errMsg+"\")");
		if ((sCurErrMsg != null)
		 || (errMsg == null)
		 || errMsg.isEmpty())
			return;
		sCurErrMsg = errMsg;
		videoPanel.queueUpdatePanel();
	}

	/** Set a stream error message from an exception.
	 * (Latches first reported error.)
	 * Uses the defaultMsg if the exception has no message. */
	protected void setErrorMsg(Exception ex, String defaultMsg) {
		String errMsg = ex.getMessage();
		System.out.println("== VidStreamMgr.setErrorMsg2(\""+errMsg+"\")");
		if ((errMsg == null) || errMsg.isEmpty())
			errMsg = defaultMsg;
		setErrorMsg(errMsg);
	}

	/** Get the current error message.
	 * If none, returns an empty string. */
	public String getErrorMsg() {
		return sCurErrMsg;
	}

	//-----

	/** Set the video component. */
	protected void setComponent(JComponent vc) {
//		String tmp = "<null>";
//		if (vc != null)
//			tmp = vc.toString();
//		System.out.println("== VidStreamMgr.setComponent(\""+tmp+"\")");

		if (vc == null)
			vc = videoPanel.placeholderComponent;
		if (!vc.equals(vcomponent)) {
			vcomponent = vc;
			Dimension dim = videoPanel.getVideoDimension();
			vcomponent.setPreferredSize(dim);
			vcomponent.setMinimumSize(dim);
			videoPanel.queueUpdatePanel();
		}
	}

	/** Get the video component */
	public JComponent getComponent() {
		return vcomponent;
	}

	//-------------------------------------------

	public void dispose() {
		queueStopStream();
	}

	//-------------------------------------------

	/** Create a video manager of the appropriate type */
	public VidStreamMgr create(VidPanel vp, VidStreamReq vr) {
		if (vreq.isGst())
			return new VidStreamMgrGst(vp, vr);
		else if (vreq.isMJPEG())
			return new VidStreamMgrMJPEG(vp, vr);
		return null; // <-- should never happen
	}
}
