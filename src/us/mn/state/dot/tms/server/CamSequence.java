/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2024  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.CameraHelper;
import us.mn.state.dot.tms.PlayList;
import us.mn.state.dot.tms.PlayListHelper;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * A camera sequence is a play list with a selected entry.
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
		seq_num = (pl != null) ? pl.getSeqNum() : null;
		play_list = pl;
		pl_item = 0;
		sl_item = 0;
		dwell = getDwellSec();
	}

	/** Create camera sequence */
	public CamSequence(int sn) {
		this(PlayListHelper.findSeqNum(sn));
	}

	/** Camera sequence number */
	private final Integer seq_num;

	/** Running play list */
	private final PlayList play_list;

	/** Item in play list */
	private int pl_item;

	/** Item in sub list */
	private int sl_item;

	/** Remaining dwell time (negative means paused) */
	private int dwell;

	/** Check if sequence is valid */
	public boolean isValid() {
		return play_list != null;
	}

	/** Check if the play list is meta */
	private boolean isMeta() {
		return (play_list != null) && play_list.getMeta();
	}

	/** Get the sequence number */
	public Integer getSeqNum() {
		return seq_num;
	}

	/** Pause the sequence */
	public void pause() {
		dwell = DWELL_PAUSED;
	}

	/** Unpause the sequence */
	public void unpause() {
		dwell = getDwellSec();
	}

	/** Update dwell time */
	public void updateDwell() {
		if (dwell > 0)
			dwell--;
		else if (0 == dwell) {
			dwell = getDwellSec();
			nextItem();
		} else
			dwell = DWELL_PAUSED;
	}

	/** Reset dwell time */
	private void resetDwell() {
		dwell = (dwell >= 0) ? getDwellSec() : DWELL_PAUSED;
	}

	/** Get the current camera */
	public Camera getCamera() {
		PlayList pl = play_list;
		int i = pl_item;
		if (isMeta()) {
			pl = getSubList();
			i = sl_item;
		}
		if (pl != null) {
			String[] ents = pl.getEntries();
			if (i < ents.length)
				return CameraHelper.lookup(ents[i]);
		}
		return null;
	}

	/** Get current meta sub list */
	private PlayList getSubList() {
		if (isMeta()) {
			String[] sl = play_list.getEntries();
			if (pl_item < sl.length)
				return PlayListHelper.lookup(sl[pl_item]);
		}
		return null;
	}

	/** Check if sequence is running */
	public boolean isRunning() {
		return dwell > DWELL_PAUSED;
	}

	/** Go to the next item */
	public void goNextItem() {
		resetDwell();
		nextItem();
	}

	/** Advance to the next item */
	private void nextItem() {
		if (isMeta()) {
			// Skip sub lists with no cameras
			for (int i = 0; i < size(); i++) {
				nextItemMeta();
				if (getCamera() != null)
					break;
			}
		} else
			nextItemPlayList();
	}

	/** Advance to the next item in a meta play list */
	private void nextItemMeta() {
		sl_item++;
		if (getCamera() == null) {
			nextItemPlayList();
			sl_item = 0;
		}
	}

	/** Advance to the next item in play list */
	private void nextItemPlayList() {
		pl_item++;
		if (pl_item >= size())
			pl_item = 0;
	}

	/** Get size of play list */
	private int size() {
		return (play_list != null) ? play_list.getEntries().length : 0;
	}

	/** Get size of meta sub play list */
	private int subSize() {
		PlayList pl = getSubList();
		return (pl != null) ? pl.getEntries().length : 0;
	}

	/** Go to the previous item */
	public void goPrevItem() {
		resetDwell();
		prevItem();
	}

	/** Revert to the previous item */
	private void prevItem() {
		if (isMeta()) {
			// Skip sub lists with no cameras
			for (int i = 0; i < size(); i++) {
				prevItemMeta();
				if (getCamera() != null)
					break;
			}
		} else
			prevItemPlayList();
	}

	/** Revert to the previous item in a meta play list */
	private void prevItemMeta() {
		if (sl_item == 0) {
			prevItemPlayList();
			sl_item = subSize();
		}
		if (sl_item > 0)
			sl_item--;
	}

	/** Revert to the previous item in play list */
	private void prevItemPlayList() {
		if (pl_item == 0)
			pl_item = size();
		if (pl_item > 0)
			pl_item--;
	}
}
