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
import us.mn.state.dot.tms.utils.wysiwyg.token.WtNewLine;

/**
 * WYSIWYG DMS Message Editor dialog form for editing newline tags.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
public class WNewLineTagDialog extends WMultiTagDialog {
	
	protected WtNewLine editTok;
	private WTagParamIntField spacingField;
	private Integer spacing;
	
	public WNewLineTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}

	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtNewLine) tok;
		spacing = editTok.getLineSpacing();
	}

	@Override
	protected void addTagForm() {
		spacingField = new WTagParamIntField(spacing, 10, false);
		addField("wysiwyg.newline_dialog.spacing", spacingField);
	}

	@Override
	protected WtNewLine makeNewTag() {
		spacing = spacingField.getValue();
		return new WtNewLine(spacing);
	}

}
