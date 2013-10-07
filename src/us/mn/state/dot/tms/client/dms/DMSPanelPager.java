/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2013  Minnesota Department of Transportation
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

import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.PageTimeHelper;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.units.Interval;
import static us.mn.state.dot.tms.units.Interval.Units.MILLISECONDS;

/**
 * Pager for a SignPixelPanel.  This class updates a sign pixel panel at the
 * appropriate times for multipage messages.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSPanelPager {

	/** Time period for timer tick which updates panel */
	static private final int TIMER_TICK_MS = 100;

	/** Sign pixel panel being controlled */
	private final SignPixelPanel pixel_pnl;

	/** Rasters for each page */
	private final RasterGraphic[] rasters;

	/** Multipage message on time */
	private final Interval[] page_on;

	/** Multipage message off time */
	private final Interval[] page_off;

	/** Total number of pages */
	private final int n_pages;

	/** Swing timer */
	private final Timer timer;

	/** Current page being displayed */
	private int page = 0;

	/** Time counter for amount of time message has been displayed */
	private int phase_ms = 0;

	/** Blanking state -- true during blank time between pages */
	private boolean isBlanking = false;

	/** Create a new DMS panel pager.
	 * @param p SignPixelPanel.
	 * @param rg Array of raster graphics, one per page.
	 * @param ms MULTI string. */
	public DMSPanelPager(SignPixelPanel p, RasterGraphic[] rg, String ms) {
		pixel_pnl = p;
		rasters = rg;
		page_on = PageTimeHelper.pageOnIntervals(ms);
		page_off = PageTimeHelper.pageOffIntervals(ms);
		// These array lengths should always be the same length, but
		// there is a short race which could make them different.  Use
		// the smallest to prevent walking off the end of one.
		n_pages = Math.min(rg.length, Math.min(page_on.length,
			page_off.length));
		assert n_pages > 0;
		pixel_pnl.setGraphic(rasters[page]);
		timer = new Timer(TIMER_TICK_MS, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pageTimerTick();
			}
		});
		if(n_pages > 1)
			timer.start();
	}

	/** Dispose of the pager */
	public void dispose() {
		timer.stop();
	}

	/** Update the phase timer by one tick. */
	private void pageTimerTick() {
		phase_ms += TIMER_TICK_MS;
		if(doTick()) {
			phase_ms = 0;
			if(isBlanking)
				makeBlank();
			else
				nextPage();
		}
	}

	/** Update the timer for one tick.
	 * @return True if panel needs updating. */
	private boolean doTick() {
		return isBlanking ? doTickOff() : doTickOn();
	}

	/** Update the timer for one tick while blanking.
	 * @return True if panel needs updating. */
	private boolean doTickOff() {
		if(phase_ms >= currentPageOffMs()) {
			isBlanking = false;
			return true;
		} else
			return false;
	}

	/** Get page-off time for current page */
	private int currentPageOffMs() {
		return page_off[page].round(MILLISECONDS);
	}

	/** Update the timer for one tick while displaying a page.
	 * @return True if panel needs updating. */
	private boolean doTickOn() {
		if(phase_ms >= currentPageOnMs()) {
			isBlanking = (currentPageOffMs() > 0);
			return true;
		} else
			return false;
	}

	/** Get page-on time for current page */
	private int currentPageOnMs() {
		Interval on_int = PageTimeHelper.validateOnInterval(
			page_on[page], n_pages == 1);
		 return on_int.round(MILLISECONDS);
	}

	/** Make the display blank (without advancing the page number) */
	private void makeBlank() {
		pixel_pnl.setGraphic(createBlankPage());
	}

	/** Create a blank raster graphic */
	private RasterGraphic createBlankPage() {
		RasterGraphic rg = rasters[0];
		return new BitmapGraphic(rg.getWidth(), rg.getHeight());
	}

	/** Display the next page of the message */
	private void nextPage() {
		page++;
		if(page >= n_pages)
			page = 0;
		pixel_pnl.setGraphic(rasters[page]);
	}
}
