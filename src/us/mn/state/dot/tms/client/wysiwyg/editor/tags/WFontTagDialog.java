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
import us.mn.state.dot.tms.utils.wysiwyg.token.WtFont;

/**
 * WYSIWYG DMS Message Editor dialog form for editing font tags.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
public class WFontTagDialog extends WMultiTagDialog {

	protected WtFont editTok;
	protected WTagParamIntField f_numField;
	protected WTagParamField f_idField;
	private Integer f_num;
	private String f_id;
	
	
	public WFontTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}

	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtFont) tok;
		f_num = editTok.getFontNum();
		f_id = editTok.getFontId();
	}

	@Override
	protected void addTagForm() {
		f_numField = new WTagParamIntField(f_num, 10, true);
		addField("wysiwyg.font_tag_dialog.f_num", f_numField);
		f_idField = new WTagParamField(f_id, 10, false);
		addField("wysiwyg.font_tag_dialog.f_id", f_idField);
	}

	@Override
	protected WtFont makeNewTag() {
		f_num = f_numField.getValue();
		f_id = f_idField.getText();
		return new WtFont(f_num, f_id);
	}

}
