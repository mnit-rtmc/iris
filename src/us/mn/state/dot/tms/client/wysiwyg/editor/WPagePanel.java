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
package us.mn.state.dot.tms.client.wysiwyg.editor;

import java.awt.Component;
import java.util.IllegalFormatException;

import us.mn.state.dot.tms.DMS;
import us.mn.state.dot.tms.DMSHelper;
import us.mn.state.dot.tms.PageTimeHelper;
import us.mn.state.dot.tms.RasterBuilder;
import us.mn.state.dot.tms.RasterGraphic;
import us.mn.state.dot.tms.client.dms.SignFacePanel;
import us.mn.state.dot.tms.client.dms.SignPixelPanel;
import us.mn.state.dot.tms.units.Interval;
import us.mn.state.dot.tms.utils.I18N;
import us.mn.state.dot.tms.utils.MultiString;

/**
 * Panel containing a information and a graphical rendering of a DMS message
 * page for the WYSIWYG DMS Message Editor.
 *
 * @author Gordon Parikh - SRF Consulting
 */
public class WPagePanel extends Component {

	/* Sign and MultiString of Message being edited */
	private final DMS sign;
	private final MultiString ms;

	/* Page # of the page shown in this panel, also other page stuff */
	private final int pn;
	private String page;
	private String pgOnInt;
	private String pgOffInt;

	/* RasterBuilder and RasterGraphic for rendering page */
	private RasterBuilder rb;
	private RasterGraphic prg;

	public WPagePanel(DMS d, MultiString m, int pageNum) {
		sign = d;
		ms = m;
		pn = pageNum;

		// get the page in question (and page on/off intervals)
		page = ms.getPage(pn);
		setPageOnIntervalStr();
		setPageOffIntervalStr();

		// render the page's graphic
		renderPageRaster();
	}

	private void setPageOnIntervalStr() {
		Interval dflt = PageTimeHelper.defaultPageOnInterval();
		Interval[] pg_on = ms.pageOnIntervals(dflt);
		if (pn >= 0 && pn < pg_on.length)
			pgOnInt = pg_on[pn].toString();
		else
			pgOnInt = "0 s";
	}

	private void setPageOffIntervalStr() {
		Interval dflt = PageTimeHelper.defaultPageOffInterval();
		Interval[] pg_off = ms.pageOffIntervals(dflt);
		if (pn >= 0 && pn < pg_off.length)
			pgOffInt = pg_off[pn].toString();
		else
			pgOffInt = "0 s";
	}

	public RasterGraphic getPageRaster() {
		return prg;
	}

	/* Render the RasterGraphic for the page */
	private void renderPageRaster() {
		// create a RasterBuilder for this sign
		rb = DMSHelper.createRasterBuilder(sign);

		if (rb != null) {
			// generate all message graphics
			// but save the graphic for this page only
			RasterGraphic[] rg = rb.createRasters(ms.toString());
			if (pn >= 0 && pn < rg.length)
				prg = rg[pn];
			else
				prg = null;
		}
	}
	
	public SignPixelPanel getSignPixelPanel() {
		SignFacePanel sf = new SignFacePanel();
		SignPixelPanel sp = sf.setSign(sign);
		sp.setFilterColor(SignPixelPanel.filterColor(sign));
		sp.setDimensions(sign.getSignConfig());
		sp.setGraphic(getPageRaster());
		return sp;
	}
	
	/* Render the page on the panel provided */
	public void renderToPanel(SignPixelPanel spp) {
		spp.setFilterColor(SignPixelPanel.filterColor(sign));
		spp.setDimensions(sign.getSignConfig());
		spp.setGraphic(getPageRaster());
	}

	public DMS getDMS() {
		return sign;
	}

	public String getDmsName() {
		return sign.getName();
	}

	public String getPageNumberLabel() {
		// add 1 to the page number since it's 0 indexed here
		try {
			return String.format(I18N.get("wysiwyg.editor.page_number"), pn+1);
		} catch (IllegalFormatException e) {
			return "Page" + pn+1;
		}
	}
	
	/** Get information about the page. For now just on/off times. */
	public String getPageInfo() {
		String pgOnStr = String.format(I18N.get("wysiwyg.editor.page_on"), pgOnInt);
		String pgOffStr = String.format(I18N.get("wysiwyg.editor.page_off"), pgOffInt);
		return String.format("%s   %s", pgOnStr, pgOffStr);
	}
}

