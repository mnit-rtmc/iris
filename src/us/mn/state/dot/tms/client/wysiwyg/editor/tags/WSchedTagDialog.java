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
import us.mn.state.dot.tms.utils.wysiwyg.token.WtSched;

/**
 * WYSIWYG DMS Message Editor dialog form for editing schedule time action
 * substitution fields.
 *
 * @author Gordon Parikh - SRF Consulting
 * @author Douglas Lau
 */
@SuppressWarnings("serial")
class WSchedTagDialog extends WMultiTagDialog {
	protected WtSched editTok;
	private WTagParamField dirField;
	private WTagParamField formatField;
	private String dir;
	private String format;

	public WSchedTagDialog(String title, WController c, WTokenType tokType,
		WToken tok)
	{
		super(title, c, tokType, tok);
	}

	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtSched) tok;
		dir = editTok.getDir();
		format = editTok.getFormat();
	}

	@Override
	protected void addTagForm() {
		dirField = new WTagParamField(dir, 20, true);
		addField("wysiwyg.sched_tag_dialog.dir_txt", dirField);
		formatField = new WTagParamField(format, 20, true);
		addField("wysiwyg.sched_tag_dialog.format_txt", formatField);
	}

	@Override
	protected WtSched makeNewTag() {
		dir = dirField.getText();
		format = formatField.getText();
		return new WtSched(dir, format);
	}
}
