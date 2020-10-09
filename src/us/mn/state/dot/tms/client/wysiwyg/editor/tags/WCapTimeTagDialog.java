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
import us.mn.state.dot.tms.utils.wysiwyg.token.WtCapTime;

/**
 * WYSIWYG DMS Message Editor dialog form for editing IPAWS alert CAP time
 * substitution fields.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
class WCapTimeTagDialog extends WMultiTagDialog {
	protected WtCapTime editTok;
	private WTagParamField f_txtField;
	private WTagParamField a_txtField;
	private WTagParamField p_txtField;
	private String f_txt;
	private String a_txt;
	private String p_txt;
	
	public WCapTimeTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}

	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtCapTime) tok;
		f_txt = editTok.getFutureText();
		a_txt = editTok.getActiveText();
		p_txt = editTok.getPastText();
	}

	@Override
	protected void addTagForm() {
		f_txtField = new WTagParamField(f_txt, 20, true);
		addField("wysiwyg.cap_time_tag_dialog.f_txt", f_txtField);
		a_txtField = new WTagParamField(a_txt, 20, true);
		addField("wysiwyg.cap_time_tag_dialog.a_txt", a_txtField);
		p_txtField = new WTagParamField(p_txt, 20, true);
		addField("wysiwyg.cap_time_tag_dialog.p_txt", p_txtField);
	}

	@Override
	protected WtCapTime makeNewTag() {
		f_txt = f_txtField.getText();
		a_txt = a_txtField.getText();
		p_txt = p_txtField.getText();
		return new WtCapTime(f_txt, a_txt, p_txt);
	}
}
