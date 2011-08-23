/*
 * IRIS -- Intelligent Roadway Information System
 * Copyright (C) 2000-2011  Minnesota Department of Transportation
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
import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DmsPgTime;
import us.mn.state.dot.tms.BitmapGraphic;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.SystemAttrEnum;

/**
 * Pager for a SignPixelPanel.  This class enables multiple page messages
 * to be displayed on a SignPixelPanel. The page on-time is passed as a
 * constructor argument and represents the on-time for all pages. The page
 * off time is determined from its system attribute.
 *
 * @author Douglas Lau
 * @author Michael Darter
 */
public class DMSPanelPager {

	/** Time counter for amount of time message has been displayed */
	static protected final int TIMER_TICK_MS = 100;

	/** Sign pixel panel being controlled */
	protected final SignPixelPanel panel;

	/** Selected DMS */
	protected final DMS dms;

	/** Rasters for each page */
	protected final RasterGraphic[] rasters;

	/** Current page being displayed */
	protected int page = 0;

	/** Multipage message on time, read from system attributes */
	protected final DmsPgTime pgOnTime;

	/** Multipage message off time, read from system attributes */
	protected final DmsPgTime pgOffTime;

	/** Swing timer */
	protected final Timer timer;

	/** Time counter for amount of time message has been displayed */
	protected int phaseTimeMS = 0;

	/** Blanking state -- true during blank time between pages */
	protected boolean isBlanking = false;

	/** Create a new DMS panel pager.
	 *  @param p SignPixelPanel.
	 *  @param proxy DMS proxy.
	 *  @param rg Array of raster graphics.
	 *  @param ot Page on-time, which is validated, so if zero, is 
	 *	   assigned the system default. */
	public DMSPanelPager(SignPixelPanel p, DMS proxy, RasterGraphic[] rg,
		DmsPgTime ot)
	{
		panel = p;
		dms = proxy;
		rasters = getRasters(rg);
		int npg = rasters.length;
		pgOnTime = DmsPgTime.validateOnTime(ot, npg <= 1);
		pgOffTime = DmsPgTime.getDefaultOff();
		setDimensions();
		panel.setGraphic(rasters[page]);
		timer = new Timer(TIMER_TICK_MS, new ActionListener() { 
			public void actionPerformed(ActionEvent e) {
				pageTimerTick();
			} 
		});
		if(isMultipage())
			timer.start();
	}

	/** Dispose of the pager */
	public void dispose() {
		timer.stop();
	}

	/** Get rasters to display on the panel */
	protected RasterGraphic[] getRasters(RasterGraphic[] rg) {
		if(rg != null && rg.length > 0)
			return rg;
		else {
			return new RasterGraphic[] {
				createBlankPage()
			};
		}
	}

	/** Set the dimensions of the pixel panel */
	protected void setDimensions() {
		setPhysicalDimensions();
		setLogicalDimensions();
		panel.repaint();
	}

	/** Set the physical dimensions of the pixel panel */
	protected void setPhysicalDimensions() {
		Integer w = dms.getFaceWidth();
		Integer h = dms.getFaceHeight();
		Integer hp = dms.getHorizontalPitch();
		Integer vp = dms.getVerticalPitch();
		Integer hb = dms.getHorizontalBorder();
		Integer vb = dms.getVerticalBorder();
		if(w != null && h != null && hp != null && vp != null &&
		   hb != null && vb != null)
		{
			panel.setPhysicalDimensions(w, h, hb, vb, hp, vp);
		}
	}

	/** Set the logical dimensions of the pixel panel */
	protected void setLogicalDimensions() {
		Integer wp = dms.getWidthPixels();
		Integer hp = dms.getHeightPixels();
		Integer cw = dms.getCharWidthPixels();
		Integer ch = dms.getCharHeightPixels();
		if(wp != null && hp != null && cw != null && ch != null)
			panel.setLogicalDimensions(wp, hp, cw, ch);
	}

	/** Create a blank raster graphic */
	protected RasterGraphic createBlankPage() {
		Integer wp = dms.getWidthPixels();
		Integer hp = dms.getHeightPixels();
		if(wp != null && hp != null)
			return new BitmapGraphic(wp, hp);
		else
			return new BitmapGraphic(0, 0);
	}

	/** 
	 * Page timer tick. Called periodically to change the sign contents
	 * for multipage signs.
	 */
	protected void pageTimerTick() {
		if(doTick()) {
			if(isBlanking)
				makeBlank();
			else
				nextPage();
		}
	}

	/** Update the timer for one tick.
	 * @return True if panel needs repainting */
	protected boolean doTick() {
		phaseTimeMS += TIMER_TICK_MS;
		if(isBlanking) {
			if(phaseTimeMS >= pgOffTime.toMs()) {
				isBlanking = false;
				phaseTimeMS = 0;
				return true;
			}
		} else {
			if(phaseTimeMS >= pgOnTime.toMs()) {
				if(pgOffTime.toMs() > 0)
					isBlanking = true;
				phaseTimeMS = 0;
				return true;
			}
		}
		return false;
	}

	/** Make the display blank (without advancing the page number) */
	protected void makeBlank() {
		if(isMultipage())
			panel.setGraphic(createBlankPage());
	}

	/** Display the next page of the message */
	protected void nextPage() {
		if(isMultipage()) {
			page++;
			if(page >= rasters.length)
				page = 0;
			panel.setGraphic(rasters[page]);
		}
	}

	/** Check if the current message has multiple pages */
	protected boolean isMultipage() {
		return rasters.length > 1;
	}
}
