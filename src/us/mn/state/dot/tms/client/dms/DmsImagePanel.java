/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2020  SRF Consulting Group
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

import static us.mn.state.dot.tms.units.Interval.Units.MILLISECONDS;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

import us.mn.state.dot.tms.InvalidMsgException;
import us.mn.state.dot.tms.PageTimeHelper;
import us.mn.state.dot.tms.client.widget.ImagePanel;
import us.mn.state.dot.tms.units.Interval;
import us.mn.state.dot.tms.units.Interval.Units;
import us.mn.state.dot.tms.utils.MultiConfig;
import us.mn.state.dot.tms.utils.wysiwyg.WMessage;
import us.mn.state.dot.tms.utils.wysiwyg.WPage;
import us.mn.state.dot.tms.utils.wysiwyg.WRaster;

/**
 * Panel for displaying a DMS image. Supports showing multiple pages with
 * the page timing indicated by the MULTI string or sign defaults.
 *
 * @author Gordon Parikh - SRF Consulting
 * @author Doug Lau
 */
@SuppressWarnings("serial")
public class DmsImagePanel extends ImagePanel {
	
	/** WMessage for rendering messages */
	private WMessage wmsg;
	
	/** All pages of the message */
	private WPage[] pages;
	
	/** Number of pages */
	private int nPages;
	
	/** Current page number (0-indexed) */
	private int pageNum = 0;
	
	/** WRasters containing message pages */
	private WRaster[] rasters;
	
	/** Array of all images (one for each page) */
	private BufferedImage[] pgImages;
	
	/** Image of a blank sign */
	private BufferedImage blankSign;
	
	/** Whether or not to use preview image (instead of WYSIWYG image) */
	private boolean preview;
	
	/** MultiConfig for rendering sign panel */
	private MultiConfig mConfig;
	
	/** MULTI string of the message */
	private String multi;

	/** Time period for timer tick which updates panel */
	static private final int TIMER_TICK_MS = 100;
	
	/** Time elapsed (in milliseconds) since last phase change. */
	private int phaseMS = 0;
	
	/** Current state of sign (blank or not) */
	private boolean isBlank;
	
