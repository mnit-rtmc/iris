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
import us.mn.state.dot.tms.utils.Multi.JustificationPage;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtJustPage;

/**
 * WYSIWYG DMS Message Editor dialog form for editing page justification tags.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
public class WPageJustTagDialog extends WMultiTagDialog {
	
	protected WtJustPage editTok;
	protected JustificationPage jp;
	
	private static final JustificationPage[] justAllowed = {
			JustificationPage.TOP, JustificationPage.MIDDLE,
			JustificationPage.BOTTOM};
	protected WTagParamEnumField<JustificationPage> jpField;
	
	public WPageJustTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}

	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtJustPage) tok;
		jp = editTok.getJustification();
	}

	@Override
	protected void addTagForm() {
		// get the list of enum values, but leave out "undefined" and "other"
		jpField = new WTagParamEnumField<JustificationPage>(
				justAllowed, jp, true);
		addField("wysiwyg.page_just_tag_dialog.jp", jpField);
	}

	@Override
	protected WtJustPage makeNewTag() {
		jp = jpField.getSelectedItem();
		return new WtJustPage(jp);
	}

}
