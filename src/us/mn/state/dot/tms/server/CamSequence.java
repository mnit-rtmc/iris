/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018  Minnesota Department of Transportation
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
package us.mn.state.dot.tms.server;

import us.mn.state.dot.tms.Camera;
import us.mn.state.dot.tms.PlayList;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * A camera sequence is either a play list or a play catalog.
 *
 * @author Douglas Lau
 */
public class CamSequence {

	/** Get the dwell time (seconds) */
	static private int getDwellSec() {
		return SystemAttrEnum.CAMERA_PLAYLIST_DWELL_SEC.getInt();
 	}

	/** Dwell time paused value */
	static private final int DWELL_PAUSED = -1;

	/** Create camera sequence */
	public CamSequence(PlayList pl) {
		play_list = pl;
		item = -1;	// nextItem will advance to 0
		dwell = 0;
	}

	/** Running play list */
	private final PlayList play_list;

	/** Item in play list */
	private int item;

	/** Remaining dwell time (negative means paused) */
	private int dwell;

	/** Get the sequence number */
	public Integer getNum() {
		return (play_list != null)
		      ? play_list.getNum()
		      : null;
	}

	/** Pause the sequence */
	public void pause() {
		dwell = DWELL_PAUSED;
	}

	/** Unpause the sequence */
	public void unpause() {
		dwell = getDwellSec();
	}

	/** Update dwell time.
	 * @return Next camera in sequence, or null for no change. */
	public Camera updateDwell() {
		if (dwell > 0) {
			dwell--;
			return null;
		} else if (0 == dwell) {
			dwell = getDwellSec();
			return nextItem();
		} else {
			// paused
			return null;
		}
	}

	/** Get next item */
	public Camera nextItem() {
		Camera[] cams = play_list.getCameras();
		item = (item + 1 < cams.length) ? item + 1 : 0;
		return (item < cams.length) ? cams[item] : null;
	}

	/** Go to the next item */
	public Camera goNextItem() {
		resetDwell();
		Camera[] cams = play_list.getCameras();
		item = (item + 1 < cams.length) ? item + 1 : 0;
		return (item < cams.length) ? cams[item] : null;
	}

	/** Go to the previous item */
	public Camera goPrevItem() {
		resetDwell();
		Camera[] cams = play_list.getCameras();
		item = (item > 0) ? item - 1 : cams.length - 1;
		return (item < cams.length) ? cams[item] : null;
	}

	/** Reset dwell time */
	private void resetDwell() {
		dwell = (dwell >= 0) ? getDwellSec() : DWELL_PAUSED;
	}

	/** Check if sequence is running */
	public boolean isRunning() {
		return dwell > DWELL_PAUSED;
	}
}