	/** Timer for updating page displayed */
	private final Timer timer = new Timer(TIMER_TICK_MS, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			pageTimerTick();
		}
	});
	
	/** Create a new DmsImagePanel with width w and height h. If usePreview is
	 *  true, the preview image is displayed (instead of the WYSIWYG image).
	 */
	public DmsImagePanel(int w, int h, boolean usePreview) {
		super(w, h);
		preview = usePreview;
	}
	
	/** Dispose of the panel */
	public void dispose() {
		stopTimer();
	}

	/** Restart the timer and all related variables. Shows the first page of
	 *  the message (if any, otherwise blank). */
	private void restartTimer() {
		phaseMS = 0;
		pageNum = 0;
		firstPage();
		timer.restart();
	}
	
	/** Stop the timer (and reset related variables). */
	private void stopTimer() {
		phaseMS = 0;
		pageNum = 0;
		timer.stop();
	}
	
	/** Update the phase timer by one tick. */
	private void pageTimerTick() {
		phaseMS += TIMER_TICK_MS;
		if (doTick()) {
			phaseMS = 0;
			if (isBlank)
				makeBlank();
			else
				nextPage();
		}
	}

	/** Update the timer for one tick.
	 * @return True if panel needs updating. */
	private boolean doTick() {
		return isBlank ? doTickOff() : doTickOn();
	}

	/** Update the timer for one tick while blanking.
	 * @return True if panel needs updating. */
	private boolean doTickOff() {
		if (phaseMS >= currentPageOffMs()) {
			isBlank = false;
			return true;
		}
		return false;
	}

	/** Get page-off time for current page */
	private int currentPageOffMs() {
		return new Interval(pages[pageNum].getPageOff(),
				Units.DECISECONDS).round(MILLISECONDS);
	}

	/** Update the timer for one tick while displaying a page.
	 * @return True if panel needs updating. */
	private boolean doTickOn() {
		if (phaseMS >= currentPageOnMs()) {
			isBlank = (currentPageOffMs() > 0);
			return true;
		}
		return false;
	}

	/** Get page-on time for current page */
	private int currentPageOnMs() {
		Interval pgOn = PageTimeHelper.validateOnInterval(new Interval(
				pages[pageNum].getPageOn(), Units.DECISECONDS), nPages==1);
		 return pgOn.round(MILLISECONDS);
	}

	/** Make the display blank (without advancing the page number) */
	private void makeBlank() {
		setImage(blankSign);
	}
	
	/** Display the first page of the message. If there are no pages, the
	 *  blank sign is shown.
	 */
	private void firstPage() {
		pageNum = 0;
		if (pgImages != null && pgImages.length > 0)
			setImage(pgImages[pageNum]);
		else
			makeBlank();
	}

	/** Display the next page of the message */
	private void nextPage() {
		pageNum++;
		if (pageNum >= nPages)
			pageNum = 0;
		setImage(pgImages[pageNum]);
	}
	
	/** Check if the MultiConfig provided is usable for rendering. */
	private static boolean isConfigUseable(MultiConfig mc) {
		return mc != null && mc.isUseable();
	}
	
	/** Get an image from a WRaster. Checks if a preview image or WYSIWYG
	 *  image should be returned.
	 */
	private BufferedImage getImage(WRaster wr) {
		if (wr != null) {
			if (!preview) {
				try {
					wr.setWysiwygImageSize(width, height);
					return wr.getWysiwygImage();
				} catch (InvalidMsgException e) {
					e.printStackTrace();
				}
			} else
				return wr.getPreviewImage();
		}
		return null;
	}
	
	/** Set if preview image should be rendered (instead of WYSIWYG image). */
	public void setPreview(boolean usePreview) {
		preview = usePreview;
	}
	
	/** Set the MultiConfig used for rendering messages. */
	public void setMultiConfig(MultiConfig mc) {
		stopTimer();
		mConfig = mc;
		
		// render a blank sign panel for page off (in case it's needed)
		WMessage wm = new WMessage("");
		
		if (isConfigUseable(mConfig)) {
			wm.renderMsg(mConfig);
			if (wm.getNumPages() > 0) {
				WPage pg = wm.getPage(1);
				WRaster wr = pg.getRaster();
				blankSign = getImage(wr);
			}
		}
	}
	
	/** Clear the MultiConfig. Displays a gray panel. */
	public void clearMultiConfig() {
		mConfig = null;
		setImage(null);
	}
	
	/** Set the MULTI string containing the message to render */
	public void setMulti(String ms) {
		multi = ms;
		
		// update the rendering and restart the timer
		renderMsg();
		restartTimer();
	}
	
	/** Clear the MULTI string. Causes the sign to blank. */
	public void clearMulti() {
		multi = null;
		stopTimer();
		setImage(blankSign);
	}
	
	/** Render the message using the current MultiConfig and MULTI string.
	 *  Returns true if the message could be rendered.
	 */
	private void renderMsg() {
		if (isConfigUseable(mConfig) && multi != null) {
			wmsg = new WMessage(multi);
			wmsg.renderMsg(mConfig);
			
			// get the pages, rasters, and images from the message
			nPages = wmsg.getNumPages();
			pages = new WPage[nPages];
			rasters = new WRaster[nPages];
			pgImages = new BufferedImage[nPages];
			for (int i = 0; i < nPages; ++i) {
				pages[i] = wmsg.getPage(i+1);
				rasters[i] = pages[i].getRaster();
				pgImages[i] = getImage(rasters[i]);
			}
		} else if (!isConfigUseable(mConfig)) {
			System.out.println("Bad MultiConfig: " + mConfig);
		}
	}
}


