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
import us.mn.state.dot.tms.utils.wysiwyg.token.WtCharSpacing;

/**
 * WYSIWYG DMS Message Editor dialog form for editing character spacing tags.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
public class WCharSpacingTagDialog extends WMultiTagDialog {
	
	protected WtCharSpacing editTok;
	private WTagParamIntField scField;
	private Integer sc;
	
	public WCharSpacingTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}

	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtCharSpacing) tok;
		sc = editTok.getCharSpacing();
	}

	@Override
	protected void addTagForm() {
		scField = new WTagParamIntField(sc, 10, false);
		addField("wysiwyg.char_spacing_dialog.sc", scField);
	}
	
	@Override
	protected WtCharSpacing makeNewTag() {
		sc = scField.getValue();
		return new WtCharSpacing(sc);
	}

}
