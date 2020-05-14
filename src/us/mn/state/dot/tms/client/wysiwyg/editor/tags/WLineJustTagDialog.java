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
import us.mn.state.dot.tms.utils.Multi.JustificationLine;
import us.mn.state.dot.tms.utils.wysiwyg.WToken;
import us.mn.state.dot.tms.utils.wysiwyg.WTokenType;
import us.mn.state.dot.tms.utils.wysiwyg.token.WtJustLine;

/**
 * WYSIWYG DMS Message Editor dialog form for editing line justification tags.
 *
 * @author Gordon Parikh - SRF Consulting
 */
@SuppressWarnings("serial")
public class WLineJustTagDialog extends WMultiTagDialog {

	protected WtJustLine editTok;
	protected JustificationLine jl;
	
	private static final JustificationLine[] justAllowed = {
			JustificationLine.LEFT, JustificationLine.CENTER,
			JustificationLine.RIGHT};
	protected WTagParamEnumField<JustificationLine> jlField;
	
	public WLineJustTagDialog(String title, WController c,
			WTokenType tokType, WToken tok) {
		super(title, c, tokType, tok);
	}

	@Override
	protected void loadFields(WToken tok) {
		editTok = (WtJustLine) tok;
		jl = editTok.getJustification();
	}

	@Override
	protected void addTagForm() {
		jlField = new WTagParamEnumField<JustificationLine>(
				justAllowed, jl, true);
		addField("wysiwyg.line_just_tag_dialog.jl", jlField);
	}

	@Override
	protected WtJustLine makeNewTag() {
		jl = jlField.getSelectedItem();
		return new WtJustLine(jl);
	}

}
