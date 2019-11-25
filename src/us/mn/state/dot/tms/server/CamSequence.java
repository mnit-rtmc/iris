/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2018-2019  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.Catalog;
import us.mn.state.dot.tms.CatalogHelper;
import us.mn.state.dot.tms.PlayList;
import us.mn.state.dot.tms.PlayListHelper;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * A camera sequence is either a play list or a play catalog.
 *
 * @author Douglas Lau
 */
public class CamSequence {

	/** Get the dwell time (seconds) */
	static private int getDwellSec() {
		return SystemAttrEnum.CAMERA_SEQUENCE_DWELL_SEC.getInt();
 	}

	/** Dwell time paused value */
	static private final int DWELL_PAUSED = -1;

	/** Maximum number of empty play lists to check */
	static private final int MAX_EMPTY_PLAY_LISTS = 10;

	/** Create camera sequence */
	public CamSequence(int sn) {
		seq_num = sn;
		catalog = CatalogHelper.findSeqNum(sn);
		c_item = 0;
		play_list = (null == catalog)
		          ? PlayListHelper.findSeqNum(sn)
		          : null;
		pl_item = -1;	// nextItem will advance to 0
		dwell = 0;
	}

	/** Create camera sequence */
	public CamSequence(PlayList pl) {
		seq_num = (pl != null) ? pl.getSeqNum() : null;
		catalog = null;
		c_item = 0;
		play_list = pl;
		pl_item = -1;	// nextItem will advance to 0
		dwell = 0;
	}

	/** Camera sequence number */
	private final Integer seq_num;

	/** Running catalog (or null for play list sequence) */
	private final Catalog catalog;

	/** Item in catalog */
	private int c_item;

	/** Running play list */
	private final PlayList play_list;

	/** Item in play list */
	private int pl_item;

	/** Remaining dwell time (negative means paused) */
	private int dwell;

	/** Check if sequence is valid */
	public boolean isValid() {
		return catalog != null || play_list != null;
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

	/** Go to the next item */
	public void goNextItem() {
		resetDwell();
		nextItem();
	}

	/** Reset dwell time */
	private void resetDwell() {
		dwell = (dwell >= 0) ? getDwellSec() : DWELL_PAUSED;
	}

	/** Check if sequence is running */
	public boolean isRunning() {
		return dwell > DWELL_PAUSED;
	}

	/** Advance to the next item */
	private void nextItem() {
		final int i = pl_item;
		nextItemPlayList();
		// Check if we rolled over to next play list
		if (catalog != null && (0 == pl_item) && (i > 0))
			nextItemCatalog();
	}

	/** Advance to the next item in play list */
	private void nextItemPlayList() {
		pl_item = (pl_item < lastPlayListItem()) ? pl_item + 1 : 0;
	}

	/** Get the last item in play list */
	private int lastPlayListItem() {
		PlayList pl = getPlayList();
		return (pl != null) ? pl.getCameras().length - 1 : 0;
	}

	/** Advance to the next item in catalog */
	private void nextItemCatalog() {
		final int c_last = catalog.getPlayLists().length - 1;
		for (int i = 0; i < MAX_EMPTY_PLAY_LISTS; i++) {
			c_item = (c_item < c_last) ? c_item + 1 : 0;
			assert 0 == pl_item; // first item in play list
			// Skip play lists with no cameras
			if (getCamera() != null)
				break;
		}
	}

	/** Get the current camera */
	public Camera getCamera() {
		PlayList pl = getPlayList();
		return (pl != null) ? getCamera(pl) : null;
	}

	/** Get the current camera in a play list */
	private Camera getCamera(PlayList pl) {
		Camera[] cams = pl.getCameras();
		return (pl_item < cams.length) ? cams[pl_item] : null;
	}

	/** Get the current play list */
	private PlayList getPlayList() {
		return (catalog != null) ? getCatalogPlayList() : play_list;
	}

	/** Get the current catalog play list */
	private PlayList getCatalogPlayList() {
		PlayList[] pls = catalog.getPlayLists();
		return (c_item < pls.length) ? pls[c_item] : null;
	}

	/** Go to the previous item */
	public void goPrevItem() {
		resetDwell();
		prevItem();
	}

	/** Revert to the previous item */
	private void prevItem() {
		final int i = pl_item;
		prevItemPlayList();
		// Check if we rolled over to previous play list
		if (catalog != null && (pl_item > i))
			prevItemCatalog();
	}

	/** Revert to the previous item in play list */
	private void prevItemPlayList() {
		pl_item = (pl_item > 0) ? pl_item - 1 : lastPlayListItem();
	}

	/** Revert to the previous item in catalog */
	private void prevItemCatalog() {
		final int c_last = catalog.getPlayLists().length - 1;
		for (int i = 0; i < MAX_EMPTY_PLAY_LISTS; i++) {
			c_item = (c_item > 0) ? c_item - 1 : c_last;
			pl_item = lastPlayListItem();
			// Skip play lists with no cameras
			if (getCamera() != null)
				break;
		}
	}
}
