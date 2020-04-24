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

package us.mn.state.dot.tms.client.wysiwyg.editor.tags;

import us.mn.state.dot.tms.client.wysiwyg.editor.WController;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtPageTime;

/**
 * WYSIWYG DMS Message Editor dialog form for editing page timing tags.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
public class WPageTimingTagDialog extends WMultiTagDialog {
	
	private WtPageTime editTok;
	private WTagParamIntField pt_onField;
	private WTagParamIntField pt_offField;
	private Integer pt_on;
	private Integer pt_off;
	
	public WPageTimingTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}

	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtPageTime) tok;
		pt_on = editTok.getPageOnTime();
		pt_off = editTok.getPageOffTime();
	}

	@Override
	protected void addTagForm() {
		pt_onField = new WTagParamIntField(pt_on, 10, false);
		addField("wysiwyg.page_timing_dialog.pt_on", pt_onField);
		pt_offField = new WTagParamIntField(pt_off, 10, false);
		addField("wysiwyg.page_timing_dialog.pt_off", pt_offField);
	}

	@Override
	protected WtPageTime makeNewTag() {
		pt_on = pt_onField.getValue();
		pt_off = pt_offField.getValue();
		return new WtPageTime(pt_on, pt_off);
	}

}
