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
 * WYSIWYG DMS Message Editor dialog form for editing alert CAP time
 * substitution fields.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
class WCapTimeTagDialog extends WMultiTagDialog {
	protected WtCapTime editTok;
	private WTagParamField b_txtField;
	private WTagParamField d_txtField;
	private WTagParamField a_txtField;
	private String b_txt;
	private String d_txt;
	private String a_txt;
	
	public WCapTimeTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}

	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtCapTime) tok;
		b_txt = editTok.getFutureText();
		d_txt = editTok.getActiveText();
		a_txt = editTok.getPastText();
	}

	@Override
	protected void addTagForm() {
		b_txtField = new WTagParamField(b_txt, 20, true);
		addField("wysiwyg.cap_time_tag_dialog.b_txt", b_txtField);
		d_txtField = new WTagParamField(d_txt, 20, true);
		addField("wysiwyg.cap_time_tag_dialog.d_txt", d_txtField);
		a_txtField = new WTagParamField(a_txt, 20, true);
		addField("wysiwyg.cap_time_tag_dialog.a_txt", a_txtField);
	}

	@Override
	protected WtCapTime makeNewTag() {
		b_txt = b_txtField.getText();
		d_txt = d_txtField.getText();
		a_txt = a_txtField.getText();
		return new WtCapTime(b_txt, d_txt, a_txt);
	}
}
